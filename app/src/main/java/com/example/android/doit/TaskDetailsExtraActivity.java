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


public class TaskDetailsExtraActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private Uri mCurrentTaskUri;
    private static final int EXISTING_TASK_LOADER = 0;

    TextView mTitle, mDesc, mDateTime, mFinished, mAlarm, mNote, mPriority;
    int mTaskId;
    int checker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        setContentView(R.layout.activity_taskdetails_extra);
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
        mDateTime = (TextView) findViewById(R.id.details_date_time);
        mAlarm = (TextView) findViewById(R.id.details_alarm);
        mPriority = (TextView) findViewById(R.id.details_priority);
//        mFinished = (TextView) findViewById(R.id.details_finished);

        getLoaderManager().initLoader(EXISTING_TASK_LOADER, null, this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem save = menu.findItem(R.id.action_save);
        if (checker == 1) {
            save.setVisible(false);
        }
        MenuItem edit = menu.findItem(R.id.action_edit);
        MenuItem delete = menu.findItem(R.id.action_delete);
        MenuItem details = menu.findItem(R.id.action_details);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_save:
                ContentValues values = new ContentValues();
                values.put(TaskEntry.COLUMN_TASK_FINISHED, 1);
                getContentResolver().update(mCurrentTaskUri, values, null, null);
                Toast.makeText(this, getString(R.string.task_completed),
                        Toast.LENGTH_SHORT).show();
                finish();
                return true;
            case R.id.action_edit:
                Intent editIntent = new Intent(TaskDetailsExtraActivity.this, EditorActivity.class);
                editIntent.setData(mCurrentTaskUri);
                startActivity(editIntent);
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case R.id.action_details:
                Intent detailsIntent = new Intent(this, TaskDetailsActivity.class);
                detailsIntent.setData(mCurrentTaskUri);
                startActivity(detailsIntent);
                return true;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(TaskDetailsExtraActivity.this);
                return true;
        }
        return super.onOptionsItemSelected(item);

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
            int idColumnIndex = cursor.getColumnIndex(TaskEntry._ID);
            int titleColumnIndex = cursor.getColumnIndex(TaskEntry.COLUMN_TASK_TITLE);
            int descColumnIndex = cursor.getColumnIndex(TaskEntry.COLUMN_TASK_DESC);
            int noteColumnIndex = cursor.getColumnIndex(TaskEntry.COLUMN_TASK_NOTE);
            int timeColumnIndex = cursor.getColumnIndex(TaskEntry.COLUMN_TASK_TIME);
            int dateColumnIndex = cursor.getColumnIndex(TaskEntry.COLUMN_TASK_DATE);
            int alarmColumnIndex = cursor.getColumnIndex(TaskEntry.COLUMN_TASK_ALARM);
            int priorityColumnIndex = cursor.getColumnIndex(TaskEntry.COLUMN_TASK_PRIORITY);
            int finishedColumnIndex = cursor.getColumnIndex(TaskEntry.COLUMN_TASK_FINISHED);

            mTaskId = cursor.getInt(idColumnIndex);
            String taskTitle = cursor.getString(titleColumnIndex);
            String taskDesc = cursor.getString(descColumnIndex);
            String taskTime = cursor.getString(timeColumnIndex);
            String taskNote = cursor.getString(noteColumnIndex);
            String taskDate = cursor.getString(dateColumnIndex);
            String taskAlarm = cursor.getString(alarmColumnIndex);
            String taskPriority = cursor.getString(priorityColumnIndex);
            String finished = cursor.getString(finishedColumnIndex);

            checker = Integer.parseInt(finished);

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

                SimpleDateFormat dayFormat = new SimpleDateFormat("EEE");
                Date dayF = new Date(year, month, day - 1);
                String dayOfWeek = dayFormat.format(dayF);

                SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMM d");
                Date date = new Date(year, month, day);
                String monthYear = monthYearFormat.format(date);

                mDateTime.setText(dayOfWeek + ", " + monthYear);

                if (taskDate.equals(currentDate)) {
                    mDateTime.setTextColor(Color.parseColor("#0288D1"));
                    mDateTime.setText("Today");
                }

                if (taskTime != null && !taskTime.isEmpty()) {
                    String[] tTime = taskTime.split(":");
                    int hour = Integer.parseInt(tTime[0]);
                    int min = Integer.parseInt(tTime[1]);
                    mTaskDate.set(Calendar.HOUR_OF_DAY, hour);
                    mTaskDate.set(Calendar.MINUTE, min);
                    mDateTime.setText(dayOfWeek + ", " + monthYear + " At " + setTime(hour, min));

                    if (finished.equals("0")) {
                        if (mTaskDate.before(mCurrentDate))
                            mDateTime.setTextColor(Color.parseColor("#D32F2F"));
                        if (mTaskDate.after(mCurrentDate))
                            mDateTime.setTextColor(Color.parseColor("#388E3C"));
                        if (taskDate.equals(currentDate)) {
                            mDateTime.setTextColor(Color.parseColor("#0288D1"));
                            mDateTime.setText("Today At " + setTime(hour, min));
                        }
                    }
                }
            } else {
                mDateTime.setText("No Date/Time Set");
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
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mTitle.setText("");
        mDesc.setText("");
        mNote.setText("");
        mDateTime.setText("");
        mAlarm.setText("");
        mPriority.setText("");
//        mFinished.setText("");
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

}
