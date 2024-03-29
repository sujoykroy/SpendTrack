package bk2suz.spendtrack;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.logging.Logger;

public class MainActivity extends AppCompatActivity {
    private static final int NEW_SPENDING_REQUEST = 1;
    private static final int EDIT_SPENDING_REQUEST = 2;
    private static final int FILE_SELECT_CODE = 3;

    private TagSpinnerAdapter mTagSpinnerAdapter;
    private SpendingListAdapter mSpendingListAdapter;
    private TagRecord mCurrentTagRecord;

    private DateView mDateViewStart;
    private DateView mDateViewEnd;

    private LinearLayout mLinearLayoutTotal;
    private TextView mTextViewTotal;

    CsvImportTask mCsvImportTask = new CsvImportTask();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mDateViewStart = (DateView) findViewById(R.id.date_view_start);
        mDateViewEnd = (DateView) findViewById(R.id.date_view_end);

        mDateViewStart.setDate(Preference.getDate(this,
                Preference.PREF_LIST_START_DATE, new GregorianCalendar(2007, 1, 1).getTime()));
        mDateViewEnd.setDate(new GregorianCalendar().getTime());

        mLinearLayoutTotal = (LinearLayout) findViewById(R.id.linear_layout_total);
        mLinearLayoutTotal.setVisibility(View.INVISIBLE);
        mTextViewTotal = (TextView) findViewById(R.id.text_view_total);

        Button btnNewTag = (Button) findViewById(R.id.button_new_tag);
        btnNewTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNewTagDialog();
            }
        });

        Button btnNewSpending = (Button) findViewById(R.id.button_new_spending);
        btnNewSpending.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNewSpendingDialog();
            }
        });

        mTagSpinnerAdapter = new TagSpinnerAdapter(getBaseContext());
        Spinner tagSpinnerView = (Spinner) findViewById(R.id.spinnerTags);
        tagSpinnerView.setAdapter(mTagSpinnerAdapter);
        tagSpinnerView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TagRecord tagRecord= mTagSpinnerAdapter.getItem(position);
                mCurrentTagRecord = tagRecord;
                Preference.saveLastTag(getBaseContext(), tagRecord.getName());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        tagSpinnerView.setSelection(mTagSpinnerAdapter.getIndexOfItem(Preference.getLastTag(this)));

        mSpendingListAdapter = new SpendingListAdapter(getBaseContext());
        ListView spendingListView = (ListView) findViewById(R.id.list_view_spendings);
        spendingListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, SpendingActivity.class);
                intent.putExtra(SpendingActivity.SPENDING_RECORD, mSpendingListAdapter.getItem(position));
                startActivityForResult(intent, EDIT_SPENDING_REQUEST);
                return true;
            }
        });
        spendingListView.setAdapter(mSpendingListAdapter);

        Button btnShowSpendings = (Button) findViewById(R.id.button_show_spendings);
        btnShowSpendings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSpendingListAdapter.updateSpendingRecords(mDateViewStart.getDate(), mDateViewEnd.getDate(), mCurrentTagRecord);
            }
        });

        Button btnExport = (Button) findViewById(R.id.button_export);
        btnExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String filename = String.format("SpendTrack_Export_%d.csv.txt",new Date().getTime());
                File folder = Environment.getExternalStoragePublicDirectory("SpendTrack");
                File file = new File(folder, filename);
                try {
                    folder.mkdir();
                    file.createNewFile();
                } catch (IOException e) {
                    return;
                }
                try {
                    FileWriter fileWriter = new FileWriter(file);
                    HashMap<Long, String> tags = TagRecord.getHashMap();
                    SpendingRecord.export(fileWriter, tags);
                    fileWriter.close();
                    Toast.makeText(getApplicationContext(), R.string.export_done, Toast.LENGTH_SHORT).show();
                } catch (IOException e) {}

            }
        });

        Button btnImport = (Button) findViewById(R.id.button_import);
        btnImport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFileChooser();
            }
        });

        Button btnSum = (Button) findViewById(R.id.button_summation);
        btnSum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SummationActivity.class);
                startActivity(intent);
            }
        });

        //mSpendingListAdapter.updateSpendingRecords(mDateViewStart.getDate(), mDateViewEnd.getDate(), mCurrentTagRecord);
    }

    @Override
    protected void onStop() {
        Preference.saveDate(this, Preference.PREF_LIST_START_DATE, mDateViewStart.getDate());
        super.onStop();
    }

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a File to Import"),
                    FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void openNewTagDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.new_tag);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String tagName = input.getText().toString();
                TagRecord.addNew(tagName);
                mTagSpinnerAdapter.updateTagRecords();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void openNewSpendingDialog() {
        Intent intent = new Intent(MainActivity.this, SpendingActivity.class);
        intent.putExtra(SpendingActivity.TAG_RECORD, mCurrentTagRecord);
        startActivityForResult(intent, NEW_SPENDING_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case NEW_SPENDING_REQUEST:
                if (resultCode == RESULT_OK) {
                    //do something
                }
                break;
            case FILE_SELECT_CODE:
                // Get the Uri of the selected file
                if (data == null) break;
                Uri uri = data.getData();
                //Log.d("SpenTrack", "File Uri: " + uri.toString());
                mCsvImportTask.execute(uri);
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    class CsvImportTask extends AsyncTask<Uri, Void, Integer> {

        @Override
        protected Integer doInBackground(Uri... uris) {
            ParcelFileDescriptor parcelFileDescriptor =
                    null;
            try {
                parcelFileDescriptor = getContentResolver().openFileDescriptor(uris[0], "r");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return 0;
            }
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            //Log.d("Spentrack", "Way 1");
            return SpendingRecord.importFromCSV(fileDescriptor);
        }

        @Override
        protected void onPreExecute() {
            String text = String.format("records are being fetched from csv.");
            Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(Integer integer) {
            //Log.d("Spentrack", "Way 2");
            String text = String.format("%d records are added from csv.", integer);
            Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
        }
    }

    class TagSpinnerAdapter extends BaseAdapter {
        private Context mContext;
        private ArrayList<TagRecord> mTagRecords = new ArrayList<>();

        public TagSpinnerAdapter(Context context) {
            super();
            mContext = context;
            updateTagRecords();
        }

        public void updateTagRecords() {
            mTagRecords.clear();
            mTagRecords.addAll(TagRecord.getList());
            notifyDataSetChanged();
        }

        public int getIndexOfItem(String name) {
            for (int i=0; i<mTagRecords.size(); i++) {
                if (mTagRecords.get(i).getName().equals(name)) return i;
            }
            return -1;
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
            if (convertView == null) {
                view = getLayoutInflater().inflate(R.layout.tag_spinner_row, parent, false);
            } else {
                view = convertView;
            }
            ((TextView) view.findViewById(R.id.textView)).setText(getItem(position).getName());
            return view;
        }
    }

    class SpendingListAdapter extends BaseAdapter {
        private Context mContext;
        private ArrayList<SpendingRecord> mSpendingRecords = new ArrayList<>();
        private float mTotal;

        public SpendingListAdapter(Context context) {
            super();
            mContext = context;
        }

        public void updateSpendingRecords(Date fromDate, Date toDate, TagRecord tagRecord) {
            mSpendingRecords.clear();
            mTotal = 0;
            mSpendingRecords.addAll(SpendingRecord.getList(fromDate, toDate, tagRecord));
            for(SpendingRecord record: mSpendingRecords) {
                mTotal += record.getAmount();
            }
            if (mSpendingRecords.size() ==0 ) {
                mLinearLayoutTotal.setVisibility(View.INVISIBLE);
            } else {
                mLinearLayoutTotal.setVisibility(View.VISIBLE);
                mTextViewTotal.setText(String.valueOf(mTotal));
            }
            notifyDataSetChanged();
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
            if (convertView == null) {
                view = getLayoutInflater().inflate(R.layout.spending_row, parent, false);
            } else {
                view = convertView;
            }
            SpendingRecord spendingRecord = getItem(position);
            ((TextView) view.findViewById(R.id.txt_date)).setText(spendingRecord.getDateString());
            ((TextView) view.findViewById(R.id.txt_purpose)).setText(spendingRecord.getPurpose());
            ((TextView) view.findViewById(R.id.txt_amount)).setText(spendingRecord.getAmountString());
            return view;
        }
    }
}
