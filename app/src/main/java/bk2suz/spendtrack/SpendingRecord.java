package bk2suz.spendtrack;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.util.SparseArray;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by sujoy on 9/5/16.
 */
public class SpendingRecord implements Parcelable  {
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

    private long mId;
    private long mTagId;
    private float mAmount;
    private String mPurpose;
    private Date mDate;

    private static SimpleDateFormat sSimpleDate =  new SimpleDateFormat("dd/MM/yyyy");

    public SpendingRecord(long id, long timestamp, String purpose, float amount, long tagId) {
        mId = id;
        mAmount = amount;
        mPurpose = purpose;
        mDate = new Date(timestamp);
        mTagId = tagId;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mId);
        dest.writeFloat(mAmount);
        dest.writeString(mPurpose);
        dest.writeLong(mDate.getTime());
        dest.writeLong(mTagId);
    }

    public static final Parcelable.Creator<SpendingRecord> CREATOR = new Creator<SpendingRecord>() {
        @Override
        public SpendingRecord createFromParcel(Parcel source) {
            Long id = source.readLong();
            float amount = source.readFloat();
            String purpose = source.readString();
            Long timstamp = source.readLong();
            Long tagId= source.readLong();
            return new SpendingRecord(id, timstamp, purpose, amount, tagId);
        }

        @Override
        public SpendingRecord[] newArray(int size) {
            return new SpendingRecord[size];
        }
    };

    public String getDateString() {
        return sSimpleDate.format(mDate);
    }

    public Date getDate() {
        return mDate;
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

    public long getTagId() {
        return mTagId;
    }

    public long getId() {
        return mId;
    }

    public boolean update(Date date, String purpose, float amount) {
        if (purpose.trim().length()==0) return false;
        ContentValues values = new ContentValues();
        values.put(FIELD_PURPOSE, purpose.trim());
        values.put(FIELD_AMOUNT, amount);
        values.put(FIELD_TIMESTAMP, date.getTime());
        long rowCount = SpendingTable.update(values, String.format("%s = %d", FIELD_ROWID, mId), null);
        if (rowCount>0) {
            mAmount = amount;
            mPurpose = purpose;
            mDate = date;
        }
        return rowCount>0;
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
                String[] mSelectColumns = new String[] {
                        FIELD_ROWID, FIELD_TIMESTAMP, FIELD_PURPOSE, FIELD_AMOUNT, FIELD_TAG_ID};
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
                            cursor.getLong(0), cursor.getLong(1), cursor.getString(2),
                            cursor.getFloat(3), cursor.getLong(4));
                    spendingRecords.add(spendingRecord);
                }
                cursor.close();
                db.close();
            }
        }
        return spendingRecords;
    }

    public static ArrayList<SpendingRecord> getList(Date fromDate, Date toDate, HashSet<Long> tagIds) {
        ArrayList<SpendingRecord> spendingRecords = new ArrayList<>();

            StringBuilder tagIdsString = new StringBuilder();
        int i =0;
        for(long tagId: tagIds) {
            tagIdsString.append(String.valueOf(tagId));
            if(i<tagIds.size()-1) {
                tagIdsString.append(",");
            }
            i++;
        }

        DbManager dbManager = SpendingTable.getDbManager();
        synchronized (dbManager.AccessLock) {
            SQLiteDatabase db = dbManager.getDbHelper().getReadableDatabase();
            if (db != null) {
                String[] mSelectColumns = new String[] {
                        FIELD_ROWID, FIELD_TIMESTAMP, FIELD_PURPOSE, FIELD_AMOUNT, FIELD_TAG_ID};
                String orderBy = String.format("%s ASC", FIELD_TIMESTAMP);
                ArrayList<String> selectionList = new ArrayList<>();
                if(fromDate != null) {
                    selectionList.add(String.format("%s >= %d", FIELD_TIMESTAMP , fromDate.getTime()));
                }
                if(toDate != null) {
                    selectionList.add(String.format("%s <= %d", FIELD_TIMESTAMP , toDate.getTime()));
                }
                if(tagIds != null) {
                    selectionList.add(String.format("%s IN (%s)", FIELD_TAG_ID , tagIdsString.toString()));
                }
                StringBuilder selection = new StringBuilder();
                for (i=0; i<selectionList.size(); i++) {
                    selection.append(selectionList.get(i));
                    if(i<selectionList.size()-1) selection.append(" AND ");
                }
                Cursor cursor = db.query(SpendingTable.getTableName(), mSelectColumns,
                        selection.toString(), null, null, null, orderBy);
                while(cursor.moveToNext()) {
                    SpendingRecord spendingRecord = new SpendingRecord(
                            cursor.getLong(0), cursor.getLong(1), cursor.getString(2),
                            cursor.getFloat(3), cursor.getLong(4));
                    spendingRecords.add(spendingRecord);
                }
                cursor.close();
                db.close();
            }
        }
        return spendingRecords;
    }

    public static void export(FileWriter fileWriter, HashMap<Long, String> tags) throws IOException {
        DbManager dbManager = SpendingTable.getDbManager();
        synchronized (dbManager.AccessLock) {
            SQLiteDatabase db = dbManager.getDbHelper().getReadableDatabase();
            if (db != null) {
                String[] mSelectColumns = new String[]{
                        FIELD_ROWID, FIELD_TIMESTAMP, FIELD_PURPOSE, FIELD_AMOUNT, FIELD_TAG_ID};
                String orderBy = String.format("%s ASC", FIELD_TIMESTAMP);
                Cursor cursor = db.query(SpendingTable.getTableName(), mSelectColumns, null, null, null, null, orderBy);
                while (cursor.moveToNext()) {
                    SpendingRecord spendingRecord = new SpendingRecord(
                            cursor.getLong(0), cursor.getLong(1), cursor.getString(2),
                            cursor.getFloat(3), cursor.getLong(4));
                    fileWriter.write(String.format("\"%s\",", spendingRecord.getDateString()));
                    fileWriter.write(String.format("\"%s\",", spendingRecord.getPurpose()));
                    fileWriter.write(String.format("%s,", spendingRecord.getAmountString()));
                    fileWriter.write(String.format("\"%s\"", tags.get(spendingRecord.mTagId)));
                    fileWriter.write("\n");
                }
                cursor.close();
                db.close();
            }
        }
    }
}
