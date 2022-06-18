package hcmute.vtv_18110069_18110051_18110070.game_sudoku.gui.inputmethod;

import android.content.Context;
import android.content.DialogInterface.OnDismissListener;
import android.view.LayoutInflater;
import android.view.View;

import hcmute.vtv_18110069_18110051_18110070.game_sudoku.R;
import hcmute.vtv_18110069_18110051_18110070.game_sudoku.game.Cell;
import hcmute.vtv_18110069_18110051_18110070.game_sudoku.game.CellCollection;
import hcmute.vtv_18110069_18110051_18110070.game_sudoku.game.CellNote;

import java.util.Map;

/**
 *
 * Người dùng sẽ chạm vào ô và lựa chọn số họ muốn trên popup.
 *
 */
public class IMPopup extends InputMethod {

    private IMPopupDialog mEditCellDialog;
    private Cell mSelectedCell;
    private boolean mHighlightCompletedValues = true;
    private boolean mShowNumberTotals = false;

    private IMPopupDialog.OnNoteEditListener mOnNoteEditListener = new IMPopupDialog.OnNoteEditListener() {
        @Override
        public boolean onNoteEdit(Integer[] numbers) {
            if (mSelectedCell != null) {
                mGame.setCellNote(mSelectedCell, CellNote.fromIntArray(numbers));
            }
            return true;
        }
    };

    private IMPopupDialog.OnNumberEditListener mOnNumberEditListener = new IMPopupDialog.OnNumberEditListener() {
        @Override
        public boolean onNumberEdit(int number) {
            if (number != -1 && mSelectedCell != null) {
                mGame.setCellValue(mSelectedCell, number);
                mBoard.setHighlightedValue(number);
            }
            return true;
        }
    };

    private OnDismissListener mOnPopupDismissedListener = dialog -> mBoard.hideTouchedCellHint();

    public boolean getHighlightCompletedValues() {
        return mHighlightCompletedValues;
    }

    public void setHighlightCompletedValues(boolean highlightCompletedValues) {
        mHighlightCompletedValues = highlightCompletedValues;
    }

    public boolean getShowNumberTotals() {
        return mShowNumberTotals;
    }

    public void setShowNumberTotals(boolean showNumberTotals) {
        mShowNumberTotals = showNumberTotals;
    }

    private void ensureEditCellDialog() {
        if (mEditCellDialog == null) {
            mEditCellDialog = new IMPopupDialog(mContext);
            mEditCellDialog.setOnNumberEditListener(mOnNumberEditListener);
            mEditCellDialog.setOnNoteEditListener(mOnNoteEditListener);
            mEditCellDialog.setOnDismissListener(mOnPopupDismissedListener);
        }

    }

    @Override
    protected void onActivated() {
        mBoard.setAutoHideTouchedCellHint(false);
    }

    @Override
    protected void onDeactivated() {
        mBoard.setAutoHideTouchedCellHint(true);
    }

    @Override
    protected void onCellTapped(Cell cell) {
        mSelectedCell = cell;
        if (cell.isEditable()) {
            ensureEditCellDialog();

            mEditCellDialog.resetButtons();
            mEditCellDialog.updateNumber(cell.getValue());
            mEditCellDialog.updateNote(cell.getNote().getNotedNumbers());

            Map<Integer, Integer> valuesUseCount = null;
            if (mHighlightCompletedValues || mShowNumberTotals)
                valuesUseCount = mGame.getCells().getValuesUseCount();

            if (mHighlightCompletedValues) {
                for (Map.Entry<Integer, Integer> entry : valuesUseCount.entrySet()) {
                    if (entry.getValue() >= CellCollection.SUDOKU_SIZE) {
                        mEditCellDialog.highlightNumber(entry.getKey());
                    }
                }
            }

            if (mShowNumberTotals) {
                for (Map.Entry<Integer, Integer> entry : valuesUseCount.entrySet()) {
                    mEditCellDialog.setValueCount(entry.getKey(), entry.getValue());
                }
            }
            mEditCellDialog.show();
        } else {
            mBoard.hideTouchedCellHint();
        }
    }

    @Override
    protected void onCellSelected(Cell cell) {
        super.onCellSelected(cell);

        if (cell != null) {
            mBoard.setHighlightedValue(cell.getValue());
        } else {
            mBoard.setHighlightedValue(0);
        }
    }

    @Override
    protected void onPause() {
        if (mEditCellDialog != null) {
            mEditCellDialog.cancel();
        }
    }

    @Override
    public int getNameResID() {
        return R.string.popup;
    }

    @Override
    public int getHelpResID() {
        return R.string.im_popup_hint;
    }

    @Override
    public String getAbbrName() {
        return mContext.getString(R.string.popup_abbr);
    }

    @Override
    protected View createControlPanelView() {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return inflater.inflate(R.layout.im_popup, null);
    }

}
