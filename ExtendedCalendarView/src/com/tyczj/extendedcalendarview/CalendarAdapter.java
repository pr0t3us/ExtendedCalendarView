package com.tyczj.extendedcalendarview;

import android.content.Context;
import android.graphics.Typeface;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class CalendarAdapter extends BaseAdapter {

    private static final int DAYS_PER_WEEK = 7;

    private Context mContext;
    private Calendar mStartCal;
    private final Calendar mTodayCal = Calendar.getInstance();

    public String[] mDays;
    private ArrayList<Day> mDayList = new ArrayList<Day>();
    private Day mSelectedDay;

    public CalendarAdapter(Context context, Calendar cal) {
        this.mContext = context;
        this.mStartCal = cal;
        this.mStartCal.set(Calendar.DAY_OF_MONTH, 1);
        this.mStartCal.setFirstDayOfWeek(Calendar.MONDAY);
        refreshDays();
    }

    @Override
    public int getCount() {
        return mDays.length;
    }

    @Override
    public Object getItem(int position) {
        return mDayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        if (position >= 7) {
            Day d = mDayList.get(position);
            Calendar selectedCal = Calendar.getInstance();
            selectedCal.set(d.getYear(), d.getMonth(), d.getDay());
            if (selectedCal.getTimeInMillis() < mTodayCal.getTimeInMillis()) {
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View v = convertView;
        LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (position >= 0 && position < 7) {
            v = vi.inflate(R.layout.day_of_week, null);
            TextView day = (TextView) v.findViewById(R.id.dayTextView);

            if (position == 0) {
                day.setText(mContext.getString(R.string.monday_short));
            } else if (position == 1) {
                day.setText(mContext.getString(R.string.tuesday_short));
            } else if (position == 2) {
                day.setText(mContext.getString(R.string.wednesday_short));
            } else if (position == 3) {
                day.setText(mContext.getString(R.string.thursday_short));
            } else if (position == 4) {
                day.setText(mContext.getString(R.string.friday_short));
            } else if (position == 5) {
                day.setText(mContext.getString(R.string.saturday_short));
            } else if (position == 6) {
                day.setText(mContext.getString(R.string.sunday_short));
            }

        } else {

            v = vi.inflate(R.layout.day_view, null);
            FrameLayout today = (FrameLayout) v.findViewById(R.id.today_frame);
            TextView dayTV = (TextView) v.findViewById(R.id.dayTextView);
            RelativeLayout rl = (RelativeLayout) v.findViewById(R.id.rl);


            Day d = mDayList.get(position);
            switch (compareWithToday(d)) {
                case 0:
                    dayTV.setTypeface(Typeface.DEFAULT_BOLD);
                    dayTV.setTextColor(mContext.getResources().getColor(android.R.color.black));
                    break;
                case 1:
                    dayTV.setTypeface(Typeface.DEFAULT);
                    break;
                case -1:
                    dayTV.setTypeface(Typeface.DEFAULT);
                    dayTV.setTextColor(mContext.getResources().getColor(android.R.color.darker_gray));
                    break;

            }
            if (mSelectedDay != null && d.compareDate(mSelectedDay)) {
                dayTV.setSelected(true);
            } else {
                dayTV.setSelected(false);
            }
            today.setVisibility(View.GONE);

            ImageView iv = (ImageView) v.findViewById(R.id.imageView1);
            ImageView blue = (ImageView) v.findViewById(R.id.imageView2);
            ImageView purple = (ImageView) v.findViewById(R.id.imageView3);
            ImageView green = (ImageView) v.findViewById(R.id.imageView4);
            ImageView orange = (ImageView) v.findViewById(R.id.imageView5);
            ImageView red = (ImageView) v.findViewById(R.id.imageView6);

            blue.setVisibility(View.VISIBLE);
            purple.setVisibility(View.VISIBLE);
            green.setVisibility(View.VISIBLE);
            purple.setVisibility(View.VISIBLE);
            orange.setVisibility(View.VISIBLE);
            red.setVisibility(View.VISIBLE);

            iv.setVisibility(View.VISIBLE);
            dayTV.setVisibility(View.VISIBLE);
            rl.setVisibility(View.VISIBLE);

            Day day = mDayList.get(position);

            /* Hide unnecessary color views */
            if(day.getNumOfEvenets() > 0){
                Set<Integer> colors = day.getColors();

                iv.setVisibility(View.INVISIBLE);
                blue.setVisibility(View.INVISIBLE);
                purple.setVisibility(View.INVISIBLE);
                green.setVisibility(View.INVISIBLE);
                purple.setVisibility(View.INVISIBLE);
                orange.setVisibility(View.INVISIBLE);
                red.setVisibility(View.INVISIBLE);

                if(colors.contains(0)){
                    iv.setVisibility(View.VISIBLE);
                }
                if(colors.contains(2)){
                    blue.setVisibility(View.VISIBLE);
                }
                if(colors.contains(4)){
                    purple.setVisibility(View.VISIBLE);
                }
                if(colors.contains(5)){
                    green.setVisibility(View.VISIBLE);
                }
                if(colors.contains(3)){
                    orange.setVisibility(View.VISIBLE);
                }
                if(colors.contains(1)){
                    red.setVisibility(View.VISIBLE);
                }

            }else{
                iv.setVisibility(View.INVISIBLE);
                blue.setVisibility(View.INVISIBLE);
                purple.setVisibility(View.INVISIBLE);
                green.setVisibility(View.INVISIBLE);
                purple.setVisibility(View.INVISIBLE);
                orange.setVisibility(View.INVISIBLE);
                red.setVisibility(View.INVISIBLE);
            }

            if (day.getDay() == 0) {
                rl.setVisibility(View.GONE);
            } else {
                dayTV.setVisibility(View.VISIBLE);
                dayTV.setText(String.valueOf(day.getDay()));
            }
        }

        return v;
    }

    public int getPrevMonth() {
        if (mStartCal.get(Calendar.MONTH) == mStartCal.getActualMinimum(Calendar.MONTH)) {
            mStartCal.set(Calendar.YEAR, mStartCal.get(Calendar.YEAR - 1));
        } else {

        }
        int month = mStartCal.get(Calendar.MONTH);
        if (month == 0) {
            return month = 11;
        }

        return month - 1;
    }

    public int getMonth() {
        return mStartCal.get(Calendar.MONTH);
    }

    public Calendar getTodayCalendar() {
        return mTodayCal;
    }

    public int compareWithToday(Day d) {
        if (d.getYear() == mTodayCal.get(Calendar.YEAR)
                && d.getMonth() == mTodayCal.get(Calendar.MONTH)
                && d.getDay() == mTodayCal.get(Calendar.DAY_OF_MONTH)) {
            return 0;
        } else {
            return d.getTime() > mTodayCal.getTimeInMillis() ? 1 : -1;
        }
    }

    public void setSelectedDay(Day selectedDay) {
        this.mSelectedDay = selectedDay;
    }

    public void refreshDays() {
        // clear items
        mDayList.clear();

//        maximum number of mDays in the current month + nr. of columns
        int totalDays = mStartCal.getActualMaximum(Calendar.DAY_OF_MONTH) + DAYS_PER_WEEK;
//        This field takes values like {@code SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY,FRIDAY, SATURDAY}.
        int firstDay = (int) mStartCal.get(Calendar.DAY_OF_WEEK);
        int daysBeforeFirst;
        if (firstDay == Calendar.SUNDAY) {
            daysBeforeFirst = DAYS_PER_WEEK - firstDay;
        } else {
            daysBeforeFirst = firstDay - 2;
        }

        int year = mStartCal.get(Calendar.YEAR);
        int month = mStartCal.get(Calendar.MONTH);
        TimeZone tz = TimeZone.getDefault();

        // figure size of the array
        // if firstDay is MONDAY
        if (firstDay == Calendar.MONDAY) {
            mDays = new String[totalDays];
        } else {
            mDays = new String[totalDays + daysBeforeFirst];
        }

        int j = 0;

        // populate empty mDays before first real day
        if (firstDay != Calendar.MONDAY) {
            for (j = 0; j < (firstDay != Calendar.SUNDAY ? firstDay : (firstDay + 7)) + 6; j++) {
                mDays[j] = "";
                Day d = new Day(mContext, 0, 0, 0);
                mDayList.add(d);
            }
        } else {
            for (j = 0; j < DAYS_PER_WEEK; j++) {
                mDays[j] = "";
                Day d = new Day(mContext, 0, 0, 0);
                mDayList.add(d);
            }
            j = 1; // sunday => 1, monday => 7
        }

        // populate mDays
        int dayNumber = 1;

        if (j > 0 && mDayList.size() > 0 && j != 1) {
            mDayList.remove(j - 1);
        }

        for (int i = j - 1; i < mDays.length; i++) {

            Day d = new Day(mContext, dayNumber, year, month);

            Calendar cTemp = Calendar.getInstance();
            cTemp.set(year, month, dayNumber);
            int startDay = Time.getJulianDay(cTemp.getTimeInMillis(), TimeUnit.MILLISECONDS.toSeconds(tz.getOffset(cTemp.getTimeInMillis())));

            d.setAdapter(this);
            d.setStartDay(startDay);

            mDays[i] = "" + dayNumber;
            dayNumber++;
            mDayList.add(d);
        }
    }

}
