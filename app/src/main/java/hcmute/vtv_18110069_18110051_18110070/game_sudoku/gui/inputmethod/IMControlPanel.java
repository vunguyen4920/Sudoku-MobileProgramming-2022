package hcmute.vtv_18110069_18110051_18110070.game_sudoku.gui.inputmethod;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import hcmute.vtv_18110069_18110051_18110070.game_sudoku.R;
import hcmute.vtv_18110069_18110051_18110070.game_sudoku.game.SudokuGame;
import hcmute.vtv_18110069_18110051_18110070.game_sudoku.gui.HintsQueue;
import hcmute.vtv_18110069_18110051_18110070.game_sudoku.gui.SudokuBoardView;
import hcmute.vtv_18110069_18110051_18110070.game_sudoku.gui.SudokuBoardView.OnCellSelectedListener;
import hcmute.vtv_18110069_18110051_18110070.game_sudoku.gui.SudokuBoardView.OnCellTappedListener;

/**
 * Đây là class layout chứa các logic về việc xử lí chọn cách nhập dữ liệu
 * Gồm 3 cách nhập dữ liệu:
 * - Popup: hiển thị hộp thoại
 * - Single Number: Chọn 1 số và chạm vào ô
 * - Numpad: Chọn 1 ô và chọn 1 số, cập nhật ngay lập tức
 */
public class IMControlPanel extends LinearLayout {
    public static final int INPUT_METHOD_POPUP = 0;
    public static final int INPUT_METHOD_SINGLE_NUMBER = 1;
    public static final int INPUT_METHOD_NUMPAD = 2;

    private Context mContext;
    private SudokuBoardView mBoard;
    private SudokuGame mGame;
    private HintsQueue mHintsQueue;

    private List<InputMethod> mInputMethods = new ArrayList<>();
    private int mActiveMethodIndex = -1;

    /**
     * Event khi chạm vào cell
     *
     * chạy hàm onCellTapped của input method tương ứng
     */
    private OnCellTappedListener mOnCellTapListener = cell -> {
        if (mActiveMethodIndex != -1 && mInputMethods != null) {
            mInputMethods.get(mActiveMethodIndex).onCellTapped(cell);
        }
    };

    /**
     * Event khi chạm vào cell
     *
     * chạy hàm onCellSelected của input method tương ứng
     */
    private OnCellSelectedListener mOnCellSelected = cell -> {
        if (mActiveMethodIndex != -1 && mInputMethods != null) {
            mInputMethods.get(mActiveMethodIndex).onCellSelected(cell);
        }
    };
    private OnClickListener mSwitchModeListener = v -> activateNextInputMethod();

    public IMControlPanel(Context context) {
        super(context);
        mContext = context;
    }

    public IMControlPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public void initialize(SudokuBoardView board, SudokuGame game, HintsQueue hintsQueue) {
        mBoard = board;
        mBoard.setOnCellTappedListener(mOnCellTapListener);
        mBoard.setOnCellSelectedListener(mOnCellSelected);

        mGame = game;
        mHintsQueue = hintsQueue;

        createInputMethods();
    }

    public void activateFirstInputMethod() {
        ensureInputMethods();
        if (mActiveMethodIndex == -1 || !mInputMethods.get(mActiveMethodIndex).isEnabled()) {
            activateInputMethod(0);
        }
    }

    /**
     * Hàm kích hoạt input method truyền vào tham số là id của 3 method trên
     *
     * @param methodID
     */
    public void activateInputMethod(int methodID) {
        if (methodID < -1 || methodID >= mInputMethods.size()) {
            throw new IllegalArgumentException(String.format("Invalid method id: %s.", methodID));
        }

        ensureInputMethods();

        if (mActiveMethodIndex != -1) {
            mInputMethods.get(mActiveMethodIndex).deactivate();
        }

        int id = methodID;
        boolean idFound = false;
        int numOfCycles = 0;

        if (id != -1) {
            while (numOfCycles <= mInputMethods.size()) {
                if (mInputMethods.get(id).isEnabled()) {
                    ensureControlPanel(id);
                    idFound = true;
                    break;
                }

                id++;
                if (id == mInputMethods.size()) {
                    id = 0;
                }
                numOfCycles++;
            }
        }

        if (!idFound) {
            id = -1;
        }

        for (int i = 0; i < mInputMethods.size(); i++) {
            InputMethod im = mInputMethods.get(i);
            if (im.isInputMethodViewCreated()) {
                im.getInputMethodView().setVisibility(i == id ? View.VISIBLE : View.GONE);
            }
        }

        mActiveMethodIndex = id;
        if (mActiveMethodIndex != -1) {
            InputMethod activeMethod = mInputMethods.get(mActiveMethodIndex);
            activeMethod.activate();

            if (mHintsQueue != null) {
                mHintsQueue.showOneTimeHint(activeMethod.getInputMethodName(),
                        activeMethod.getNameResID(), activeMethod.getHelpResID());
            }
        }
    }

    public void activateNextInputMethod() {
        ensureInputMethods();

        int id = mActiveMethodIndex + 1;
        if (id >= mInputMethods.size()) {
            if (mHintsQueue != null) {
                mHintsQueue.showOneTimeHint("thatIsAll", R.string.that_is_all, R.string.im_disable_modes_hint);
            }
            id = 0;
        }
        activateInputMethod(id);
    }

    public <T extends InputMethod> T getInputMethod(int methodId) {
        ensureInputMethods();

        return (T) mInputMethods.get(methodId);
    }

    public List<InputMethod> getInputMethods() {
        return Collections.unmodifiableList(mInputMethods);
    }

    public int getActiveMethodIndex() {
        return mActiveMethodIndex;
    }

    public void pause() {
        for (InputMethod im : mInputMethods) {
            im.pause();
        }
    }

    private void ensureInputMethods() {
        if (mInputMethods.size() == 0) {
            throw new IllegalStateException("Input methods are not created yet. Call initialize() first.");
        }
    }

    private void createInputMethods() {
        if (mInputMethods.size() == 0) {
            addInputMethod(INPUT_METHOD_POPUP, new IMPopup());
            addInputMethod(INPUT_METHOD_SINGLE_NUMBER, new IMSingleNumber());
            addInputMethod(INPUT_METHOD_NUMPAD, new IMNumpad());
        }
    }

    private void addInputMethod(int methodIndex, InputMethod im) {
        im.initialize(mContext, this, mGame, mBoard, mHintsQueue);
        mInputMethods.add(methodIndex, im);
    }

    /**
     *
     * Đảm bảo luôn có layout cho các cách nhập dữ liệu.
     *
     * @param methodID
     */
    private void ensureControlPanel(int methodID) {
        InputMethod im = mInputMethods.get(methodID);
        if (!im.isInputMethodViewCreated()) {
            View controlPanel = im.getInputMethodView();
            Button switchModeButton = controlPanel.findViewById(R.id.switch_input_mode);
            switchModeButton.setOnClickListener(mSwitchModeListener);
            this.addView(controlPanel, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
        }
    }
}
