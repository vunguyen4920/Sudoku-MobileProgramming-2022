package hcmute.vtv_18110069_18110051_18110070.game_sudoku.gui;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;

import hcmute.vtv_18110069_18110051_18110070.game_sudoku.R;
import hcmute.vtv_18110069_18110051_18110070.game_sudoku.db.SudokuDatabase;
import hcmute.vtv_18110069_18110051_18110070.game_sudoku.game.Cell;
import hcmute.vtv_18110069_18110051_18110070.game_sudoku.game.SudokuGame;
import hcmute.vtv_18110069_18110051_18110070.game_sudoku.game.SudokuGame.OnPuzzleSolvedListener;
import hcmute.vtv_18110069_18110051_18110070.game_sudoku.gui.inputmethod.IMControlPanel;
import hcmute.vtv_18110069_18110051_18110070.game_sudoku.gui.inputmethod.IMControlPanelStatePersister;
import hcmute.vtv_18110069_18110051_18110070.game_sudoku.gui.inputmethod.IMNumpad;
import hcmute.vtv_18110069_18110051_18110070.game_sudoku.gui.inputmethod.IMPopup;
import hcmute.vtv_18110069_18110051_18110070.game_sudoku.gui.inputmethod.IMSingleNumber;
import hcmute.vtv_18110069_18110051_18110070.game_sudoku.utils.ThemeUtils;

public class PlayActivity extends ThemedActivity {

    public static final String EXTRA_SUDOKU_ID = "sudoku_id";

    public static final int MENU_ITEM_RESTART = Menu.FIRST;
    public static final int MENU_ITEM_CLEAR_ALL_NOTES = Menu.FIRST + 1;
    public static final int MENU_ITEM_FILL_IN_NOTES = Menu.FIRST + 2;
    public static final int MENU_ITEM_FILL_IN_NOTES_WITH_ALL_VALUES = Menu.FIRST + 3;
    public static final int MENU_ITEM_UNDO_ACTION = Menu.FIRST + 4;
    public static final int MENU_ITEM_UNDO = Menu.FIRST + 5;
    public static final int MENU_ITEM_HELP = Menu.FIRST + 6;
    public static final int MENU_ITEM_SETTINGS_ACTION = Menu.FIRST + 7;
    public static final int MENU_ITEM_SETTINGS = Menu.FIRST + 8;

    public static final int MENU_ITEM_SET_CHECKPOINT = Menu.FIRST + 9;
    public static final int MENU_ITEM_UNDO_TO_CHECKPOINT = Menu.FIRST + 10;
    public static final int MENU_ITEM_UNDO_TO_BEFORE_MISTAKE = Menu.FIRST + 11;
    public static final int MENU_ITEM_SOLVE = Menu.FIRST + 12;
    public static final int MENU_ITEM_HINT = Menu.FIRST + 13;

    private static final int DIALOG_RESTART = 1;
    private static final int DIALOG_WELL_DONE = 2;
    private static final int DIALOG_CLEAR_NOTES = 3;
    private static final int DIALOG_UNDO_TO_CHECKPOINT = 4;
    private static final int DIALOG_UNDO_TO_BEFORE_MISTAKE = 5;
    private static final int DIALOG_SOLVE_PUZZLE = 6;
    private static final int DIALOG_USED_SOLVER = 7;
    private static final int DIALOG_PUZZLE_NOT_SOLVED = 8;
    private static final int DIALOG_HINT = 9;
    private static final int DIALOG_CANNOT_GIVE_HINT = 10;

    private static final int REQUEST_SETTINGS = 1;

    private SudokuGame mSudokuGame;

    private SudokuDatabase mDatabase;

    private Handler mGuiHandler;

    private ViewGroup mRootLayout;
    private SudokuBoardView mSudokuBoard;
    private TextView mTimeLabel;
    private Menu mOptionsMenu;

    // Khai báo các kiểu chơi
    private IMControlPanel mIMControlPanel;
    private IMControlPanelStatePersister mIMControlPanelStatePersister;
    private IMPopup mIMPopup;
    private IMSingleNumber mIMSingleNumber;
    private IMNumpad mIMNumpad;

    private boolean mShowTime = true;
    private GameTimer mGameTimer;
    private TimerFormat mTimerFormatter = new TimerFormat();
    private boolean mFullScreen;
    private boolean mFillInNotesEnabled = false;

    private HintsQueue mHintsQueue;
    private OnPuzzleSolvedListener onSolvedListener = new OnPuzzleSolvedListener() {
        @Override
        public void onPuzzleSolved() {
            if (mShowTime) {
                mGameTimer.stop();
            }
            mSudokuBoard.setReadOnly(true);
            mOptionsMenu.findItem(MENU_ITEM_UNDO_ACTION).setEnabled(false);
            if (mSudokuGame.usedSolver()) {
                showDialog(DIALOG_USED_SOLVER);
            } else {
                showDialog(DIALOG_WELL_DONE);
            }
        }

    };
    private OnSelectedNumberChangedListener onSelectedNumberChangedListener = new OnSelectedNumberChangedListener() {
        @Override
        public void onSelectedNumberChanged(int number) {
            if (number != 0) {
                Cell cell = mSudokuGame.getCells().findFirstCell(number);
                mSudokuBoard.setHighlightedValue(number);
                if (cell != null) {
                    mSudokuBoard.moveCellSelectionTo(cell.getRowIndex(), cell.getColumnIndex());
                } else {
                    mSudokuBoard.clearCellSelection();
                }
            } else {
                mSudokuBoard.clearCellSelection();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // go fullscreen for devices with QVGA screen (only way I found
        // how to fit UI on the screen)
        Display display = getWindowManager().getDefaultDisplay();
        if ((display.getWidth() == 240 || display.getWidth() == 320)
                && (display.getHeight() == 240 || display.getHeight() == 320)) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            mFullScreen = true;
        }

        setContentView(R.layout.sudoku_play);

        mRootLayout = findViewById(R.id.root_layout);
        mSudokuBoard = findViewById(R.id.sudoku_board);
        mTimeLabel = findViewById(R.id.time_label);

        mDatabase = new SudokuDatabase(getApplicationContext());
        mHintsQueue = new HintsQueue(this);
        mGameTimer = new GameTimer();

        mGuiHandler = new Handler();

        // create sudoku game instance
        if (savedInstanceState == null) {
            // activity runs for the first time, read game from database
            Log.d("WTF", String.valueOf(getIntent().getLongExtra(EXTRA_SUDOKU_ID, 0)));
            long mSudokuGameID = getIntent().getLongExtra(EXTRA_SUDOKU_ID, 0);
            mSudokuGame = mDatabase.getSudoku(mSudokuGameID);
        } else {
            // activity has been running before, restore its state
            mSudokuGame = new SudokuGame();
            mSudokuGame.restoreState(savedInstanceState);
            mGameTimer.restoreState(savedInstanceState);
        }

        // Lưu màn chơi gần nhất vào SharedPrefs
        SharedPreferences gameSettings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = gameSettings.edit();
        editor.putLong("most_recently_played_sudoku_id", mSudokuGame.getId());
        editor.apply();

        if (mSudokuGame.getState() == SudokuGame.GAME_STATE_NOT_STARTED) {
            mSudokuGame.start();
        } else if (mSudokuGame.getState() == SudokuGame.GAME_STATE_PLAYING) {
            mSudokuGame.resume();
        }

        if (mSudokuGame.getState() == SudokuGame.GAME_STATE_COMPLETED) {
            mSudokuBoard.setReadOnly(true);
        }

        mSudokuBoard.setGame(mSudokuGame);
        mSudokuGame.setOnPuzzleSolvedListener(onSolvedListener);

        mHintsQueue.showOneTimeHint("welcome", R.string.welcome, R.string.first_run_hint);

        mIMControlPanel = findViewById(R.id.input_methods);
        mIMControlPanel.initialize(mSudokuBoard, mSudokuGame, mHintsQueue);

        mIMControlPanelStatePersister = new IMControlPanelStatePersister(this);

        mIMPopup = mIMControlPanel.getInputMethod(IMControlPanel.INPUT_METHOD_POPUP);
        mIMSingleNumber = mIMControlPanel.getInputMethod(IMControlPanel.INPUT_METHOD_SINGLE_NUMBER);
        mIMNumpad = mIMControlPanel.getInputMethod(IMControlPanel.INPUT_METHOD_NUMPAD);

        Cell cell = mSudokuGame.getLastChangedCell();
        if (cell != null && !mSudokuBoard.isReadOnly())
            mSudokuBoard.moveCellSelectionTo(cell.getRowIndex(), cell.getColumnIndex());
        else
            mSudokuBoard.moveCellSelectionTo(0, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences gameSettings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        int screenPadding = gameSettings.getInt("screen_border_size", 0);
        mRootLayout.setPadding(screenPadding, screenPadding, screenPadding, screenPadding);

        mFillInNotesEnabled = gameSettings.getBoolean("fill_in_notes_enabled", false);

        String theme = gameSettings.getString("theme", "opensudoku");
        if (theme.equals("custom") || theme.equals("custom_light")) {
            ThemeUtils.applyCustomThemeToSudokuBoardViewFromContext(mSudokuBoard, getApplicationContext());
        }

        mSudokuBoard.setHighlightWrongVals(gameSettings.getBoolean("highlight_wrong_values", true));
        mSudokuBoard.setHighlightTouchedCell(gameSettings.getBoolean("highlight_touched_cell", true));

        boolean highlightSimilarCells = gameSettings.getBoolean("highlight_similar_cells", true);
        boolean highlightSimilarNotes = gameSettings.getBoolean("highlight_similar_notes", true);
        if (highlightSimilarCells) {
            mSudokuBoard.setHighlightSimilarCell(highlightSimilarNotes ?
                    SudokuBoardView.HighlightMode.NUMBERS_AND_NOTES :
                    SudokuBoardView.HighlightMode.NUMBERS);
        } else {
            mSudokuBoard.setHighlightSimilarCell(SudokuBoardView.HighlightMode.NONE);
        }

        mSudokuGame.setRemoveNotesOnEntry(gameSettings.getBoolean("remove_notes_on_input", false));

        mShowTime = gameSettings.getBoolean("show_time", true);
        if (mSudokuGame.getState() == SudokuGame.GAME_STATE_PLAYING) {
            mSudokuGame.resume();

            if (mShowTime) {
                mGameTimer.start();
            }
        }
        mTimeLabel.setVisibility(mFullScreen && mShowTime ? View.VISIBLE : View.GONE);

        mIMPopup.setEnabled(gameSettings.getBoolean("im_popup", true));
        mIMSingleNumber.setEnabled(gameSettings.getBoolean("im_single_number", true));
        mIMNumpad.setEnabled(gameSettings.getBoolean("im_numpad", true));
        mIMNumpad.setMoveCellSelectionOnPress(gameSettings.getBoolean("im_numpad_move_right", false));
        mIMPopup.setHighlightCompletedValues(gameSettings.getBoolean("highlight_completed_values", true));
        mIMPopup.setShowNumberTotals(gameSettings.getBoolean("show_number_totals", false));
        mIMSingleNumber.setHighlightCompletedValues(gameSettings.getBoolean("highlight_completed_values", true));
        mIMSingleNumber.setShowNumberTotals(gameSettings.getBoolean("show_number_totals", false));
        mIMSingleNumber.setBidirectionalSelection(gameSettings.getBoolean("bidirectional_selection", true));
        mIMSingleNumber.setHighlightSimilar(gameSettings.getBoolean("highlight_similar", true));
        mIMSingleNumber.setmOnSelectedNumberChangedListener(onSelectedNumberChangedListener);
        mIMNumpad.setHighlightCompletedValues(gameSettings.getBoolean("highlight_completed_values", true));
        mIMNumpad.setShowNumberTotals(gameSettings.getBoolean("show_number_totals", false));

        mIMControlPanel.activateFirstInputMethod(); // make sure that some input method is activated
        mIMControlPanelStatePersister.restoreState(mIMControlPanel);

        if (!mSudokuBoard.isReadOnly()) {
            mSudokuBoard.invokeOnCellSelected();
        }

        updateTime();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            if (mFullScreen) {
                mGuiHandler.postDelayed(() -> {
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                    mRootLayout.requestLayout();
                }, 1000);
            }

        }
    }

    /**
     * Khi dừng trò chơi thì lưu vào db
     *
     */
    @Override
    protected void onPause() {
        super.onPause();

        mDatabase.updateSudoku(mSudokuGame);

        mGameTimer.stop();
        mIMControlPanel.pause();
        mIMControlPanelStatePersister.saveState(mIMControlPanel);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mDatabase.close();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mGameTimer.stop();

        if (mSudokuGame.getState() == SudokuGame.GAME_STATE_PLAYING) {
            mSudokuGame.pause();
        }

        mSudokuGame.saveState(outState);
        mGameTimer.saveState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        final boolean isLightTheme = ThemeUtils.isLightTheme(ThemeUtils.getCurrentThemeFromPreferences(getApplicationContext()));

        menu.add(0, MENU_ITEM_UNDO_ACTION, 0, R.string.undo)
                .setIcon(isLightTheme ? R.drawable.ic_undo_action_black : R.drawable.ic_undo_action_white)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        menu.add(0, MENU_ITEM_UNDO, 0, R.string.undo)
                .setShortcut('1', 'u')
                .setIcon(R.drawable.ic_undo);

        if (mFillInNotesEnabled) {
            menu.add(0, MENU_ITEM_FILL_IN_NOTES, 1, R.string.fill_in_notes)
                    .setIcon(R.drawable.ic_edit_grey);
        }

        menu.add(0, MENU_ITEM_FILL_IN_NOTES_WITH_ALL_VALUES, 1, R.string.fill_all_notes)
                .setIcon(R.drawable.ic_edit_grey);

        menu.add(0, MENU_ITEM_CLEAR_ALL_NOTES, 2, R.string.clear_all_notes)
                .setShortcut('3', 'a')
                .setIcon(R.drawable.ic_delete);

        menu.add(0, MENU_ITEM_SET_CHECKPOINT, 3, R.string.set_checkpoint);
        menu.add(0, MENU_ITEM_UNDO_TO_CHECKPOINT, 4, R.string.undo_to_checkpoint);
        menu.add(0, MENU_ITEM_UNDO_TO_BEFORE_MISTAKE, 4, getString(R.string.undo_to_before_mistake));

        menu.add(0, MENU_ITEM_HINT, 5, R.string.solver_hint);
        menu.add(0, MENU_ITEM_SOLVE, 6, R.string.solve_puzzle);

        menu.add(0, MENU_ITEM_RESTART, 7, R.string.restart)
                .setShortcut('7', 'r')
                .setIcon(R.drawable.ic_restore);

        menu.add(0, MENU_ITEM_SETTINGS_ACTION, 8, R.string.settings)
                .setIcon(isLightTheme ? R.drawable.ic_settings_action_black : R.drawable.ic_settings_action_white)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        menu.add(0, MENU_ITEM_SETTINGS, 8, R.string.settings)
                .setShortcut('9', 's')
                .setIcon(R.drawable.ic_settings);

        menu.add(0, MENU_ITEM_HELP, 9, R.string.help)
                .setShortcut('0', 'h')
                .setIcon(R.drawable.ic_help);


        // Generate any additional actions that can be performed on the
        // overall list.  In a normal install, there are no additional
        // actions found here, but this allows other applications to extend
        // our menu with their own actions.
        Intent intent = new Intent(null, getIntent().getData());
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
                new ComponentName(this, PlayActivity.class), null, intent, 0, null);

        mOptionsMenu = menu;
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (mSudokuGame.getState() == SudokuGame.GAME_STATE_PLAYING) {
            menu.findItem(MENU_ITEM_CLEAR_ALL_NOTES).setEnabled(true);
            if (mFillInNotesEnabled) {
                menu.findItem(MENU_ITEM_FILL_IN_NOTES).setEnabled(true);
            }
            menu.findItem(MENU_ITEM_FILL_IN_NOTES_WITH_ALL_VALUES).setEnabled(true);
            menu.findItem(MENU_ITEM_UNDO).setEnabled(mSudokuGame.hasSomethingToUndo());
            menu.findItem(MENU_ITEM_UNDO_TO_CHECKPOINT).setEnabled(mSudokuGame.hasUndoCheckpoint());
        } else {
            menu.findItem(MENU_ITEM_CLEAR_ALL_NOTES).setEnabled(false);
            if (mFillInNotesEnabled) {
                menu.findItem(MENU_ITEM_FILL_IN_NOTES).setEnabled(false);
            }
            menu.findItem(MENU_ITEM_FILL_IN_NOTES_WITH_ALL_VALUES).setEnabled(false);
            menu.findItem(MENU_ITEM_UNDO).setEnabled(false);
            menu.findItem(MENU_ITEM_UNDO_TO_CHECKPOINT).setEnabled(false);
            menu.findItem(MENU_ITEM_UNDO_TO_BEFORE_MISTAKE).setEnabled(false);
            menu.findItem(MENU_ITEM_SOLVE).setEnabled(false);
            menu.findItem(MENU_ITEM_HINT).setEnabled(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ITEM_RESTART:
                showDialog(DIALOG_RESTART);
                return true;
            case MENU_ITEM_CLEAR_ALL_NOTES:
                showDialog(DIALOG_CLEAR_NOTES);
                return true;
            case MENU_ITEM_FILL_IN_NOTES:
                mSudokuGame.fillInNotes();
                return true;
            case MENU_ITEM_FILL_IN_NOTES_WITH_ALL_VALUES:
                mSudokuGame.fillInNotesWithAllValues();
                return true;
            case MENU_ITEM_UNDO_ACTION:
                if (mSudokuGame.hasSomethingToUndo()) {
                    mSudokuGame.undo();
                    selectLastChangedCell();
                }
                return true;
            case MENU_ITEM_UNDO:
                mSudokuGame.undo();
                selectLastChangedCell();
                return true;
            case MENU_ITEM_SETTINGS_ACTION:
            case MENU_ITEM_SETTINGS:
                Intent i = new Intent();
                i.setClass(this, GameSettingsActivity.class);
                startActivityForResult(i, REQUEST_SETTINGS);
                return true;
            case MENU_ITEM_HELP:
                mHintsQueue.showHint(R.string.help, R.string.help_text);
                return true;
            case MENU_ITEM_SET_CHECKPOINT:
                mSudokuGame.setUndoCheckpoint();
                return true;
            case MENU_ITEM_UNDO_TO_CHECKPOINT:
                showDialog(DIALOG_UNDO_TO_CHECKPOINT);
                return true;
            case MENU_ITEM_UNDO_TO_BEFORE_MISTAKE:
                showDialog(DIALOG_UNDO_TO_BEFORE_MISTAKE);
                return true;
            case MENU_ITEM_SOLVE:
                showDialog(DIALOG_SOLVE_PUZZLE);
                return true;
            case MENU_ITEM_HINT:
                showDialog(DIALOG_HINT);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SETTINGS) {
            restartActivity();
        }
    }

    private void restartActivity() {
        startActivity(getIntent());
        finish();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_WELL_DONE:
                return new AlertDialog.Builder(this)
                        .setIcon(R.drawable.ic_info)
                        .setTitle(R.string.well_done)
                        .setMessage(getString(R.string.congrats, mTimerFormatter.format(mSudokuGame.getTime())))
                        .setPositiveButton(android.R.string.ok, null)
                        .create();
            case DIALOG_RESTART:
                return new AlertDialog.Builder(this)
                        .setIcon(R.drawable.ic_restore)
                        .setTitle(R.string.app_name)
                        .setMessage(R.string.restart_confirm)
                        .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                            // Restart game
                            mSudokuGame.reset();
                            mSudokuGame.start();
                            mSudokuBoard.setReadOnly(false);
                            if (mShowTime) {
                                mGameTimer.start();
                            }
                            removeDialog(DIALOG_WELL_DONE);
                            MenuItem menuItemSolve = mOptionsMenu.findItem(MENU_ITEM_SOLVE);
                            menuItemSolve.setEnabled(true);
                            MenuItem menuItemHint = mOptionsMenu.findItem(MENU_ITEM_HINT);
                            menuItemHint.setEnabled(true);
                            MenuItem menuItemUndoAction = mOptionsMenu.findItem(MENU_ITEM_UNDO_ACTION);
                            menuItemUndoAction.setEnabled(true);
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .create();
            case DIALOG_CLEAR_NOTES:
                return new AlertDialog.Builder(this)
                        .setIcon(R.drawable.ic_delete)
                        .setTitle(R.string.app_name)
                        .setMessage(R.string.clear_all_notes_confirm)
                        .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> mSudokuGame.clearAllNotes())
                        .setNegativeButton(android.R.string.no, null)
                        .create();
            case DIALOG_UNDO_TO_CHECKPOINT:
                return new AlertDialog.Builder(this)
                        .setIcon(R.drawable.ic_undo)
                        .setTitle(R.string.app_name)
                        .setMessage(R.string.undo_to_checkpoint_confirm)
                        .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                            mSudokuGame.undoToCheckpoint();
                            selectLastChangedCell();
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .create();
            case DIALOG_UNDO_TO_BEFORE_MISTAKE:
                return new AlertDialog.Builder(this)
                        .setIcon(R.drawable.ic_undo)
                        .setTitle(R.string.app_name)
                        .setMessage(getString(R.string.undo_to_before_mistake_confirm))
                        .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                            mSudokuGame.undoToBeforeMistake();
                            selectLastChangedCell();
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .create();
            case DIALOG_SOLVE_PUZZLE:
                return new AlertDialog.Builder(this)
                        .setTitle(R.string.app_name)
                        .setMessage(R.string.solve_puzzle_confirm)
                        .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                            if (mSudokuGame.isSolvable()) {
                                mSudokuGame.solve();
                            } else {
                                showDialog(DIALOG_PUZZLE_NOT_SOLVED);
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .create();
            case DIALOG_USED_SOLVER:
                return new AlertDialog.Builder(this)
                        .setTitle(R.string.app_name)
                        .setMessage(R.string.used_solver)
                        .setPositiveButton(android.R.string.ok, null)
                        .create();
            case DIALOG_PUZZLE_NOT_SOLVED:
                return new AlertDialog.Builder(this)
                        .setTitle(R.string.app_name)
                        .setMessage(R.string.puzzle_not_solved)
                        .setPositiveButton(android.R.string.ok, null)
                        .create();
            case DIALOG_HINT:
                return new AlertDialog.Builder(this)
                        .setTitle(R.string.app_name)
                        .setMessage(R.string.hint_confirm)
                        .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                            Cell cell = mSudokuBoard.getSelectedCell();
                            if (cell != null && cell.isEditable()) {
                                if (mSudokuGame.isSolvable()) {
                                    mSudokuGame.solveCell(cell);
                                } else {
                                    showDialog(DIALOG_PUZZLE_NOT_SOLVED);
                                }
                            } else {
                                showDialog(DIALOG_CANNOT_GIVE_HINT);
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .create();
            case DIALOG_CANNOT_GIVE_HINT:
                return new AlertDialog.Builder(this)
                        .setTitle(R.string.app_name)
                        .setMessage(R.string.cannot_give_hint)
                        .setPositiveButton(android.R.string.ok, null)
                        .create();
        }
        return null;
    }

    private void selectLastChangedCell() {
        Cell cell = mSudokuGame.getLastChangedCell();
        if (cell != null)
            mSudokuBoard.moveCellSelectionTo(cell.getRowIndex(), cell.getColumnIndex());
    }

    void updateTime() {
        if (mShowTime) {
            setTitle(mTimerFormatter.format(mSudokuGame.getTime()));
            mTimeLabel.setText(mTimerFormatter.format(mSudokuGame.getTime()));
        } else {
            setTitle(R.string.app_name);
        }

    }

    public interface OnSelectedNumberChangedListener {
        void onSelectedNumberChanged(int number);
    }

    private final class GameTimer extends Timer {

        GameTimer() {
            super(1000);
        }

        @Override
        protected boolean step(int count, long time) {
            updateTime();

            // Run until explicitly stopped.
            return false;
        }
    }
}
