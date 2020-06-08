package com.example.android.doit.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.android.doit.Data.TaskContract.TaskEntry;
import com.example.android.doit.Data.TaskDbHelper;
import com.example.android.doit.MainActivity;
import com.example.android.doit.R;
import com.example.android.doit.ReportDetailsActivity;
import com.example.android.doit.TaskDetailsActivity;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ReportFragment extends Fragment {

    private double onDateCounter;
    private double overDueCounter;
    private double mProgress;
    private TextView mPercentage, mReportTitle;
    private ProgressBar mProgressBar;
    private CompactCalendarView mReportCalendar;
    private Button mLeftButton, mRightButton;
    private Button mOverall, mDaily, mWeekly, mMonthly, mReport;
    private TextView monthText;
    private String[] monthName;
    private String mDate, mReportMonth, mReportMonthEnd, mReportWeekStart, mReportWeekEnd;
    private Calendar mWeek = Calendar.getInstance();
    public TaskDbHelper mDbHelper;
    public static List<Event> events;
    Intent reportDetails;
    private String mEmptyMonth, mEmptyWeek, mEmptyDay;

    private int mSwitch = 0;

    public ReportFragment() {
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_report, container, false);

        reportDetails = new Intent(getContext(), ReportDetailsActivity.class);
        count();
        mProgressBar = (ProgressBar) view.findViewById(R.id.productivity);
        mPercentage = (TextView) view.findViewById(R.id.productivity_report);
        mReportTitle = (TextView) view.findViewById(R.id.report_title);
        monthText = (TextView) view.findViewById(R.id.report_month);
        mReportCalendar = (CompactCalendarView) view.findViewById(R.id.report_calendar);
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        int firstDay = Integer.parseInt(sharedPrefs.getString(
                getString(R.string.settings_first_day_key),
                getString(R.string.settings_first_day_default)));
        mReportCalendar.setFirstDayOfWeek(firstDay);
        mReportCalendar.shouldSelectFirstDayOfMonthOnScroll(false);
        mReportCalendar.displayOtherMonthDays(true);

        ((MainActivity) getActivity()).hideFloatingActionButton();

        mReport = (Button) view.findViewById(R.id.report_details);
        mReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reportDetails.putExtra("progress", mProgress);
                reportDetails.putExtra("ondate", onDateCounter);
                reportDetails.putExtra("overdue", overDueCounter);
                startActivity(reportDetails);
            }
        });

        Calendar c = Calendar.getInstance();

        mDate = c.get(Calendar.YEAR) + "-";
        mReportMonth = c.get(Calendar.YEAR) + "-";
        mReportMonthEnd = mReportMonth = c.get(Calendar.YEAR) + "-";
        int mMonthEnd = c.get(Calendar.MONTH) + 1;

        if (c.get(Calendar.MONTH) < 10) {
            mDate += "0" + c.get(Calendar.MONTH);
            mReportMonth += "0" + c.get(Calendar.MONTH);
            mReportMonthEnd += "0" + mMonthEnd;
        } else {
            mDate += c.get(Calendar.MONTH);
            mReportMonth += c.get(Calendar.MONTH);
            mReportMonthEnd += mMonthEnd;
        }
        if (c.get(Calendar.DAY_OF_MONTH) < 10)
            mDate += "-" + "0" + c.get(Calendar.DAY_OF_MONTH);
        else
            mDate += "-" + c.get(Calendar.DAY_OF_MONTH);

        mEmptyDay = mDate;
        mEmptyWeek = mDate;
        mEmptyMonth = mDate;

        monthName = new String[]{"January", "February",
                "March", "April", "May", "June", "July",
                "August", "September", "October", "November",
                "December"};

        String month = monthName[c.get(Calendar.MONTH)];

        monthText.setText(month + " " + c.get(Calendar.YEAR));


//>>>>> BUTTONS STYLE !!!!
        mOverall = (Button) view.findViewById(R.id.report_overview);
        mOverall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOverall.setBackgroundDrawable(getResources().getDrawable(R.drawable.report_button_active));
                mDaily.setBackgroundDrawable(getResources().getDrawable(R.drawable.report_button_inactive));
                mWeekly.setBackgroundDrawable(getResources().getDrawable(R.drawable.report_button_inactive));
                mMonthly.setBackgroundDrawable(getResources().getDrawable(R.drawable.report_button_inactive));

                mSwitch = 0;
                onDateCounter = 0;
                overDueCounter = 0;
                count();

                mReportTitle.setText("Overall Productivity:");

                mProgress = (onDateCounter / (onDateCounter + overDueCounter)) * 100;
                mProgressBar.setProgress(100 - ((int) mProgress));
                mPercentage.setText((int) mProgress + "%");
            }
        });
        mDaily = (Button) view.findViewById(R.id.report_daily);
        mDaily.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOverall.setBackgroundDrawable(getResources().getDrawable(R.drawable.report_button_inactive));
                mDaily.setBackgroundDrawable(getResources().getDrawable(R.drawable.report_button_active));
                mWeekly.setBackgroundDrawable(getResources().getDrawable(R.drawable.report_button_inactive));
                mMonthly.setBackgroundDrawable(getResources().getDrawable(R.drawable.report_button_inactive));

                mSwitch = 1;
                onDateCounter = 0;
                overDueCounter = 0;
                count();

                mReportTitle.setText("Daily Productivity:");

                mProgress = (onDateCounter / (onDateCounter + overDueCounter)) * 100;
                mProgressBar.setProgress(100 - ((int) mProgress));
                mPercentage.setText((int) mProgress + "%");
            }
        });
        mWeekly = (Button) view.findViewById(R.id.report_weekly);
        mWeekly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOverall.setBackgroundDrawable(getResources().getDrawable(R.drawable.report_button_inactive));
                mDaily.setBackgroundDrawable(getResources().getDrawable(R.drawable.report_button_inactive));
                mWeekly.setBackgroundDrawable(getResources().getDrawable(R.drawable.report_button_active));
                mMonthly.setBackgroundDrawable(getResources().getDrawable(R.drawable.report_button_inactive));

                mSwitch = 2;
                onDateCounter = 0;
                overDueCounter = 0;
                count();

                mReportTitle.setText("Weekly Productivity:");

                mProgress = (onDateCounter / (onDateCounter + overDueCounter)) * 100;
                mProgressBar.setProgress(100 - ((int) mProgress));
                mPercentage.setText((int) mProgress + "%");

            }
        });
        mMonthly = (Button) view.findViewById(R.id.report_monthly);
        mMonthly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOverall.setBackgroundDrawable(getResources().getDrawable(R.drawable.report_button_inactive));
                mDaily.setBackgroundDrawable(getResources().getDrawable(R.drawable.report_button_inactive));
                mWeekly.setBackgroundDrawable(getResources().getDrawable(R.drawable.report_button_inactive));
                mMonthly.setBackgroundDrawable(getResources().getDrawable(R.drawable.report_button_active));

                mSwitch = 3;
                onDateCounter = 0;
                overDueCounter = 0;
                count();

                mReportTitle.setText("Monthly Productivity:");

                mProgress = (onDateCounter / (onDateCounter + overDueCounter)) * 100;
                mProgressBar.setProgress(100 - ((int) mProgress));
                mPercentage.setText((int) mProgress + "%");
            }
        });
//<<<<<<<<<<<<<<<< BUTTONS STYLE !!!


        mLeftButton = (Button) view.findViewById(R.id.report_left_button);

        mLeftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mReportCalendar.scrollLeft();
            }
        });

        mRightButton = (Button) view.findViewById(R.id.report_right_button);

        mRightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mReportCalendar.scrollRight();
            }
        });

        mReportCalendar.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {

                Calendar c = Calendar.getInstance();
                c.setTime(dateClicked);

                mDate = c.get(Calendar.YEAR) + "-";

                if (c.get(Calendar.MONTH) < 10)
                    mDate += "0" + c.get(Calendar.MONTH);
                else
                    mDate += c.get(Calendar.MONTH);
                if (c.get(Calendar.DAY_OF_MONTH) < 10)
                    mDate += "-" + "0" + c.get(Calendar.DAY_OF_MONTH);
                else
                    mDate += "-" + c.get(Calendar.DAY_OF_MONTH);

                mEmptyDay = mDate;
                mEmptyWeek = mDate;
                mEmptyMonth = mDate;

                switch (mSwitch) {
                    case 1:
                        onDateCounter = 0;
                        overDueCounter = 0;
                        count();

                        mProgress = (onDateCounter / (onDateCounter + overDueCounter)) * 100;
                        mProgressBar.setProgress(100 - ((int) mProgress));
                        mPercentage.setText((int) mProgress + "%");
                        break;
                    case 2:
                        mWeek.setTime(dateClicked);
                        findDay();
                        onDateCounter = 0;
                        overDueCounter = 0;
                        count();

                        mProgress = (onDateCounter / (onDateCounter + overDueCounter)) * 100;
                        mProgressBar.setProgress(100 - ((int) mProgress));
                        mPercentage.setText((int) mProgress + "%");
                        break;
                }


            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {

                Calendar c = Calendar.getInstance();
                c.setTime(firstDayOfNewMonth);

                String month = monthName[firstDayOfNewMonth.getMonth()];

                monthText.setText(month + " " + c.get(Calendar.YEAR));

                mDate = c.get(Calendar.YEAR) + "-";
                mReportMonth = c.get(Calendar.YEAR) + "-";
                mReportMonthEnd = mReportMonth = c.get(Calendar.YEAR) + "-";
                int mMonthEnd = c.get(Calendar.MONTH) + 1;

                if (c.get(Calendar.MONTH) < 10) {
                    mDate += "0" + c.get(Calendar.MONTH);
                    mReportMonth += "0" + c.get(Calendar.MONTH);
                    mReportMonthEnd += "0" + mMonthEnd;

                } else {
                    mDate += c.get(Calendar.MONTH);
                    mReportMonth += c.get(Calendar.MONTH);
                    mReportMonthEnd += mMonthEnd;
                }

                mEmptyMonth = mDate;

                if (c.get(Calendar.DAY_OF_MONTH) < 10)
                    mEmptyMonth += "-" + "0" + c.get(Calendar.DAY_OF_MONTH);
                else
                    mEmptyMonth += "-" + c.get(Calendar.DAY_OF_MONTH);

                if (mSwitch == 3) {
                    onDateCounter = 0;
                    overDueCounter = 0;
                    count();

                    mProgress = (onDateCounter / (onDateCounter + overDueCounter)) * 100;
                    mProgressBar.setProgress(100 - ((int) mProgress));
                    mPercentage.setText((int) mProgress + "%");
                }
            }
        });

        mProgress = (onDateCounter / (onDateCounter + overDueCounter)) * 100;
        mProgressBar.setProgress(100 - ((int) mProgress));
        mPercentage.setText((int) mProgress + "%");


        try {
            retrieveData();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return view;
    }


    public void count() {


        TaskDbHelper mDbHelper = new TaskDbHelper(getContext());

        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TaskEntry.TABLE_NAME + " where " + TaskEntry.COLUMN_TASK_FINISHED + " =?", new String[]{String.valueOf(1)});


        switch (mSwitch) {
            case 0:
                cursor.close();
                cursor = db.rawQuery("select * from " + TaskEntry.TABLE_NAME + " where " + TaskEntry.COLUMN_TASK_FINISHED + " =?", new String[]{String.valueOf(1)});
                reportDetails.putExtra("selection", TaskEntry.COLUMN_TASK_FINISHED + " =? AND " + TaskEntry.COLUMN_TASK_DATE + " IS NOT NULL AND " + TaskEntry.COLUMN_TASK_DATE + "!= \"\"");
                reportDetails.putExtra("date", "");
                reportDetails.putExtra("empty", mDate);
                reportDetails.putExtra("switch", 0);
                break;
            case 1:
                cursor = db.rawQuery("select * from " + TaskEntry.TABLE_NAME + " where " + TaskEntry.COLUMN_TASK_FINISHED + " =? " + " AND " + TaskEntry.COLUMN_TASK_DATE + " =?", new String[]{String.valueOf(1), mDate});
                reportDetails.putExtra("selection", TaskEntry.COLUMN_TASK_FINISHED + " =? " + " AND " + TaskEntry.COLUMN_TASK_DATE + " =? ");
                reportDetails.putExtra("date", mDate);
                reportDetails.putExtra("empty", mEmptyDay);
                reportDetails.putExtra("switch", 1);
                break;
            case 2:
                cursor = db.rawQuery("select * from " + TaskEntry.TABLE_NAME + " where " + TaskEntry.COLUMN_TASK_FINISHED + " =? " + " AND " + TaskEntry.COLUMN_TASK_DATE + " BETWEEN '" + mReportWeekStart + "' AND '" + mReportWeekEnd + "'", new String[]{String.valueOf(1)});
                reportDetails.putExtra("selection", TaskEntry.COLUMN_TASK_FINISHED + " =? " + " AND " + TaskEntry.COLUMN_TASK_DATE + " BETWEEN '" + mReportWeekStart + "' AND '" + mReportWeekEnd + "' AND " + TaskEntry.COLUMN_TASK_DATE + " IS NOT NULL AND " + TaskEntry.COLUMN_TASK_DATE + "!= \"\"");
                reportDetails.putExtra("date", "");
                reportDetails.putExtra("empty", mEmptyWeek);
                reportDetails.putExtra("switch", 2);
                break;
            case 3:
                cursor = db.rawQuery("select * from " + TaskEntry.TABLE_NAME + " where " + TaskEntry.COLUMN_TASK_FINISHED + " =? " + " AND " + TaskEntry.COLUMN_TASK_DATE + " BETWEEN '" + mReportMonth + "' AND '" + mReportMonthEnd + "'", new String[]{String.valueOf(1)});
                reportDetails.putExtra("selection", TaskEntry.COLUMN_TASK_FINISHED + " =? " + " AND " + TaskEntry.COLUMN_TASK_DATE + " BETWEEN '" + mReportMonth + "' AND '" + mReportMonthEnd + "' AND " + TaskEntry.COLUMN_TASK_DATE + " IS NOT NULL AND " + TaskEntry.COLUMN_TASK_DATE + "!= \"\"");
                reportDetails.putExtra("date", "");
                reportDetails.putExtra("empty", mEmptyMonth);
                reportDetails.putExtra("switch", 3);
                break;
        }

        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {

                int dateColumnIndex = cursor.getColumnIndex(TaskEntry.COLUMN_TASK_DATE);
                int timeColumnIndex = cursor.getColumnIndex(TaskEntry.COLUMN_TASK_TIME);
                int dateFinishedColumnIndex = cursor.getColumnIndex(TaskEntry.COLUMN_TASK_FINISHED_DATE);

                String sqlDate = cursor.getString(dateColumnIndex);
                String sqlTime = cursor.getString(timeColumnIndex);
                String sqlDateFinished = cursor.getString(dateFinishedColumnIndex);

                if (sqlDate == null || sqlDate.isEmpty()) {
                    cursor.moveToNext();
                } else {
                    String[] sDate = sqlDate.split("-");
                    int mYear = Integer.parseInt(sDate[0]);
                    int mMonth = Integer.parseInt(sDate[1]);
                    int mDay = Integer.parseInt(sDate[2]);

                    Calendar taskDuoDate = Calendar.getInstance();

                    taskDuoDate.set(Calendar.YEAR, mYear);
                    taskDuoDate.set(Calendar.MONTH, mMonth);
                    taskDuoDate.set(Calendar.DAY_OF_MONTH, mDay);

                    if (sqlTime != null && !sqlTime.isEmpty()) {
                        String[] sTime = sqlTime.split(":");
                        int mHour = Integer.parseInt(sTime[0]);
                        int mMinute = Integer.parseInt(sTime[1]);

                        taskDuoDate.set(Calendar.HOUR_OF_DAY, mHour);
                        taskDuoDate.set(Calendar.MINUTE, mMinute);
                    }

                    Calendar taskDateFinished = Calendar.getInstance();

                    String[] sDateFinished = sqlDateFinished.split(":");
                    int nYear = Integer.parseInt(sDateFinished[0]);
                    int nMonth = Integer.parseInt(sDateFinished[1]);
                    nMonth--;
                    int nDay = Integer.parseInt(sDateFinished[2]);
                    int nHour = Integer.parseInt(sDateFinished[3]);
                    int nMin = Integer.parseInt(sDateFinished[4]);


                    taskDateFinished.set(Calendar.YEAR, nYear);
                    taskDateFinished.set(Calendar.MONTH, nMonth);
                    taskDateFinished.set(Calendar.DAY_OF_MONTH, nDay);

                    if (sqlTime != null && !sqlTime.isEmpty()) {
                        taskDateFinished.set(Calendar.HOUR_OF_DAY, nHour);
                        taskDateFinished.set(Calendar.MINUTE, nMin);
                    }

                    Date duoDate = taskDuoDate.getTime();
                    Date finishDate = taskDateFinished.getTime();

                    if (duoDate.after(finishDate))
                        onDateCounter += 1;
                    else
                        overDueCounter += 1;

                    cursor.moveToNext();
                }
            }
        }
    }

    public void retrieveData() throws ParseException {

        events = new ArrayList<Event>();

        mDbHelper = new TaskDbHelper(getContext());

        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("select * from " + TaskEntry.TABLE_NAME + " where " + TaskEntry.COLUMN_TASK_FINISHED + " =?", new String[]{String.valueOf(1)});

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

            mReportCalendar.removeAllEvents();
            mReportCalendar.addEvents(events);
        }
    }

    public void findDay() {

        mReportWeekEnd = mWeek.get(Calendar.YEAR) + "-";
        mReportWeekStart = mWeek.get(Calendar.YEAR) + "-";

        if (mWeek.get(Calendar.MONTH) < 10) {
            mReportWeekEnd += "0" + mWeek.get(Calendar.MONTH);
            mReportWeekStart += "0" + mWeek.get(Calendar.MONTH);
        } else {
            mReportWeekEnd += mWeek.get(Calendar.MONTH);
            mReportWeekStart += mWeek.get(Calendar.MONTH);
        }

        if (mWeek.get(Calendar.DAY_OF_MONTH) < 10) {
            mReportWeekEnd += "-" + "0" + mWeek.get(Calendar.DAY_OF_MONTH);
        } else {
            mReportWeekEnd += "-" + mWeek.get(Calendar.DAY_OF_MONTH);
        }

        if (mWeek.get(Calendar.DAY_OF_MONTH) >= 8)
            mWeek.add(Calendar.DAY_OF_MONTH, -7);
        else
            mWeek.set(Calendar.DAY_OF_MONTH, 1);

        if (mWeek.get(Calendar.DAY_OF_MONTH) < 10) {
            mReportWeekStart += "-" + "0" + mWeek.get(Calendar.DAY_OF_MONTH);
        } else {
            mReportWeekStart += "-" + mWeek.get(Calendar.DAY_OF_MONTH);
        }

    }
}