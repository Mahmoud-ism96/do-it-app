package com.example.android.doit.Data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import com.example.android.doit.Data.TaskContract.TaskEntry;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class TaskProvider extends ContentProvider {


    public static final String LOG_TAG = TaskProvider.class.getSimpleName();
    public TaskDbHelper mDbHelper;

    public static final int TASKS = 100;
    public static final int TASK_ID = 101;
    public static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {

        sUriMatcher.addURI(TaskContract.CONTENT_AUTHORITY, TaskContract.TASK_PATH, TASKS);
        sUriMatcher.addURI(TaskContract.CONTENT_AUTHORITY, TaskContract.TASK_PATH + "/#", TASK_ID);

    }


    @Override
    public boolean onCreate() {

        mDbHelper = new TaskDbHelper(getContext());

        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case TASKS:
                cursor = database.query(TaskEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case TASK_ID:
                selection = TaskEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(TaskEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case TASKS:
                return TaskEntry.CONTENT_LIST_TYPE;
            case TASK_ID:
                return TaskEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case TASKS:
                return insertTask(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }
    private Uri insertTask(Uri uri, ContentValues values) {

        values.getAsString(TaskEntry.COLUMN_TASK_TITLE);
        values.getAsString(TaskEntry.COLUMN_TASK_DESC);
        values.getAsString(TaskEntry.COLUMN_TASK_NOTE);
        values.getAsString(TaskEntry.COLUMN_TASK_TIME);
        values.getAsString(TaskEntry.COLUMN_TASK_DATE);
        values.getAsString(TaskEntry.COLUMN_TASK_ALARM);
        values.getAsInteger(TaskEntry.COLUMN_TASK_PRIORITY);
        values.getAsString(TaskEntry.COLUMN_TASK_CREATED);
        values.getAsString(TaskEntry.COLUMN_TASK_UPDATED);
        values.getAsString(TaskEntry.COLUMN_TASK_FINISHED_DATE);
        values.getAsInteger(TaskEntry.COLUMN_TASK_FINISHED);

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        long id = database.insert(TaskEntry.TABLE_NAME, null, values);

        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case TASKS:
                getContext().getContentResolver().notifyChange(uri, null);
                rowsDeleted = database.delete(TaskEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case TASK_ID:
                selection = TaskEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(TaskEntry.TABLE_NAME, selection, selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case TASKS:
                return updateTask(uri, contentValues, selection, selectionArgs);
            case TASK_ID:
                selection = TaskEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateTask(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateTask(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        values.getAsString(TaskEntry.COLUMN_TASK_TITLE);
        values.getAsString(TaskEntry.COLUMN_TASK_DESC);
        values.getAsString(TaskEntry.COLUMN_TASK_NOTE);
        values.getAsString(TaskEntry.COLUMN_TASK_TIME);
        values.getAsString(TaskEntry.COLUMN_TASK_DATE);
        values.getAsString(TaskEntry.COLUMN_TASK_ALARM);
        values.getAsInteger(TaskEntry.COLUMN_TASK_PRIORITY);

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsUpdated = database.update(TaskEntry.TABLE_NAME, values, selection, selectionArgs);

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }
}
