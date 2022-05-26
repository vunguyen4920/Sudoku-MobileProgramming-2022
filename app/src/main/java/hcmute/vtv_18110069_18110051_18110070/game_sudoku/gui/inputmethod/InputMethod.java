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
 * Base class for several input methods used to edit sudoku contents.
 *
 * @author romario
 */
public abstract class InputMethod {

    // TODO: I should not have mPrefix for fields used in subclasses, create proper getters
    protected Context mContext;
    protected IMControlPanel mControlPanel;
    protected SudokuGame mGame;
    protected SudokuBoardView mBoard;
    protected HintsQueue mHintsQueue;
    protected View mInputMethodView;
    protected boolean mActive = false;
    private String mInputMethodName;
    private boolean mEnabled = true;

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
            //switchModeButton.getBackground().setColorFilter(
            //        new LightingColorFilter(Color.parseColor("#00695c"), 0));
            onControlPanelCreated(mInputMethodView);
        }

        return mInputMethodView;
    }

    /**
     * This should be called when activity is paused (so InputMethod can do some cleanup,
     * for example properly dismiss dialogs because of WindowLeaked exception).
     */
    public void pause() {
        onPause();
    }

    protected void onPause() {

    }

    /**
     * This should be unique name of input method.
     *
     * @return
     */
    protected String getInputMethodName() {
        return mInputMethodName;
    }

    public abstract int getNameResID();

    public abstract int getHelpResID();

    /**
     * Gets abbreviated name of input method, which will be displayed on input method switch button.
     *
     * @return
     */
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

    /**
     * Called when cell is selected. Please note that cell selection can
     * change without direct user interaction.
     *
     * @param cell
     */
    protected void onCellSelected(Cell cell) {
    }

    /**
     * Called when cell is tapped.
     *
     * @param cell
     */
    protected void onCellTapped(Cell cell) {
    }

    protected void onSaveState(IMControlPanelStatePersister.StateBundle outState) {
    }

    protected void onRestoreState(IMControlPanelStatePersister.StateBundle savedState) {
    }
}
