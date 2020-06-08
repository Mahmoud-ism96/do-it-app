package com.example.android.doit.ui;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.android.doit.Data.TaskContract.TaskEntry;

import com.example.android.doit.EditorActivity;
import com.example.android.doit.MainActivity;
import com.example.android.doit.Notification.AlertReceiver;
import com.example.android.doit.R;
import com.example.android.doit.SettingsActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SendFragment extends Fragment {

    private EditText mTitle, mDesc, mNote;
    private Button mDatePicker, mTimePicker, mCancelDate, mCancelTime;
    private int mPriority = TaskEntry.PRIORITY_DEFAULT;
    private String mDate, mTime, mDueDate;
    private int mYear, mMonth, mDay, mHour, mMinute;
    public static String titleString, descString, noteString;
    String currentDateAndTime;
    private Calendar alarm = Calendar.getInstance();
    private Spinner mPrioritySpinner;

    public SendFragment() {
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_send, container, false);
        ((MainActivity) getActivity()).hideFloatingActionButton();

        mTitle = (EditText) view.findViewById(R.id.send_task_title);
        mDesc = (EditText) view.findViewById(R.id.send_task_desc);
        mNote = (EditText) view.findViewById(R.id.send_task_note);
        mDatePicker = (Button) view.findViewById(R.id.send_task_date);
        mTimePicker = (Button) view.findViewById(R.id.send_task_time);
        mCancelDate = (Button) view.findViewById(R.id.send_cancel_date);
        mCancelTime = (Button) view.findViewById(R.id.send_cancel_time);
        mPrioritySpinner = (Spinner) view.findViewById(R.id.spinner_priority);

        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);

        mTimePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timePicker();
            }
        });
        mCancelTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelTime();
            }
        });
        mDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePicker();
            }
        });
        mCancelDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelDate();
            }
        });


        setupSpinner();
        return view;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem send = menu.findItem(R.id.action_send);
        send.setVisible(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_settings:
                Intent settingsIntent = new Intent(getContext(), SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            case R.id.action_send:
                String mData = getData();
                if (mData != null) {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, mData);
                    sendIntent.setType("text/plain");
                    sendIntent.setPackage("com.whatsapp");
                    startActivity(sendIntent);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupSpinner() {
        ArrayAdapter prioritySpinnerAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.array_priority, android.R.layout.simple_spinner_item);

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

    public void datePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
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
        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
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

    public String getData() {

        if (TextUtils.isEmpty(titleString) && TextUtils.isEmpty(descString) && TextUtils.isEmpty(noteString) &&
                TextUtils.isEmpty(mDate) && TextUtils.isEmpty(mTime)) {
            return null;
        }

        titleString = mTitle.getText().toString().trim();
        noteString = mNote.getText().toString().trim();
        descString = mDesc.getText().toString().trim();
        mDueDate = null;

        if (titleString == null || titleString.isEmpty())
            titleString = "Untitled";
        String data = "Title: " + titleString;
        if (noteString != null && !noteString.isEmpty())
            data += "\nNote: " + noteString;

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

        if (mDate != null && !mDate.isEmpty()) {

            String[] taskDay = mDate.split("-");
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

            mDueDate = (dayOfWeek + ", " + monthYear);

            if (mDate.equals(currentDate)) {
                mDueDate = "Today";
            }

            if (mTime != null && !mTime.isEmpty()) {
                mDueDate = dayOfWeek + ", " + monthYear + " At " + setTime(mHour, mMinute);
                if (mDate.equals(currentDate)) {
                    mDueDate = "Today At " + setTime(mHour, mMinute);
                }
            }
        } else if (mTime != null && !mTime.isEmpty()) {
            mDueDate = setTime(mHour, mMinute);
        }

        if (mDueDate != null && !mDueDate.isEmpty())
            data += "\nDueDate: " + mDueDate;
        if (mPriority > 0) {
            switch (mPriority) {

                case 1:
                    data += "\nPriority: " + "Not Important";
                    break;
                case 2:
                    data += "\nPriority: " + "Important";
                    break;
                case 3:
                    data += "\nPriority: " + "Very Important";
                    break;
            }
        }
        if (descString != null && !descString.isEmpty())
            data += "\nDescription: " + descString;
        return data;
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