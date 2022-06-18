package hcmute.vtv_18110069_18110051_18110070.game_sudoku.gui;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;

import hcmute.vtv_18110069_18110051_18110070.game_sudoku.R;
import hcmute.vtv_18110069_18110051_18110070.game_sudoku.db.SudokuColumns;
import hcmute.vtv_18110069_18110051_18110070.game_sudoku.db.SudokuDatabase;
import hcmute.vtv_18110069_18110051_18110070.game_sudoku.game.CellCollection;
import hcmute.vtv_18110069_18110051_18110070.game_sudoku.game.FolderInfo;
import hcmute.vtv_18110069_18110051_18110070.game_sudoku.game.SudokuGame;
import hcmute.vtv_18110069_18110051_18110070.game_sudoku.utils.ThemeUtils;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

/**
 * Show danh mục độ khó các màn chơi sudoku.
 *
 */
public class SudokuListActivity extends ThemedActivity {

    public static final String EXTRA_FOLDER_ID = "folder_id";

    public static final int MENU_ITEM_INSERT = Menu.FIRST;
    public static final int MENU_ITEM_EDIT = Menu.FIRST + 1;
    public static final int MENU_ITEM_DELETE = Menu.FIRST + 2;
    public static final int MENU_ITEM_PLAY = Menu.FIRST + 3;
    public static final int MENU_ITEM_RESET = Menu.FIRST + 4;
    public static final int MENU_ITEM_RESET_ALL = Menu.FIRST + 5;
    public static final int MENU_ITEM_EDIT_NOTE = Menu.FIRST + 6;
    public static final int MENU_ITEM_FILTER = Menu.FIRST + 7;
    public static final int MENU_ITEM_SORT = Menu.FIRST + 8;
    public static final int MENU_ITEM_FOLDERS = Menu.FIRST + 9;
    public static final int MENU_ITEM_SETTINGS = Menu.FIRST + 10;

    private static final int DIALOG_DELETE_PUZZLE = 0;
    private static final int DIALOG_RESET_PUZZLE = 1;
    private static final int DIALOG_RESET_ALL = 2;
    private static final int DIALOG_EDIT_NOTE = 3;
    private static final int DIALOG_FILTER = 4;
    private static final int DIALOG_SORT = 5;

    private static final String FILTER_STATE_NOT_STARTED = "filter" + SudokuGame.GAME_STATE_NOT_STARTED;
    private static final String FILTER_STATE_PLAYING = "filter" + SudokuGame.GAME_STATE_PLAYING;
    private static final String FILTER_STATE_SOLVED = "filter" + SudokuGame.GAME_STATE_COMPLETED;

    private static final String SORT_TYPE = "sort_type";
    private static final String SORT_ORDER = "sort_order";

    private static final String TAG = "SudokuListActivity";

    private long mFolderID;

    private long mDeletePuzzleID;
    private long mResetPuzzleID;
    private long mEditNotePuzzleID;
    private TextView mEditNoteInput;
    private SudokuListFilter mListFilter;
    private SudokuListSorter mListSorter;

    private TextView mFilterStatus;

    private SimpleCursorAdapter mAdapter;
    private Cursor mCursor;
    private SudokuDatabase mDatabase;
    private FolderDetailLoader mFolderDetailLoader;
    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.sudoku_list);
        mFilterStatus = findViewById(R.id.filter_status);

        setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);

        mDatabase = new SudokuDatabase(getApplicationContext());
        mFolderDetailLoader = new FolderDetailLoader(getApplicationContext());

        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_FOLDER_ID)) {
            mFolderID = intent.getLongExtra(EXTRA_FOLDER_ID, 0);
        } else {
            Log.d(TAG, "No 'folder_id' extra provided, exiting.");
            finish();
            return;
        }

        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mListFilter = new SudokuListFilter(getApplicationContext());
        mListFilter.showStateNotStarted = settings.getBoolean(FILTER_STATE_NOT_STARTED, true);
        mListFilter.showStatePlaying = settings.getBoolean(FILTER_STATE_PLAYING, true);
        mListFilter.showStateCompleted = settings.getBoolean(FILTER_STATE_SOLVED, true);

        mListSorter = new SudokuListSorter(getApplicationContext());
        mListSorter.setSortType(settings.getInt(SORT_TYPE, SudokuListSorter.SORT_BY_CREATED));
        mListSorter.setAscending(settings.getBoolean(SORT_ORDER, false));

        mAdapter = new SimpleCursorAdapter(this, R.layout.sudoku_list_item,
                null, new String[] { SudokuColumns.DATA, SudokuColumns.STATE,
                        SudokuColumns.TIME, SudokuColumns.LAST_PLAYED,
                        SudokuColumns.CREATED, SudokuColumns.PUZZLE_NOTE },
                new int[] { R.id.sudoku_board, R.id.state, R.id.time,
                        R.id.last_played, R.id.created, R.id.note });
        mAdapter.setViewBinder(new SudokuListViewBinder(this));
        updateList();

        mListView = findViewById(android.R.id.list);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener((parent, view, position, id) -> playSudoku(id));
        registerForContextMenu(mListView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mDatabase.close();
        mFolderDetailLoader.destroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putLong("mDeletePuzzleID", mDeletePuzzleID);
        outState.putLong("mResetPuzzleID", mResetPuzzleID);
        outState.putLong("mEditNotePuzzleID", mEditNotePuzzleID);
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);

        mDeletePuzzleID = state.getLong("mDeletePuzzleID");
        mResetPuzzleID = state.getLong("mResetPuzzleID");
        mEditNotePuzzleID = state.getLong("mEditNotePuzzleID");
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateTitle();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (isTaskRoot() && keyCode == KeyEvent.KEYCODE_BACK) {
            Intent i = new Intent();
            i.setClass(this, DifficultiesListActivity.class);
            startActivity(i);
            finish();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add(0, MENU_ITEM_FOLDERS, 0, R.string.folders).setShortcut('1', 'f')
                .setIcon(R.drawable.ic_folder);
        menu.add(0, MENU_ITEM_INSERT, 1, R.string.add_sudoku).setShortcut('1', 'a')
                .setIcon(R.drawable.ic_add);
        menu.add(0, MENU_ITEM_FILTER, 2, R.string.filter).setShortcut('2', 'f')
                .setIcon(R.drawable.ic_view);
        menu.add(0, MENU_ITEM_SORT, 2, R.string.sort).setShortcut('2', 'o')
                .setIcon(R.drawable.ic_sort);
        menu.add(0, MENU_ITEM_RESET_ALL, 3, R.string.reset_all_puzzles).setShortcut('3', 'r')
                .setIcon(R.drawable.ic_undo);
        menu.add(0, MENU_ITEM_SETTINGS, 4, R.string.settings).setShortcut('4', 's')
                .setIcon(R.drawable.ic_settings);
        Intent intent = new Intent(null, getIntent().getData());
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
                new ComponentName(this, SudokuListActivity.class), null,
                intent, 0, null);

        return true;

    }

    @Override
    protected Dialog onCreateDialog(int id) {
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        switch (id) {
            case DIALOG_DELETE_PUZZLE:
                return new AlertDialog.Builder(this)
                        .setIcon(R.drawable.ic_delete)
                        .setTitle("Puzzle")
                        .setMessage(R.string.delete_puzzle_confirm)
                        .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                            long mostRecentId = settings.getLong("most_recently_played_sudoku_id", 0);
                            if (mDeletePuzzleID == mostRecentId) {
                                settings.edit().remove("most_recently_played_sudoku_id").apply();
                            }
                            mDatabase.deleteSudoku(mDeletePuzzleID);
                            updateList();
                        })
                        .setNegativeButton(android.R.string.no, null).create();
            case DIALOG_EDIT_NOTE:

                LayoutInflater factory = LayoutInflater.from(this);
                final View noteView = factory.inflate(R.layout.sudoku_list_item_note,
                        null);
                mEditNoteInput = noteView.findViewById(R.id.note);
                return new AlertDialog.Builder(this)
                        .setIcon(R.drawable.ic_add)
                        .setTitle(R.string.edit_note)
                        .setView(noteView)
                        .setPositiveButton(R.string.save, (dialog, whichButton) -> {
                            SudokuGame game = mDatabase.getSudoku(mEditNotePuzzleID);
                            game.setNote(mEditNoteInput.getText().toString());
                            mDatabase.updateSudoku(game);
                            updateList();
                        })
                        .setNegativeButton(android.R.string.cancel, null).create();
            case DIALOG_RESET_PUZZLE:
                return new AlertDialog.Builder(this)
                        .setIcon(R.drawable.ic_restore)
                        .setTitle("Puzzle")
                        .setMessage(R.string.reset_puzzle_confirm)
                        .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                            SudokuGame game = mDatabase.getSudoku(mResetPuzzleID);
                            if (game != null) {
                                game.reset();
                                mDatabase.updateSudoku(game);
                            }
                            updateList();
                        })
                        .setNegativeButton(android.R.string.no, null).create();
            case DIALOG_FILTER:
                return new AlertDialog.Builder(this)
                        .setIcon(R.drawable.ic_view)
                        .setTitle(R.string.filter_by_gamestate)
                        .setMultiChoiceItems(
                                R.array.game_states,
                                new boolean[] {
                                        mListFilter.showStateNotStarted,
                                        mListFilter.showStatePlaying,
                                        mListFilter.showStateCompleted,
                                },
                                (dialog, whichButton, isChecked) -> {
                                    switch (whichButton) {
                                        case 0:
                                            mListFilter.showStateNotStarted = isChecked;
                                            break;
                                        case 1:
                                            mListFilter.showStatePlaying = isChecked;
                                            break;
                                        case 2:
                                            mListFilter.showStateCompleted = isChecked;
                                            break;
                                    }
                                })
                        .setPositiveButton(android.R.string.ok, (dialog, whichButton) -> {
                            settings.edit()
                                    .putBoolean(FILTER_STATE_NOT_STARTED, mListFilter.showStateNotStarted)
                                    .putBoolean(FILTER_STATE_PLAYING, mListFilter.showStatePlaying)
                                    .putBoolean(FILTER_STATE_SOLVED, mListFilter.showStateCompleted)
                                    .apply();
                            updateList();
                        })
                        .setNegativeButton(android.R.string.cancel, (dialog, whichButton) -> {
                            // User clicked No, so do some stuff
                        })
                        .create();
            case DIALOG_SORT:
                return new AlertDialog.Builder(this)
                        .setIcon(R.drawable.ic_sort)
                        .setTitle(R.string.sort_puzzles_by)
                        .setSingleChoiceItems(
                                R.array.game_sort,
                                mListSorter.getSortType(),
                                (dialog, whichButton) -> mListSorter.setSortType(whichButton))
                        .setPositiveButton(R.string.sort_order_ascending, (dialog, whichButton) -> {
                            mListSorter.setAscending(true);
                            settings.edit()
                                    .putInt(SORT_TYPE, mListSorter.getSortType())
                                    .putBoolean(SORT_ORDER, true)
                                    .apply();
                            updateList();
                        })
                        .setNegativeButton(R.string.sort_order_descending, (dialog, whichButton) -> {
                            mListSorter.setAscending(false);
                            settings.edit()
                                    .putInt(SORT_TYPE, mListSorter.getSortType())
                                    .putBoolean(SORT_ORDER, false)
                                    .apply();
                            updateList();
                        })
                        .setNeutralButton(android.R.string.cancel, (dialog, whichButton) -> {
                        })
                        .create();
            case DIALOG_RESET_ALL:
                return new AlertDialog.Builder(this)
                        .setIcon(R.drawable.ic_restore)
                        .setTitle(R.string.reset_all_puzzles_confirm)
                        .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                            List<SudokuGame> sudokuGames = mDatabase.getAllSudokuByFolder(mFolderID, mListSorter);
                            for (SudokuGame sudokuGame : sudokuGames) {
                                sudokuGame.reset();
                                mDatabase.updateSudoku(sudokuGame);
                            }
                            updateList();
                        })
                        .setNegativeButton(android.R.string.no, (dialog, whichButton) -> {
                        })
                        .create();
        }
        return null;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        super.onPrepareDialog(id, dialog);

        if (id == DIALOG_EDIT_NOTE) {
            SudokuDatabase db = new SudokuDatabase(getApplicationContext());
            SudokuGame game = db.getSudoku(mEditNotePuzzleID);
            mEditNoteInput.setText(game.getNote());
        }
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
            return;
        }

        menu.setHeaderTitle("Puzzle");

        menu.add(0, MENU_ITEM_PLAY, 0, R.string.play_puzzle);
        menu.add(0, MENU_ITEM_EDIT_NOTE, 1, R.string.edit_note);
        menu.add(0, MENU_ITEM_RESET, 2, R.string.reset_puzzle);
        menu.add(0, MENU_ITEM_EDIT, 3, R.string.edit_puzzle);
        menu.add(0, MENU_ITEM_DELETE, 4, R.string.delete_puzzle);
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
            case MENU_ITEM_PLAY:
                playSudoku(info.id);
                return true;
            case MENU_ITEM_EDIT:
                Intent i = new Intent(this, SudokuEditActivity.class);
                i.setAction(Intent.ACTION_EDIT);
                i.putExtra(SudokuEditActivity.EXTRA_SUDOKU_ID, info.id);
                startActivity(i);
                return true;
            case MENU_ITEM_DELETE:
                mDeletePuzzleID = info.id;
                showDialog(DIALOG_DELETE_PUZZLE);
                return true;
            case MENU_ITEM_EDIT_NOTE:
                mEditNotePuzzleID = info.id;
                showDialog(DIALOG_EDIT_NOTE);
                return true;
            case MENU_ITEM_RESET:
                mResetPuzzleID = info.id;
                showDialog(DIALOG_RESET_PUZZLE);
                return true;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;
        switch (item.getItemId()) {
            case MENU_ITEM_INSERT: {
                i = new Intent(this, SudokuEditActivity.class);
                i.setAction(Intent.ACTION_INSERT);
                i.putExtra(SudokuEditActivity.EXTRA_FOLDER_ID, mFolderID);
                startActivity(i);
                return true;
            }
            case MENU_ITEM_SETTINGS:
                i = new Intent(this, GameSettingsActivity.class);
                startActivity(i);
                return true;
            case MENU_ITEM_FILTER:
                showDialog(DIALOG_FILTER);
                return true;
            case MENU_ITEM_SORT:
                showDialog(DIALOG_SORT);
                return true;
            case MENU_ITEM_FOLDERS: {
                i = new Intent(this, DifficultiesListActivity.class);
                startActivity(i);
                finish();
                return true;
            }
            case MENU_ITEM_RESET_ALL:
                showDialog(DIALOG_RESET_ALL);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateList() {
        updateTitle();
        updateFilterStatus();

        if (mCursor != null) {
            stopManagingCursor(mCursor);
        }
        mCursor = mDatabase.getSudokuList(mFolderID, mListFilter, mListSorter);
        startManagingCursor(mCursor);
        mAdapter.changeCursor(mCursor);
    }

    private void updateFilterStatus() {

        if (mListFilter.showStateCompleted && mListFilter.showStateNotStarted && mListFilter.showStatePlaying) {
            mFilterStatus.setVisibility(View.GONE);
        } else {
            mFilterStatus.setText(getString(R.string.filter_active, mListFilter));
            mFilterStatus.setVisibility(View.VISIBLE);
        }
    }

    private void updateTitle() {
        FolderInfo folder = mDatabase.getFolderInfo(mFolderID);
        setTitle(folder.name);

        mFolderDetailLoader.loadDetailAsync(mFolderID, folderInfo -> {
            if (folderInfo != null)
                setTitle(folderInfo.name + " - " + folderInfo.getDetail(getApplicationContext()));
        });
    }

    private void playSudoku(long sudokuID) {
        Intent i = new Intent(SudokuListActivity.this, PlayActivity.class);
        i.putExtra(PlayActivity.EXTRA_SUDOKU_ID, sudokuID);
        startActivity(i);
    }

    private static class SudokuListViewBinder implements ViewBinder {
        private Context mContext;
        private TimerFormat mTimerFormatter = new TimerFormat();
        private DateFormat mDateTimeFormatter = DateFormat.getDateTimeInstance(
                DateFormat.SHORT, DateFormat.SHORT);
        private DateFormat mTimeFormatter = DateFormat
                .getTimeInstance(DateFormat.SHORT);

        public SudokuListViewBinder(Context context) {
            mContext = context;
        }

        @Override
        public boolean setViewValue(View view, Cursor c, int columnIndex) {

            int state = c.getInt(c.getColumnIndex(SudokuColumns.STATE));

            TextView label;

            switch (view.getId()) {
                case R.id.sudoku_board:
                    String data = c.getString(columnIndex);
                    CellCollection cells = null;
                    try {
                        cells = CellCollection.deserialize(data);
                    } catch (Exception e) {
                        long id = c.getLong(c.getColumnIndex(SudokuColumns._ID));
                        Log.e(TAG, String.format("Exception occurred when deserializing puzzle with id %s.", id), e);
                    }
                    SudokuBoardView board = (SudokuBoardView) view;
                    board.setReadOnly(true);
                    board.setFocusable(false);
                    ((SudokuBoardView) view).setCells(cells);
                    ThemeUtils.applyThemeToSudokuBoardViewFromContext(
                            ThemeUtils.getCurrentThemeFromPreferences(mContext),
                            board,
                            mContext);
                    break;
                case R.id.state:
                    label = ((TextView) view);
                    String stateString = null;
                    switch (state) {
                        case SudokuGame.GAME_STATE_COMPLETED:
                            stateString = mContext.getString(R.string.solved);
                            break;
                        case SudokuGame.GAME_STATE_PLAYING:
                            stateString = mContext.getString(R.string.playing);
                            break;
                    }
                    label.setVisibility(stateString == null ? View.GONE
                            : View.VISIBLE);
                    label.setText(stateString);
                    if (state == SudokuGame.GAME_STATE_COMPLETED) {
                        label.setTextColor(
                                ThemeUtils.getCurrentThemeColor(view.getContext(), android.R.attr.colorAccent));
                    } else {
                        label.setTextColor(
                                ThemeUtils.getCurrentThemeColor(view.getContext(), android.R.attr.textColorPrimary));
                    }
                    break;
                case R.id.time:
                    long time = c.getLong(columnIndex);
                    label = ((TextView) view);
                    String timeString = null;
                    if (time != 0) {
                        timeString = mTimerFormatter.format(time);
                    }
                    label.setVisibility(timeString == null ? View.GONE
                            : View.VISIBLE);
                    label.setText(timeString);
                    if (state == SudokuGame.GAME_STATE_COMPLETED) {
                        label.setTextColor(
                                ThemeUtils.getCurrentThemeColor(view.getContext(), android.R.attr.colorAccent));
                    } else {
                        label.setTextColor(
                                ThemeUtils.getCurrentThemeColor(view.getContext(), android.R.attr.textColorPrimary));
                    }
                    break;
                case R.id.last_played:
                    long lastPlayed = c.getLong(columnIndex);
                    label = ((TextView) view);
                    String lastPlayedString = null;
                    if (lastPlayed != 0) {
                        lastPlayedString = mContext.getString(R.string.last_played_at,
                                getDateAndTimeForHumans(lastPlayed));
                    }
                    label.setVisibility(lastPlayedString == null ? View.GONE
                            : View.VISIBLE);
                    label.setText(lastPlayedString);
                    break;
                case R.id.created:
                    long created = c.getLong(columnIndex);
                    label = ((TextView) view);
                    String createdString = null;
                    if (created != 0) {
                        createdString = mContext.getString(R.string.created_at,
                                getDateAndTimeForHumans(created));
                    }
                    label.setVisibility(createdString == null ? View.GONE
                            : View.VISIBLE);
                    label.setText(createdString);
                    break;
                case R.id.note:
                    String note = c.getString(columnIndex);
                    label = ((TextView) view);
                    if (note == null || note.trim().equals("")) {
                        view.setVisibility(View.GONE);
                    } else {
                        ((TextView) view).setText(note);
                    }
                    label
                            .setVisibility((note == null || note.trim().equals("")) ? View.GONE
                                    : View.VISIBLE);
                    label.setText(note);
                    break;
            }

            return true;
        }

        private String getDateAndTimeForHumans(long datetime) {
            Date date = new Date(datetime);

            Date now = new Date(System.currentTimeMillis());
            Date today = new Date(now.getYear(), now.getMonth(), now.getDate());
            Date yesterday = new Date(System.currentTimeMillis()
                    - (1000 * 60 * 60 * 24));

            if (date.after(today)) {
                return mContext.getString(R.string.at_time, mTimeFormatter.format(date));
            } else if (date.after(yesterday)) {
                return mContext.getString(R.string.yesterday_at_time, mTimeFormatter.format(date));
            } else {
                return mContext.getString(R.string.on_date, mDateTimeFormatter.format(date));
            }
        }
    }
}
