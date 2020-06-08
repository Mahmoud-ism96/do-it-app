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
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.android.doit.Data.TaskContract.TaskEntry;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.android.doit.Data.TaskDbHelper;
import com.example.android.doit.EditorActivity;
import com.example.android.doit.MainActivity;
import com.example.android.doit.Notification.AlertReceiver;
import com.example.android.doit.R;
import com.example.android.doit.TaskCursorAdapter;
import com.example.android.doit.TaskDetailsActivity;
import com.example.android.doit.TaskDetailsExtraActivity;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CalendarFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int TASK_LOADER = 5;
    TaskCursorAdapter mCursorAdapter;
    ListView taskListView;
    long itemID;
    private Calendar mCalendar;
    private TextView monthText, mCalendarDay;
    private String mDate;
    private String[] monthName;
    private CompactCalendarView mCalendarView;
    private Button mLeftButton, mRightButton;
    public static List<Event> events;
    private String sqlCreatedDate;
    private String mSortBy = null;
    private int mSortingID;
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String SWITCH = "switch";
    private int mYear, mMonth, mDay, mHour, mMinute;
    private SQLiteDatabase db;
    private Calendar mToday;

    private TextView emptyDay, emptyMonth, emptyTitle, emptySubTitle;

    public CalendarFragment() {
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_calendar, container, false);


        monthText = (TextView) view.findViewById(R.id.month);
        mCalendarView = (CompactCalendarView) view.findViewById(R.id.compactcalendar_view);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        int firstDay = Integer.parseInt(sharedPrefs.getString(
                getString(R.string.settings_first_day_key),
                getString(R.string.settings_first_day_default)));

        mCalendarView.setFirstDayOfWeek(firstDay);

        Calendar c = Calendar.getInstance();

        monthName = new String[]{"January", "February",
                "March", "April", "May", "June", "July",
                "August", "September", "October", "November",
                "December"};

        String month = monthName[c.get(Calendar.MONTH)];

        monthText.setText(month + " " + c.get(Calendar.YEAR));

        mCalendarView.shouldSelectFirstDayOfMonthOnScroll(false);


        mCalendar = Calendar.getInstance();
        mDate = mCalendar.get(Calendar.YEAR) + "-";

        if (mCalendar.get(Calendar.MONTH) < 10)
            mDate += "0" + mCalendar.get(Calendar.MONTH);
        else
            mDate += mCalendar.get(Calendar.MONTH);
        if (mCalendar.get(Calendar.DAY_OF_MONTH) < 10)
            mDate += "-" + "0" + mCalendar.get(Calendar.DAY_OF_MONTH);
        else
            mDate += "-" + mCalendar.get(Calendar.DAY_OF_MONTH);

        mCalendarDay = (TextView) view.findViewById(R.id.calendar_day);

        Date d = Calendar.getInstance().getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM dd");
        String formattedDate = sdf.format(d);
        mCalendarDay.setText(formattedDate);

        taskListView = (ListView) view.findViewById(R.id.calendar_list);

        String[] emptyMonthName = new String[]{"Jan", "Feb",
                "Mar", "Apr", "May", "Jun", "Jul",
                "Aug", "Sep", "Oct", "Nov",
                "Dec"};

        mToday = Calendar.getInstance();
        View emptyView = view.findViewById(R.id.empty_view);
        emptyDay = view.findViewById(R.id.empty_day);
        emptyMonth = view.findViewById(R.id.empty_month);
        emptyTitle = view.findViewById(R.id.empty_title_text);
        emptySubTitle = view.findViewById(R.id.empty_subtitle_text);
        emptyDay.setText("" + mToday.get(Calendar.DAY_OF_MONTH));
        emptyMonth.setText("" + emptyMonthName[mToday.get(Calendar.MONTH)]);
        emptyTitle.setText("You Don't Have Tasks Today");
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

        String[] a = {String.valueOf(1), mDate};
        CursorLoader loader = new CursorLoader(getContext());
        loader.setSelectionArgs(a);

        registerForContextMenu(taskListView);

        taskListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                itemID = id;
                return false;
            }
        });

        mLeftButton = (Button) view.findViewById(R.id.left_button);

        mLeftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mCalendarView.scrollLeft();
            }
        });

        mRightButton = (Button) view.findViewById(R.id.right_button);


        mRightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mCalendarView.scrollRight();
            }
        });

        mCalendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                Calendar c = Calendar.getInstance();
                c.setTime(dateClicked);

                mToday.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH));
                mToday.set(Calendar.MONTH, c.get(Calendar.MONTH));


                Date d = c.getTime();
                SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM dd");
                String formattedDate = sdf.format(d);
                mCalendarDay.setText(formattedDate);
                mDate = c.get(Calendar.YEAR) + "-";

                if (c.get(Calendar.MONTH) < 10)
                    mDate += "0" + c.get(Calendar.MONTH);
                else
                    mDate += c.get(Calendar.MONTH);
                if (c.get(Calendar.DAY_OF_MONTH) < 10)
                    mDate += "-" + "0" + c.get(Calendar.DAY_OF_MONTH);
                else
                    mDate += "-" + c.get(Calendar.DAY_OF_MONTH);

                List<Event> events = mCalendarView.getEvents(dateClicked);

                try {
                    retrieveData();
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                getActivity().getLoaderManager().restartLoader(TASK_LOADER, null, CalendarFragment.this);

            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {

                Calendar c = Calendar.getInstance();
                c.setTime(firstDayOfNewMonth);

                String month = monthName[firstDayOfNewMonth.getMonth()];

                monthText.setText(month + " " + c.get(Calendar.YEAR));

            }
        });


        ((MainActivity) getActivity()).hideFloatingActionButton();

        loadData();

        switch (mSortingID) {
            case 0:
                mSortBy = null;
                getActivity().getLoaderManager().restartLoader(TASK_LOADER, null, CalendarFragment.this);
                break;
            case 1:
                mSortBy = "(CASE WHEN taskdate IS NULL THEN 0 ELSE 1 END) DESC,taskdate ASC, (CASE WHEN tasktime IS NULL THEN 0 ELSE 1 END) DESC, tasktime ASC";
                getActivity().getLoaderManager().restartLoader(TASK_LOADER, null, CalendarFragment.this);
                break;
            case 2:
                mSortBy = TaskEntry.COLUMN_TASK_PRIORITY + " DESC," + "(CASE WHEN taskdate IS NULL THEN 0 ELSE 1 END) DESC,taskdate ASC, (CASE WHEN tasktime IS NULL THEN 0 ELSE 1 END) DESC, tasktime ASC";
                getActivity().getLoaderManager().restartLoader(TASK_LOADER, null, CalendarFragment.this);
                break;
            case 3:
                mSortBy = "title ASC,(CASE WHEN taskdate IS NULL THEN 0 ELSE 1 END) DESC,taskdate ASC, (CASE WHEN tasktime IS NULL THEN 0 ELSE 1 END) DESC, tasktime ASC";
                getActivity().getLoaderManager().restartLoader(TASK_LOADER, null, CalendarFragment.this);
                break;
        }

        try {
            retrieveData();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        getActivity().getLoaderManager().initLoader(TASK_LOADER, null, this);

        return view;
    }

    public void retrieveData() throws ParseException {

        events = new ArrayList<Event>();

        TaskDbHelper mDbHelper = new TaskDbHelper(getContext());

        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("select * from " + TaskEntry.TABLE_NAME + " where " + TaskEntry.COLUMN_TASK_FINISHED + " =?", new String[]{String.valueOf(0)});

        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                int idColumnIndex = cursor.getColumnIndex(TaskEntry._ID);
                int dateColumnIndex = cursor.getColumnIndex(TaskEntry.COLUMN_TASK_DATE);
                int priorityColumnIndex = cursor.getColumnIndex(TaskEntry.COLUMN_TASK_PRIORITY);

                String sqlDate = cursor.getString(dateColumnIndex);
                int sqlId = cursor.getInt(idColumnIndex);
                int sqlPriority = cursor.getInt(priorityColumnIndex);

                if (sqlDate == null || sqlDate.isEmpty()) {
                    cursor.moveToNext();
                } else {
                    String[] sDate = sqlDate.split("-");
                    int mYear = Integer.parseInt(sDate[0]);
                    int mMonth = Integer.parseInt(sDate[1]);
                    int mDay = Integer.parseInt(sDate[2]);

                    Calendar taskDate = Calendar.getInstance();

                    taskDate.set(Calendar.YEAR, mYear);
                    taskDate.set(Calendar.MONTH, mMonth);
                    taskDate.set(Calendar.DAY_OF_MONTH, mDay);
                    Event event;

                    int color1 = Color.parseColor("#0288D1");
                    int color2 = Color.parseColor("#0bd40f");
                    int color3 = Color.parseColor("#ead612");
                    int color4 = Color.parseColor("#D32F2F");

                    switch (sqlPriority) {
                        case 0:
                            event = new Event(color1, taskDate.getTimeInMillis(), sqlId);
                            events.add(event);
                            break;
                        case 1:
                            event = new Event(color2, taskDate.getTimeInMillis(), sqlId);
                            events.add(event);
                            break;
                        case 2:
                            event = new Event(color3, taskDate.getTimeInMillis(), sqlId);
                            events.add(event);
                            break;
                        case 3:
                            event = new Event(color4, taskDate.getTimeInMillis(), sqlId);
                            events.add(event);
                            break;
                    }

                    cursor.moveToNext();
                }
            }

            mCalendarView.removeAllEvents();
            mCalendarView.addEvents(events);
        }
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
        return new CursorLoader(getContext(),
                TaskEntry.CONTENT_URI,
                projection, selection, new String[]{String.valueOf(0), mDate},
                mSortBy);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        emptyDay.setText("" + mToday.get(Calendar.DAY_OF_MONTH));
        emptyMonth.setText("" + monthName[mToday.get(Calendar.MONTH)]);
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

    public void loadData() {
        SharedPreferences sharedPreferences = this.getContext().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        mSortingID = sharedPreferences.getInt(SWITCH, 0);
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
                        getActivity().getLoaderManager().restartLoader(TASK_LOADER, null, CalendarFragment.this);
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
                getActivity().getLoaderManager().restartLoader(TASK_LOADER, null, CalendarFragment.this);

            }
        }, mHour, mMinute, false);
        timePickerDialog.show();
    }
}
