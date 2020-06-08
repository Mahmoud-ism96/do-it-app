package com.example.android.doit.ui;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.doit.Data.TaskContract.TaskEntry;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.ListFragment;

import com.example.android.doit.EditorActivity;
import com.example.android.doit.MainActivity;
import com.example.android.doit.R;
import com.example.android.doit.TaskCursorAdapter;
import com.example.android.doit.TaskDetailsActivity;
import com.example.android.doit.TaskDetailsExtraActivity;

import java.util.Calendar;

public class CompletedTaskFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int TASK_LOADER = 2;
    TaskCursorAdapter mCursorAdapter;
    ListView taskListView;
    long itemID;
    private Spinner mSort;
    private String mSortBy = null;
    private int mSortingID;
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String SWITCH = "switch";

    public CompletedTaskFragment() {
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_tasklist, container, false);

        taskListView = (ListView) view.findViewById(R.id.list);

        String[] monthName = new String[]{"Jan", "Feb",
                "Mar", "Apr", "May", "Jun", "Jul",
                "Aug", "Sep", "Oct", "Nov",
                "Dec"};

        Calendar mToday = Calendar.getInstance();
        View emptyView = view.findViewById(R.id.empty_view);
        TextView emptyDay = view.findViewById(R.id.empty_day);
        TextView emptyMonth = view.findViewById(R.id.empty_month);
        TextView emptyTitle = view.findViewById(R.id.empty_title_text);
        TextView emptySubTitle = view.findViewById(R.id.empty_subtitle_text);
        emptyDay.setText("" + mToday.get(Calendar.DAY_OF_MONTH));
        emptyMonth.setText("" + monthName[mToday.get(Calendar.MONTH)]);
        emptyTitle.setText("You Didn't Complete Any Task Yet");
        emptySubTitle.setText("How About You Start By Adding Some...");
        taskListView.setEmptyView(emptyView);

        mCursorAdapter = new TaskCursorAdapter(getActivity(), null);
        taskListView.setAdapter(mCursorAdapter);

        taskListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                Intent intent = new Intent(getContext(), TaskDetailsExtraActivity.class);

                Uri currentTaskUri = ContentUris.withAppendedId(TaskEntry.CONTENT_URI, id);

                intent.setData(currentTaskUri);

                startActivity(intent);
            }
        });

        registerForContextMenu(taskListView);

        taskListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                itemID = id;
                return false;
            }
        });

        ((MainActivity) getActivity()).hideFloatingActionButton();

        getActivity().getLoaderManager().initLoader(TASK_LOADER, null, this);

        LayoutInflater layoutinflater = getLayoutInflater();
        ViewGroup header = (ViewGroup) layoutinflater.inflate(R.layout.header, taskListView, false);
        taskListView.addHeaderView(header);

        mSort = (Spinner) view.findViewById(R.id.spinner_sort);

        setupSpinner();
        loadData();

        return view;
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
        String selection = TaskEntry.COLUMN_TASK_FINISHED + "=?";

        return new CursorLoader(this.getContext(),
                TaskEntry.CONTENT_URI,
                projection, selection, new String[]{String.valueOf(1)},
                mSortBy);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }




    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.menu_item, menu);
        MenuItem edit = menu.findItem(R.id.context_edit);
        edit.setVisible(false);
        MenuItem postpone = menu.findItem(R.id.context_postpone);
        postpone.setVisible(false);

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.context_delete:
                showDeleteConfirmationDialog();
                return true;
        }
        return false;
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
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
        Uri currentTaskUri = ContentUris.withAppendedId(TaskEntry.CONTENT_URI, itemID);
        if (currentTaskUri != null) {
            int rowsDeleted = getContext().getContentResolver().delete(currentTaskUri, null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(this.getContext(), getString(R.string.editor_delete_task_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this.getContext(), getString(R.string.editor_delete_task_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void setupSpinner() {

        ArrayAdapter sortingSpinnerAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.array_sorting, R.layout.spinner_sorting);

        sortingSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        mSort.setAdapter(sortingSpinnerAdapter);


        mSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.date_created))) {
                        mSortBy = null;
                        getActivity().getLoaderManager().restartLoader(TASK_LOADER, null, CompletedTaskFragment.this);
                        saveData();
                    } else if (selection.equals(getString(R.string.date))) {
                        mSortBy = "(CASE WHEN taskdate IS NULL THEN 0 ELSE 1 END) DESC,taskdate ASC, (CASE WHEN tasktime IS NULL THEN 0 ELSE 1 END) DESC, tasktime ASC";
                        getActivity().getLoaderManager().restartLoader(TASK_LOADER, null, CompletedTaskFragment.this);
                        saveData();
                    } else if (selection.equals(getString(R.string.priority))) {
                        mSortBy = TaskEntry.COLUMN_TASK_PRIORITY + " DESC," + "(CASE WHEN taskdate IS NULL THEN 0 ELSE 1 END) DESC,taskdate ASC, (CASE WHEN tasktime IS NULL THEN 0 ELSE 1 END) DESC, tasktime ASC";
                        getActivity().getLoaderManager().restartLoader(TASK_LOADER, null, CompletedTaskFragment.this);
                        saveData();
                    } else if (selection.equals(getString(R.string.sort_title))){
                        mSortBy = "title ASC,(CASE WHEN taskdate IS NULL THEN 0 ELSE 1 END) DESC,taskdate ASC, (CASE WHEN tasktime IS NULL THEN 0 ELSE 1 END) DESC, tasktime ASC";
                        getActivity().getLoaderManager().restartLoader(TASK_LOADER, null, CompletedTaskFragment.this);
                        saveData();
                    } else{
                        mSortBy = null;
                    }
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mSortBy = null;
            }
        });
    }

    public void saveData() {
        SharedPreferences sharedPreferences = this.getContext().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt(SWITCH, mSort.getSelectedItemPosition());
        editor.apply();
    }

    public void loadData() {
        SharedPreferences sharedPreferences = this.getContext().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        mSortingID = sharedPreferences.getInt(SWITCH, 0);
        mSort.setSelection(mSortingID);
    }

}
