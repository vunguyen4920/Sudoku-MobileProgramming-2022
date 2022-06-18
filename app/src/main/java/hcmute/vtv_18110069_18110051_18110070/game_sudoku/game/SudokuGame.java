package hcmute.vtv_18110069_18110051_18110070.game_sudoku.game;

import android.os.Bundle;
import android.os.SystemClock;

import androidx.annotation.Nullable;

import hcmute.vtv_18110069_18110051_18110070.game_sudoku.game.command.AbstractCommand;
import hcmute.vtv_18110069_18110051_18110070.game_sudoku.game.command.ClearAllNotesCommand;
import hcmute.vtv_18110069_18110051_18110070.game_sudoku.game.command.CommandStack;
import hcmute.vtv_18110069_18110051_18110070.game_sudoku.game.command.EditCellNoteCommand;
import hcmute.vtv_18110069_18110051_18110070.game_sudoku.game.command.FillInNotesCommand;
import hcmute.vtv_18110069_18110051_18110070.game_sudoku.game.command.FillInNotesWithAllValuesCommand;
import hcmute.vtv_18110069_18110051_18110070.game_sudoku.game.command.SetCellValueAndRemoveNotesCommand;
import hcmute.vtv_18110069_18110051_18110070.game_sudoku.game.command.SetCellValueCommand;

import java.util.ArrayList;

public class SudokuGame {
    /*Các biến dùng để xét State của game: đã bắt đầu, chưa bắt đầu và đã hoàn thành*/
    public static final int GAME_STATE_PLAYING = 0;
    public static final int GAME_STATE_NOT_STARTED = 1;
    public static final int GAME_STATE_COMPLETED = 2;

    //Các biến cơ bản của 1 màn chơi: Id, thời gian tạo, trạng thái của màn chơi, 
    //thời gian chơi trong màn đó và thời gian lúc ta dừng màn chơi
    private long mId;
    private long mCreated;
    private int mState;
    private long mTime;
    private long mLastPlayed;
    //Biến cho node
    private String mNote;
    //Biến cho collection
    private CellCollection mCells;
    //Biến cho giải nhanh
    private SudokuSolver mSolver;
    //Biến cho dùng giải nhanh
    private boolean mUsedSolver = false;
    private boolean mRemoveNotesOnEntry = false;

    private OnPuzzleSolvedListener mOnPuzzleSolvedListener;
    private CommandStack mCommandStack;
    // biến chưa thời gian lúc ta bắt đầu/tiếp tục chơi,
    //dùng để tính toàn thời gian đã chơi 1 màn.
    private long mActiveFromTime = -1;

    //Hàm dùng để tạo 1 màn chơi mới rỗng
    public SudokuGame() {
        mTime = 0;
        mLastPlayed = 0;
        mCreated = 0;

        mState = GAME_STATE_NOT_STARTED;
    }

    public static SudokuGame createEmptyGame() {
        SudokuGame game = new SudokuGame();
        game.setCells(CellCollection.createEmpty());
        // set creation time
        game.setCreated(System.currentTimeMillis());
        return game;
    }

    //Hàm dùng để lưu trạng thái hiện tại của màn chơi
    public void saveState(Bundle outState) {
        outState.putLong("id", mId);
        outState.putString("note", mNote);
        outState.putLong("created", mCreated);
        outState.putInt("state", mState);
        outState.putLong("time", mTime);
        outState.putLong("lastPlayed", mLastPlayed);
        outState.putString("cells", mCells.serialize());
        outState.putString("command_stack", mCommandStack.serialize());
    }
    //hàm dùng để phục hồi màn chơi
    public void restoreState(Bundle inState) {
        mId = inState.getLong("id");
        mNote = inState.getString("note");
        mCreated = inState.getLong("created");
        mState = inState.getInt("state");
        mTime = inState.getLong("time");
        mLastPlayed = inState.getLong("lastPlayed");
        mCells = CellCollection.deserialize(inState.getString("cells"));
        mCommandStack = CommandStack.deserialize(inState.getString("command_stack"), mCells);

        validate();
    }

    //Hàm để đặt cho màn chơi khi được giải xong
    public void setOnPuzzleSolvedListener(OnPuzzleSolvedListener l) {
        mOnPuzzleSolvedListener = l;
    }
    //hàm get,set cho ghi chú
    public String getNote() {
        return mNote;
    }

    public void setNote(String note) {
        mNote = note;
    }
    //hàm get,set cho thời gian màn chơi được tạo
    public long getCreated() {
        return mCreated;
    }

    public void setCreated(long created) {
        mCreated = created;
    }
    //hàm get,set cho trạng thái của màn chơi
    public int getState() {
        return mState;
    }

    public void setState(int state) {
        mState = state;
    }

    //hàm get,set cho tổng thời gian chơi của màn
    public long getTime() {
        if (mActiveFromTime != -1) {
            return mTime + SystemClock.uptimeMillis() - mActiveFromTime;
        } else {
            return mTime;
        }
    }
    public void setTime(long time) {
        mTime = time;
    }

    //hàm get,set cho thời gian lần cuối ta dừng màn chơi
    public long getLastPlayed() {
        return mLastPlayed;
    }
    public void setLastPlayed(long lastPlayed) {
        mLastPlayed = lastPlayed;
    }

    //hàm get,set cho collection lưu trữ cell cho màn chơi
    public CellCollection getCells() {
        return mCells;
    }
    public void setCells(CellCollection cells) {
        mCells = cells;
        validate();
        mCommandStack = new CommandStack(mCells);
    }

    //hàm get,set cho id của màn chơi
    public long getId() {
        return mId;
    }
    public void setId(long id) {
        mId = id;
    }
    //hàm get,set cho Stack chứa command
    public CommandStack getCommandStack() {
        return mCommandStack;
    }

    public void setCommandStack(CommandStack commandStack) {
        mCommandStack = commandStack;
    }

    public void setRemoveNotesOnEntry(boolean removeNotesOnEntry) {
        mRemoveNotesOnEntry = removeNotesOnEntry;
    }

    //Gắn giá trị cho 1 cell sau đó xem thử màn chơi đã được giải hay chưa
    public void setCellValue(Cell cell, int value) {
        if (cell == null) {
            throw new IllegalArgumentException("Cell cannot be null.");
        }
        if (value < 0 || value > 9) {
            throw new IllegalArgumentException("Value must be between 0-9.");
        }

        if (cell.isEditable()) {
            if (mRemoveNotesOnEntry) {
                executeCommand(new SetCellValueAndRemoveNotesCommand(cell, value));
            } else {
                executeCommand(new SetCellValueCommand(cell, value));
            }

            validate();
            if (isCompleted()) {
                finish();
                if (mOnPuzzleSolvedListener != null) {
                    mOnPuzzleSolvedListener.onPuzzleSolved();
                }
            }
        }
    }

    //Gắn ghi chú cho 1 cell
    public void setCellNote(Cell cell, CellNote note) {
        if (cell == null) {
            throw new IllegalArgumentException("Cell cannot be null.");
        }
        if (note == null) {
            throw new IllegalArgumentException("Note cannot be null.");
        }

        if (cell.isEditable()) {
            executeCommand(new EditCellNoteCommand(cell, note));
        }
    }
    //Hàm dùng để gọi bên command
    private void executeCommand(AbstractCommand c) {
        mCommandStack.execute(c);
    }

    //Undo lại command
    public void undo() {
        mCommandStack.undo();
    }
    //kiểm tra xem có command để undo hay không
    public boolean hasSomethingToUndo() {
        return mCommandStack.hasSomethingToUndo();
    }

    //Đặt checkpoint để undo
    public void setUndoCheckpoint() {
        mCommandStack.setCheckpoint();
    }
    //Undo đến checkpoint
    public void undoToCheckpoint() {
        mCommandStack.undoToCheckpoint();
    }
    //Kiểm tra xem có checkpoint hay không
    public boolean hasUndoCheckpoint() {
        return mCommandStack.hasCheckpoint();
    }

    public void undoToBeforeMistake() {
        mCommandStack.undoToSolvableState();
    }
    //Cho biết cell nào được chỉnh sửa cuối khi màn chơi dừng lại
    //dùng cho việc tiếp tục màn chơi
    @Nullable
    public Cell getLastChangedCell() {
        return mCommandStack.getLastChangedCell();
    }

    /**
     * Start game-play.
     */
    //Bắt đầu/tiếp tục màn chơi
    public void start() {
        mState = GAME_STATE_PLAYING;
        resume();
    }

    //Dùng để tiếp tục màn chơi,đặt thời gian hoạt động lại bằng khi tiếp tục
    //dùng để tính toán tổng thời gian chơi
    public void resume() {
        mActiveFromTime = SystemClock.uptimeMillis();
    }

    //Dùng để dừng màn chơi, tính tổng thời gian chơi sau đó bỏ mốc thời gian hoạt động
    public void pause() {
        mTime += SystemClock.uptimeMillis() - mActiveFromTime;
        mActiveFromTime = -1;

        setLastPlayed(System.currentTimeMillis());
    }

    //Kiểm tra xem màn chơi được thêm phần giải chưa
    public boolean isSolvable () {
        mSolver = new SudokuSolver();
        mSolver.setPuzzle(mCells);
        ArrayList<int[]> finalValues = mSolver.solve();
        return !finalValues.isEmpty();
    }

    
    //Dùng để giải nhanh màn chơi: màn chơi lập tức hoàn thành
    public void solve() {
        mUsedSolver = true;
        mSolver = new SudokuSolver();
        mSolver.setPuzzle(mCells);
        ArrayList<int[]> finalValues = mSolver.solve();
        for (int[] rowColVal : finalValues) {
            int row = rowColVal[0];
            int col = rowColVal[1];
            int val = rowColVal[2];
            Cell cell = mCells.getCell(row, col);
            this.setCellValue(cell, val);
        }
    }

    //Kiểm tra xem có được dùng giải nhanh hay không
    public boolean usedSolver() {
        return mUsedSolver;
    }

    //Dùng để có giá trị đúng của 1 cell, dùng cho phần hint
    public void solveCell(Cell cell) {
        mSolver = new SudokuSolver();
        mSolver.setPuzzle(mCells);
        ArrayList<int[]> finalValues = mSolver.solve();

        int row = cell.getRowIndex();
        int col = cell.getColumnIndex();
        for (int[] rowColVal : finalValues) {
            if (rowColVal[0] == row && rowColVal[1] == col) {
                int val = rowColVal[2];
                this.setCellValue(cell, val);
            }
        }
    }

    //Dùng khi kết thúc màn chơi
    private void finish() {
        pause();
        mState = GAME_STATE_COMPLETED;
    }

    //Đặt lại màn chơi về dạng ban đầu
    public void reset() {
        for (int r = 0; r < CellCollection.SUDOKU_SIZE; r++) {
            for (int c = 0; c < CellCollection.SUDOKU_SIZE; c++) {
                Cell cell = mCells.getCell(r, c);
                if (cell.isEditable()) {
                    cell.setValue(0);
                    cell.setNote(new CellNote());
                }
            }
        }
        mCommandStack = new CommandStack(mCells);
        validate();
        setTime(0);
        setLastPlayed(0);
        mState = GAME_STATE_NOT_STARTED;
        mUsedSolver = false;
    }

    //Kiểm tra xem màn chơi đã hoàn thành hay chưa
    public boolean isCompleted() {
        return mCells.isCompleted();
    }

    //Dọn sạch hết ghi chú
    public void clearAllNotes() {
        executeCommand(new ClearAllNotesCommand());
    }

    //Đặt ghi chú cho cell
    public void fillInNotes() {
        executeCommand(new FillInNotesCommand());
    }

    //Đặt ghi chú cho các ghi chú tất cả giá trị từ 1-9
    public void fillInNotesWithAllValues() { executeCommand(new FillInNotesWithAllValuesCommand()); }

    //Kiểm tra xem cell có đúng theo luật sudoku không
    public void validate() {
        mCells.validate();
    }

    //Được dùng khi màn chơi được giải
    public interface OnPuzzleSolvedListener {
        void onPuzzleSolved();
    }
}
