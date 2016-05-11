package bk2suz.spendtrack;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by sujoy on 9/5/16.
 */
public class SpendingRecord {
    public static final String FIELD_ROWID = "rowid";
    public static final String FIELD_TAG_ID = "tagId";
    public static final String FIELD_PURPOSE = "purpose";
    public static final String FIELD_AMOUNT = "amount";
    public static final String FIELD_TIMESTAMP = "timestamp";

    public static Table SpendingTable =  new Table("spendings") {
        @Override
        public String getCreateSchema() {
            ArrayList<Column> columns = new ArrayList<Column>();
            columns.add(new Column(FIELD_TAG_ID, Column.Type.Integer).setIsNullable(false));
            columns.add(new Column(FIELD_PURPOSE, Column.Type.Text).setIsNullable(false));
            columns.add(new Column(FIELD_AMOUNT, Column.Type.Real).setIsNullable(false));
            columns.add(new Column(FIELD_TIMESTAMP, Column.Type.Integer).setIsNullable(false));
            return buildCreateSchema(columns);
        }
    };

    private float mAmount;
    private String mPurpose;
    private Date mDate;

    private static SimpleDateFormat sSimpleDate =  new SimpleDateFormat("dd/MM/yyyy z");

    public SpendingRecord(long timestamp, String purpose, float amount) {
        mAmount = amount;
        mPurpose = purpose;
        mDate = new Date(timestamp);
    }

    public String getDateString() {
        return sSimpleDate.format(mDate);
    }

    public String getPurpose() {
        return mPurpose;
    }

    public String getAmountString() {
        return String.valueOf(mAmount);
    }

    public float getAmount() {
        return mAmount;
    }

    public static void addNew(TagRecord tagRecord, Date date, String purpose, float amount) {
        if (purpose.trim().length()==0) return;
        ContentValues values = new ContentValues();
        values.put(FIELD_TAG_ID, tagRecord.getId());
        values.put(FIELD_PURPOSE, purpose.trim());
        values.put(FIELD_AMOUNT, amount);
        values.put(FIELD_TIMESTAMP, date.getTime());
        SpendingTable.insert(values);
    }

    public static ArrayList<SpendingRecord> getList(Date fromDate, Date toDate, TagRecord tagRecord) {
        ArrayList<SpendingRecord> spendingRecords = new ArrayList<>();
        DbManager dbManager = SpendingTable.getDbManager();
        synchronized (dbManager.AccessLock) {
            SQLiteDatabase db = dbManager.getDbHelper().getReadableDatabase();
            if (db != null) {
                String[] mSelectColumns = new String[] {FIELD_TIMESTAMP, FIELD_PURPOSE, FIELD_AMOUNT};
                String orderBy = String.format("%s ASC", FIELD_TIMESTAMP);
                ArrayList<String> selectionList = new ArrayList<>();
                if(fromDate != null) {
                    selectionList.add(String.format("%s >= %d", FIELD_TIMESTAMP , fromDate.getTime()));
                }
                if(toDate != null) {
                    selectionList.add(String.format("%s <= %d", FIELD_TIMESTAMP , toDate.getTime()));
                }
                if(tagRecord != null) {
                    selectionList.add(String.format("%s = %d", FIELD_TAG_ID , tagRecord.getId()));
                }
                StringBuilder selection = new StringBuilder();
                for (int i=0; i<selectionList.size(); i++) {
                    selection.append(selectionList.get(i));
                    if(i<selectionList.size()-1) selection.append(" AND ");
                }
                Cursor cursor = db.query(SpendingTable.getTableName(), mSelectColumns,
                        selection.toString(), null, null, null, orderBy);
                while(cursor.moveToNext()) {
                    SpendingRecord spendingRecord = new SpendingRecord(
                            cursor.getLong(0), cursor.getString(1), cursor.getFloat(2));
                    spendingRecords.add(spendingRecord);
                }
                cursor.close();
                db.close();
            }
        }
        return spendingRecords;
    }
}
