package com.example.android.doit;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.widget.CompoundButtonCompat;

import com.example.android.doit.Data.TaskContract.TaskEntry;
import com.example.android.doit.Notification.AlertReceiver;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

public class TaskCursorAdapter extends CursorAdapter {

    int checker;
    public static final String SHARED_PREFS = "sharedPref";
    public static final String TIP = "tip";


    public TaskCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void bindView(final View view, final Context context, Cursor cursor) {

        final View v = view;

        TextView titleTextView = (TextView) view.findViewById(R.id.task_title);
        TextView noteTextView = (TextView) view.findViewById(R.id.task_note);
        TextView timeTextView = (TextView) view.findViewById(R.id.task_time);
        TextView dateTextView = (TextView) view.findViewById(R.id.task_date);

        TextView priorityTextView = (TextView) view.findViewById(R.id.task_priority);

        final CheckBox checkBox = (CheckBox) view.findViewById(R.id.finish_task);

        int idColumnIndex = cursor.getColumnIndex(TaskEntry._ID);
        int titleColumnIndex = cursor.getColumnIndex(TaskEntry.COLUMN_TASK_TITLE);
        int descColumnIndex = cursor.getColumnIndex(TaskEntry.COLUMN_TASK_DESC);
        int noteColumnIndex = cursor.getColumnIndex(TaskEntry.COLUMN_TASK_NOTE);
        int timeColumnIndex = cursor.getColumnIndex(TaskEntry.COLUMN_TASK_TIME);
        int dateColumnIndex = cursor.getColumnIndex(TaskEntry.COLUMN_TASK_DATE);
        int alarmColumnIndex = cursor.getColumnIndex(TaskEntry.COLUMN_TASK_ALARM);
        int priorityColumnIndex = cursor.getColumnIndex(TaskEntry.COLUMN_TASK_PRIORITY);
        int createdColumnIndex = cursor.getColumnIndex(TaskEntry.COLUMN_TASK_CREATED);
        int updatedColumnIndex = cursor.getColumnIndex(TaskEntry.COLUMN_TASK_UPDATED);
        int dateFinishedColumnIndex = cursor.getColumnIndex(TaskEntry.COLUMN_TASK_FINISHED_DATE);
        int finishedColumnIndex = cursor.getColumnIndex(TaskEntry.COLUMN_TASK_FINISHED);

        String taskTitle = cursor.getString(titleColumnIndex);
        String taskDesc = cursor.getString(descColumnIndex);
        String taskNote = cursor.getString(noteColumnIndex);
        String taskTime = cursor.getString(timeColumnIndex);
        String taskDate = cursor.getString(dateColumnIndex);
        final String taskAlarm = cursor.getString(alarmColumnIndex);
        String taskPriority = cursor.getString(priorityColumnIndex);
        final String createdDate = cursor.getString(createdColumnIndex);
        String updatedDate = cursor.getString(updatedColumnIndex);
        final String taskFinished = cursor.getString(dateFinishedColumnIndex);
        String finished = cursor.getString(finishedColumnIndex);

        checker = Integer.parseInt(finished);


        if (taskDate != null) {
            dateTextView.setVisibility(View.VISIBLE);
            timeTextView.setVisibility(View.GONE);
        } else {
            dateTextView.setVisibility(View.GONE);
            timeTextView.setVisibility(View.VISIBLE);
        }

        titleTextView.setText(taskTitle);
        noteTextView.setText(taskNote);
        timeTextView.setText(taskTime);
        dateTextView.setText(taskDate);

// DATE COLORING >>>
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

            if (finished.equals("0")) {
                if (mTaskDate.before(mCurrentDate))
                    dateTextView.setTextColor(Color.parseColor("#D32F2F"));
                if (mTaskDate.after(mCurrentDate))
                    dateTextView.setTextColor(Color.parseColor("#388E3C"));
                if (taskDate.equals(currentDate)) {
                    dateTextView.setTextColor(Color.parseColor("#0288D1"));
                    dateTextView.setText("Today");
                }
            }
        }

        int titleColor = Integer.parseInt(taskPriority);
        if (checkBox.isChecked())
            CompoundButtonCompat.setButtonTintList(checkBox, ColorStateList.valueOf(Color.parseColor("#23b1ff")));
        else
            CompoundButtonCompat.setButtonTintList(checkBox, ColorStateList.valueOf(Color.parseColor("#949494")));
        if (titleColor > 0) {
            switch (titleColor) {
                case 1:
                    CompoundButtonCompat.setButtonTintList(checkBox, ColorStateList.valueOf(Color.parseColor("#388E3C")));
                    break;
                case 2:
                    CompoundButtonCompat.setButtonTintList(checkBox, ColorStateList.valueOf(Color.parseColor("#dbca24")));
                    break;
                case 3:
                    CompoundButtonCompat.setButtonTintList(checkBox, ColorStateList.valueOf(Color.parseColor("#D32F2F")));
                    break;
            }
        }
// <<< DATE COLORING


        if (checker == 1)
            checkBox.setChecked(true);

        v.setAlpha(1);

        checkBox.setTag(cursor.getLong(0));
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                long id = ((Long) view.getTag());
                final Uri currentTaskUri = ContentUris.withAppendedId(TaskEntry.CONTENT_URI, id);
                final ContentValues values = new ContentValues();

                if (checker == 0) {
                    checkBox.setChecked(false);
                    Animation aniFade = AnimationUtils.loadAnimation(context, R.anim.fade_out);
                    v.startAnimation(aniFade);

                    aniFade.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            checkBox.setEnabled(false);
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {

                            SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            int mTipTimer = sharedPreferences.getInt(TIP, 0);
                            mTipTimer += 1;
                            if (mTipTimer >= 4) {
                                mTipTimer = 0;

                                String[] arrayTips = {"“People who have fully prepared always save time. Albert Einstein was right to teach that if he is given six hours to chop" +
                                        " down a tree, he would spend the first four sharpening the axes. When you are done with your action plans, work will be easier!”"
                                        , "“Our greatest currency is our time and we cannot save it. Spend it wisely and never waste another's or your own.”"
                                        , "“When in court, the primary role of lawyers is not to prove or disprove innocence; unbeknown to almost all lawyers and their clients, it is to save the court time.”"
                                        , "“Time flies fast, but it's good you are the pilot. Just control your time well away from hijackers. Save time profitably; Spend time productively!”"
                                        , "“Many millions of people have lost their lives while trying to save a few minutes.” "
                                        , "“Most of us love timesavers because they give us more time to waste.”"
                                        , "“always fear fools not because of they are wise just to save your time.”"
                                        , "“The reason there is ample time left is because days can succumb into hours, hours into minutes and minutes into seconds. Accomplish any new task in an hour" +
                                        " and you have a whole new life in a year.”"
                                        , "“He who every morning plans the transactions of that day and follows that plan carries a thread that will guide him through the labyrinth of the busiest life.”"
                                        , "“Lack of direction, not lack of time, is the problem. We all have twenty four-hour days.”"
                                        , "“The most efficient way to live reasonably is every morning to make a plan of one’s day and every night to examine the results obtained.”"
                                        , "“The common man is not concerned about the passage of time; the man of talent is driven by it.”"
                                        , "“The bad news is time flies. The good news is you’re the pilot”"
                                        , "“You must vie with time’s swiftness in the speed of using it, and, as from a torrent that rushes by and will not always flow, you must drink quickly.”"
                                        , "“We all know our money isn’t infinite, yet we end up treating our time and nergy and attention as if they are.”"
                                        , "“You get to decide where your time goes. You can either spend it moving forward, or you can spend it putting out fires. You decide. And if " +
                                        "you don’t decide, others will decide for you.”"
                                        , "“Time = life; therefore, waste your time and waste of your life, or master your time and master your life.”"
                                        , "“Tomorrow is often the busiest day of the week.”"
                                        , "“Procrastination is the art of keeping up with yesterday and avoiding today.”"
                                        , "“You may delay, but time will not.”"
                                        , "“A plan is what, a schedule is when. It takes both a plan and a schedule to get things done.”"
                                        , "“A man who dares to waste one hour of life has not discovered the value of life.”"
                                        , "“You can’t depend on your eyes when your imagination is out of focus.”"
                                        , "“You can have it all. Just not all at once.”"
                                        , "“The essence of self-discipline is to do the important thing rather than the urgent thing.”"
                                        , "“You can’t put your value on time, because time is priceless”"
                                        , "“Absorb what is useful, reject what is useless, add what is specifically your own.”"
                                        , "“It’s surprising how much free time and productivity you gain when you lose the busyness in your mind.”"
                                        , "“It’s not that I’m so smart, it’s just that I stay with problems longer.”"
                                        , "“Realize that now, in this moment of time, you are creating. You are creating your next moment. That is what’s real.”"};

                                String[] array = {"Abraham Lincoln"
                                        , "Kyle Barger"
                                        , "Mokokoma Mokhonoana"
                                        , "Israelmore Ayivor"
                                        , "Mokokoma Mokhonoana"
                                        , "Mokokoma Mokhonoana"
                                        , "Jordan Hoechlin"
                                        , "Suyasha Subedi"
                                        , "Victor Hugo"
                                        , "Zig Ziglar"
                                        , "Alexis Carrel"
                                        , "Arthur Schopenhauer"
                                        , "Michael Altshuler"
                                        , "Seneca"
                                        , "Shane Parrish"
                                        , "Tony Morgan"
                                        , "Alan Lakein"
                                        , "unknown"
                                        , "Wayne Dyer"
                                        , "Benjamin Franklin"
                                        , "Peter Turla"
                                        , "Charles Darwin"
                                        , "Mark Twain"
                                        , "Oprah Winfrey"
                                        , "Barry Werner"
                                        , "Hazem Khaled"
                                        , "Bruce Lee"
                                        , "Brittany Burgunder"
                                        , "Albert Einstein"
                                        , "Sara Paddison"};

                                int randomTip = ThreadLocalRandom.current().nextInt(0, 30);
                                int selectedNumb = randomTip;

                                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                                Intent intent = new Intent(context, AlertReceiver.class);
                                intent.putExtra("requestCode", 10);
                                intent.putExtra("taskTitle", "Tip");
                                intent.putExtra("taskNote", arrayTips[selectedNumb] + "—" + array[selectedNumb]);
                                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 10, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                                Calendar c = Calendar.getInstance();
                                alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis() + 5000, pendingIntent);

                            }
                            editor.putInt(TIP, mTipTimer);
                            editor.apply();

                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd:HH:mm:ss", Locale.getDefault());
                            String currentDateAndTime = sdf.format(new Date());

                            v.setAlpha(0);
                            values.put(TaskEntry.COLUMN_TASK_FINISHED_DATE, currentDateAndTime);
                            values.put(TaskEntry.COLUMN_TASK_FINISHED, 1);
                            context.getContentResolver().update(currentTaskUri, values, null, null);
                            Toast.makeText(context, context.getString(R.string.task_completed),
                                    Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }
                    });

                    if (!TextUtils.isEmpty(taskAlarm)) {
                        Calendar mNotificationTime = Calendar.getInstance();
                        int mNotificationID;
                        String[] cDate = createdDate.split(":");
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

                        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                        Intent intent = new Intent(context, AlertReceiver.class);
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, mNotificationID, intent, 0);
                        alarmManager.cancel(pendingIntent);
                    }

                } else {

                    Animation aniFade = AnimationUtils.loadAnimation(context, R.anim.fade_in);
                    v.startAnimation(aniFade);
                    checkBox.setChecked(false);

                    aniFade.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            checkBox.setEnabled(false);
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            v.setAlpha(0);
                            checkBox.setChecked(false);
                            values.put(TaskEntry.COLUMN_TASK_FINISHED, 0);
                            if (taskFinished != null && !taskFinished.isEmpty())
                                values.put(TaskEntry.COLUMN_TASK_FINISHED_DATE, "");
                            context.getContentResolver().update(currentTaskUri, values, null, null);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }
                    });
                }
            }
        });

    }

}
