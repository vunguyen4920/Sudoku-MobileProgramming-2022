package hcmute.vtv_18110069_18110051_18110070.game_sudoku.game.command;

import hcmute.vtv_18110069_18110051_18110070.game_sudoku.game.CellCollection;

//Lệnh dùng để điền 1-9 vào ghi chú của mọi cell
public class FillInNotesWithAllValuesCommand extends AbstractMultiNoteCommand {

    public FillInNotesWithAllValuesCommand() {
    }

    @Override
    void execute() {
        CellCollection cells = getCells();

        mOldNotes.clear();
        saveOldNotes();

        cells.fillInNotesWithAllValues();
    }
}
