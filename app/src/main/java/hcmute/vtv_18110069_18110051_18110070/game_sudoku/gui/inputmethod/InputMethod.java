package hcmute.vtv_18110069_18110051_18110070.game_sudoku.gui.inputmethod;

import android.content.Context;
import android.view.View;
import android.widget.Button;

import hcmute.vtv_18110069_18110051_18110070.game_sudoku.R;
import hcmute.vtv_18110069_18110051_18110070.game_sudoku.game.Cell;
import hcmute.vtv_18110069_18110051_18110070.game_sudoku.game.SudokuGame;
import hcmute.vtv_18110069_18110051_18110070.game_sudoku.gui.HintsQueue;
import hcmute.vtv_18110069_18110051_18110070.game_sudoku.gui.SudokuBoardView;

/**
 *
 * Class abstract cho các cách nhập dữ liệu
 *
 */
public abstract class InputMethod {

    protected IMControlPanel mControlPanel;
    protected SudokuGame mGame;
    protected SudokuBoardView mBoard;
    protected Context mContext;
    protected View mInputMethodView;
    protected HintsQueue mHintsQueue;
    protected boolean mActive = false;
    private boolean mEnabled = true;
    private String mInputMethodName;

    public InputMethod() {

    }

    protected void initialize(Context context, IMControlPanel controlPanel, SudokuGame game,
            SudokuBoardView board, HintsQueue hintsQueue) {
        mContext = context;
        mControlPanel = controlPanel;
        mGame = game;
        mBoard = board;
        mHintsQueue = hintsQueue;
        mInputMethodName = this.getClass().getSimpleName();
    }

    public boolean isInputMethodViewCreated() {
        return mInputMethodView != null;
    }

    public View getInputMethodView() {
        if (mInputMethodView == null) {
            mInputMethodView = createControlPanelView();
            View switchModeView = mInputMethodView.findViewById(R.id.switch_input_mode);
            Button switchModeButton = (Button) switchModeView;
            switchModeButton.setText(getAbbrName());
            onControlPanelCreated(mInputMethodView);
        }

        return mInputMethodView;
    }

    public void pause() {
        onPause();
    }

    protected void onPause() {

    }

    protected String getInputMethodName() {
        return mInputMethodName;
    }

    public abstract int getNameResID();

    public abstract int getHelpResID();

    public abstract String getAbbrName();

    public boolean isEnabled() {
        return mEnabled;
    }

    public void setEnabled(boolean enabled) {
        mEnabled = enabled;

        if (!enabled) {
            mControlPanel.activateNextInputMethod();
        }
    }

    public void activate() {
        mActive = true;
        onActivated();
    }

    public void deactivate() {
        mActive = false;
        onDeactivated();
    }

    protected abstract View createControlPanelView();

    protected void onControlPanelCreated(View controlPanel) {
    }

    protected void onActivated() {
    }

    protected void onDeactivated() {
    }

    protected void onCellSelected(Cell cell) {
    }

    protected void onCellTapped(Cell cell) {
    }

    protected void onSaveState(IMControlPanelStatePersister.StateBundle outState) {
    }

    protected void onRestoreState(IMControlPanelStatePersister.StateBundle savedState) {
    }
}
