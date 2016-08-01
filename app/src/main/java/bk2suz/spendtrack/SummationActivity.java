package bk2suz.spendtrack;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;

/**
 * Created by sujoy on 23/5/16.
 */
public class SummationActivity extends AppCompatActivity {
    private TagsAdapter mTagsAdapter;
    private SpendingsAdapter mSpendingsAdapter;
    private TextView mTextViewTotal;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summation);


        DateView dateViewStart = (DateView) findViewById(R.id.date_view_start);
        DateView dateViewEnd = (DateView) findViewById(R.id.date_view_end);

        dateViewStart.setDate(Preference.getDate(this,
                Preference.PREF_SUM_START_DATE, new GregorianCalendar(2007, 1, 1).getTime()));
        dateViewEnd.setDate(new GregorianCalendar().getTime());

        mTextViewTotal = (TextView) findViewById(R.id.text_view_total);

        mTagsAdapter = new TagsAdapter(this);
        GridView tagGridView = (GridView) findViewById(R.id.grid_view_tags);
        tagGridView.setAdapter(mTagsAdapter);

        mSpendingsAdapter = new SpendingsAdapter(this);
        ListView spendingListView = (ListView) findViewById(R.id.list_view_spendings);
        spendingListView.setAdapter(mSpendingsAdapter);

        CheckBox checkBox = (CheckBox) findViewById(R.id.check_all);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mSpendingsAdapter.selectAll(isChecked);
                showTotal();
            }
        });

        Button btnFetch = (Button) findViewById(R.id.button_fetch);
        btnFetch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date startDate = ((DateView) findViewById(R.id.date_view_start)).getDate();
                Date endDate = ((DateView) findViewById(R.id.date_view_end)).getDate();
                ((CheckBox) findViewById(R.id.check_all)).setChecked(false);
                mSpendingsAdapter.updateSpendingRecords(startDate, endDate, mTagsAdapter.mSelectedIds);
                showTotal();
            }
        });
    }

    @Override
    protected void onStop() {
        DateView dateViewStart = (DateView) findViewById(R.id.date_view_start);
        Preference.saveDate(this, Preference.PREF_SUM_START_DATE, dateViewStart.getDate());
        super.onStop();
    }

    public void showTotal() {
        mTextViewTotal.setText(String.valueOf(mSpendingsAdapter.getTotal()));
    }

    class TagsAdapter extends BaseAdapter {
        private Context mContext;
        private ArrayList<TagRecord> mTagRecords = new ArrayList<>();
        private HashSet<Long> mSelectedIds = new HashSet<>();

        public TagsAdapter(Context context) {
            mContext = context;
            mTagRecords.addAll(TagRecord.getList());
        }

        @Override
        public int getCount() {
            return mTagRecords.size();
        }

        @Override
        public TagRecord getItem(int position) {
            return mTagRecords.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if(convertView == null) {
                view = getLayoutInflater().inflate(R.layout.tag_grid_cell, parent, false);
            } else {
                view = convertView;
            }
            CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkBox);
            TagRecord tagRecord = getItem(position);
            final long tagId = tagRecord.getId();
            checkBox.setText(tagRecord.getName());
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                private long mTagId = tagId;
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) mSelectedIds.add(mTagId);
                    else mSelectedIds.remove(mTagId);
                }
            });
            checkBox.setChecked(mSelectedIds.contains(tagRecord.getId()));
            return view;
        }
    }


    class SpendingsAdapter extends BaseAdapter {
        private Context mContext;
        private ArrayList<SpendingRecord> mSpendingRecords = new ArrayList<>();
        private HashSet<Long> mSelectedIds = new HashSet<>();

        public SpendingsAdapter(Context context) {
            mContext = context;
        }

        public void updateSpendingRecords(Date fromDate, Date toDate, HashSet<Long> tagIds) {
            mSpendingRecords.clear();
            mSelectedIds.clear();

            mSpendingRecords.addAll(SpendingRecord.getList(fromDate, toDate, tagIds));
            notifyDataSetChanged();
        }

        public void selectAll(boolean select) {
            if (select) {
                for(SpendingRecord record: mSpendingRecords) {
                    mSelectedIds.add(record.getId());
                }
            } else {
                mSelectedIds.clear();
            }
            notifyDataSetChanged();
        }

        public float getTotal() {
            float total = 0;
            for(SpendingRecord record: mSpendingRecords) {
                if (!mSelectedIds.contains(record.getId())) continue;
                total += record.getAmount();
            }
            return total;
        }

        @Override
        public int getCount() {
            return mSpendingRecords.size();
        }

        @Override
        public SpendingRecord getItem(int position) {
            return mSpendingRecords.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if(convertView == null) {
                view = getLayoutInflater().inflate(R.layout.spending_sum_row, parent, false);
            } else {
                view = convertView;
            }
            final SpendingRecord spendingRecord = getItem(position);

            ((TextView) view.findViewById(R.id.txt_date)).setText(spendingRecord.getDateString());
            ((TextView) view.findViewById(R.id.txt_purpose)).setText(spendingRecord.getPurpose());
            ((TextView) view.findViewById(R.id.txt_amount)).setText(spendingRecord.getAmountString());

            CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkBox);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                private long mSpendingId = spendingRecord.getId();
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) mSelectedIds.add(mSpendingId);
                    else mSelectedIds.remove(mSpendingId);
                    showTotal();
                }
            });
            checkBox.setChecked(mSelectedIds.contains(spendingRecord.getId()));
            return view;
        }
    }
}
