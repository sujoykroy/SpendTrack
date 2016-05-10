package bk2suz.spendtrack;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.zip.Inflater;

/**
 * Created by sujoy on 10/5/16.
 */
public class DateView extends RelativeLayout {
    private DayAdapter mDayAdapter;
    private MonthAdapter mMonthAdapter;
    private YearAdapter mYearAdapter;
    Spinner mDaySpinner, mMonthSpinner, mYearSpinner;

    private int mCurrentDay, mCurrentYear, mCurrentMonth;

    public DateView(Context context) {
        super(context);
        doInit(context);
    }

    public DateView(Context context, AttributeSet attrs) {
        super(context, attrs);
        doInit(context);
    }

    public DateView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        doInit(context);
    }

    private void doInit(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.date_view, null, false);
        addView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        mDaySpinner = (Spinner) view.findViewById(R.id.spinner_day);
        mMonthSpinner = (Spinner) view.findViewById(R.id.spinner_month);
        mYearSpinner = (Spinner) view.findViewById(R.id.spinner_year);

        mDayAdapter = new DayAdapter(context);
        mMonthAdapter = new MonthAdapter(context);
        mYearAdapter = new YearAdapter(context);

        mDaySpinner.setAdapter(mDayAdapter);
        mMonthSpinner.setAdapter(mMonthAdapter);
        mYearSpinner.setAdapter(mYearAdapter);

        mDaySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mCurrentDay = mDayAdapter.getItem(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mMonthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mCurrentMonth = mMonthAdapter.getItem(position).value;
                mDayAdapter.loadDaysFor(mCurrentMonth, mCurrentYear);
                mDaySpinner.setSelection(mCurrentDay-1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mYearSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mCurrentYear = mYearAdapter.getItem(position);
                mDayAdapter.loadDaysFor(mCurrentMonth, mCurrentYear);
                mDaySpinner.setSelection(mCurrentDay-1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        setDate(new GregorianCalendar().getTime());
    }

    public Date getDate() {
        return new GregorianCalendar(mCurrentYear, mCurrentMonth, mCurrentDay).getTime();
    }

    public void setDate(Date date) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        mCurrentDay = calendar.get(Calendar.DAY_OF_MONTH);
        mCurrentMonth = calendar.get(Calendar.MONTH);
        mCurrentYear = calendar.get(Calendar.YEAR);

        mYearSpinner.setSelection(new GregorianCalendar().get(Calendar.YEAR)-mCurrentYear);
        mMonthSpinner.setSelection(mCurrentMonth-1);
        mDaySpinner.setSelection(mCurrentDay-1);
    }

    class DayAdapter extends BaseAdapter {
        private Context mContext;
        private ArrayList<Integer> mDays = new ArrayList<>();

        public DayAdapter(Context context) {
            mContext = context;
        }

        public void loadDaysFor(int month, int year) {
            mDays.clear();
            Calendar calender = new GregorianCalendar(year, month, 1);
            int daysInMonth = calender.getActualMaximum(Calendar.DAY_OF_MONTH);
            for(int i=0; i<daysInMonth; i++) {
                mDays.add(i+1);
            }
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mDays.size();
        }

        @Override
        public Integer getItem(int position) {
            return mDays.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService
                        (Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.tag_spinner_row, parent, false);
            } else {
                view = convertView;
            }
            ((TextView) view.findViewById(R.id.textView)).setText(String.format("%02d", getItem(position)));
            return view;
        }
    }

    class Month {
        int value;
        String name;

        public Month(int value, String name) {
            this.value = value;
            this.name= name;
        }
    }

    class MonthAdapter extends BaseAdapter {
        private Context mContext;
        private ArrayList<Month> mMonths = new ArrayList<>();

        public MonthAdapter(Context context) {
            mContext = context;
            mMonths.add(new Month(Calendar.JANUARY, "JAN"));
            mMonths.add(new Month(Calendar.FEBRUARY, "FEB"));
            mMonths.add(new Month(Calendar.MARCH, "MAR"));
            mMonths.add(new Month(Calendar.APRIL, "APR"));
            mMonths.add(new Month(Calendar.MAY, "MAY"));
            mMonths.add(new Month(Calendar.JUNE, "JUN"));
            mMonths.add(new Month(Calendar.JULY, "JUL"));
            mMonths.add(new Month(Calendar.AUGUST, "AUG"));
            mMonths.add(new Month(Calendar.SEPTEMBER, "SEP"));
            mMonths.add(new Month(Calendar.OCTOBER, "OCT"));
            mMonths.add(new Month(Calendar.NOVEMBER, "NOV"));
            mMonths.add(new Month(Calendar.DECEMBER, "DEC"));
        }

        @Override
        public int getCount() {
            return mMonths.size();
        }

        @Override
        public Month getItem(int position) {
            return mMonths.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService
                        (Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.tag_spinner_row, parent, false);
            } else {
                view = convertView;
            }
            ((TextView) view.findViewById(R.id.textView)).setText(getItem(position).name);
            return view;

        }
    }

    class YearAdapter extends BaseAdapter {
        private Context mContext;
        private ArrayList<Integer> mYears = new ArrayList<>();

        public YearAdapter(Context context) {
            mContext = context;
            int currentYear = new GregorianCalendar().get(Calendar.YEAR);
            for(int i=0; i<10; i++) {
                mYears.add(currentYear-i);
            }
        }

        @Override
        public int getCount() {
            return mYears.size();
        }

        @Override
        public Integer getItem(int position) {
            return mYears.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService
                        (Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.tag_spinner_row, parent, false);
            } else {
                view = convertView;
            }
            ((TextView) view.findViewById(R.id.textView)).setText(String.valueOf(getItem(position)));
            return view;
        }
    }
}
