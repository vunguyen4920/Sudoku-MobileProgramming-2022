package hcmute.vtv_18110069_18110051_18110070.game_sudoku.game.command;

import hcmute.vtv_18110069_18110051_18110070.game_sudoku.game.Cell;
import hcmute.vtv_18110069_18110051_18110070.game_sudoku.game.CellCollection;
import hcmute.vtv_18110069_18110051_18110070.game_sudoku.game.CellNote;

//Câu lệnh để xóa hết các ghi chú, tiện cho việc undo
public class ClearAllNotesCommand extends AbstractMultiNoteCommand {

    public ClearAllNotesCommand() {
    }
    //Command dùng để xóa hết các ghi chú
    @Override
    void execute() {
        CellCollection cells = getCells();

        mOldNotes.clear();
        for (int r = 0; r < CellCollection.SUDOKU_SIZE; r++) {
            for (int c = 0; c < CellCollection.SUDOKU_SIZE; c++) {
                Cell cell = cells.getCell(r, c);
                CellNote note = cell.getNote();
                if (!note.isEmpty()) {
                    mOldNotes.add(new NoteEntry(r, c, note));
                    cell.setNote(new CellNote());
                }
            }
        }
    }
}
