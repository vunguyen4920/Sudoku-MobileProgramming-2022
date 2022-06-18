package hcmute.vtv_18110069_18110051_18110070.game_sudoku.game.command;

import hcmute.vtv_18110069_18110051_18110070.game_sudoku.game.Cell;

import java.util.StringTokenizer;

//Các lệnh dùng để lấy giá trị từ cell, tiện cho việc undo
public class SetCellValueCommand extends AbstractSingleCellCommand {

    private int mValue;
    private int mOldValue;

    //Lệnh để xét giá trị cho cell
    public SetCellValueCommand(Cell cell, int value) {
        super(cell);
        mValue = value;
    }

    SetCellValueCommand() {
    }
    //Hàm để xử lí độc nhất hóa
    @Override
    public void serialize(StringBuilder data) {
        super.serialize(data);

        data.append(mValue).append("|");
        data.append(mOldValue).append("|");
    }

    @Override
    protected void _deserialize(StringTokenizer data) {
        super._deserialize(data);

        mValue = Integer.parseInt(data.nextToken());
        mOldValue = Integer.parseInt(data.nextToken());
    }
    //Hàm để thực thi lệnh, và lưu lại giá trị cũ
    @Override
    void execute() {
        Cell cell = getCell();
        mOldValue = cell.getValue();
        cell.setValue(mValue);
    }
    //Undo lại lệnh
    @Override
    void undo() {
        Cell cell = getCell();
        cell.setValue(mOldValue);
    }

}
