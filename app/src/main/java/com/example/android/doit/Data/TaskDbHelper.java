package com.example.android.doit.Data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.doit.Data.TaskContract.TaskEntry;

public class TaskDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "DoIt.db";
    public static final int DATABASE_VERISON = 5;

    public TaskDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERISON);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        String SQL_CREATE_ENTRIES =
                "CREATE TABLE IF NOT EXISTS " + TaskEntry.TABLE_NAME + " (" +
                        TaskEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        TaskEntry.COLUMN_TASK_TITLE + " TEXT, " +
                        TaskEntry.COLUMN_TASK_DESC + " TEXT, " +
                        TaskEntry.COLUMN_TASK_NOTE + " TEXT, " +
                        TaskEntry.COLUMN_TASK_TIME + " TEXT, " +
                        TaskEntry.COLUMN_TASK_DATE + " TEXT, " +
                        TaskEntry.COLUMN_TASK_ALARM + " TEXT, " +
                        TaskEntry.COLUMN_TASK_PRIORITY + " INTEGER DEFAULT 0, " +
                        TaskEntry.COLUMN_TASK_CREATED + " TEXT, " +
                        TaskEntry.COLUMN_TASK_UPDATED + " TEXT, " +
                        TaskEntry.COLUMN_TASK_FINISHED_DATE + " TEXT, " +
                        TaskEntry.COLUMN_TASK_FINISHED + " INTEGER DEFAULT 0" + ");";

        sqLiteDatabase.execSQL(SQL_CREATE_ENTRIES);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + TaskEntry.TABLE_NAME);
        onCreate(db);

    }
}
