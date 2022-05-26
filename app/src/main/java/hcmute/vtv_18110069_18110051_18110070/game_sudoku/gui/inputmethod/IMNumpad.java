package hcmute.vtv_18110069_18110051_18110070.game_sudoku.gui.inputmethod;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;

import hcmute.vtv_18110069_18110051_18110070.game_sudoku.R;
import hcmute.vtv_18110069_18110051_18110070.game_sudoku.game.Cell;
import hcmute.vtv_18110069_18110051_18110070.game_sudoku.game.CellCollection;
import hcmute.vtv_18110069_18110051_18110070.game_sudoku.game.CellNote;
import hcmute.vtv_18110069_18110051_18110070.game_sudoku.game.SudokuGame;
import hcmute.vtv_18110069_18110051_18110070.game_sudoku.gui.HintsQueue;
import hcmute.vtv_18110069_18110051_18110070.game_sudoku.gui.SudokuBoardView;
import hcmute.vtv_18110069_18110051_18110070.game_sudoku.utils.ThemeUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IMNumpad extends InputMethod {

    private static final int MODE_EDIT_VALUE = 0;
    private static final int MODE_EDIT_NOTE = 1;
    private boolean moveCellSelectionOnPress = true;
    private boolean mHighlightCompletedValues = true;
    private boolean mShowNumberTotals = false;
    private Cell mSelectedCell;
    private ImageButton mSwitchNumNoteButton;

    private int mEditMode = MODE_EDIT_VALUE;

    private Map<Integer, Button> mNumberButtons;
    private OnClickListener mNumberButtonClick = new OnClickListener() {

        @Override
        public void onClick(View v) {
            int selNumber = (Integer) v.getTag();
            Cell selCell = mSelectedCell;

            if (selCell != null) {
                switch (mEditMode) {
                    case MODE_EDIT_NOTE:
                        if (selNumber == 0) {
                            mGame.setCellNote(selCell, CellNote.EMPTY);
                        } else if (selNumber > 0 && selNumber <= 9) {
                            mGame.setCellNote(selCell, selCell.getNote().toggleNumber(selNumber));
                        }
                        break;
                    case MODE_EDIT_VALUE:
                        if (selNumber >= 0 && selNumber <= 9) {
                            mGame.setCellValue(selCell, selNumber);
                            mBoard.setHighlightedValue(selNumber);
                            if (isMoveCellSelectionOnPress()) {
                                mBoard.moveCellSelectionRight();
                            }
                        }
                        break;
                }
            }
        }

    };
    private CellCollection.OnChangeListener mOnCellsChangeListener = () -> {
        if (mActive) {
            update();
        }
    };

    public boolean isMoveCellSelectionOnPress() {
        return moveCellSelectionOnPress;
    }

    public void setMoveCellSelectionOnPress(boolean moveCellSelectionOnPress) {
        this.moveCellSelectionOnPress = moveCellSelectionOnPress;
    }

    public boolean getHighlightCompletedValues() {
        return mHighlightCompletedValues;
    }

    /**
     * If set to true, buttons for numbers, which occur in {@link CellCollection}
     * more than {@link CellCollection#SUDOKU_SIZE}-times, will be highlighted.
     *
     * @param highlightCompletedValues
     */
    public void setHighlightCompletedValues(boolean highlightCompletedValues) {
        mHighlightCompletedValues = highlightCompletedValues;
    }

    public boolean getShowNumberTotals() {
        return mShowNumberTotals;
    }

    public void setShowNumberTotals(boolean showNumberTotals) {
        mShowNumberTotals = showNumberTotals;
    }

    @Override
    protected void initialize(Context context, IMControlPanel controlPanel,
                              SudokuGame game, SudokuBoardView board, HintsQueue hintsQueue) {
        super.initialize(context, controlPanel, game, board, hintsQueue);

        game.getCells().addOnChangeListener(mOnCellsChangeListener);
    }

    @Override
    protected View createControlPanelView() {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View controlPanel = inflater.inflate(R.layout.im_numpad, null);

        mNumberButtons = new HashMap<>();
        mNumberButtons.put(1, controlPanel.findViewById(R.id.button_1));
        mNumberButtons.put(2, controlPanel.findViewById(R.id.button_2));
        mNumberButtons.put(3, controlPanel.findViewById(R.id.button_3));
        mNumberButtons.put(4, controlPanel.findViewById(R.id.button_4));
        mNumberButtons.put(5, controlPanel.findViewById(R.id.button_5));
        mNumberButtons.put(6, controlPanel.findViewById(R.id.button_6));
        mNumberButtons.put(7, controlPanel.findViewById(R.id.button_7));
        mNumberButtons.put(8, controlPanel.findViewById(R.id.button_8));
        mNumberButtons.put(9, controlPanel.findViewById(R.id.button_9));
        mNumberButtons.put(0, controlPanel.findViewById(R.id.button_clear));

        for (Integer num : mNumberButtons.keySet()) {
            Button b = mNumberButtons.get(num);
            b.setTag(num);
            b.setOnClickListener(mNumberButtonClick);
        }

        mSwitchNumNoteButton = controlPanel.findViewById(R.id.switch_num_note);
        mSwitchNumNoteButton.setOnClickListener(v -> {
            mEditMode = mEditMode == MODE_EDIT_VALUE ? MODE_EDIT_NOTE : MODE_EDIT_VALUE;
            update();
        });

        return controlPanel;

    }

    @Override
    public int getNameResID() {
        return R.string.numpad;
    }

    @Override
    public int getHelpResID() {
        return R.string.im_numpad_hint;
    }

    @Override
    public String getAbbrName() {
        return mContext.getString(R.string.numpad_abbr);
    }

    @Override
    protected void onActivated() {
        onCellSelected(mBoard.isReadOnly() ? null : mBoard.getSelectedCell());
    }

    @Override
    protected void onCellSelected(Cell cell) {
        if (cell != null) {
            mBoard.setHighlightedValue(cell.getValue());
        } else {
            mBoard.setHighlightedValue(0);
        }

        mSelectedCell = cell;
        update();
    }

    private void update() {
        switch (mEditMode) {
            case MODE_EDIT_NOTE:
                mSwitchNumNoteButton.setImageResource(R.drawable.ic_edit_white);
                break;
            case MODE_EDIT_VALUE:
                mSwitchNumNoteButton.setImageResource(R.drawable.ic_edit_grey);
                break;
        }

        if (mEditMode == MODE_EDIT_VALUE) {
            int selectedNumber = mSelectedCell == null ? 0 : mSelectedCell.getValue();
            for (Button b : mNumberButtons.values()) {
                if (b.getTag().equals(selectedNumber)) {
                    ThemeUtils.applyIMButtonStateToView(b, ThemeUtils.IMButtonStyle.ACCENT);
                } else {
                    ThemeUtils.applyIMButtonStateToView(b, ThemeUtils.IMButtonStyle.DEFAULT);
                }
            }
        } else {
            CellNote note = mSelectedCell == null ? new CellNote() : mSelectedCell.getNote();
            List<Integer> notedNumbers = note.getNotedNumbers();
            for (Button b : mNumberButtons.values()) {
                if (notedNumbers.contains(b.getTag())) {
                    ThemeUtils.applyIMButtonStateToView(b, ThemeUtils.IMButtonStyle.ACCENT);
                } else {
                    ThemeUtils.applyIMButtonStateToView(b, ThemeUtils.IMButtonStyle.DEFAULT);
                }
            }
        }

        Map<Integer, Integer> valuesUseCount = null;
        if (mHighlightCompletedValues || mShowNumberTotals)
            valuesUseCount = mGame.getCells().getValuesUseCount();

        if (mHighlightCompletedValues && mEditMode == MODE_EDIT_VALUE) {
            int selectedNumber = mSelectedCell == null ? 0 : mSelectedCell.getValue();
            for (Map.Entry<Integer, Integer> entry : valuesUseCount.entrySet()) {
                boolean highlightValue = entry.getValue() >= CellCollection.SUDOKU_SIZE;
                boolean selected = entry.getKey() == selectedNumber;
                Button b = mNumberButtons.get(entry.getKey());
                if (highlightValue && !selected) {
                    ThemeUtils.applyIMButtonStateToView(b, ThemeUtils.IMButtonStyle.ACCENT_HIGHCONTRAST);
                }
            }
        }

        if (mShowNumberTotals) {
            for (Map.Entry<Integer, Integer> entry : valuesUseCount.entrySet()) {
                Button b = mNumberButtons.get(entry.getKey());
                b.setText(entry.getKey() + " (" + entry.getValue() + ")");
            }
        }
    }

    @Override
    protected void onSaveState(IMControlPanelStatePersister.StateBundle outState) {
        outState.putInt("editMode", mEditMode);
    }

    @Override
    protected void onRestoreState(IMControlPanelStatePersister.StateBundle savedState) {
        mEditMode = savedState.getInt("editMode", MODE_EDIT_VALUE);
        if (isInputMethodViewCreated()) {
            update();
        }
    }
}
