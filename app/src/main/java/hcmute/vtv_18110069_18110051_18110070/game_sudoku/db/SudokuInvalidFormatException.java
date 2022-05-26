package hcmute.vtv_18110069_18110051_18110070.game_sudoku.db;

public class SudokuInvalidFormatException extends Exception {

    private static final long serialVersionUID = -5415032786641425594L;

    private final String mData;

    public SudokuInvalidFormatException(String data) {
        super("Invalid format of sudoku.");
        mData = data;
    }

    public String getData() {
        return mData;
    }

}
