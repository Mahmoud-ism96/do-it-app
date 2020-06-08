package com.example.android.doit.Notification;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.text.format.DateFormat;

import androidx.core.app.NotificationCompat;

import com.example.android.doit.Data.TaskContract.TaskEntry;
import com.example.android.doit.MainActivity;
import com.example.android.doit.R;
import com.example.android.doit.TaskDetailsActivity;
import com.example.android.doit.TaskDetailsExtraActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class NotificationHelper extends ContextWrapper {
    public static final String channelID = "TaskReminderID";
    public static final String channelID2 = "OverdueTaskID";
    public static final String channelID3 = "TipID";
    public static final String channelName = "Tasks Reminder";
    public static final String channelName2 = "Overdue Tasks Reminder";
    public static final String channelName3 = "Tips";

    private NotificationManager mManager;

    public NotificationHelper(Context base) {
        super(base);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel();
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createChannel() {
        NotificationChannel channel = new NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH);
        NotificationChannel channel2 = new NotificationChannel(channelID2, channelName2, NotificationManager.IMPORTANCE_HIGH);
        NotificationChannel channel3 = new NotificationChannel(channelID3, channelName3, NotificationManager.IMPORTANCE_MAX);

        getManager().createNotificationChannel(channel);
        getManager().createNotificationChannel(channel2);
        getManager().createNotificationChannel(channel3);
    }

    public NotificationManager getManager() {
        if (mManager == null) {
            mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }

        return mManager;
    }

    public NotificationCompat.Builder getGroupChannel() {

        int color = 0xff03A9F4;
        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(this, AlertReceiver.requestCode, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        return new NotificationCompat.Builder(getApplicationContext(), channelID)
                .setContentTitle("Reminder")
                .setContentText(AlertReceiver.taskTitle)
                .setStyle(new NotificationCompat.InboxStyle())
                .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_CHILDREN)
                .setGroupSummary(true)
                .setGroup("TaskGroup")
                .setSmallIcon(R.mipmap.greentick)
                .setColor(color)
                .setAutoCancel(true)
//                .addAction(R.mipmap.greentick,"Complete",)
//                .setContentIntent(contentIntent)
                ;
    }

    public NotificationCompat.Builder getChannelNotification() {

        int color = 0xff03A9F4;

        Intent notificationIntent = new Intent(this, TaskDetailsExtraActivity.class);
        Uri currentTaskUri = ContentUris.withAppendedId(TaskEntry.CONTENT_URI, AlertReceiver.taskID);
        notificationIntent.setData(currentTaskUri);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(this, AlertReceiver.requestCode, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent completeIntent = new Intent(this, AlertReceiver.class);
        completeIntent.putExtra("action", "completeTask");
        completeIntent.putExtra("taskID", AlertReceiver.taskID);
        completeIntent.putExtra("requestCode", AlertReceiver.requestCode);
        PendingIntent completePendingIntent = PendingIntent.getBroadcast(this, AlertReceiver.requestCode - 1, completeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent snoozeIntent = new Intent(this, AlertReceiver.class);
        snoozeIntent.putExtra("action", "snoozeTask");
        snoozeIntent.putExtra("taskID", AlertReceiver.taskID);
        snoozeIntent.putExtra("requestCode", AlertReceiver.requestCode);
        snoozeIntent.putExtra("taskTitle", AlertReceiver.taskTitle);
        snoozeIntent.putExtra("taskNote", AlertReceiver.taskNote);
        snoozeIntent.putExtra("taskAlarm", AlertReceiver.taskAlarm);
        PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(this, AlertReceiver.requestCode + 1, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (AlertReceiver.taskNote == null || AlertReceiver.taskNote.equals(""))
            return new NotificationCompat.Builder(getApplicationContext(), channelID)
                    .setContentTitle(AlertReceiver.taskTitle)
                    .setSmallIcon(R.mipmap.greentick)
                    .setColor(color)
                    .setGroup("TaskGroup")
                    .setSubText("Reminder")
                    .setAutoCancel(true)
                    .setContentIntent(contentIntent)
                    .addAction(R.mipmap.greentick, "Snooze", snoozePendingIntent)
                    .addAction(R.mipmap.greentick, "Complete", completePendingIntent);

        else
            return new NotificationCompat.Builder(getApplicationContext(), channelID)
                    .setContentTitle(AlertReceiver.taskTitle)
                    .setSmallIcon(R.mipmap.greentick)
                    .setColor(color)
                    .setGroup("TaskGroup")
                    .setSubText("Reminder")
                    .setAutoCancel(true)
                    .setContentIntent(contentIntent)
                    .setContentText(AlertReceiver.taskNote)
                    .addAction(R.mipmap.greentick, "Snooze", snoozePendingIntent)
                    .addAction(R.mipmap.greentick, "Complete", completePendingIntent);
    }


    public int overDueCount() {

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
        Cursor countCursor = getContentResolver().query(TaskEntry.CONTENT_URI,
                new String[]{"count(*) AS count"},
                selection,
                new String[]{String.valueOf(0), mTodayDate},
                null);

        countCursor.moveToFirst();
        int count = countCursor.getInt(0);
        return count;
    }

    public NotificationCompat.Builder getDuoChannel() {

        int color = 0xff03A9F4;
        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM dd");
        String formattedDate = sdf.format(c);

        return new NotificationCompat.Builder(getApplicationContext(), channelID2)
                .setContentTitle(formattedDate)
                .setContentText("You have " + overDueCount() + " tasks overdue")
                .setSmallIcon(R.mipmap.greentick)
                .setColor(color)
                .setAutoCancel(true)
                .setContentIntent(contentIntent);

    }

    public NotificationCompat.Builder getTipChannel() {

        int color = 0xff03A9F4;

        Intent notificationIntent = new Intent(this, MainActivity.class);
        Uri currentTaskUri = ContentUris.withAppendedId(TaskEntry.CONTENT_URI, AlertReceiver.taskID);
        notificationIntent.setData(currentTaskUri);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(this, AlertReceiver.requestCode, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        return new NotificationCompat.Builder(getApplicationContext(), channelID3)
                .setContentTitle(AlertReceiver.taskTitle)
                .setSmallIcon(R.mipmap.greentick)
                .setColor(color)
                .setContentText(AlertReceiver.taskNote)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(AlertReceiver.taskNote)
                        .setBigContentTitle(AlertReceiver.taskTitle))
                .setPriority(Notification.PRIORITY_MAX)
                .setAutoCancel(true)
                .setContentIntent(contentIntent);
    }
}