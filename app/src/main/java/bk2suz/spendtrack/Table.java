package bk2suz.spendtrack;

import android.content.ContentValues;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by sujoy on 9/5/16.
 */
public abstract class Table {
    private static final String PRIMARY_KEY = "PRIMARY KEY";
    private static final String NOT_NULL = "NOT NULL";
    private static final String DEFAULT = "DEFAULT";
    private static final String SPACE = " ";
    private static final String COMMA = ",";
    private static final String TEXT = "TEXT";
    private static final String INTEGER = "INTEGER";
    private static final String REAL = "REAL";
    private static final String EMPTY_STRING = "";
    private static final String SYNC = "sync";
    private static final String ON_CONFLICT = "ON CONFLICT";
    private static final String UNIQUE = "UNIQUE";
    private static final String AUTOINCREMENT = "AUTOINCREMENT";

    private static final String CREATE_TABLE_SYNTAX = "CREATE TABLE IF NOT EXISTS %s (%s) %s";
    private static final String DROP_TABLE_SYNTAX = "DROP TABLE IF EXISTS %s";
    private static final String CLEAR_TABLE_SYNTAX = "DELETE FROM %s";

    protected static class Column {
        public static final String CONFLICT_REPLACE = "REPLACE";

        public enum Type {
            Text, Integer, Constraint, Real
        }

        private String mColumnName;
        private Type mColumnType;
        private String mDefaultValue;
        private boolean mIsPrimaryKey;
        private boolean mIsNullable;
        private boolean mIsUnique;
        private String mPrimaryKeyConflictClause;
        private boolean mAutoIncrment = false;

        public Column(String name, Type type) {
            mColumnName = name;
            mColumnType = type;
            mIsNullable = true;
            mIsPrimaryKey = false;
            mDefaultValue = null;
            mIsUnique = false;
            mPrimaryKeyConflictClause = null;
        }

        public Column setIsNullable(boolean value) {
            mIsNullable = value;
            return this;
        }

        public Column setIsPrimaryKey(boolean value, String conflictClause) {
            mIsPrimaryKey = value;
            mPrimaryKeyConflictClause = conflictClause;
            return this;
        }

        public Column setIsPrimaryKey(boolean value) {
            return setIsPrimaryKey(value, null);
        }

        public Column autoIncrment() {
            mAutoIncrment = true;
            return this;
        }

        public Column setDefaultValue(String value) {
            mDefaultValue = value;
            return this;
        }

        public Column setIsUnique(boolean value) {
            mIsUnique = value;
            return this;
        }

        public String getDefinition() {
            StringBuilder defn = new StringBuilder();
            defn.append(mColumnName);
            defn.append(SPACE);
            switch(mColumnType) {
                case Integer: defn.append(INTEGER);break;
                case Text: defn.append(TEXT);break;
                case Real: defn.append(REAL);break;
            }
            if (mColumnType != Type.Constraint) {
                if (mIsPrimaryKey) {
                    defn.append(SPACE);
                    defn.append(PRIMARY_KEY);
                    if (mPrimaryKeyConflictClause != null) {
                        defn.append(SPACE);
                        defn.append(ON_CONFLICT);
                        defn.append(SPACE);
                        defn.append(mPrimaryKeyConflictClause);
                    }
                }
                if (mIsUnique) {
                    defn.append(SPACE);
                    defn.append(UNIQUE);
                }
                if (!mIsNullable) {
                    defn.append(SPACE);
                    defn.append(NOT_NULL);
                }
                if (mDefaultValue != null) {
                    defn.append(SPACE);
                    defn.append(DEFAULT);
                    defn.append(SPACE);
                    defn.append(mDefaultValue);
                }
                if(mAutoIncrment) {
                    defn.append(SPACE);
                    defn.append(AUTOINCREMENT);
                }
            }
            return defn.toString();
        }
    }

    protected String mTableName;
    protected DbManager mDbManager;
    protected String mSyncPrefName;

    public Table(String tableName) {
        mTableName = tableName;
        mDbManager = null;
    }

    public String getTableName() {
        return mTableName;
    }

    public void setDbManager(DbManager dbManager) {
        mDbManager = dbManager;
    }

    public DbManager getDbManager() {
        return mDbManager;
    }

    public String getDatabaseName() {
        return getDbManager().getDatabaseName();
    }

    public abstract String getCreateSchema();

    protected String buildCreateSchema(ArrayList<Column> columns) {
        StringBuilder columnsDefn = new StringBuilder();
        for(int i=0; i< columns.size(); i++) {
            Column column = columns.get(i);
            columnsDefn.append(column.getDefinition());
            if(i<columns.size()-1) columnsDefn.append(COMMA);
        }
        return String.format(CREATE_TABLE_SYNTAX, mTableName, columnsDefn, EMPTY_STRING);
    }

    public String getDeleteSchema() {
        return String.format(DROP_TABLE_SYNTAX, mTableName);
    }

    public void deleteAllRecords() {
        synchronized(mDbManager.AccessLock) {
            SQLiteDatabase db = mDbManager.getDbHelper().getWritableDatabase();
            if (db != null) {
                try {
                    db.execSQL(String.format(CLEAR_TABLE_SYNTAX, getTableName()));
                } catch (SQLException e) {}
                db.close();
            }
        }
    }

    public long inset(ContentValues values) {
        long rowId = -1;
        synchronized (mDbManager.AccessLock) {
            SQLiteDatabase db = null;
            try {
                db = mDbManager.getDbHelper().getWritableDatabase();
            } catch (Exception e) {
                db = null;
            }
            if (db == null) {
                rowId = -2;
            } else {
                rowId = db.insert(mTableName, null, values);
                db.close();
            }
        }
        return rowId;
    }

    public long update(ContentValues values, String whereClause, String[] whereArgs) {
        long rowCount = -1;
        synchronized (mDbManager.AccessLock) {
            SQLiteDatabase db = null;
            try {
                db = mDbManager.getDbHelper().getWritableDatabase();
            } catch (Exception e) {
                db = null;
            }
            if (db == null) {
                rowCount = -2;
            } else {
                rowCount = db.update(mTableName, values, whereClause, whereArgs);
                db.close();
            }
        }
        return rowCount;
    }
}
