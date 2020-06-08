package com.example.android.doit;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.android.doit.Data.TaskContract.TaskEntry;

import java.util.Calendar;

public class ReportDetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {


    private static final int TASK_LOADER = 5;
    ReportCursorAdapter mCursorAdapter;
    ListView taskListView;
    private ProgressBar mProgressBar;
    private double mProgress, mOnTimeCounter, mOverdueCounter;
    private TextView mPercentage, mOnTime, mOverdue;
    private String mSelection, mDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        setContentView(R.layout.activity_reportlist);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setTitle("Report Details");

        taskListView = (ListView) findViewById(R.id.list);

        mCursorAdapter = new ReportCursorAdapter(this, null);
        taskListView.setAdapter(mCursorAdapter);

        taskListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                Intent intent = new Intent(ReportDetailsActivity.this, TaskDetailsExtraActivity.class);

                Uri currentTaskUri = ContentUris.withAppendedId(TaskEntry.CONTENT_URI, id);

                intent.setData(currentTaskUri);

                startActivity(intent);

            }
        });

        LayoutInflater layoutinflater = getLayoutInflater();
        ViewGroup header = (ViewGroup) layoutinflater.inflate(R.layout.report_header, taskListView, false);
        header.setEnabled(false);
        header.setOnClickListener(null);
        taskListView.addHeaderView(header);

        Intent intent = getIntent();
        mProgress = intent.getDoubleExtra("progress", 100);
        mOnTimeCounter = intent.getDoubleExtra("ondate", 0);
        mOverdueCounter = intent.getDoubleExtra("overdue", 0);
        mSelection = intent.getStringExtra("selection");
        mDate = intent.getStringExtra("date");
        mProgressBar = (ProgressBar) findViewById(R.id.report_progressbar);
        mProgressBar.setProgress((int) mProgress);
        mPercentage = (TextView) findViewById(R.id.report_percentage);
        mPercentage.setText((int) mProgress + "%");

        mOnTime = (TextView) findViewById(R.id.report_ontime);
        mOverdue = (TextView) findViewById(R.id.report_overdue);

        mOnTime.setText((int) mOnTimeCounter + "");
        mOverdue.setText((int) mOverdueCounter + "");

        String[] monthName = new String[]{"Jan", "Feb",
                "Mar", "Apr", "May", "Jun", "Jul",
                "Aug", "Sep", "Oct", "Nov",
                "Dec"};

        String mEmptyMonth = intent.getStringExtra("empty");
        int mSwitch = intent.getIntExtra("switch", 0);
        Calendar mToday = Calendar.getInstance();
        View emptyView = findViewById(R.id.empty_view);
        TextView emptyDay = findViewById(R.id.empty_day);
        TextView emptyMonth = findViewById(R.id.empty_month);
        TextView emptyTitle = findViewById(R.id.empty_title_text);
        TextView emptySubTitle = findViewById(R.id.empty_subtitle_text);
        emptyTitle.setText("You Don't Have Any Completed Task");
        emptySubTitle.setText("How About You Start By Creating Some...");


        switch (mSwitch) {
            case 0:
                emptyDay.setText("" + mToday.get(Calendar.DAY_OF_MONTH));
                emptyMonth.setText("" + monthName[mToday.get(Calendar.MONTH)]);
                emptyTitle.setText("You Did't Complete Any Task Yet");
                emptySubTitle.setText("How About You Start By Adding Some...");
                break;
            case 1:
                mDate = mEmptyMonth;
                String[] sDate = mEmptyMonth.split("-");
                mToday.set(Calendar.MONTH, Integer.parseInt(sDate[1]));
                mToday.set(Calendar.DAY_OF_MONTH, Integer.parseInt(sDate[2]));
                emptyDay.setText("" + mToday.get(Calendar.DAY_OF_MONTH));
                emptyMonth.setText("" + monthName[mToday.get(Calendar.MONTH)]);
                break;
            case 2:
                String[] bDate = mEmptyMonth.split("-");
                mToday.set(Calendar.MONTH, Integer.parseInt(bDate[1]));
                mToday.set(Calendar.DAY_OF_MONTH, Integer.parseInt(bDate[2]));
                String x = "-" + mToday.get(Calendar.DAY_OF_MONTH);
                if (mToday.get(Calendar.DAY_OF_MONTH) >= 8)
                    mToday.add(Calendar.DAY_OF_MONTH, -7);
                else
                    mToday.set(Calendar.DAY_OF_MONTH, 1);
                x = mToday.get(Calendar.DAY_OF_MONTH) + x;

                emptyDay.setText(x);
                emptyDay.setTextSize(40);
                emptyMonth.setText("" + monthName[mToday.get(Calendar.MONTH)]);
                break;
            case 3:
                String[] eDate = mEmptyMonth.split("-");
                mToday.set(Calendar.MONTH, Integer.parseInt(eDate[1]));
                emptyDay.setText(1 + "-" + mToday.getActualMaximum(Calendar.DATE));
                emptyDay.setTextSize(40);
                emptyMonth.setText("" + monthName[mToday.get(Calendar.MONTH)]);
                break;

        }

        taskListView.setEmptyView(emptyView);

        getLoaderManager().initLoader(TASK_LOADER, null, this);
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
        String args[] = new String[]{String.valueOf(1)};
        if (!mDate.equals("")) {
            String args2[] = new String[]{String.valueOf(1), mDate};
            args = args2.clone();
        }
        return new CursorLoader(this,
                TaskEntry.CONTENT_URI,
                projection, mSelection, args,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }

}
