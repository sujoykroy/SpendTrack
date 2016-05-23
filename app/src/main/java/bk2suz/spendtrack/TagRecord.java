package bk2suz.spendtrack;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by sujoy on 9/5/16.
 */
public class TagRecord implements Parcelable {
    public static final String FIELD_ROWID = "rowid";
    public static final String FIELD_NAME = "name";

    public static Table TagTable =  new Table("tags") {
        @Override
        public String getCreateSchema() {
            ArrayList<Column> columns = new ArrayList<Column>();
            columns.add(new Column(FIELD_NAME, Column.Type.Text).setIsNullable(false));
            StringBuilder sql = new StringBuilder(buildCreateSchema(columns));
            return buildCreateSchema(columns);
        }
    };

    private String mName;
    private long mId;

    public TagRecord(String name, long id) {
        mName = name;
        mId = id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mId);
        dest.writeString(mName);
    }

    public static final Parcelable.Creator<TagRecord> CREATOR = new Creator<TagRecord>() {
        @Override
        public TagRecord createFromParcel(Parcel source) {
            Long id = source.readLong();
            String name = source.readString();
            return new TagRecord(name, id);
        }

        @Override
        public TagRecord[] newArray(int size) {
            return new TagRecord[size];
        }
    };

    public String getName() {
        return mName;
    }

    public long getId() {
        return mId;
    }

    public static void addNew(String tagName) {
        if (tagName.trim().length()==0) return;
        ContentValues values = new ContentValues();
        values.put(FIELD_NAME, tagName.trim());
        TagTable.insert(values);
    }

    public static ArrayList<TagRecord> getList() {
        ArrayList<TagRecord> tagRecords = new ArrayList<>();
        DbManager dbManager = TagTable.getDbManager();
        synchronized (dbManager.AccessLock) {
            SQLiteDatabase db = dbManager.getDbHelper().getReadableDatabase();
            if (db != null) {
                String[] mSelectColumns = new String[] {FIELD_ROWID, FIELD_NAME};
                String orderBy = String.format("%s ASC", FIELD_NAME);
                Cursor cursor = db.query(TagTable.getTableName(), mSelectColumns, null, null, null, null, orderBy);
                while(cursor.moveToNext()) {
                    TagRecord tagRecord = new TagRecord(cursor.getString(1), cursor.getLong(0));
                    tagRecords.add(tagRecord);
                }
                cursor.close();
                if (tagRecords.size()==0) {
                    addNew("General Entries");
                    addNew("Pendings");
                    tagRecords = getList();
                }
                db.close();
            }
        }
        return tagRecords;
    }

    public static HashMap<Long, String> getHashMap() {
        ArrayList<TagRecord> tagRecords = getList();
        HashMap<Long, String> tags = new HashMap<>();
        for (TagRecord tagRecord: tagRecords) {
            tags.put(tagRecord.mId, tagRecord.mName);
        }
        return tags;
    }

    public static TagRecord getById(long id) {
        TagRecord tagRecord = null;
        DbManager dbManager = TagTable.getDbManager();
        synchronized (dbManager.AccessLock) {
            SQLiteDatabase db = dbManager.getDbHelper().getReadableDatabase();
            if (db != null) {
                String[] mSelectColumns = new String[] {FIELD_ROWID, FIELD_NAME};
                String selection = String.format("%s = %d", FIELD_ROWID, id);
                Cursor cursor = db.query(TagTable.getTableName(), mSelectColumns, selection, null, null, null, null);
                while(cursor.moveToNext()) {
                    tagRecord = new TagRecord(cursor.getString(1), cursor.getLong(0));
                }
                cursor.close();
                db.close();
            }
        }
        return tagRecord;
    }

}
