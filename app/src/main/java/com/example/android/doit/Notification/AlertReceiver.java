package com.example.android.doit.Notification;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.android.doit.Data.TaskContract.TaskEntry;
import com.example.android.doit.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AlertReceiver extends BroadcastReceiver {
    public static int requestCode;
    public static String taskTitle;
    public static String taskNote;
    public static int taskID;
    public static long taskAlarm;

    @Override
    public void onReceive(Context context, Intent intent) {
//        final int id = new Random().nextInt(99999999);
        requestCode = intent.getIntExtra("requestCode", 1);
        taskTitle = intent.getStringExtra("taskTitle");
        taskNote = intent.getStringExtra("taskNote");
        taskID = intent.getIntExtra("taskID", 0);
        String action = intent.getStringExtra("action");
        taskAlarm = intent.getLongExtra("taskAlarm", 0);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        int snoozeTime = Integer.parseInt(sharedPrefs.getString(
                context.getString(R.string.settings_snooze_time_key),
                context.getString(R.string.settings_first_day_default)));

        com.example.android.doit.Notification.NotificationHelper notificationHelper = new com.example.android.doit.Notification.NotificationHelper(context);

        NotificationCompat.Builder nb = notificationHelper.getChannelNotification();
        NotificationCompat.Builder nb2 = notificationHelper.getDuoChannel();
        NotificationCompat.Builder nb3 = notificationHelper.getGroupChannel();
        NotificationCompat.Builder nb4 = notificationHelper.getTipChannel();


        Calendar mToday = Calendar.getInstance();
        String mTodayDate = mToday.get(Calendar.YEAR) + "-";

        if (mToday.get(Calendar.MONTH) < 10)
            mTodayDate += "0" + mToday.get(Calendar.MONTH) + "-";
        else
            mTodayDate += mToday.get(Calendar.MONTH) + "-";
        if (mToday.get(Calendar.DAY_OF_MONTH) < 10)
            mTodayDate += "0" + mToday.get(Calendar.DAY_OF_MONTH);
        else
            mTodayDate += mToday.get(Calendar.DAY_OF_MONTH);


        String selection = TaskEntry.COLUMN_TASK_FINISHED + "=? " + "AND " + TaskEntry.COLUMN_TASK_DATE + "<?";
        Cursor countCursor = context.getContentResolver().query(TaskEntry.CONTENT_URI,
                new String[]{"count(*) AS count"},
                selection,
                new String[]{String.valueOf(0), mTodayDate},
                null);

        countCursor.moveToFirst();
        int count = countCursor.getInt(0);

        if (requestCode == 10) {
            notificationHelper.getManager().notify(10, nb4.build());

        } else if (action == null || action.isEmpty()) {
            if (requestCode == 0 && count > 0)
                notificationHelper.getManager().notify(0, nb2.build());

            else if (requestCode != 0) {
//                notificationHelper.getManager().notify(2, nb3.build());
                notificationHelper.getManager().notify(requestCode, nb.build());
            }
        }
        if (action != null && !action.isEmpty()) {
            if (action.equals("completeTask")) {

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd:HH:mm:ss", Locale.getDefault());
                String currentDateAndTime = sdf.format(new Date());

                final Uri currentTaskUri = ContentUris.withAppendedId(TaskEntry.CONTENT_URI, taskID);
                final ContentValues values = new ContentValues();
                values.put(TaskEntry.COLUMN_TASK_FINISHED_DATE, currentDateAndTime);
                values.put(TaskEntry.COLUMN_TASK_FINISHED, 1);
                context.getContentResolver().update(currentTaskUri, values, null, null);

                notificationHelper.getManager().cancel(requestCode);

                Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
                context.sendBroadcast(it);

            } else if (action.equals("snoozeTask")) {

                notificationHelper.getManager().cancel(requestCode);

                Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
                context.sendBroadcast(it);

                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                Intent snooze = new Intent(context, AlertReceiver.class);
                snooze.putExtra("taskID", taskID);
                snooze.putExtra("requestCode", requestCode);
                snooze.putExtra("taskTitle", taskTitle);
                snooze.putExtra("taskNote", taskNote);
                if (snoozeTime <= 1)
                    snoozeTime = 1;
                long x = Calendar.getInstance().getTimeInMillis();
                taskAlarm = x + (snoozeTime * 60000);
//                snooze.putExtra("taskAlarm", taskAlarm);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, snooze, PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, taskAlarm, pendingIntent);

            }
        }
    }

}