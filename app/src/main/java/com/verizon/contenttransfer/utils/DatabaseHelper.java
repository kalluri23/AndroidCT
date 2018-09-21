package com.verizon.contenttransfer.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * This class helps open, create, and upgrade the database file.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    static final String SYNCDATA_TABLE_NAME = "sync";
    static final String PERSONID = "person";
    static final String SYNCID = "syncid";
    static final String DATABASE_NAME = "syncdata.db";

    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + SYNCDATA_TABLE_NAME + " ("
                + PERSONID + " INTEGER PRIMARY KEY,"
                + SYNCID + " TEXT UNIQUE"
                +");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // No need to do anything --- this is version 1

    }
}