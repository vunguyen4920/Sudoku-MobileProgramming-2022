package hcmute.vtv_18110069_18110051_18110070.game_sudoku.game;

import java.util.HashMap;
import java.util.Map;


 //Tượng trưng cho mỗi nhóm của sudoku, trong nhóm thì các giá trị là duy nhất
 //để xác định cho cột, dòng hay theo vùng
public class CellGroup {
    private Cell[] mCells = new Cell[CellCollection.SUDOKU_SIZE];
    private int mPos = 0;

    public void addCell(Cell cell) {
        mCells[mPos] = cell;
        mPos++;
    }


     //Dùng để kiểm tra các giá trị trong group có duy nhất không,
     //nếu có thì sẽ chuyển giá trị đó là không phạm quy
     //Hàm được sử dụng sau khi xét tất cả cell là phạm quy ở collection
    protected boolean validate() {
        boolean valid = true;

        Map<Integer, Cell> cellsByValue = new HashMap<>();
        for (Cell cell : mCells) {
            int value = cell.getValue();
            if (cellsByValue.get(value) != null) {
                cell.setValid(false);
                cellsByValue.get(value).setValid(false);
                valid = false;
            } else {
                cellsByValue.put(value, cell);
            }
        }

        return valid;
    }

    //Kiểm tra xem trong nhóm có giá trị nào đó chưa
    public boolean DoesntContain(int value) {
        for (Cell mCell : mCells) {
            if (mCell.getValue() == value) {
                return false;
            }
        }
        return true;
    }

    public Cell[] getCells() {
        return mCells;
    }
}
