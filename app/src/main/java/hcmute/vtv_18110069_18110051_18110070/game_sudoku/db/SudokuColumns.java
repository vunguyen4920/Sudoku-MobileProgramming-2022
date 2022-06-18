package hcmute.vtv_18110069_18110051_18110070.game_sudoku.db;

import android.provider.BaseColumns;

public abstract class SudokuColumns implements BaseColumns {
    // ID thư mụuc
    public static final String FOLDER_ID = "folder_id";
    // tạo cờ xác định level đã tạo hay chưa
    public static final String CREATED = "created";
    // tạo cờ xác định đã tạo hay chưa
    public static final String STATE = "state";
    // thời gian cho bàn chơi
    public static final String TIME = "time";
    // dữ liệu của lần chơi trước
    public static final String LAST_PLAYED = "last_played";
    // thông tin ban đầu của bàn chơi
    public static final String DATA = "data";
    // chú thích cho bàn chơi
    public static final String PUZZLE_NOTE = "puzzle_note";
    // dùng để lưu lệnh command
    public static final String COMMAND_STACK = "command_stack";
}
