package com.example.smdassignment3;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "password_manager.db";
    private static final int DATABASE_VERSION = 4;

    // Table name and columns
    // User table
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    // Entries table
    public static final String TABLE_ENTRIES = "entries";
    public static final String COLUMN_ENTRY_ID = "entry_id";
    public static final String COLUMN_ENTRY_USERNAME = "entry_username";
    public static final String COLUMN_ENTRY_PASSWORD = "entry_password";
    public static final String COLUMN_ENTRY_URL = "entry_url";
    // Recycle bin table
    public static final String TABLE_RECYCLE_BIN = "recycle_bin";
    public static final String COLUMN_DELETED_ENTRY_ID = "deleted_entry_id";
    public static final String COLUMN_DELETED_ENTRY_USERNAME = "deleted_entry_username";
    public static final String COLUMN_DELETED_ENTRY_PASSWORD = "deleted_entry_password";
    public static final String COLUMN_DELETED_ENTRY_URL = "deleted_entry_url";


    // Create table query
    // Create users table
    private static final String SQL_CREATE_TABLE_USERS =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USERNAME + " TEXT, " +
                    COLUMN_PASSWORD + " TEXT)";

    // Create entries table
    private static final String SQL_CREATE_TABLE_ENTRIES =
            "CREATE TABLE " + TABLE_ENTRIES + " (" +
                    COLUMN_ENTRY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USER_ID + " INTEGER, " +
                    COLUMN_ENTRY_USERNAME + " TEXT, " +
                    COLUMN_ENTRY_PASSWORD + " TEXT, " +
                    COLUMN_ENTRY_URL + " TEXT, " +
                    "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " +
                    TABLE_USERS + "(" + COLUMN_USER_ID + "))";

    // Create recycle bin table
    private static final String SQL_CREATE_TABLE_RECYCLE_BIN =
            "CREATE TABLE " + TABLE_RECYCLE_BIN + " (" +
                    COLUMN_DELETED_ENTRY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USER_ID + " INTEGER, " +
                    COLUMN_DELETED_ENTRY_USERNAME + " TEXT, " +
                    COLUMN_DELETED_ENTRY_PASSWORD + " TEXT, " +
                    COLUMN_DELETED_ENTRY_URL + " TEXT," +
                    "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " +
                    TABLE_USERS + "(" + COLUMN_USER_ID + "))";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create tables
        db.execSQL(SQL_CREATE_TABLE_USERS);
        db.execSQL(SQL_CREATE_TABLE_ENTRIES);
        db.execSQL(SQL_CREATE_TABLE_RECYCLE_BIN);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older tables if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECYCLE_BIN);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ENTRIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        // Create tables again
        onCreate(db);
    }

    // Method to restore an entry from the recycle bin
    public void restoreEntry(int deletedEntryId, String username, String password, String url) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ENTRY_USERNAME, username);
        values.put(COLUMN_ENTRY_PASSWORD, password);
        values.put(COLUMN_ENTRY_URL, url);

        // Insert the restored entry into the entries table
        long newRowId = db.insert(TABLE_ENTRIES, null, values);
        if (newRowId != -1) {
            // Entry restored successfully
            // Delete the entry from the recycle bin
            db.delete(TABLE_RECYCLE_BIN, COLUMN_DELETED_ENTRY_ID + " = ?", new String[]{String.valueOf(deletedEntryId)});
        }
    }

    // Retrieve deleted entries from the recycle bin table
    public List<Entry> getDeletedEntriesFromRecycleBin() {
        List<Entry> deletedEntries = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_RECYCLE_BIN,
                null,
                null,
                null,
                null,
                null,
                null
        );
        if (cursor != null) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range") int entryId = cursor.getInt(cursor.getColumnIndex(COLUMN_DELETED_ENTRY_ID));
                @SuppressLint("Range") String username = cursor.getString(cursor.getColumnIndex(COLUMN_DELETED_ENTRY_USERNAME));
                @SuppressLint("Range") String password = cursor.getString(cursor.getColumnIndex(COLUMN_DELETED_ENTRY_PASSWORD));
                @SuppressLint("Range") String url = cursor.getString(cursor.getColumnIndex(COLUMN_DELETED_ENTRY_URL));
                Entry entry = new Entry(entryId, username, password, url);
                deletedEntries.add(entry);
            }
            cursor.close();
        }
        return deletedEntries;
    }


}

