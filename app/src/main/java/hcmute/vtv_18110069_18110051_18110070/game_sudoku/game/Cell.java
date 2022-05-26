package hcmute.vtv_18110069_18110051_18110070.game_sudoku.game;

import java.util.StringTokenizer;

/**
 * Sudoku cell. Every cell has value, some notes attached to it and some basic
 * state (whether it is editable and valid).
 *
 * @author romario
 */
public class Cell {
    private final Object mCellCollectionLock = new Object();
    // if cell is included in collection, here are some additional information
    // about collection and cell's position in it
    private CellCollection mCellCollection;
    private int mRowIndex = -1;
    private int mColumnIndex = -1;
    private CellGroup mSector; // sector containing this cell
    private CellGroup mRow; // row containing this cell
    private CellGroup mColumn; // column containing this cell

    private int mValue;
    private CellNote mNote;
    private boolean mEditable;
    private boolean mValid;

    /**
     * Creates empty editable cell.
     */
    public Cell() {
        this(0, new CellNote(), true, true);
    }

    /**
     * Creates empty editable cell containing given value.
     *
     * @param value Value of the cell.
     */
    public Cell(int value) {
        this(value, new CellNote(), true, true);
    }

    private Cell(int value, CellNote note, boolean editable, boolean valid) {
        if (value < 0 || value > 9) {
            throw new IllegalArgumentException("Value must be between 0-9.");
        }

        mValue = value;
        mNote = note;
        mEditable = editable;
        mValid = valid;
    }

    /**
     * Creates instance from given <code>StringTokenizer</code>.
     *
     * @param data
     * @return
     */
    public static Cell deserialize(StringTokenizer data, int version) {
        Cell cell = new Cell();
        cell.setValue(Integer.parseInt(data.nextToken()));
        cell.setNote(CellNote.deserialize(data.nextToken(), version));
        cell.setEditable(data.nextToken().equals("1"));

        return cell;
    }

    /**
     * Creates instance from given string (string which has been
     * created by {@link #serialize(StringBuilder)} or {@link #serialize()} method).
     * earlier.
     *
     * @param cellData
     */
    public static Cell deserialize(String cellData) {
        StringTokenizer data = new StringTokenizer(cellData, "|");
        return deserialize(data, CellCollection.DATA_VERSION);
    }

    /**
     * Gets cell's row index within {@link CellCollection}.
     *
     * @return Cell's row index within CellCollection.
     */
    public int getRowIndex() {
        return mRowIndex;
    }

    /**
     * Gets cell's column index within {@link CellCollection}.
     *
     * @return Cell's column index within CellCollection.
     */
    public int getColumnIndex() {
        return mColumnIndex;
    }

    /**
     * Called when <code>Cell</code> is added to {@link CellCollection}.
     *
     * @param rowIndex Cell's row index within collection.
     * @param colIndex Cell's column index within collection.
     * @param sector   Reference to sector group in which cell is included.
     * @param row      Reference to row group in which cell is included.
     * @param column   Reference to column group in which cell is included.
     */
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

    /**
     * Returns sector containing this cell. Sector is 3x3 group of cells.
     *
     * @return Sector containing this cell.
     */
    public CellGroup getSector() {
        return mSector;
    }

    /**
     * Returns row containing this cell.
     *
     * @return Row containing this cell.
     */
    public CellGroup getRow() {
        return mRow;
    }

    /**
     * Returns column containing this cell.
     *
     * @return Column containing this cell.
     */
    public CellGroup getColumn() {
        return mColumn;
    }

    /**
     * Gets cell's value. Value can be 1-9 or 0 if cell is empty.
     *
     * @return Cell's value. Value can be 1-9 or 0 if cell is empty.
     */
    public int getValue() {
        return mValue;
    }

    /**
     * Sets cell's value. Value can be 1-9 or 0 if cell should be empty.
     *
     * @param value 1-9 or 0 if cell should be empty.
     */
    public void setValue(int value) {
        if (value < 0 || value > 9) {
            throw new IllegalArgumentException("Value must be between 0-9.");
        }
        mValue = value;
        onChange();
    }

    /**
     * Gets note attached to the cell.
     *
     * @return Note attached to the cell.
     */
    public CellNote getNote() {
        return mNote;
    }

    /**
     * Sets note attached to the cell
     *
     * @param note Note attached to the cell
     */
    public void setNote(CellNote note) {
        mNote = note;
        onChange();
    }

    /**
     * Returns whether cell can be edited.
     *
     * @return True if cell can be edited.
     */
    public boolean isEditable() {
        return mEditable;
    }

    /**
     * Sets whether cell can be edited.
     *
     * @param editable True, if cell should allow editing.
     */
    public void setEditable(Boolean editable) {
        mEditable = editable;
        onChange();
    }

    /**
     * Returns true, if cell contains valid value according to sudoku rules.
     *
     * @return True, if cell contains valid value according to sudoku rules.
     */
    public boolean isValid() {
        return mValid;
    }

    /**
     * Sets whether cell contains valid value according to sudoku rules.
     *
     * @param valid
     */
    public void setValid(Boolean valid) {
        mValid = valid;
        onChange();
    }

    /**
     * Appends string representation of this object to the given <code>StringBuilder</code>
     * in a given data format version.
     * You can later recreate object from this string by calling {@link #deserialize}.
     *
     * @see CellCollection#serialize(StringBuilder, int) for supported data format versions.
     *
     * @param data A <code>StringBuilder</code> where to write data.
     */
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

    /**
     * Returns a string representation of this object in a default data format version.
     *
     * @see #serialize(StringBuilder, int)
     *
     * @return A string representation of this object.
     */
    public String serialize() {
        StringBuilder sb = new StringBuilder();
        serialize(sb, CellCollection.DATA_VERSION);
        return sb.toString();
    }

    /**
     * Returns a string representation of this object in a given data format version.
     *
     * @see #serialize(StringBuilder, int)
     *
     * @param dataVersion A version of data format.
     * @return A string representation of this object.
     */
    public String serialize(int dataVersion) {
        StringBuilder sb = new StringBuilder();
        serialize(sb, dataVersion);
        return sb.toString();
    }

    /**
     * Notify CellCollection that something has changed.
     */
    private void onChange() {
        synchronized (mCellCollectionLock) {
            if (mCellCollection != null) {
                mCellCollection.onChange();
            }

        }
    }
}
