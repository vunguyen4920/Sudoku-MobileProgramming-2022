package hcmute.vtv_18110069_18110051_18110070.game_sudoku.game.command;

import hcmute.vtv_18110069_18110051_18110070.game_sudoku.game.CellCollection;

//Các lệnh dùng cho lưu lại ghi chú cũ khi đổi giá trị của cell, tiện cho việc undo
public class FillInNotesCommand extends AbstractMultiNoteCommand {

    public FillInNotesCommand() {
    }

    @Override
    void execute() {
        CellCollection cells = getCells();

        mOldNotes.clear();
        saveOldNotes();

        cells.fillInNotes();
    }
}
