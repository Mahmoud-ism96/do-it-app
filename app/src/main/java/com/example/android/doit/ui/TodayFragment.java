package com.example.android.doit.ui;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.LoaderManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.android.doit.Data.TaskContract.TaskEntry;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.ListFragment;

import com.example.android.doit.Data.TaskDbHelper;
import com.example.android.doit.EditorActivity;
import com.example.android.doit.MainActivity;
import com.example.android.doit.Notification.AlertReceiver;
import com.example.android.doit.R;
import com.example.android.doit.TaskCursorAdapter;
import com.example.android.doit.TaskDetailsActivity;
import com.example.android.doit.TaskDetailsExtraActivity;

import java.util.Calendar;

public class TodayFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int TASK_LOADER = 0;
    public TaskDbHelper mDbHelper;
    TaskCursorAdapter mCursorAdapter;
    ListView taskListView;
    long itemID;
    private Calendar mToday;
    private String mTodayDate;
    private String sqlCreatedDate;
    private Spinner mSort;
    private String mSortBy = null;
    private int mSortingID;
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String SWITCH = "switch";
    private int mYear, mMonth, mDay, mHour, mMinute;
    private SQLiteDatabase db;

    public TodayFragment() {
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_tasklist, container, false);

        mToday = Calendar.getInstance();

        mTodayDate = mToday.get(Calendar.YEAR) + "-";
        if (mToday.get(Calendar.MONTH) < 10)
            mTodayDate += "0" + mToday.get(Calendar.MONTH) + "-";
        else
            mTodayDate += mToday.get(Calendar.MONTH) + "-";
        if (mToday.get(Calendar.DAY_OF_MONTH) < 10)
            mTodayDate += "0" + mToday.get(Calendar.DAY_OF_MONTH);
        else
            mTodayDate += mToday.get(Calendar.DAY_OF_MONTH);

        taskListView = (ListView) view.findViewById(R.id.list);

        String[] monthName = new String[]{"Jan", "Feb",
                "Mar", "Apr", "May", "Jun", "Jul",
                "Aug", "Sep", "Oct", "Nov",
                "Dec"};

        View emptyView = view.findViewById(R.id.empty_view);
        TextView emptyDay = view.findViewById(R.id.empty_day);
        TextView emptyMonth = view.findViewById(R.id.empty_month);
        TextView emptyTitle = view.findViewById(R.id.empty_title_text);
        TextView emptySubTitle = view.findViewById(R.id.empty_subtitle_text);
        emptyDay.setText("" + mToday.get(Calendar.DAY_OF_MONTH));
        emptyMonth.setText("" + monthName[mToday.get(Calendar.MONTH)]);
        emptyTitle.setText("You Don't Have Any Tasks Today");
        emptySubTitle.setText("How About You Start Your Day By Adding Some...");
        taskListView.setEmptyView(emptyView);

        mCursorAdapter = new TaskCursorAdapter(getActivity(), null);
        taskListView.setAdapter(mCursorAdapter);

        taskListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                Intent intent = new Intent(getContext(), TaskDetailsExtraActivity.class);

                Uri currentTaskUri = ContentUris.withAppendedId(TaskEntry.CONTENT_URI, id);

                intent.setData(currentTaskUri);

                startActivity(intent);
            }
        });

        registerForContextMenu(taskListView);

        taskListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                itemID = id;
                return false;
            }
        });

        ((MainActivity) getActivity()).showFloatingActionButton();

        getActivity().getLoaderManager().initLoader(TASK_LOADER, null, this);

        LayoutInflater layoutinflater = getLayoutInflater();
        ViewGroup header = (ViewGroup) layoutinflater.inflate(R.layout.header, taskListView, false);
        taskListView.addHeaderView(header);

        mSort = (Spinner) view.findViewById(R.id.spinner_sort);

        setupSpinner();
        loadData();

        return view;
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        String[] projection = {
                TaskEntry._ID,
                TaskEntry.COLUMN_TASK_TITLE,
                TaskEntry.COLUMN_TASK_DESC,
                TaskEntry.COLUMN_TASK_NOTE,
                TaskEntry.COLUMN_TASK_TIME,
                TaskEntry.COLUMN_TASK_DATE,
                TaskEntry.COLUMN_TASK_ALARM,
                TaskEntry.COLUMN_TASK_PRIORITY,
                TaskEntry.COLUMN_TASK_CREATED,
                TaskEntry.COLUMN_TASK_UPDATED,
                TaskEntry.COLUMN_TASK_FINISHED_DATE,
                TaskEntry.COLUMN_TASK_FINISHED};
        String selection = TaskEntry.COLUMN_TASK_FINISHED + "=? " + "AND " + TaskEntry.COLUMN_TASK_DATE + "=?";
        return new CursorLoader(this.getContext(),
                TaskEntry.CONTENT_URI,
                projection, selection, new String[]{String.valueOf(0), mTodayDate},
                mSortBy);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.menu_item, menu);

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.context_edit:
                Intent intent = new Intent(getContext(), EditorActivity.class);
                Uri currentTaskUri = ContentUris.withAppendedId(TaskEntry.CONTENT_URI, itemID);
                intent.setData(currentTaskUri);
                startActivity(intent);
                return true;
            case R.id.context_postpone:
                postpone();
                return true;
            case R.id.context_delete:
                showDeleteConfirmationDialog();
                return true;
        }
        return false;
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                cancelAlarm();
                deleteTask();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteTask() {
        Uri currentTaskUri = ContentUris.withAppendedId(TaskEntry.CONTENT_URI, itemID);
        if (currentTaskUri != null) {
            int rowsDeleted = getContext().getContentResolver().delete(currentTaskUri, null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(this.getContext(), getString(R.string.editor_delete_task_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this.getContext(), getString(R.string.editor_delete_task_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void cancelAlarm() {

        TaskDbHelper mDbHelper = new TaskDbHelper(getContext());

        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("select * from " + TaskEntry.TABLE_NAME + " where " + TaskEntry._ID + "=?", new String[]{String.valueOf(itemID)});
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        if (cursor.moveToFirst()) {
            int idColumnIndex = cursor.getColumnIndex(TaskEntry._ID);
            int createdDateColumnIndex = cursor.getColumnIndex(TaskEntry.COLUMN_TASK_CREATED);

            sqlCreatedDate = cursor.getString(createdDateColumnIndex);
            int sqlId = cursor.getInt(idColumnIndex);

        }

        Calendar mNotificationTime = Calendar.getInstance();
        int mNotificationID;
        String[] cDate = sqlCreatedDate.split(":");
        int y = Integer.parseInt(cDate[0]);
        int mo = Integer.parseInt(cDate[1]);
        int d = Integer.parseInt(cDate[2]);
        int h = Integer.parseInt(cDate[3]);
        int mi = Integer.parseInt(cDate[4]);
        int s = Integer.parseInt(cDate[5]);
        mNotificationTime.set(Calendar.YEAR, y);
        mNotificationTime.set(Calendar.MONTH, mo);
        mNotificationTime.set(Calendar.DAY_OF_MONTH, d);
        mNotificationTime.set(Calendar.HOUR_OF_DAY, h);
        mNotificationTime.set(Calendar.MINUTE, mi);
        mNotificationTime.set(Calendar.SECOND, s);
        mNotificationTime.set(Calendar.MILLISECOND, 0);
        mNotificationID = ((int) mNotificationTime.getTimeInMillis());

        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this.getContext(), AlertReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getContext(), mNotificationID, intent, 0);
        alarmManager.cancel(pendingIntent);
    }

    private void setupSpinner() {

        ArrayAdapter sortingSpinnerAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.array_sorting, R.layout.spinner_sorting);

        sortingSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        mSort.setAdapter(sortingSpinnerAdapter);


        mSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.date_created))) {
                        mSortBy = null;
                        getActivity().getLoaderManager().restartLoader(TASK_LOADER, null, TodayFragment.this);
                        saveData();
                    } else if (selection.equals(getString(R.string.date))) {
                        mSortBy = "(CASE WHEN taskdate IS NULL THEN 0 ELSE 1 END) DESC,taskdate ASC, (CASE WHEN tasktime IS NULL THEN 0 ELSE 1 END) DESC, tasktime ASC";
                        getActivity().getLoaderManager().restartLoader(TASK_LOADER, null, TodayFragment.this);
                        saveData();
                    } else if (selection.equals(getString(R.string.priority))) {
                        mSortBy = TaskEntry.COLUMN_TASK_PRIORITY + " DESC," + "(CASE WHEN taskdate IS NULL THEN 0 ELSE 1 END) DESC,taskdate ASC, (CASE WHEN tasktime IS NULL THEN 0 ELSE 1 END) DESC, tasktime ASC";
                        getActivity().getLoaderManager().restartLoader(TASK_LOADER, null, TodayFragment.this);
                        saveData();
                    } else if (selection.equals(getString(R.string.sort_title))) {
                        mSortBy = "title ASC,(CASE WHEN taskdate IS NULL THEN 0 ELSE 1 END) DESC,taskdate ASC, (CASE WHEN tasktime IS NULL THEN 0 ELSE 1 END) DESC, tasktime ASC";
                        getActivity().getLoaderManager().restartLoader(TASK_LOADER, null, TodayFragment.this);
                        saveData();
                    } else {
                        mSortBy = null;
                    }
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mSortBy = null;
            }
        });
    }

    public void saveData() {
        SharedPreferences sharedPreferences = this.getContext().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt(SWITCH, mSort.getSelectedItemPosition());
        editor.apply();
    }

    public void loadData() {
        SharedPreferences sharedPreferences = this.getContext().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        mSortingID = sharedPreferences.getInt(SWITCH, 0);
        mSort.setSelection(mSortingID);
    }

    public void postpone() {

        TaskDbHelper mDbHelper = new TaskDbHelper(getContext());
        db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TaskEntry.TABLE_NAME + " where " + TaskEntry.COLUMN_TASK_FINISHED + " =?" + " AND " + TaskEntry._ID + " =?", new String[]{String.valueOf(0), String.valueOf(itemID)});
        if (cursor.moveToFirst()) {
            int dateColumnIndex = cursor.getColumnIndex(TaskEntry.COLUMN_TASK_DATE);
            int timeColumnIndex = cursor.getColumnIndex(TaskEntry.COLUMN_TASK_TIME);
            String sqlDate = cursor.getString(dateColumnIndex);
            String sqlTime = cursor.getString(timeColumnIndex);


            if (sqlTime != null && !sqlTime.isEmpty()) {
                String[] sTime = sqlTime.split(":");
                mHour = Integer.parseInt(sTime[0]);
                mMinute = Integer.parseInt(sTime[1]);
                timePicker();
            }

            if (sqlDate != null && !sqlDate.isEmpty()) {
                String[] sDate = sqlDate.split("-");
                mYear = Integer.parseInt(sDate[0]);
                mMonth = Integer.parseInt(sDate[1]);
                mDay = Integer.parseInt(sDate[2]);
                datePicker();
            }
            if ((sqlDate == null || sqlDate.isEmpty()) && (sqlTime == null || sqlTime.isEmpty())) {
                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);
                mHour = c.get(Calendar.HOUR_OF_DAY);
                mMinute = c.get(Calendar.MINUTE);

                timePicker();
                datePicker();
            }

        }

    }


    public void datePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {

                        String mDate = year + "-";

                        if (month < 10)
                            mDate += "0" + month + "-";
                        else
                            mDate += month + "-";
                        if (day < 10)
                            mDate += "0" + day;
                        else
                            mDate += day;
                        ContentValues values = new ContentValues();
                        values.put(TaskEntry.COLUMN_TASK_DATE, mDate);
                        db.update(TaskEntry.TABLE_NAME, values, "_id=" + itemID, null);
                        getActivity().getLoaderManager().restartLoader(TASK_LOADER, null, TodayFragment.this);
                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }

    public void timePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minutes) {

                String mTime;

                if (hourOfDay < 10)
                    mTime = "0" + hourOfDay + ":";
                else
                    mTime = hourOfDay + ":";

                if (minutes < 10)
                    mTime += "0" + minutes;
                else
                    mTime += minutes;

                ContentValues values = new ContentValues();
                values.put(TaskEntry.COLUMN_TASK_TIME, mTime);
                db.update(TaskEntry.TABLE_NAME, values, "_id=" + itemID, null);
                getActivity().getLoaderManager().restartLoader(TASK_LOADER, null, TodayFragment.this);

            }
        }, mHour, mMinute, false);
        timePickerDialog.show();
    }

}
