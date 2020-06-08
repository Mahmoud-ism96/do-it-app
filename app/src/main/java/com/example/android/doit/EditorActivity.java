package com.example.android.doit;

import android.app.ActionBar;
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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;

import com.example.android.doit.Data.TaskContract.TaskEntry;
import com.example.android.doit.Data.TaskDbHelper;
import com.example.android.doit.Notification.AlertReceiver;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private Uri mCurrentTaskUri;
    private static final int EXISTING_TASK_LOADER = 0;

    private EditText mTitle;
    private EditText mDesc;
    private EditText mNote;
    private String mDate;
    private String mTime;
    private int mAlarmHour, mAlarmMin;
    private Calendar mNotificationTime = Calendar.getInstance();
    public static int mNotificationID;
    public static String mAlarmTime;
    public static String mAlarm = "";
    Button mDatePicker, mTimePicker, mAlarmPicker, mCancelAlarm, mCancelDate, mCancelTime;
    private int mYear, mMonth, mDay, mHour, mMinute;
    private int mAlarmYear, mAlarmMonth, mAlarmDay;
    String currentDateAndTime;
    private String mCreated;
    private String mUpdated;
    public static String titleString, descString, noteString;
    private Calendar alarm = Calendar.getInstance();
    private String mFinished;

    private int mPriority = TaskEntry.PRIORITY_DEFAULT;

    private Spinner mPrioritySpinner;

    private boolean mTaskHasChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mTaskHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        setContentView(R.layout.activity_editor);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);

        mAlarmYear = c.get(Calendar.YEAR);
        mAlarmMonth = c.get(Calendar.MONTH);
        mAlarmDay = c.get(Calendar.DAY_OF_MONTH);
        mAlarmHour = c.get(Calendar.HOUR_OF_DAY);
        mAlarmMin = c.get(Calendar.MINUTE);


        mTitle = (EditText) findViewById(R.id.edit_task_title);
        mDesc = (EditText) findViewById(R.id.edit_task_desc);
        mNote = (EditText) findViewById(R.id.edit_task_note);
        mDatePicker = (Button) findViewById(R.id.task_date);
        mTimePicker = (Button) findViewById(R.id.task_time);
        mCancelDate = (Button) findViewById(R.id.cancel_date);
        mCancelTime = (Button) findViewById(R.id.cancel_time);
        mPrioritySpinner = (Spinner) findViewById(R.id.spinner_priority);

        mCancelTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelTime();
            }
        });

        mCancelDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelDate();
            }
        });


//ALARM SETTER >>>

        mAlarmPicker = (Button) findViewById(R.id.set_alarm);
        mCancelAlarm = (Button) findViewById(R.id.cancel_alarm);

        mAlarmPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alarmPicker();
            }
        });

        mCancelAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelAlarm();
            }
        });

//<<< ALARM SETTER


        mTitle.setOnTouchListener(mTouchListener);
        mDesc.setOnTouchListener(mTouchListener);
        mDatePicker.setOnTouchListener(mTouchListener);
        mTimePicker.setOnTouchListener(mTouchListener);
        mAlarmPicker.setOnTouchListener(mTouchListener);
        mPrioritySpinner.setOnTouchListener(mTouchListener);

        Intent intent = getIntent();
        mCurrentTaskUri = intent.getData();

        if (mCurrentTaskUri == null) {
            setTitle(getString(R.string.editor_activity_title_new_task));
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.editor_activity_title_edit_task));
            getLoaderManager().initLoader(EXISTING_TASK_LOADER, null, this);

        }

        mDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePicker();
            }
        });


        mTimePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timePicker();
            }
        });

        setupSpinner();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mCurrentTaskUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        MenuItem menuItem = menu.findItem(R.id.action_edit);
        menuItem.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_save:
                saveTask();
                overDueDate();
                if (mCurrentTaskUri == null &&
                        TextUtils.isEmpty(titleString) && TextUtils.isEmpty(descString) && TextUtils.isEmpty(noteString) &&
                        TextUtils.isEmpty(mDate) && TextUtils.isEmpty(mTime) && TextUtils.isEmpty(mAlarmTime)) {
                    finish();
                } else if (mAlarm.equals("1"))
                    startAlarm(alarm);

                finish();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if (!mTaskHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onBackPressed() {
        if (!mTaskHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    public void saveTask() {

        if (mCurrentTaskUri == null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd:HH:mm:ss", Locale.getDefault());
            currentDateAndTime = sdf.format(new Date());

            String[] cDate = currentDateAndTime.split(":");
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
            mUpdated = null;

        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd:HH:mm:ss", Locale.getDefault());
            mUpdated = sdf.format(new Date());

            currentDateAndTime = mCreated;

            String[] cDate = currentDateAndTime.split(":");
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

        }

        titleString = mTitle.getText().toString().trim();

        descString = mDesc.getText().toString().trim();

        noteString = mNote.getText().toString().trim();


        if (mCurrentTaskUri == null &&
                TextUtils.isEmpty(titleString) && TextUtils.isEmpty(descString) && TextUtils.isEmpty(noteString) &&
                TextUtils.isEmpty(mDate) && TextUtils.isEmpty(mTime) && TextUtils.isEmpty(mAlarmTime)) {
            return;
        }

        if (titleString == null || titleString.isEmpty())
            titleString = "Untitled";

        if (mAlarm.equals("1")) {
            mAlarmTime = mAlarmYear + ":" + mAlarmMonth + ":" + mAlarmDay + ":" + mAlarmHour + ":" + mAlarmMin;
        } else {
            mAlarmTime = null;
        }

        ContentValues values = new ContentValues();
        values.put(TaskEntry.COLUMN_TASK_TITLE, titleString);
        values.put(TaskEntry.COLUMN_TASK_DESC, descString);
        values.put(TaskEntry.COLUMN_TASK_NOTE, noteString);
        values.put(TaskEntry.COLUMN_TASK_TIME, mTime);
        values.put(TaskEntry.COLUMN_TASK_DATE, mDate);
        values.put(TaskEntry.COLUMN_TASK_ALARM, mAlarmTime);
        values.put(TaskEntry.COLUMN_TASK_PRIORITY, mPriority);
        values.put(TaskEntry.COLUMN_TASK_CREATED, currentDateAndTime);
        values.put(TaskEntry.COLUMN_TASK_UPDATED, mUpdated);

        if (mFinished != null && !mFinished.isEmpty())
            if (mFinished.equals("0"))
                values.put(TaskEntry.COLUMN_TASK_FINISHED, 0);
            else
                values.put(TaskEntry.COLUMN_TASK_FINISHED, 1);

        if (mCurrentTaskUri == null) {
            Uri newUri = getContentResolver().insert(TaskEntry.CONTENT_URI, values);

            if (newUri == null) {
                Toast.makeText(this, getString(R.string.editor_insert_task_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_insert_task_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(mCurrentTaskUri, values, null, null);

            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.editor_update_task_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_update_task_successful),
                        Toast.LENGTH_SHORT).show();
            }
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
//            int updatedColumnIndex = cursor.getColumnIndex(TaskEntry.COLUMN_TASK_UPDATED);
            int finishedColumnIndex = cursor.getColumnIndex(TaskEntry.COLUMN_TASK_FINISHED);

            String title = cursor.getString(titleColumnIndex);
            String desc = cursor.getString(descColumnIndex);
            String note = cursor.getString(noteColumnIndex);
            String time = cursor.getString(timeColumnIndex);
            String date = cursor.getString(dateColumnIndex);
            String alarm = cursor.getString(alarmColumnIndex);
            int priority = cursor.getInt(priorityColumnIndex);
            mCreated = cursor.getString(createdColumnIndex);
            String finished = cursor.getString(finishedColumnIndex);
            mFinished = finished;

            if (time == null || time.isEmpty()) {
                mTimePicker.setText("SELECT TIME");
            } else {
                String[] sTime = time.split(":");
                mHour = Integer.parseInt(sTime[0]);
                mMinute = Integer.parseInt(sTime[1]);
                mTime = mHour + ":" + mMinute;
                mTimePicker.setText(timeLoop(mHour, mMinute));
                mCancelTime.setVisibility(View.VISIBLE);
            }

            if (date == null || date.isEmpty()) {
                mDatePicker.setText("SELECT DATE");
            } else {
                String[] sDate = date.split("-");
                mYear = Integer.parseInt(sDate[0]);
                mMonth = Integer.parseInt(sDate[1]);
                mDay = Integer.parseInt(sDate[2]);
                mDate = mYear + "-";

                if (mMonth < 10)
                    mDate += "0" + mMonth + "-";
                else
                    mDate += mMonth + "-";
                if (mDay < 10)
                    mDate += "0" + mDay;
                else
                    mDate += mDay;
                mDatePicker.setText(setDate(mYear, mMonth, mDay));
                mCancelDate.setVisibility(View.VISIBLE);
            }

            if (alarm == null || alarm.isEmpty()) {
                mAlarmPicker.setText("SET ALARM");
            } else {
                String[] sAlarm = alarm.split(":");
                mAlarmYear = Integer.parseInt(sAlarm[0]);
                mAlarmMonth = Integer.parseInt(sAlarm[1]);
                mAlarmDay = Integer.parseInt(sAlarm[2]);
                mAlarmHour = Integer.parseInt(sAlarm[3]);
                mAlarmMin = Integer.parseInt(sAlarm[4]);

                Calendar currentTime = Calendar.getInstance();
                Calendar alarmTime = Calendar.getInstance();
                alarmTime.set(Calendar.YEAR, mAlarmYear);
                alarmTime.set(Calendar.MONTH, mAlarmMonth);
                alarmTime.set(Calendar.DAY_OF_MONTH, mAlarmDay);
                alarmTime.set(Calendar.HOUR_OF_DAY, mAlarmHour);
                alarmTime.set(Calendar.MINUTE, mAlarmMin);

                if (alarmTime.before(currentTime)) {
                    mAlarm = "0";
                    mAlarmTime = null;
                    mAlarmPicker.setText("  Set Alarm");

                    mAlarmYear = currentTime.get(Calendar.YEAR);
                    mAlarmMonth = currentTime.get(Calendar.MONTH);
                    mAlarmDay = currentTime.get(Calendar.DAY_OF_MONTH);
                    mAlarmHour = currentTime.get(Calendar.HOUR_OF_DAY);
                    mAlarmMin = currentTime.get(Calendar.MINUTE);

                } else {
                    mAlarmTime = mAlarmYear + ":" + mAlarmMonth + ":" + mAlarmDay + ":" + mAlarmHour + ":" + mAlarmMin;
                    mAlarmPicker.setText(setDate(mAlarmYear, mAlarmMonth, mAlarmDay) + " At " + timeLoop(mAlarmHour, mAlarmMin));
                    mCancelAlarm.setVisibility(View.VISIBLE);
                }
            }
            mTitle.setText(title);
            mDesc.setText(desc);
            mNote.setText(note);

            switch (priority) {
                case TaskEntry.PRIORITY_LOW:
                    mPrioritySpinner.setSelection(1);
                    break;
                case TaskEntry.PRIORITY_MID:
                    mPrioritySpinner.setSelection(2);
                    break;
                case TaskEntry.PRIORITY_HIGH:
                    mPrioritySpinner.setSelection(3);
                    break;
                default:
                    mPrioritySpinner.setSelection(0);
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mTitle.setText("");
        mDesc.setText("");
        mNote.setText("");
        mTimePicker.setText("");
        mDatePicker.setText("");

    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }

        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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

    //DATE + TIME SETTERS >>>

    public void datePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(EditorActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {

                        mDate = year + "-";

                        if (month < 10)
                            mDate += "0" + month + "-";
                        else
                            mDate += month + "-";
                        if (day < 10)
                            mDate += "0" + day;
                        else
                            mDate += day;

                        mDatePicker.setText("  " + setDate(year, month, day) + "  ");
                        mYear = year;
                        mMonth = month;
                        mDay = day;
                        mCancelDate.setVisibility(View.VISIBLE);
                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }

    public void timePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(EditorActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minutes) {

                if (hourOfDay < 10)
                    mTime = "0" + hourOfDay + ":";
                else
                    mTime = hourOfDay + ":";
                if (minutes < 10)
                    mTime += "0" + minutes;
                else
                    mTime += minutes;

                String time = timeLoop(hourOfDay, minutes);

                mTimePicker.setText(time);

                mHour = hourOfDay;
                mMinute = minutes;
                mCancelTime.setVisibility(View.VISIBLE);
            }
        }, mHour, mMinute, false);
        timePickerDialog.show();
    }

    public String timeLoop(int hours, int minutes) {

        int hour = hours;
        int minute = minutes;
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

        String min = "";
        if (minute < 10)
            min = "0" + minute;
        else
            min = String.valueOf(minute);

        String time = new StringBuilder().append(hour).append(':')
                .append(min).append(" ").append(timeSet).toString();

        return time;
    }

    public String setDate(int year, int month, int day) {

        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE");
        Date dayF = new Date(year, month, day - 1);
        String dayOfWeek = dayFormat.format(dayF);

        SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMM d, ''yy");
        Date date = new Date(year, month, day);
        String monthYear = monthYearFormat.format(date);

        return dayOfWeek + ", " + monthYear;
    }

    public void cancelDate() {
        mCancelDate.setVisibility(View.GONE);
        mDatePicker.setText("  SET Date");
        mDate = null;
    }

    public void cancelTime() {

        mCancelTime.setVisibility(View.GONE);
        mTimePicker.setText("  SET Time");
        mTime = null;

    }

    //<<< DATE + TIME SETTERS

    //ALARM SETTER >>>


    public void alarmPicker() {
        TimePickerDialog alarmPickerDialog = new TimePickerDialog(EditorActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minutes) {

                mAlarmHour = hourOfDay;
                mAlarmMin = minutes;

                alarm.set(Calendar.HOUR_OF_DAY, mAlarmHour);
                alarm.set(Calendar.MINUTE, mAlarmMin);
                alarm.set(Calendar.SECOND, 0);

                alarmDatePicker();

            }
        }, mAlarmHour, mAlarmMin, false);
        alarmPickerDialog.show();
    }

    public void alarmDatePicker() {
        DatePickerDialog alarmDatePickerDialog = new DatePickerDialog(EditorActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {

                        mAlarmYear = year;
                        mAlarmMonth = month;
                        mAlarmDay = day;

                        alarm.set(Calendar.YEAR, mAlarmYear);
                        alarm.set(Calendar.MONTH, mAlarmMonth);
                        alarm.set(Calendar.DAY_OF_MONTH, mAlarmDay);

                        Calendar currentTime = Calendar.getInstance();
                        if (alarm.before(currentTime)) {
                            mAlarm = "0";
                            mAlarmTime = null;
                            mAlarmPicker.setText("  Invalid Alarm Time");

                            mAlarmYear = currentTime.get(Calendar.YEAR);
                            mAlarmMonth = currentTime.get(Calendar.MONTH);
                            mAlarmDay = currentTime.get(Calendar.DAY_OF_MONTH);
                            mAlarmHour = currentTime.get(Calendar.HOUR_OF_DAY);
                            mAlarmMin = currentTime.get(Calendar.MINUTE);

                        } else {
                            String time = timeLoop(mAlarmHour, mAlarmMin);

                            String date = setDate(mAlarmYear, mAlarmMonth, mAlarmDay);

                            mAlarmPicker.setText("  " + date + " At " + time + "  ");

                            mCancelAlarm.setVisibility(View.VISIBLE);
                            mAlarm = "1";
                        }
                    }
                }, mAlarmYear, mAlarmMonth, mAlarmDay);
        alarmDatePickerDialog.show();
    }

    private void startAlarm(Calendar c) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlertReceiver.class);
        intent.putExtra("requestCode", mNotificationID);
        intent.putExtra("taskTitle", titleString);
        intent.putExtra("taskNote", noteString);

        TaskDbHelper mDbHelper = new TaskDbHelper(this);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.query(TaskEntry.TABLE_NAME, null, TaskEntry.COLUMN_TASK_CREATED + "=?", new String[]{currentDateAndTime}, null, null, null);
        cursor.moveToLast();
        int taskID = cursor.getInt(0);
        intent.putExtra("taskID", taskID);
        intent.putExtra("taskAlarm", c.getTimeInMillis());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, mNotificationID, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (c.before(Calendar.getInstance())) {
            c.add(Calendar.DATE, 1);
        }
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
    }

    public void cancelAlarm() {

        if (mCurrentTaskUri != null) {
            currentDateAndTime = mCreated;
            String[] cDate = currentDateAndTime.split(":");
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
        }

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlertReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, mNotificationID, intent, 0);

        alarmManager.cancel(pendingIntent);
        mCancelAlarm.setVisibility(View.GONE);
        mAlarmPicker.setText("  SET ALARM");
        mAlarmTime = null;
        mAlarm = "0";
    }


    public void overDueDate() {

        AlarmManager resetAlarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent cancelIntent = new Intent(this, AlertReceiver.class);
        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(this, 0, cancelIntent, 0);
        resetAlarm.cancel(cancelPendingIntent);

        Calendar dailyReminder = Calendar.getInstance();
        dailyReminder.set(Calendar.HOUR_OF_DAY, 9);
        dailyReminder.set(Calendar.MINUTE, 00);
        dailyReminder.set(Calendar.SECOND, 00);
        long startUpTime = dailyReminder.getTimeInMillis();

        if (System.currentTimeMillis() > startUpTime) {
            startUpTime = startUpTime + 24 * 60 * 60 * 1000;
        }

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlertReceiver.class);
        intent.putExtra("requestCode", 0);
        intent.putExtra("test", 0);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, startUpTime, AlarmManager.INTERVAL_DAY, pendingIntent);
    }


//<< ALARM SETTER

    private void setupSpinner() {

        ArrayAdapter prioritySpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.array_priority, android.R.layout.simple_spinner_item);

        prioritySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        mPrioritySpinner.setAdapter(prioritySpinnerAdapter);

        mPrioritySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.low_priority))) {
                        mPriority = TaskEntry.PRIORITY_LOW;
                    } else if (selection.equals(getString(R.string.mid_priority))) {
                        mPriority = TaskEntry.PRIORITY_MID;
                    } else if (selection.equals(getString(R.string.high_priority))) {
                        mPriority = TaskEntry.PRIORITY_HIGH;
                    } else {
                        mPriority = TaskEntry.PRIORITY_DEFAULT;
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mPriority = TaskEntry.PRIORITY_DEFAULT;
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getStringExtra("methodName").equals("myMethod")) {
            final Uri currentTaskUri = ContentUris.withAppendedId(TaskEntry.CONTENT_URI, AlertReceiver.taskID);
            final ContentValues values = new ContentValues();
        }
    }

}
