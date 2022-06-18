package hcmute.vtv_18110069_18110051_18110070.game_sudoku.db;

// Dùng để handle lỗi về data format của bàn chơi sudoku
public class SudokuInvalidFormatException extends Exception {

    private static final long serialVersionUID = -5415032786641425594L;

    private final String mData;

    // constructor
    public SudokuInvalidFormatException(String data) {
        super("Invalid format of sudoku.");
        mData = data;
    }

    // lấy data
    public String getData() {
        return mData;
    }

}
