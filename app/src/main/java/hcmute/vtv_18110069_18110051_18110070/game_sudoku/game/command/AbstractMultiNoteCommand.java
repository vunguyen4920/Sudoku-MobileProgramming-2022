package hcmute.vtv_18110069_18110051_18110070.game_sudoku.game.command;

import hcmute.vtv_18110069_18110051_18110070.game_sudoku.game.CellCollection;
import hcmute.vtv_18110069_18110051_18110070.game_sudoku.game.CellNote;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

//Lệnh dùng cho nhiều ghi chú cho colection
public abstract class AbstractMultiNoteCommand extends AbstractCellCommand {

    protected List<NoteEntry> mOldNotes = new ArrayList<>();

    //Hàm để đọc giá trị/hoặc độc nhất hóa
    @Override
    public void serialize(StringBuilder data) {
        super.serialize(data);

        data.append(mOldNotes.size()).append("|");

        for (NoteEntry ne : mOldNotes) {
            data.append(ne.rowIndex).append("|");
            data.append(ne.colIndex).append("|");
            ne.note.serialize(data);
        }
    }

    @Override
    protected void _deserialize(StringTokenizer data) {
        super._deserialize(data);

        int notesSize = Integer.parseInt(data.nextToken());
        for (int i = 0; i < notesSize; i++) {
            int row = Integer.parseInt(data.nextToken());
            int col = Integer.parseInt(data.nextToken());

            mOldNotes.add(new NoteEntry(row, col, CellNote.deserialize(data.nextToken())));
        }
    }

    ////Để undo lại lệnh
    @Override
    void undo() {
        CellCollection cells = getCells();

        for (NoteEntry ne : mOldNotes) {
            cells.getCell(ne.rowIndex, ne.colIndex).setNote(ne.note);
        }
    }

    //Lưu lại các ghi chú để có cần thiết cho undo
    protected void saveOldNotes() {
        CellCollection cells = getCells();
        for (int r = 0; r < CellCollection.SUDOKU_SIZE; r++) {
            for (int c = 0; c < CellCollection.SUDOKU_SIZE; c++) {
                mOldNotes.add(new NoteEntry(r, c, cells.getCell(r, c).getNote()));
            }
        }
    }
    
    protected static class NoteEntry {
        public int rowIndex;
        public int colIndex;
        public CellNote note;

        public NoteEntry(int rowIndex, int colIndex, CellNote note) {
            this.rowIndex = rowIndex;
            this.colIndex = colIndex;
            this.note = note;
        }
    }
}
