package hcmute.vtv_18110069_18110051_18110070.game_sudoku.game.command;

import hcmute.vtv_18110069_18110051_18110070.game_sudoku.game.Cell;
import hcmute.vtv_18110069_18110051_18110070.game_sudoku.game.CellNote;

import java.util.StringTokenizer;

//Các lệnh dùng để chỉnh sửa ghi chú, tiện cho việc undo
public class EditCellNoteCommand extends AbstractSingleCellCommand {

    private CellNote mNote;
    private CellNote mOldNote;
    //Lệnh dùng để chỉnh sửa ghi chú
    public EditCellNoteCommand(Cell cell, CellNote note) {
        super(cell);
        mNote = note;
    }

    EditCellNoteCommand() {

    }
    //Hàm để xử lí độc nhất hóa
    @Override
    public void serialize(StringBuilder data) {
        super.serialize(data);

        mNote.serialize(data);
        mOldNote.serialize(data);
    }

    @Override
    protected void _deserialize(StringTokenizer data) {
        super._deserialize(data);

        mNote = CellNote.deserialize(data.nextToken());
        mOldNote = CellNote.deserialize(data.nextToken());
    }
    //Hàm để thực thi lệnh
    @Override
    void execute() {
        Cell cell = getCell();
        mOldNote = cell.getNote();
        cell.setNote(mNote);
    }
    //Hàm để undo lại cùng với giá trị ghi chú cũ
    @Override
    void undo() {
        Cell cell = getCell();
        cell.setNote(mOldNote);
    }

}
