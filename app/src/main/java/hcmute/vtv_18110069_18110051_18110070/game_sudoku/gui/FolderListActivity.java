package hcmute.vtv_18110069_18110051_18110070.game_sudoku.gui;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import hcmute.vtv_18110069_18110051_18110070.game_sudoku.R;
import hcmute.vtv_18110069_18110051_18110070.game_sudoku.db.FolderColumns;
import hcmute.vtv_18110069_18110051_18110070.game_sudoku.db.SudokuDatabase;
import hcmute.vtv_18110069_18110051_18110070.game_sudoku.game.FolderInfo;
import hcmute.vtv_18110069_18110051_18110070.game_sudoku.utils.AndroidUtils;

/**
 * List of puzzle's folder. This activity also serves as root activity of application.
 *
 * @author romario
 */
public class FolderListActivity extends ThemedActivity {

    public static final int MENU_ITEM_ADD = Menu.FIRST;
    public static final int MENU_ITEM_RENAME = Menu.FIRST + 1;
    public static final int MENU_ITEM_DELETE = Menu.FIRST + 2;
    public static final int MENU_ITEM_ABOUT = Menu.FIRST + 3;
    public static final int MENU_ITEM_EXPORT = Menu.FIRST + 4;
    public static final int MENU_ITEM_EXPORT_ALL = Menu.FIRST + 5;
    public static final int MENU_ITEM_IMPORT = Menu.FIRST + 6;
    public static final int MENU_ITEM_SETTINGS = Menu.FIRST + 7;

    private static final int OPEN_FILE = 1;

    private static final int DIALOG_ABOUT = 0;
    private static final int DIALOG_ADD_FOLDER = 1;
    private static final int DIALOG_RENAME_FOLDER = 2;
    private static final int DIALOG_DELETE_FOLDER = 3;
    private static final String TAG = "FolderListActivity";
    private int STORAGE_PERMISSION_CODE = 1;
    private Cursor mCursor;
    private SudokuDatabase mDatabase;
    private FolderListViewBinder mFolderListBinder;
    private ListView mListView;
    private Menu mMenu;

    // input parameters for dialogs
    private TextView mAddFolderNameInput;
    private TextView mRenameFolderNameInput;
    private long mRenameFolderID;
    private long mDeleteFolderID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.folder_list);
        setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);

        View getMorePuzzles = findViewById(R.id.get_more_puzzles);
        getMorePuzzles.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://example.com/"));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        mDatabase = new SudokuDatabase(getApplicationContext());
        mCursor = mDatabase.getFolderList();
        startManagingCursor(mCursor);
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.folder_list_item,
                mCursor, new String[]{FolderColumns.NAME, FolderColumns._ID},
                new int[]{R.id.name, R.id.detail});
        mFolderListBinder = new FolderListViewBinder(this);
        adapter.setViewBinder(mFolderListBinder);

        mListView = findViewById(android.R.id.list);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener((parent, view, position, id) -> {
            Intent i = new Intent(getApplicationContext(), SudokuListActivity.class);
            i.putExtra(SudokuListActivity.EXTRA_FOLDER_ID, id);
            startActivity(i);
        });
        registerForContextMenu(mListView);

        // show changelog on first run
        Changelog changelog = new Changelog(this);
        changelog.showOnFirstRun();
    }

    @Override
    protected void onStart() {
        super.onStart();

        updateList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDatabase.close();
        mFolderListBinder.destroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putLong("mRenameFolderID", mRenameFolderID);
        outState.putLong("mDeleteFolderID", mDeleteFolderID);
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);

        mRenameFolderID = state.getLong("mRenameFolderID");
        mDeleteFolderID = state.getLong("mDeleteFolderID");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        // This is our one standard application action -- inserting a
        // new note into the list.
        menu.add(0, MENU_ITEM_ADD, 0, R.string.add_folder)
                .setShortcut('3', 'a')
                .setIcon(R.drawable.ic_add);
        menu.add(0, MENU_ITEM_IMPORT, 0, R.string.import_file)
                .setShortcut('8', 'i')
                .setIcon(R.drawable.ic_cloud_download);
        menu.add(0, MENU_ITEM_EXPORT_ALL, 1, R.string.export_all_folders)
                .setShortcut('7', 'e')
                .setIcon(R.drawable.ic_share);
        menu.add(0, MENU_ITEM_SETTINGS, 2, R.string.settings)
                .setShortcut('6', 's')
                .setIcon(R.drawable.ic_settings);
        menu.add(0, MENU_ITEM_ABOUT, 2, R.string.about)
                .setShortcut('1', 'h')
                .setIcon(R.drawable.ic_info);


        // Generate any additional actions that can be performed on the
        // overall list.  In a normal install, there are no additional
        // actions found here, but this allows other applications to extend
        // our menu with their own actions.
        Intent intent = new Intent(null, getIntent().getData());
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
                new ComponentName(this, FolderListActivity.class), null, intent, 0, null);

        mMenu = menu;
        return true;

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info;
        try {
            info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfo", e);
            return;
        }

        Cursor cursor = (Cursor) mListView.getAdapter().getItem(info.position);
        if (cursor == null) {
            // For some reason the requested item isn't available, do nothing
            return;
        }
        menu.setHeaderTitle(cursor.getString(cursor.getColumnIndex(FolderColumns.NAME)));

        menu.add(0, MENU_ITEM_EXPORT, 0, R.string.export_folder);
        menu.add(0, MENU_ITEM_RENAME, 1, R.string.rename_folder);
        menu.add(0, MENU_ITEM_DELETE, 2, R.string.delete_folder);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        LayoutInflater factory = LayoutInflater.from(this);

        switch (id) {
            case DIALOG_ABOUT:
                final View aboutView = factory.inflate(R.layout.about, null);
                TextView versionLabel = aboutView.findViewById(R.id.version_label);
                String versionName = AndroidUtils.getAppVersionName(getApplicationContext());
                versionLabel.setText(getString(R.string.version, versionName));
                return new AlertDialog.Builder(this)
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle(R.string.app_name)
                        .setView(aboutView)
                        .setPositiveButton("OK", null)
                        .create();
            case DIALOG_ADD_FOLDER:
                View addFolderView = factory.inflate(R.layout.folder_name, null);
                mAddFolderNameInput = addFolderView.findViewById(R.id.name);
                return new AlertDialog.Builder(this)
                        .setIcon(R.drawable.ic_add)
                        .setTitle(R.string.add_folder)
                        .setView(addFolderView)
                        .setPositiveButton(R.string.save, (dialog, whichButton) -> {
                            mDatabase.insertFolder(mAddFolderNameInput.getText().toString().trim(), System.currentTimeMillis());
                            updateList();
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .create();
            case DIALOG_RENAME_FOLDER:
                final View renameFolderView = factory.inflate(R.layout.folder_name, null);
                mRenameFolderNameInput = renameFolderView.findViewById(R.id.name);

                return new AlertDialog.Builder(this)
                        .setIcon(R.drawable.ic_edit_grey)
                        .setTitle(R.string.rename_folder_title)
                        .setView(renameFolderView)
                        .setPositiveButton(R.string.save, (dialog, whichButton) -> {
                            mDatabase.updateFolder(mRenameFolderID, mRenameFolderNameInput.getText().toString().trim());
                            updateList();
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .create();
            case DIALOG_DELETE_FOLDER:
                return new AlertDialog.Builder(this)
                        .setIcon(R.drawable.ic_delete)
                        .setTitle(R.string.delete_folder_title)
                        .setMessage(R.string.delete_folder_confirm)
                        .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                            // TODO: this could take a while, I should show progress dialog
                            mDatabase.deleteFolder(mDeleteFolderID);
                            updateList();
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .create();


        }

        return null;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        super.onPrepareDialog(id, dialog);

        switch (id) {
            case DIALOG_ADD_FOLDER:
                break;
            case DIALOG_RENAME_FOLDER: {
                FolderInfo folder = mDatabase.getFolderInfo(mRenameFolderID);
                String folderName = folder != null ? folder.name : "";
                dialog.setTitle(getString(R.string.rename_folder_title, folderName));
                mRenameFolderNameInput.setText(folderName);
                break;
            }
            case DIALOG_DELETE_FOLDER: {
                FolderInfo folder = mDatabase.getFolderInfo(mDeleteFolderID);
                String folderName = folder != null ? folder.name : "";
                dialog.setTitle(getString(R.string.delete_folder_title, folderName));
                break;
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info;
        try {
            info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfo", e);
            return false;
        }


        switch (item.getItemId()) {
            case MENU_ITEM_EXPORT:
                Intent intent = new Intent();
                intent.setClass(this, SudokuExportActivity.class);
                intent.putExtra(SudokuExportActivity.EXTRA_FOLDER_ID, info.id);
                startActivity(intent);
                return true;
            case MENU_ITEM_RENAME:
                mRenameFolderID = info.id;
                showDialog(DIALOG_RENAME_FOLDER);
                return true;
            case MENU_ITEM_DELETE:
                mDeleteFolderID = info.id;
                showDialog(DIALOG_DELETE_FOLDER);
                return true;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case MENU_ITEM_ADD:
                showDialog(DIALOG_ADD_FOLDER);
                return true;
            case MENU_ITEM_IMPORT:
                intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                startActivityForResult(intent, OPEN_FILE);
                return true;
            case MENU_ITEM_EXPORT_ALL:
                intent = new Intent();
                intent.setClass(this, SudokuExportActivity.class);
                intent.putExtra(SudokuExportActivity.EXTRA_FOLDER_ID, SudokuExportActivity.ALL_FOLDERS);
                startActivity(intent);
                return true;
            case MENU_ITEM_SETTINGS:
                intent = new Intent();
                intent.setClass(this, GameSettingsActivity.class);
                startActivity(intent);
                return true;
            case MENU_ITEM_ABOUT:
                showDialog(DIALOG_ABOUT);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == OPEN_FILE && resultCode == RESULT_OK) {
            Uri uri = null;
            if (data != null) {
                uri = data.getData();
                Intent i = new Intent(this, SudokuImportActivity.class);
                i.setData(uri);
                startActivity(i);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onOptionsItemSelected(mMenu.findItem(MENU_ITEM_IMPORT));
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateList() {
        mCursor.requery();
    }

    private static class FolderListViewBinder implements ViewBinder {
        private Context mContext;
        private FolderDetailLoader mDetailLoader;


        public FolderListViewBinder(Context context) {
            mContext = context;
            mDetailLoader = new FolderDetailLoader(context);
        }

        @Override
        public boolean setViewValue(View view, Cursor c, int columnIndex) {

            switch (view.getId()) {
                case R.id.name:
                    ((TextView) view).setText(c.getString(columnIndex));
                    break;
                case R.id.detail:
                    final long folderID = c.getLong(columnIndex);
                    final TextView detailView = (TextView) view;
                    detailView.setText(mContext.getString(R.string.loading));
                    mDetailLoader.loadDetailAsync(folderID, folderInfo -> {
                        if (folderInfo != null)
                            detailView.setText(folderInfo.getDetail(mContext));
                    });
            }
            return true;
        }

        public void destroy() {
            mDetailLoader.destroy();
        }
    }
}
