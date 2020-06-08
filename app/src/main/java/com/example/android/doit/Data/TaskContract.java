package com.example.android.doit.Data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class TaskContract {

    private TaskContract() {
    }

    public static final String CONTENT_AUTHORITY = "com.example.android.doit";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String TASK_PATH = "task";

    public static final class TaskEntry implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, TASK_PATH);

        public static final String TABLE_NAME = "task";

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_TASK_TITLE = "title";
        public static final String COLUMN_TASK_DESC = "desc";
        public static final String COLUMN_TASK_NOTE = "note";
        public static final String COLUMN_TASK_TIME = "tasktime";
        public static final String COLUMN_TASK_DATE = "taskdate";
        public static final String COLUMN_TASK_ALARM = "alarm";
        public static final String COLUMN_TASK_PRIORITY = "taskpriority";
        public static final String COLUMN_TASK_CREATED = "cdate";
        public static final String COLUMN_TASK_UPDATED = "udate";
        public static final String COLUMN_TASK_FINISHED_DATE = "fdate";
        public static final String COLUMN_TASK_FINISHED = "finished";

        public static final int PRIORITY_DEFAULT = 0;
        public static final int PRIORITY_LOW = 1;
        public static final int PRIORITY_MID = 2;
        public static final int PRIORITY_HIGH = 3;

        public static boolean isValidPriority(int priority) {
            if (priority == PRIORITY_DEFAULT || priority == PRIORITY_LOW || priority == PRIORITY_MID || priority == PRIORITY_HIGH) {
                return true;
            }
            return false;
        }


        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + TASK_PATH;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + TASK_PATH;

    }

}
