package hcmute.vtv_18110069_18110051_18110070.game_sudoku.game;

import java.util.StringTokenizer;

//cell chứa thông tin về 1 ô trong sudoku
public class Cell {
    // if cell is included in collection, here are some additional information
    // about collection and cell's position in it

    //Biến lưu trữ thông tin về màn chơi bao gồm cell, vị trí của nó trong collection
    //lưu trữ các cell
    private CellCollection mCellCollection;
    private final Object mCellCollectionLock = new Object();
    private int mRowIndex = -1;
    private int mColumnIndex = -1;

    //Vị trí của cell: vùng, dòng và cột
    private CellGroup mSector; 
    private CellGroup mRow; 
    private CellGroup mColumn; 

    //các giá trị cơ bản của 1 cell: giá trị hiện tại của cell, các giá trị phỏng đoán,
    //đây là cell được điền vào hay được tạo sẵn và nó có theo đúng luật của sudoku hay không
    private int mValue;
    private CellNote mNote;
    private boolean mEditable;
    private boolean mValid;

    //Tạo 1 cell mới và có thể chỉnh sửa/điền được
    public Cell() {
        this(0, new CellNote(), true, true);
    }

    //Tạo 1 cell mới có giá trị value, chỉnh sửa được
    public Cell(int value) {
        this(value, new CellNote(), true, true);
    }

    //Đặt giá trị của 1 cell với thông tin có sẵn
    private Cell(int value, CellNote note, boolean editable, boolean valid) {
        if (value < 0 || value > 9) {
            throw new IllegalArgumentException("Value must be between 0-9.");
        }

        mValue = value;
        mNote = note;
        mEditable = editable;
        mValid = valid;
    }

    //Lấy vị trí dòng của cell trong collection chứa nó
    public int getRowIndex() {
        return mRowIndex;
    }

    //Lấy vị trí cột của cell trong collection chứa nó
    public int getColumnIndex() {
        return mColumnIndex;
    }

    //Dùng để thêm cell vào collection lưu trữ của màn chơi
    protected void initCollection(CellCollection cellCollection, int rowIndex, int colIndex,
                                  CellGroup sector, CellGroup row, CellGroup column) {
        synchronized (mCellCollectionLock) {
            mCellCollection = cellCollection;
        }

        mRowIndex = rowIndex;
        mColumnIndex = colIndex;
        mSector = sector;
        mRow = row;
        mColumn = column;

        sector.addCell(this);
        row.addCell(this);
        column.addCell(this);
    }

    //Dùng để lấy vùng chứa cell
    public CellGroup getSector() {
        return mSector;
    }

    //Dùng để lấy dòng chứa cell
    public CellGroup getRow() {
        return mRow;
    }

    //Dùng để lấy cột chứa cell
    public CellGroup getColumn() {
        return mColumn;
    }

    //Đặt giá trị cho cell, từ 0-9
    public void setValue(int value) {
        if (value < 0 || value > 9) {
            throw new IllegalArgumentException("Value must be between 0-9.");
        }
        mValue = value;
        onChange();
    }

    //Lấy giá trị từ cell, từ 0-9, cell sẽ trống khi để giá trị 0
    public int getValue() {
        return mValue;
    }


    //Lấy ghi chú/phỏng đoán của cell
    public CellNote getNote() {
        return mNote;
    }

    //Đặt ghi chú cho 1 cell
    public void setNote(CellNote note) {
        mNote = note;
        onChange();
    }

    //Trả về giá trị true hoặc false, cho biết cell có thể chỉnh sửa được hay không
    public boolean isEditable() {
        return mEditable;
    }

    //Thay đổi khả năng chỉnh sửa giá trị của 1 cell
    public void setEditable(Boolean editable) {
        mEditable = editable;
        onChange();
    }

    //Lưu biến chứa thông tin cho biết cell có theo đúng luật hay không
    public void setValid(Boolean valid) {
        mValid = valid;
        onChange();
    }

    //Trả về giá trị cho biết cell có theo đúng luật hay không
    public boolean isValid() {
        return mValid;
    }

    //Tạo các thông số đọc nhất để dùng cho phần command
    public static Cell deserialize(StringTokenizer data, int version) {
        Cell cell = new Cell();
        cell.setValue(Integer.parseInt(data.nextToken()));
        cell.setNote(CellNote.deserialize(data.nextToken(), version));
        cell.setEditable(data.nextToken().equals("1"));

        return cell;
    }

    public static Cell deserialize(String cellData) {
        StringTokenizer data = new StringTokenizer(cellData, "|");
        return deserialize(data, CellCollection.DATA_VERSION);
    }

    public void serialize(StringBuilder data, int dataVersion) {
        if (dataVersion == CellCollection.DATA_VERSION_PLAIN) {
            data.append(mValue);
        } else {
            data.append(mValue).append("|");
            if (mNote == null || mNote.isEmpty()) {
                data.append("0").append("|");
            } else {
                mNote.serialize(data);
            }
            data.append(mEditable ? "1" : "0").append("|");
        }
    }
    public void serialize(StringBuilder data) {
        data.append(mValue).append("|");
        if (mNote == null || mNote.isEmpty()) {
            data.append("0").append("|");
        } else {
            mNote.serialize(data);
        }
        data.append(mEditable ? "1" : "0").append("|");
    }

    public String serialize() {
        StringBuilder sb = new StringBuilder();
        serialize(sb);
        return sb.toString();
    }

    //Để thông báo rằng có gì đó đã thay đổi
    private void onChange() {
        synchronized (mCellCollectionLock) {
            if (mCellCollection != null) {
                mCellCollection.onChange();
            }

        }
    }
}