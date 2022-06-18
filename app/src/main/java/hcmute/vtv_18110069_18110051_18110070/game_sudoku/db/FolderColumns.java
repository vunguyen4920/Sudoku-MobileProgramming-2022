package hcmute.vtv_18110069_18110051_18110070.game_sudoku.db;

import android.provider.BaseColumns;

public abstract class FolderColumns implements BaseColumns {
    // tên thư mục tương đương với độ khó của game (easy, medium, hard)
    public static final String NAME = "name";
    // tạo cờ xác định đã tạo hay chưa
    public static final String CREATED = "created";
}
