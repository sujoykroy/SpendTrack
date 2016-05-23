package bk2suz.spendtrack;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

/**
 * Created by sujoy on 9/5/16.
 */
public class DbManager {
    public static final String DB_EXTENSION = ".db";
    private static final int DATABASE_VERSION = 2;

    private static ArrayList<DbManager> sDbManList = null;

    public static void createManagers(Context context) {
        sDbManList = new ArrayList<DbManager>();

        DbManager dbMan= new DbManager("SpendTrack", context);
        dbMan.addTable(TagRecord.TagTable);
        dbMan.addTable(SpendingRecord.SpendingTable);
        sDbManList.add(dbMan);
    }

    public static void clearTables() {
        TagRecord.TagTable.deleteAllRecords();
        SpendingRecord.SpendingTable.deleteAllRecords();
    }

    public Object AccessLock = new Object();
    private String mDatabaseName;
    private ArrayList<Table> mTables = new ArrayList<Table>();

    private class DbHelper extends SQLiteOpenHelper {
        public DbHelper(Context context) {
            super(context, mDatabaseName + DB_EXTENSION, null, mDbVersion);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            for(Table table: mTables) {
                db.execSQL(table.getCreateSchema());
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            /*for(Table table: mTables) {
                db.execSQL(table.getDeleteSchema());
            }
            onCreate(db);*/
        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }
    }

    private int mDbVersion;
    private DbHelper mDbHelper;
    private Context mContext;

    public DbManager(String name, Context context, int extraVersion) {
        mDatabaseName = name;
        mContext = context;
        mDbVersion = DATABASE_VERSION + extraVersion;
    }

    public DbManager(String name, Context context) {
        this(name, context, 0);
    }

    public String getDatabaseName() {
        return mDatabaseName;
    }

    public SQLiteOpenHelper getDbHelper() {
        if (mDbHelper == null) {
            mDbHelper = new DbHelper(mContext);
        }
        return mDbHelper;
    }

    private void addTable(Table table) {
        if (mDbHelper != null) return; /*Avoid adding table after SQLiteDBHelper is instantiated*/
        mTables.add(table);
        table.setDbManager(this);
    }
}
