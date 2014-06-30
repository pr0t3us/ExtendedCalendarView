package com.tyczj.extendedcalendarview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class ExtendedCalendarView extends RelativeLayout implements OnItemClickListener,
        OnClickListener {

    public static final int NO_GESTURE = 0;
    public static final int LEFT_RIGHT_GESTURE = 1;
    public static final int UP_DOWN_GESTURE = 2;
    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;

    private Context mContext;
    private GridView mCalendarGridView;
    private ProgressBar mProgressView;
    private CalendarAdapter mAdapter;
    private Calendar mCalendar;
    private TextView mMonthTextView;
    private RelativeLayout mBackNavigation;
    private ImageView mNextImageView;
    private ImageView mPrevImageView;
    private int mGestureType = 0;

    private Day mSelectedDay;

    private OnDayClickListener mDayListener;
    private final GestureDetector mCalendarGesture =
            new GestureDetector(mContext, new GestureListener());


    public interface OnDayClickListener {
        public void onDayClicked(AdapterView<?> adapter, View view, int position, long id, Day day);
    }

    public ExtendedCalendarView(Context context) {
        super(context);
        this.mContext = context;
        init();
    }

    public ExtendedCalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        init();
    }

    public ExtendedCalendarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mContext = context;
        init();
    }

    private void init() {
        mCalendar = Calendar.getInstance();
        removeAllViews();
        /*
        Inflating main view.
         */
        LayoutInflater.from(mContext).inflate(R.layout.layout_calendar, this);

        /*
        Finding view components.
         */
        mProgressView = (ProgressBar) findViewById(R.id.calendar_progress);
        mBackNavigation = (RelativeLayout) findViewById(R.id.navigation);
        mPrevImageView = (ImageView) findViewById(R.id.img_prev);
        mMonthTextView = (TextView) findViewById(R.id.tv_month);
        mNextImageView = (ImageView) findViewById(R.id.img_next);
        mCalendarGridView = (GridView) findViewById(R.id.grid_calendar);

        mMonthTextView.setText(mCalendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())
                + " " + mCalendar.get(Calendar.YEAR));

        mPrevImageView.setOnClickListener(this);
        mNextImageView.setOnClickListener(this);

        initAdapter();
    }

    private void initAdapter() {

        new InitializeAdapterTask().execute();
        mCalendarGridView.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mCalendarGesture.onTouchEvent(event);
            }
        });
    }

    private class InitializeAdapterTask extends AsyncTask<Void, Void, CalendarAdapter> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgress();
        }

        @Override
        protected CalendarAdapter doInBackground(Void... params) {
            mAdapter = new CalendarAdapter(mContext, mCalendar);
            return mAdapter;
        }

        @Override
        protected void onPostExecute(CalendarAdapter calendarAdapter) {

            if (mCalendarGridView != null && isShown()) {
                if (mSelectedDay != null) {
                    calendarAdapter.setSelectedDay(mSelectedDay);
                }
                mCalendarGridView.setAdapter(calendarAdapter);
            }
            hideProgress();
        }
    }

    private class GestureListener extends SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

            if (mGestureType == LEFT_RIGHT_GESTURE) {
                if (e1.getX() - e2.getX() >
                        SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    cancelTouch(e1);
                    nextMonth();
                    return true; // Right to left
                } else if (e2.getX() - e1.getX() >
                        SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    cancelTouch(e1);
                    previousMonth();
                    return true; // Left to right
                }
            } else if (mGestureType == UP_DOWN_GESTURE) {
                if (e1.getY() - e2.getY() >
                        SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                    cancelTouch(e1);
                    nextMonth();
                    return true; // Bottom to top
                } else if (e2.getY() - e1.getY() >
                        SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                    cancelTouch(e1);
                    previousMonth();
                    return true; // Top to bottom
                }
            }
            return false;
        }
    }

    private void cancelTouch(MotionEvent e1) {
        // Canceling touch (un-highlighting the item)
        MotionEvent cancelEvent = MotionEvent.obtain(e1);
        cancelEvent.setAction(MotionEvent.ACTION_CANCEL |
                (e1.getActionIndex()
                        << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
        dispatchTouchEvent(cancelEvent);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mDayListener != null) {
            Day d = (Day) mAdapter.getItem(position);
            if (d.getDay() != 0) {
                Calendar todayCal = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
                Calendar selectedCal = Calendar.getInstance();
                selectedCal.set(d.getYear(), d.getMonth(), d.getDay());
                if (selectedCal.getTimeInMillis() >= todayCal.getTimeInMillis()) {
                    mDayListener.onDayClicked(parent, view, position, id, d);
                    if (mAdapter != null) {
                        mAdapter.setSelectedDay(d);
                    }
                }
            }


        }
    }

    /**
     * @param listener Set a listener for when you press on a day in the mMonthTextView
     */
    public void setOnDayClickListener(OnDayClickListener listener) {
        if (mCalendarGridView != null) {
            mDayListener = listener;
            mCalendarGridView.setOnItemClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.img_prev) {
            previousMonth();

        } else if (v.getId() == R.id.img_next) {
            nextMonth();

        }
    }

    private void previousMonth() {
        /* stop calendar move past this month */
        if (mAdapter != null && mAdapter.getTodayCalendar().get(Calendar.MONTH) == mCalendar.get(Calendar.MONTH)) {
            return;
        }
        if (mCalendar.get(Calendar.MONTH) == mCalendar.getActualMinimum(Calendar.MONTH)) {
            mCalendar.set((mCalendar.get(Calendar.YEAR) - 1), mCalendar.getActualMaximum(Calendar.MONTH), 1);
        } else {
            mCalendar.set(Calendar.MONTH, mCalendar.get(Calendar.MONTH) - 1);
        }
        rebuildCalendar();
    }

    private void nextMonth() {
        if (mCalendar.get(Calendar.MONTH) == mCalendar.getActualMaximum(Calendar.MONTH)) {
            mCalendar.set((mCalendar.get(Calendar.YEAR) + 1), mCalendar.getActualMinimum(Calendar.MONTH), 1);
        } else {
            mCalendar.set(Calendar.MONTH, mCalendar.get(Calendar.MONTH) + 1);
        }
        rebuildCalendar();
    }

    private void hideProgress() {
        if (mProgressView != null) {
            mProgressView.setVisibility(INVISIBLE);
        }
    }

    private void showProgress() {
        if (mProgressView != null) {
            mProgressView.setVisibility(VISIBLE);
        }
    }

    public void rebuildCalendar() {
        if (mMonthTextView != null) {
            mMonthTextView.setText(mCalendar.getDisplayName(Calendar.MONTH, Calendar.LONG,
                    Locale.getDefault()) + " " + mCalendar.get(Calendar.YEAR));
            refreshCalendar();
        }
    }

    /**
     * Refreshes the mMonthTextView
     */
    public void refreshCalendar() {
        mAdapter.refreshDays();
        mAdapter.notifyDataSetChanged();
    }

    /**
     * @param color Sets the background color of the mMonthTextView bar
     */
    public void setMonthTextBackgroundColor(int color) {
        mBackNavigation.setBackgroundColor(color);
    }

    @SuppressLint("NewApi")
    /**
     *
     * @param drawable
     *
     * Sets the background color of the mMonthTextView bar. Requires at least API level 16
     */
    public void setMonthTextBackgroundDrawable(Drawable drawable) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            mBackNavigation.setBackground(drawable);
        }

    }

    /**
     * @param resource Sets the background color of the mMonthTextView bar
     */
    public void setMonthTextBackgroundResource(int resource) {
        mBackNavigation.setBackgroundResource(resource);
    }

    /**
     * @param recource change the image of the previous mMonthTextView button
     */
    public void setPreviousMonthButtonImageResource(int recource) {
        mPrevImageView.setImageResource(recource);
    }

    /**
     * @param bitmap change the image of the previous mMonthTextView button
     */
    public void setPreviousMonthButtonImageBitmap(Bitmap bitmap) {
        mPrevImageView.setImageBitmap(bitmap);
    }

    /**
     * @param drawable change the image of the previous mMonthTextView button
     */
    public void setPreviousMonthButtonImageDrawable(Drawable drawable) {
        mPrevImageView.setImageDrawable(drawable);
    }

    /**
     * @param recource change the image of the mNextImageView mMonthTextView button
     */
    public void setNextMonthButtonImageResource(int recource) {
        mNextImageView.setImageResource(recource);
    }

    /**
     * @param bitmap change the image of the mNextImageView mMonthTextView button
     */
    public void setNextMonthButtonImageBitmap(Bitmap bitmap) {
        mNextImageView.setImageBitmap(bitmap);
    }

    /**
     * @param drawable change the image of the mNextImageView mMonthTextView button
     */
    public void setNextMonthButtonImageDrawable(Drawable drawable) {
        mNextImageView.setImageDrawable(drawable);
    }

    /**
     * @param gestureType Allow swiping the mCalendarGridView left/right or up/down to
     *                    change the mMonthTextView.
     *                    <p/>
     *                    Default value no gesture
     */
    public void setGesture(int gestureType) {
        this.mGestureType = gestureType;
    }

    /**
     * Rebuilds calendar view according to new data.
     *
     * @param calendar New calendar to display.
     */
    public void setCalendar(Calendar calendar) {
        mCalendar = calendar;
        rebuildCalendar();
    }

    public void setSelectedDay(Day selectedDay) {
        this.mSelectedDay = selectedDay;
        if (mAdapter != null) {
            mAdapter.setSelectedDay(selectedDay);
        }
    }

}
