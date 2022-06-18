package hcmute.vtv_18110069_18110051_18110070.game_sudoku.game.command;

import hcmute.vtv_18110069_18110051_18110070.game_sudoku.game.Cell;

import java.util.StringTokenizer;

//Dùng lệnh để xử lí bên checkpoint
public abstract class AbstractSingleCellCommand extends AbstractCellCommand {

    private int mCellRow;
    private int mCellColumn;

    public AbstractSingleCellCommand(Cell cell) {
        mCellRow = cell.getRowIndex();
        mCellColumn = cell.getColumnIndex();
    }

    AbstractSingleCellCommand() {

    }

    @Override
    public void serialize(StringBuilder data) {
        super.serialize(data);

        data.append(mCellRow).append("|");
        data.append(mCellColumn).append("|");
    }

    @Override
    protected void _deserialize(StringTokenizer data) {
        super._deserialize(data);

        mCellRow = Integer.parseInt(data.nextToken());
        mCellColumn = Integer.parseInt(data.nextToken());
    }

    public Cell getCell() {
        return getCells().getCell(mCellRow, mCellColumn);
    }

}
