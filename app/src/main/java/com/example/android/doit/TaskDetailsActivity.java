package com.example.android.doit;


import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.doit.Data.TaskContract.TaskEntry;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class TaskDetailsActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private Uri mCurrentTaskUri;
    private static final int EXISTING_TASK_LOADER = 0;

    TextView mTitle, mDesc, mTime, mDate, mCreated, mUpdated, mFinished, mAlarm, mNote, mPriority, mDateFinished, mDateFinishedLabel;
    int checker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        setContentView(R.layout.activity_taskdetails);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setTitle(getString(R.string.task_details_activity_title));

        Intent intent = getIntent();
        mCurrentTaskUri = intent.getData();
        String path = mCurrentTaskUri.getPath();
        String idStr = path.substring(path.lastIndexOf('/') + 1);
        int id = Integer.parseInt(idStr);

        mTitle = (TextView) findViewById(R.id.details_title);
        mDesc = (TextView) findViewById(R.id.details_desc);
        mNote = (TextView) findViewById(R.id.details_note);
        mTime = (TextView) findViewById(R.id.details_time);
        mDate = (TextView) findViewById(R.id.details_date);
        mAlarm = (TextView) findViewById(R.id.details_alarm);
        mPriority = (TextView) findViewById(R.id.details_priority);
        mCreated = (TextView) findViewById(R.id.details_created);
        mUpdated = (TextView) findViewById(R.id.details_updated);
        mDateFinished = (TextView) findViewById(R.id.details_dfinished);
        mFinished = (TextView) findViewById(R.id.details_finished);

        mDateFinishedLabel = (TextView) findViewById(R.id.details_dfinished_label);

        getLoaderManager().initLoader(EXISTING_TASK_LOADER, null, this);

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

        return new CursorLoader(this,   // Parent activity context
                mCurrentTaskUri,         // Query the content URI for the current pet
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        if (cursor.moveToFirst()) {
            int titleColumnIndex = cursor.getColumnIndex(TaskEntry.COLUMN_TASK_TITLE);
            int descColumnIndex = cursor.getColumnIndex(TaskEntry.COLUMN_TASK_DESC);
            int noteColumnIndex = cursor.getColumnIndex(TaskEntry.COLUMN_TASK_NOTE);
            int timeColumnIndex = cursor.getColumnIndex(TaskEntry.COLUMN_TASK_TIME);
            int dateColumnIndex = cursor.getColumnIndex(TaskEntry.COLUMN_TASK_DATE);
            int alarmColumnIndex = cursor.getColumnIndex(TaskEntry.COLUMN_TASK_ALARM);
            int priorityColumnIndex = cursor.getColumnIndex(TaskEntry.COLUMN_TASK_PRIORITY);
            int createdColumnIndex = cursor.getColumnIndex(TaskEntry.COLUMN_TASK_CREATED);
            int updatedColumnIndex = cursor.getColumnIndex(TaskEntry.COLUMN_TASK_UPDATED);
            int finishedDateColumnIndex = cursor.getColumnIndex(TaskEntry.COLUMN_TASK_FINISHED_DATE);
            int finishedColumnIndex = cursor.getColumnIndex(TaskEntry.COLUMN_TASK_FINISHED);

            String taskTitle = cursor.getString(titleColumnIndex);
            String taskDesc = cursor.getString(descColumnIndex);
            String taskTime = cursor.getString(timeColumnIndex);
            String taskNote = cursor.getString(noteColumnIndex);
            String taskDate = cursor.getString(dateColumnIndex);
            String taskAlarm = cursor.getString(alarmColumnIndex);
            String taskPriority = cursor.getString(priorityColumnIndex);
            String taskCreated = cursor.getString(createdColumnIndex);
            String taskUpdated = cursor.getString(updatedColumnIndex);
            String dateFinished = cursor.getString(finishedDateColumnIndex);
            String finished = cursor.getString(finishedColumnIndex);

            checker = Integer.parseInt(finished);

//            if (time == null || time.isEmpty()) {
//                mTimePicker.setText("SELECT TIME");
//            } else {
//                String[] sTime = time.split(":");
//                mHour = Integer.parseInt(sTime[0]);
//                mMinute = Integer.parseInt(sTime[1]);
//                mTime = mHour + ":" + mMinute;
//                mTimePicker.setText(timeLoop(mHour, mMinute));
//            }
//
//            if (date == null || date.isEmpty()){
//                mDatePicker.setText("SELECT DATE");
//            } else {
//                String[] sDate = date.split("-");
//                mYear = Integer.parseInt(sDate[0]);
//                mMonth = Integer.parseInt(sDate[1]);
//                mDay =Integer.parseInt(sDate[2]);
//                mDate = mYear + "-" + mMonth + "-" + mDay;
//                mDatePicker.setText(setDate(mYear,mMonth,mDay));
//            }


            if (taskTime != null && !taskTime.isEmpty()) {
                String[] taskDay = taskTime.split(":");
                int hour = Integer.parseInt(taskDay[0]);
                int min = Integer.parseInt(taskDay[1]);
                mTime.setText(setTime(hour, min));
            }

            Calendar mCurrentDate = Calendar.getInstance();
            Calendar mTaskDate = Calendar.getInstance();

            String currentDate = mCurrentDate.get(Calendar.YEAR) + "-";

            if (mCurrentDate.get(Calendar.MONTH) < 10)
                currentDate += "0" + mCurrentDate.get(Calendar.MONTH) + "-";
            else
                currentDate += mCurrentDate.get(Calendar.MONTH) + "-";
            if (mCurrentDate.get(Calendar.DAY_OF_MONTH) < 10)
                currentDate += "0" + mCurrentDate.get(Calendar.DAY_OF_MONTH);
            else
                currentDate += mCurrentDate.get(Calendar.DAY_OF_MONTH);

            if (taskDate != null && !taskDate.isEmpty()) {

                String[] taskDay = taskDate.split("-");
                int year = Integer.parseInt(taskDay[0]);
                int month = Integer.parseInt(taskDay[1]);
                int day = Integer.parseInt(taskDay[2]);
                mTaskDate.set(year, month, day);

                if (taskTime != null && !taskTime.isEmpty()) {
                    String[] tTime = taskTime.split(":");
                    int hour = Integer.parseInt(tTime[0]);
                    int min = Integer.parseInt(tTime[1]);
                    mTaskDate.set(Calendar.HOUR_OF_DAY, hour);
                    mTaskDate.set(Calendar.MINUTE, min);
                }

                SimpleDateFormat dayFormat = new SimpleDateFormat("EEE");
                Date dayF = new Date(year, month, day - 1);
                String dayOfWeek = dayFormat.format(dayF);

                SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMM d");
                Date date = new Date(year, month, day);
                String monthYear = monthYearFormat.format(date);

                mDate.setText(dayOfWeek + ", " + monthYear);

                if (finished.equals("0")) {
                    if (mTaskDate.before(mCurrentDate))
                        mDate.setTextColor(Color.parseColor("#D32F2F"));
                    if (mTaskDate.after(mCurrentDate))
                        mDate.setTextColor(Color.parseColor("#388E3C"));
                    if (taskDate.equals(currentDate)) {
                        mDate.setTextColor(Color.parseColor("#0288D1"));
                        mDate.setText("Today");
                    }
                }
            }

            if (taskAlarm == null || taskAlarm.isEmpty()) {
                mAlarm.setText("No Alarm Set");
            } else {
                String[] tAlarm = taskAlarm.split(":");
                int year = Integer.parseInt(tAlarm[0]);
                int month = Integer.parseInt(tAlarm[1]);
                int day = Integer.parseInt(tAlarm[2]);
                int hour = Integer.parseInt(tAlarm[3]);
                int min = Integer.parseInt(tAlarm[4]);
                mTaskDate.set(Calendar.YEAR, year);
                mTaskDate.set(Calendar.MONTH, month);
                mTaskDate.set(Calendar.DAY_OF_MONTH, day);
                mTaskDate.set(Calendar.HOUR_OF_DAY, hour);
                mTaskDate.set(Calendar.MINUTE, min);

                Calendar todayTime = Calendar.getInstance();
                String mTodayTime = todayTime.get(Calendar.YEAR) + ":" + todayTime.get(Calendar.MONTH) + ":" + todayTime.get(Calendar.DAY_OF_MONTH);
                String mAlarmTime = year + ":" + month + ":" + day;

                SimpleDateFormat dayFormat = new SimpleDateFormat("EEE");
                Date dayF = new Date(year, month, day - 1);
                String dayOfWeek = dayFormat.format(dayF);

                SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMM d");
                Date date = new Date(year, month, day);
                String monthYear = monthYearFormat.format(date);

                mAlarm.setText(dayOfWeek + ", " + monthYear + " at " + setTime(hour, min));

                if (mAlarmTime.equals(mTodayTime)) {
                    mAlarm.setText("Today At " + setTime(hour, min));
                }
            }
            if (finished.equals("0")) {
                mFinished.setText("Incomplete");
                mDateFinished.setText(null);
                mDateFinished.setVisibility(View.GONE);
                mDateFinishedLabel.setVisibility(View.GONE);
            } else {
                mFinished.setText("Complete");
                mDateFinished.setText(setDate(dateFinished));
                mDateFinished.setVisibility(View.VISIBLE);
                mDateFinishedLabel.setVisibility(View.VISIBLE);
            }

            mTitle.setText(taskTitle);
            mDesc.setText(taskDesc);
            mNote.setText(taskNote);


            switch (taskPriority) {
                case "1":
                    mPriority.setText("Low Priority");
                    mPriority.setTextColor(Color.parseColor("#388E3C"));
                    break;
                case "2":
                    mPriority.setText("Medium Priority");
                    mPriority.setTextColor(Color.parseColor("#dbca24"));
                    break;
                case "3":
                    mPriority.setText("High Priority");
                    mPriority.setTextColor(Color.parseColor("#D32F2F"));
                    break;
                default:
                    mPriority.setText("No Priority");
                    break;
            }

            mCreated.setText(setDate(taskCreated));
            if (taskUpdated != null && !taskUpdated.isEmpty())
                mUpdated.setText(setDate(taskUpdated));
            else
                mUpdated.setText("No Updates");
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mTitle.setText("");
        mDesc.setText("");
        mNote.setText("");
        mTime.setText("");
        mDate.setText("");
        mAlarm.setText("");
        mPriority.setText("");
        mCreated.setText("");
        mUpdated.setText("");
        mDateFinished.setText("");
        mFinished.setText("");
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
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
        if (mCurrentTaskUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentTaskUri, null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_task_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_task_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    private String setTime(int hour, int min) {
        String timeSet = "";
        if (hour > 12) {
            hour -= 12;
            timeSet = "PM";
        } else if (hour == 0) {
            hour += 12;
            timeSet = "AM";
        } else if (hour == 12) {
            timeSet = "PM";
        } else {
            timeSet = "AM";
        }

        String minn = "";
        if (min < 10)
            minn = "0" + min;
        else
            minn = String.valueOf(min);

        return new StringBuilder().append(hour).append(':')
                .append(minn).append(" ").append(timeSet).toString();
    }

    private String setDate(String formatedDate) {

        String[] tAlarm = formatedDate.split(":");
        int year = Integer.parseInt(tAlarm[0]);
        int month = Integer.parseInt(tAlarm[1]);
        int day = Integer.parseInt(tAlarm[2]);
        int hour = Integer.parseInt(tAlarm[3]);
        int min = Integer.parseInt(tAlarm[4]);

        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE");
        Date dayF = new Date(year, month, day - 1);
        String dayOfWeek = dayFormat.format(dayF);

        SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMM d");
        Date date = new Date(year, month, day);
        String monthYear = monthYearFormat.format(date);

        String mAlarmTime = year + ":" + month + ":" + day;
        Calendar todayTime = Calendar.getInstance();
        String mTodayTime = todayTime.get(Calendar.YEAR) + ":" + todayTime.get(Calendar.MONTH) + ":" + todayTime.get(Calendar.DAY_OF_MONTH);

        if (mAlarmTime.equals(mTodayTime))
            return "Today At " + setTime(hour, min);
        else
            return (dayOfWeek + ", " + monthYear + " At " + setTime(hour, min));
    }
}
