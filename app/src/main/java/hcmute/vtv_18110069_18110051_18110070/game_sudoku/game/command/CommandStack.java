package hcmute.vtv_18110069_18110051_18110070.game_sudoku.game.command;

import hcmute.vtv_18110069_18110051_18110070.game_sudoku.game.Cell;
import hcmute.vtv_18110069_18110051_18110070.game_sudoku.game.CellCollection;
import hcmute.vtv_18110069_18110051_18110070.game_sudoku.game.SudokuSolver;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Stack;
import java.util.StringTokenizer;

//Stack để chứa các lệnh, tiện cho việc undo, chỉ cần xóa lênh trong stack để rollback
public class CommandStack {
    //Stack để lưu lại các lệnh được thực thi
    private Stack<AbstractCommand> mCommandStack = new Stack<>();
    //Biến chứa collection
    private CellCollection mCells;
    //Tạo stack có sẵn collection
    public CommandStack(CellCollection cells) {
        mCells = cells;
    }

    //Đọc giá trị độc nhất
    public static CommandStack deserialize(String data, CellCollection cells) {
        StringTokenizer st = new StringTokenizer(data, "|");
        return deserialize(st, cells);
    }

    public static CommandStack deserialize(StringTokenizer data, CellCollection cells) {
        CommandStack result = new CommandStack(cells);
        int stackSize = Integer.parseInt(data.nextToken());
        for (int i = 0; i < stackSize; i++) {
            AbstractCommand command = AbstractCommand.deserialize(data);
            result.push(command);
        }

        return result;
    }

    //Độc nhất hóa giá trị
    public String serialize() {
        StringBuilder sb = new StringBuilder();
        serialize(sb);
        return sb.toString();
    }

    public void serialize(StringBuilder data) {
        data.append(mCommandStack.size()).append("|");
        for (int i = 0; i < mCommandStack.size(); i++) {
            AbstractCommand command = mCommandStack.get(i);
            command.serialize(data);
        }
    }

    //Làm trống Stack
    public boolean empty() {
        return mCommandStack.empty();
    }
    //Thực thi lệnh
    public void execute(AbstractCommand command) {
        push(command);
        command.execute();
    }
    //Undo lệnh
    public void undo() {
        if (!mCommandStack.empty()) {
            AbstractCommand c = pop();
            c.undo();
            validateCells();
        }
    }
    //Đặt checkpoint để undo
    public void setCheckpoint() {
        if (!mCommandStack.empty()) {
            AbstractCommand c = mCommandStack.peek();
            if (c instanceof CheckpointCommand)
                return;
        }
        push(new CheckpointCommand());
    }

    //Kiểm tra xem có checkpoint không
    public boolean hasCheckpoint() {
        for (AbstractCommand c : mCommandStack) {
            if (c instanceof CheckpointCommand)
                return true;
        }
        return false;
    }

    //Undo đến checkpoint bằng cách đẩy các command ra khỏi stack
    public void undoToCheckpoint() {
        AbstractCommand c;
        while (!mCommandStack.empty()) {
            c = mCommandStack.pop();
            c.undo();

            if (c instanceof CheckpointCommand)
                break;
        }
        validateCells();
    }

    //Kiểm tra xem giá trị của cell có đúng như giải hay không
    private boolean hasMistakes(ArrayList<int[]> finalValues) {
        for (int[] rowColVal : finalValues) {
            int row = rowColVal[0];
            int col = rowColVal[1];
            int val = rowColVal[2];
            Cell cell = mCells.getCell(row, col);

            if (cell.getValue() != val && cell.getValue() != 0) {
                return true;
            }
        }

        return false;
    }

    //Undo lại các command bị sai đến khi không còn cell bị sai nữa
    public void undoToSolvableState() {
        SudokuSolver solver = new SudokuSolver();
        solver.setPuzzle(mCells);
        ArrayList<int[]> finalValues = solver.solve();

        while (!mCommandStack.empty() && hasMistakes(finalValues)) {
            mCommandStack.pop().undo();
        }

        validateCells();
    }

    //Kiểm tra xem stack có rỗng hay không
    public boolean hasSomethingToUndo() {
        return mCommandStack.size() != 0;
    }

    //Lấy cell có thay đổi cuối cùng, dùng trong phần tiếp tục màn chơi
    public Cell getLastChangedCell() {
        ListIterator<AbstractCommand> iter = mCommandStack.listIterator(mCommandStack.size());
        while (iter.hasPrevious()) {
            AbstractCommand o = iter.previous();
            if (o instanceof AbstractSingleCellCommand) {
                return ((AbstractSingleCellCommand) o).getCell();
            } else if (o instanceof SetCellValueAndRemoveNotesCommand) {
                return ((SetCellValueAndRemoveNotesCommand) o).getCell();
            }
        }

        return null;
    }

    //Hàm dùng để thêm command vào stack
    private void push(AbstractCommand command) {
        if (command instanceof AbstractCellCommand) {
            ((AbstractCellCommand) command).setCells(mCells);
        }
        mCommandStack.push(command);
    }

    //Hàm dùng để đẩy command ra khỏi stack
    private AbstractCommand pop() {
        return mCommandStack.pop();
    }

    private void validateCells() {
        mCells.validate();
    }


}
