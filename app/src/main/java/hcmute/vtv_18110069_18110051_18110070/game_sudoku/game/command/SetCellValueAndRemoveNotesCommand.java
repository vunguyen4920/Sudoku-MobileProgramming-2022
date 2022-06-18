package hcmute.vtv_18110069_18110051_18110070.game_sudoku.game.command;

import hcmute.vtv_18110069_18110051_18110070.game_sudoku.game.Cell;

import java.util.StringTokenizer;

//lệnh dùng để đặt giá trị cho cell và xóa command của nó
public class SetCellValueAndRemoveNotesCommand extends AbstractMultiNoteCommand {

    private int mCellRow;
    private int mCellColumn;
    private int mValue;
    private int mOldValue;
    public SetCellValueAndRemoveNotesCommand(Cell cell, int value) {
        mCellRow = cell.getRowIndex();
        mCellColumn = cell.getColumnIndex();
        mValue = value;
    }

    SetCellValueAndRemoveNotesCommand() {
    }
    //Hàm để lấy cell
    public Cell getCell() {
        return getCells().getCell(mCellRow, mCellColumn);
    }
    //Hàm để xử lí độc nhất hóa
    @Override
    public void serialize(StringBuilder data) {
        super.serialize(data);

        data.append(mCellRow).append("|");
        data.append(mCellColumn).append("|");
        data.append(mValue).append("|");
        data.append(mOldValue).append("|");
    }

    @Override
    protected void _deserialize(StringTokenizer data) {
        super._deserialize(data);

        mCellRow = Integer.parseInt(data.nextToken());
        mCellColumn = Integer.parseInt(data.nextToken());
        mValue = Integer.parseInt(data.nextToken());
        mOldValue = Integer.parseInt(data.nextToken());
    }
    //Thực thi lệnh và lưu lại giá trị cell cũ
    @Override
    void execute() {
        mOldNotes.clear();
        saveOldNotes();

        Cell cell = getCell();
        getCells().removeNotesForChangedCell(cell, mValue);
        mOldValue = cell.getValue();
        cell.setValue(mValue);
    }
    //Undo lại lệnh và thêm giá trị cũ lại
    @Override
    void undo() {
        super.undo();
        Cell cell = getCell();
        cell.setValue(mOldValue);
    }
}
