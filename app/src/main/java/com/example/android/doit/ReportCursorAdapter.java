package com.example.android.doit;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.example.android.doit.Data.TaskContract.TaskEntry;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ReportCursorAdapter extends CursorAdapter {

    int checker;


    public ReportCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.report_item, parent, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void bindView(final View view, final Context context, Cursor cursor) {

        final View v = view;

        TextView titleTextView = (TextView) view.findViewById(R.id.task_title);
        TextView timeTextView = (TextView) view.findViewById(R.id.task_time);
        TextView dateTextView = (TextView) view.findViewById(R.id.task_date);
        TextView diffTextView = (TextView) view.findViewById(R.id.time_difference);

        TextView priorityTextView = (TextView) view.findViewById(R.id.task_priority);

        int idColumnIndex = cursor.getColumnIndex(TaskEntry._ID);
        int titleColumnIndex = cursor.getColumnIndex(TaskEntry.COLUMN_TASK_TITLE);
        int timeColumnIndex = cursor.getColumnIndex(TaskEntry.COLUMN_TASK_TIME);
        int dateColumnIndex = cursor.getColumnIndex(TaskEntry.COLUMN_TASK_DATE);
        int priorityColumnIndex = cursor.getColumnIndex(TaskEntry.COLUMN_TASK_PRIORITY);
        int createdColumnIndex = cursor.getColumnIndex(TaskEntry.COLUMN_TASK_CREATED);
        int updatedColumnIndex = cursor.getColumnIndex(TaskEntry.COLUMN_TASK_UPDATED);
        int dateFinishedColumnIndex = cursor.getColumnIndex(TaskEntry.COLUMN_TASK_FINISHED_DATE);
        int finishedColumnIndex = cursor.getColumnIndex(TaskEntry.COLUMN_TASK_FINISHED);

        String taskTitle = cursor.getString(titleColumnIndex);
        String taskTime = cursor.getString(timeColumnIndex);
        String taskDate = cursor.getString(dateColumnIndex);
        String taskPriority = cursor.getString(priorityColumnIndex);
        final String createdDate = cursor.getString(createdColumnIndex);
        String updatedDate = cursor.getString(updatedColumnIndex);
        final String taskFinished = cursor.getString(dateFinishedColumnIndex);
        String finished = cursor.getString(finishedColumnIndex);

        checker = Integer.parseInt(finished);

        titleTextView.setText(taskTitle);
        timeTextView.setText(taskTime);
        dateTextView.setText(taskDate);

// DATE COLORING >>>
        Calendar mCurrentDate = Calendar.getInstance();
        Calendar taskDateFinished = Calendar.getInstance();

        String currentDate = mCurrentDate.get(Calendar.YEAR) + "-";

        if (mCurrentDate.get(Calendar.MONTH) < 10)
            currentDate += "0" + mCurrentDate.get(Calendar.MONTH) + "-";
        else
            currentDate += mCurrentDate.get(Calendar.MONTH) + "-";
        if (mCurrentDate.get(Calendar.DAY_OF_MONTH) < 10)
            currentDate += "0" + mCurrentDate.get(Calendar.DAY_OF_MONTH);
        else
            currentDate += mCurrentDate.get(Calendar.DAY_OF_MONTH);

        Calendar mTaskDate = Calendar.getInstance();

        if (taskTime != null && !taskTime.isEmpty()) {

            String[] taskDay = taskTime.split(":");
            int hours = Integer.parseInt(taskDay[0]);
            int minutes = Integer.parseInt(taskDay[1]);


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

            timeTextView.setText(time);

        }

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

            dateTextView.setText(dayOfWeek + ", " + monthYear);
        }


// <<< DATE COLORING

        if (taskDate != null && !taskDate.isEmpty()) {

            String[] sDate = taskDate.split("-");
            int mYear = Integer.parseInt(sDate[0]);
            int mMonth = Integer.parseInt(sDate[1]);
            int mDay = Integer.parseInt(sDate[2]);

            Calendar taskDuoDate = Calendar.getInstance();

            taskDuoDate.set(Calendar.YEAR, mYear);
            taskDuoDate.set(Calendar.MONTH, mMonth);
            taskDuoDate.set(Calendar.DAY_OF_MONTH, mDay);

            if (taskTime != null && !taskTime.isEmpty()) {
                String[] sTime = taskTime.split(":");
                int mHour = Integer.parseInt(sTime[0]);
                int mMinute = Integer.parseInt(sTime[1]);

                taskDuoDate.set(Calendar.HOUR_OF_DAY, mHour);
                taskDuoDate.set(Calendar.MINUTE, mMinute);
            }

            taskDateFinished = Calendar.getInstance();

            String[] sDateFinished = taskFinished.split(":");
            int nYear = Integer.parseInt(sDateFinished[0]);
            int nMonth = Integer.parseInt(sDateFinished[1]);
            nMonth--;
            int nDay = Integer.parseInt(sDateFinished[2]);
            int nHour = Integer.parseInt(sDateFinished[3]);
            int nMin = Integer.parseInt(sDateFinished[4]);


            taskDateFinished.set(Calendar.YEAR, nYear);
            taskDateFinished.set(Calendar.MONTH, nMonth);
            taskDateFinished.set(Calendar.DAY_OF_MONTH, nDay);

            if (taskTime != null && !taskTime.isEmpty()) {
                taskDateFinished.set(Calendar.HOUR_OF_DAY, nHour);
                taskDateFinished.set(Calendar.MINUTE, nMin);
            }

            Date duoDate = taskDuoDate.getTime();
            Date finishDate = taskDateFinished.getTime();

            if (duoDate.after(finishDate))
                dateTextView.setTextColor(Color.parseColor("#ff669900"));
            else
                dateTextView.setTextColor(Color.parseColor("#ffcc0000"));

        }
        diffTextView.setText("");
        if (taskDate != null && !taskDate.isEmpty()) {
            if (taskTime != null && !taskTime.isEmpty()) {
                long different;
                if (mTaskDate.getTimeInMillis() > taskDateFinished.getTimeInMillis()) {
                    different = mTaskDate.getTimeInMillis() - taskDateFinished.getTimeInMillis();

                    long secondsInMilli = 1000;
                    long minutesInMilli = secondsInMilli * 60;
                    long hoursInMilli = minutesInMilli * 60;
                    long daysInMilli = hoursInMilli * 24;

                    long elapsedDays = different / daysInMilli;
                    different = different % daysInMilli;

                    long elapsedHours = different / hoursInMilli;
                    different = different % hoursInMilli;

                    long elapsedMinutes = different / minutesInMilli;

                    diffTextView.setText(elapsedDays + " Days, " + elapsedHours + " Hours, " + elapsedMinutes + " Minutes Early");

                } else {
                    different = taskDateFinished.getTimeInMillis() - mTaskDate.getTimeInMillis();

                    long secondsInMilli = 1000;
                    long minutesInMilli = secondsInMilli * 60;
                    long hoursInMilli = minutesInMilli * 60;
                    long daysInMilli = hoursInMilli * 24;

                    long elapsedDays = different / daysInMilli;
                    different = different % daysInMilli;

                    long elapsedHours = different / hoursInMilli;
                    different = different % hoursInMilli;

                    long elapsedMinutes = different / minutesInMilli;

                    diffTextView.setText(elapsedDays + " Days, " + elapsedHours + " Hours, " + elapsedMinutes + " Minutes Late");
                }

            }

        }

    }

}
