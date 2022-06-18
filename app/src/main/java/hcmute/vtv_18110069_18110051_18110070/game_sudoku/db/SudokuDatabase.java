package hcmute.vtv_18110069_18110051_18110070.game_sudoku.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;

import hcmute.vtv_18110069_18110051_18110070.game_sudoku.game.CellCollection;
import hcmute.vtv_18110069_18110051_18110070.game_sudoku.game.FolderInfo;
import hcmute.vtv_18110069_18110051_18110070.game_sudoku.game.SudokuGame;
import hcmute.vtv_18110069_18110051_18110070.game_sudoku.game.command.CommandStack;
import hcmute.vtv_18110069_18110051_18110070.game_sudoku.gui.SudokuListFilter;
import hcmute.vtv_18110069_18110051_18110070.game_sudoku.gui.SudokuListSorter;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Wrapper around sudoku's database.
 * <p/>
 * You have to pass application context when creating instance:
 * <code>SudokuDatabase db = new SudokuDatabase(getApplicationContext());</code>
 * <p/>
 * You have to explicitly close connection when you're done with database (see
 * {@link #close()}).
 * <p/>
 * This class supports database transactions using {@link #beginTransaction()},
 * \
 * {@link #setTransactionSuccessful()} and {@link #endTransaction()}.
 * See {@link SQLiteDatabase} for details on how to use them.
 *
 */
public class SudokuDatabase {
    // định nghĩa các biến cần có cho database
    public static final String DATABASE_NAME = "susudodokuku";
    public static final String SUDOKU_TABLE_NAME = "sudoku";
    public static final String FOLDER_TABLE_NAME = "folder";
    private static final String INBOX_FOLDER_NAME = "Inbox";

    // khai báo liên quan đến database
    private DatabaseHelper mOpenHelper;
    private SQLiteStatement mInsertSudokuStatement;

    // constructor
    public SudokuDatabase(Context context) {
        mOpenHelper = new DatabaseHelper(context);
    }

    // lấy folder list hay lấy danh sách bàn chơi theo độ khó
    public Cursor getFolderList() {
        // khởi tạo query builder
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        // set table name cho query
        qb.setTables(FOLDER_TABLE_NAME);

        // khởi tạo database để lấy thông tin trong database
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        // trả về con trỏ chứ thực thi query với lệnh sql và sort theo created column
        // theo tăng dần
        return qb.query(db, null, null, null, null, null, "created ASC");
    }

    // dùng để lấy thông tin các bàn chơi có trong folder
    public FolderInfo getFolderInfo(long folderID) {
        // khởi tạo query builder
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        // set table name cho query
        qb.setTables(FOLDER_TABLE_NAME);

        // thêm lệnh where cho query
        qb.appendWhere(FolderColumns._ID + "=" + folderID);

        // khởi tạo sqlitedatabase để lấy thông tin trong database
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        // lệnh try catch để thực thi query
        // chạy lệnh query với lệnh sql qb
        try (Cursor c = qb.query(db, null, null,
                null, null, null, null)) {

            // khi câu lệnh sql được thực thi và con trỏ nhận được
            // check con trỏ có thể di chuyển đầu tiên
            if (c.moveToFirst()) {
                // lấy thông tin từ con trỏ
                long id = c.getLong(c.getColumnIndex(FolderColumns._ID));
                String name = c.getString(c.getColumnIndex(FolderColumns.NAME));

                // khai báo forderInfo và gắn thông tin vào
                FolderInfo folderInfo = new FolderInfo();
                folderInfo.id = id;
                folderInfo.name = name;

                // trả folderInfo
                return folderInfo;
            } else {
                // trả null khi con trỏ không trả về vị trí đầu tiên được
                return null;
            }
        }
    }

    // dùng để lấy thông tin đầy đủ các bàn chơi có trong folder
    public FolderInfo getFolderInfoFull(long folderID) {
        // khai báo folderInfo null
        FolderInfo folder = null;

        // ghi câu lệnh sql để thực hiện query
        String q = "select folder._id as _id, folder.name as name, sudoku.state as state, count(sudoku.state) as count from folder left join sudoku on folder._id = sudoku.folder_id where folder._id = "
                + folderID + " group by sudoku.state";

        // khởi tạo sqlitedatabase để lấy thông tin trong database
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        // thực thi câu lệnh sql trả kết quả vào con trỏ
        try (Cursor c = db.rawQuery(q, null)) {

            // check xem con trỏ di chuyển được nữa hay không
            while (c.moveToNext()) {
                // lấy thông tin từ con trỏ
                long id = c.getLong(c.getColumnIndex(FolderColumns._ID));
                String name = c.getString(c.getColumnIndex(FolderColumns.NAME));
                int state = c.getInt(c.getColumnIndex(SudokuColumns.STATE));
                int count = c.getInt(c.getColumnIndex("count"));

                // nếu folder còn là null thì tạo lại folderInfo mới
                if (folder == null) {
                    folder = new FolderInfo(id, name);
                }

                // đếm số bàn đang ở trạng thái đã hoàn thành
                folder.puzzleCount += count;
                if (state == SudokuGame.GAME_STATE_COMPLETED) {
                    folder.solvedCount += count;
                }
                // đếm số bàn đang ở trạng thái đang chơi
                if (state == SudokuGame.GAME_STATE_PLAYING) {
                    folder.playingCount += count;
                }
            }
        }
        // trả giá trị folderInfo
        return folder;
    }

    // dùng để trả lại folder tên Inbox
    public FolderInfo getInboxFolder() {
        // tìm folder theo tên (ở đây là "Inbox")
        FolderInfo inbox = findFolder(INBOX_FOLDER_NAME);
        // nếu folder không null thì insert vào FolderInfo để trả vèe
        if (inbox != null) {
            inbox = insertFolder(INBOX_FOLDER_NAME, System.currentTimeMillis());
        }
        return inbox;
    }

    // dùng để trả lại folder theo tên nhập vào
    public FolderInfo findFolder(String folderName) {
        // khởi tạo query builder

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        // set table name cho query
        qb.setTables(FOLDER_TABLE_NAME);

        // thêm lệnh where cho query
        qb.appendWhere(FolderColumns.NAME + " = ?");

        // khởi tạo sqlitedatabase để lấy thông tin trong database
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        // trả về con trỏ chứ thực thi query với lệnh sql qb và với selectionArg là
        // folderName
        try (Cursor c = qb.query(db, null, null,
                new String[] { folderName }, null, null, null)) {

            // khi câu lệnh sql được thực thi và con trỏ nhận được
            // check con trỏ có thể di chuyển đầu tiên
            if (c.moveToFirst()) {
                // lấy thông tin từ con trỏ
                long id = c.getLong(c.getColumnIndex(FolderColumns._ID));
                String name = c.getString(c.getColumnIndex(FolderColumns.NAME));

                // khai báo forderInfo và gắn thông tin vào
                FolderInfo folderInfo = new FolderInfo();
                folderInfo.id = id;
                folderInfo.name = name;

                // trả folderInfo
                return folderInfo;
            } else {
                // trả null khi con trỏ không trả về vị trí đầu tiên được
                return null;
            }
        }
    }

    // dùng để insert folder bàn chơi
    public FolderInfo insertFolder(String name, Long created) {
        // khai báo contentValue để insert vào database
        ContentValues values = new ContentValues();
        values.put(FolderColumns.CREATED, created);
        values.put(FolderColumns.NAME, name);

        // khai báo rowId để tìm dòng data được insert vào database
        long rowId;

        // khởi tạo sqlitedatabase để viết và thực thi query vào database
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        // sau khi thực thi query thì gắn id của dòng vừa được insert vào rowId
        rowId = db.insert(FOLDER_TABLE_NAME, FolderColumns._ID, values);

        // rowId có giá trị xác thực
        if (rowId > 0) {
            // khai báo folder info và gắn giá trị của rowId và name vào folderInfo đó
            FolderInfo fi = new FolderInfo();
            fi.id = rowId;
            fi.name = name;
            // trả lại thông tin folderinfo vừa được insert
            return fi;
        }
        // nếu câu lệnh sql lỗi, trả về SQLexception
        throw new SQLException(String.format("Failed to insert folder '%s'.", name));
    }

    // dùng để cập nhật thông tin folder theo id
    public void updateFolder(long folderID, String name) {
        // khai báo contentValue
        ContentValues values = new ContentValues();

        // bỏ name vào value
        values.put(FolderColumns.NAME, name);

        // khởi tạo sqlitedatabase để viết và thực thi query vào database
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        // thực thi query với method update với FOLDER_TABLE_NAME, value được put ở
        // trên, tại id folderID
        db.update(FOLDER_TABLE_NAME, values, FolderColumns._ID + "=" + folderID, null);
    }

    // dùng để xóa folder theo id
    public void deleteFolder(long folderID) {

        // khởi tạo sqlitedatabase để viết và thực thi query vào database
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        // thực thi việc xóa các bàn chơi trong thư mục
        db.delete(SUDOKU_TABLE_NAME, SudokuColumns.FOLDER_ID + "=" + folderID, null);
        // thực thi việc xóa thư mục
        db.delete(FOLDER_TABLE_NAME, FolderColumns._ID + "=" + folderID, null);
    }

    // dùng để lấy bàn chơi từ thư mục theo id, listFilter, listSort(created, name,
    // by_last_played, v.v... )
    public Cursor getSudokuList(long folderID, SudokuListFilter filter, SudokuListSorter sorter) {
        // khởi tạo query builder
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        // set table name cho query
        qb.setTables(SUDOKU_TABLE_NAME);

        // thêm lệnh where cho query
        qb.appendWhere(SudokuColumns.FOLDER_ID + "=" + folderID);

        // nếu listfilter khác null
        if (filter != null) {
            // nếu state completed không được chọn
            if (!filter.showStateCompleted) {
                // gắn thêm vào query điều kiện state != complete
                qb.appendWhere(" and " + SudokuColumns.STATE + "!=" + SudokuGame.GAME_STATE_COMPLETED);
            }
            // nếu state not_started không được chọn
            if (!filter.showStateNotStarted) {
                // gắn thêm vào query điều kiện state != not_started
                qb.appendWhere(" and " + SudokuColumns.STATE + "!=" + SudokuGame.GAME_STATE_NOT_STARTED);
            }
            // nếu state completed không được chọn

            if (!filter.showStatePlaying) {
                // gắn thêm vào query điều kiện state != playing
                qb.appendWhere(" and " + SudokuColumns.STATE + "!=" + SudokuGame.GAME_STATE_PLAYING);
            }
        }

        // khởi tạo sqlitedatabase để lấy thông tin trong database
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        // trả về con trỏ chứ thực thi query qb với lệnh sql và sort theo sorter ở trên
        return qb.query(db, null, null, null,
                null, null, sorter.getSortOrder());
    }

    // dùng để trả về bàn chơi sudoku từ các dòng data lưu trong database theo
    // folderId và sorter
    public List<SudokuGame> getAllSudokuByFolder(long folderID, SudokuListSorter sorter) {
        // khai báo con trỏ với việc lấy sudoku list theo folder và sort
        Cursor cursor = getSudokuList(folderID, null, sorter);

        // khi câu lệnh sql được thực thi và con trỏ nhận được
        // check con trỏ có thể di chuyển đầu tiên
        if (cursor.moveToFirst()) {
            // khai báo list sudokulist là linkedList
            List<SudokuGame> sudokuList = new LinkedList<>();
            // vòng lặp với điều kiện con trỏ chưa cuối
            while (!cursor.isAfterLast()) {
                // add bàn sudoku được xuất ra bản từ dòng data lấy từ con trỏ vào sudokuList
                sudokuList.add(extractSudokuGameFromCursorRow(cursor));
                // di chuyển con trỏ
                cursor.moveToNext();
            }
            // trả về sudokuList
            return sudokuList;
        }
        // nếu con trỏ di về đầu tiên được (con trỏ rỗng) trả về collection rỗng
        return Collections.emptyList();
    }

    // dùng để lấy bàn chơi sudoku từ id
    public SudokuGame getSudoku(long sudokuID) {
        // khởi tạo query builder
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        // set table name cho query
        qb.setTables(SUDOKU_TABLE_NAME);

        // thêm lệnh where cho query
        qb.appendWhere(SudokuColumns._ID + "=" + sudokuID);

        // khởi tạo sqlitedatabase để lấy thông tin trong database
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        // khai báo sudoku s
        SudokuGame s = null;

        // trả về con trỏ chứ thực thi query với lệnh sql qb
        try (Cursor c = qb.query(db, null, null,
                null, null, null, null)) {

            // khi câu lệnh sql được thực thi và con trỏ nhận được
            // check con trỏ có thể di chuyển đầu tiên
            if (c.moveToFirst()) {
                // // add bàn sudoku được xuất ra bản từ dòng data lấy từ con trỏ vào s
                s = extractSudokuGameFromCursorRow(c);
            }
        }
        // trả về s
        return s;
    }

    // dùng để trả về bàn sudoku từ dòng data từ con trỏ
    private SudokuGame extractSudokuGameFromCursorRow(Cursor cursor) {
        // lấy thông tin từ con trỏ
        long id = cursor.getLong(cursor.getColumnIndex(SudokuColumns._ID));
        long created = cursor.getLong(cursor.getColumnIndex(SudokuColumns.CREATED));
        String data = cursor.getString(cursor.getColumnIndex(SudokuColumns.DATA));
        long lastPlayed = cursor.getLong(cursor.getColumnIndex(SudokuColumns.LAST_PLAYED));
        int state = cursor.getInt(cursor.getColumnIndex(SudokuColumns.STATE));
        long time = cursor.getLong(cursor.getColumnIndex(SudokuColumns.TIME));
        String note = cursor.getString(cursor.getColumnIndex(SudokuColumns.PUZZLE_NOTE));

        // khai báo sudoku
        SudokuGame sudoku = new SudokuGame();
        // gắn data từ con trỏ vào sudoku
        sudoku.setId(id);
        sudoku.setCreated(created);
        sudoku.setCells(CellCollection.deserialize(data));
        sudoku.setLastPlayed(lastPlayed);
        sudoku.setState(state);
        sudoku.setTime(time);
        sudoku.setNote(note);

        // kiểm tra xem bàn chơi có đang ở state playing hay không
        // nếu có
        if (sudoku.getState() == SudokuGame.GAME_STATE_PLAYING) {
            // khai báo command_stack và gắn giá trị từ con trỏ vào
            String command_stack = cursor.getString(cursor.getColumnIndex(SudokuColumns.COMMAND_STACK));
            // kiểm tra command_stack có rỗng hay không
            // nếu không
            if (command_stack != null && !command_stack.equals("")) {
                // set command_stack vào sudoku
                sudoku.setCommandStack(CommandStack.deserialize(command_stack, sudoku.getCells()));
            }
        }
        // trả về sudoku
        return sudoku;
    }

    // dùng để add sudoku mới vào database theo folderid
    public long insertSudoku(long folderID, SudokuGame sudoku) {
        // khởi tạo sqlitedatabase để viết và thực thi query vào database
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        // khai báo contentValue và put dữ liệu vào contentValue
        ContentValues values = new ContentValues();
        values.put(SudokuColumns.DATA, sudoku.getCells().serialize());
        values.put(SudokuColumns.CREATED, sudoku.getCreated());
        values.put(SudokuColumns.LAST_PLAYED, sudoku.getLastPlayed());
        values.put(SudokuColumns.STATE, sudoku.getState());
        values.put(SudokuColumns.TIME, sudoku.getTime());
        values.put(SudokuColumns.PUZZLE_NOTE, sudoku.getNote());
        values.put(SudokuColumns.FOLDER_ID, folderID);

        // khai báo command_stack là rỗng
        String command_stack = "";
        // kiểm trả xem state có phải là playing hay không
        // nếu có
        if (sudoku.getState() == SudokuGame.GAME_STATE_PLAYING) {
            // gắn command_stack
            command_stack = sudoku.getCommandStack().serialize();
        }
        // put command_stacl
        values.put(SudokuColumns.COMMAND_STACK, command_stack);

        // khai báo rowId và sau khi thực thi query thì gắn id của dòng vừa được insert
        // vào rowId
        long rowId = db.insert(SUDOKU_TABLE_NAME, FolderColumns.NAME, values);

        // rowId có giá trị xác thực
        if (rowId > 0) {
            // trả lại thông tin rowId vừa được insert
            return rowId;
        }
        // nếu câu lệnh sql lỗi, trả về SQLexception
        throw new SQLException("Failed to insert sudoku.");
    }

    // dùng để import bàn chơi sudoku từ param trong thiết bị
    public long importSudoku(long folderID, SudokuImportParams pars) throws SudokuInvalidFormatException {
        // nếu data trong param rỗng
        if (pars.data == null) {
            // trả về SudokuInvalidFormatException
            throw new SudokuInvalidFormatException(null);
        }

        // nếu param không phù hợp với CellCollection
        if (!CellCollection.isValid(pars.data)) {
            // trả về SudokuInvalidFormatException
            throw new SudokuInvalidFormatException(pars.data);
        }

        // kiểm tra nếu insertsqlstatement có null hay không
        // nếu null
        if (mInsertSudokuStatement == null) {
            // khởi tạo sqlitedatabase để viết và thực thi query vào database
            SQLiteDatabase db = mOpenHelper.getWritableDatabase();
            // biên dịch lệnh query
            mInsertSudokuStatement = db.compileStatement(
                    "insert into sudoku (folder_id, created, state, time, last_played, data, puzzle_note, command_stack) values (?, ?, ?, ?, ?, ?, ?, ?)");
        }

        // binding giá trị (1->6) từ param vào statement
        mInsertSudokuStatement.bindLong(1, folderID);
        mInsertSudokuStatement.bindLong(2, pars.created);
        mInsertSudokuStatement.bindLong(3, pars.state);
        mInsertSudokuStatement.bindLong(4, pars.time);
        mInsertSudokuStatement.bindLong(5, pars.lastPlayed);
        mInsertSudokuStatement.bindString(6, pars.data);
        // nếu note == null
        // nếu null
        if (pars.note == null) {
            // bind giá trị thứ 7 là null
            mInsertSudokuStatement.bindNull(7);
        } else {
            // bind giá trị thứ 7 là note
            mInsertSudokuStatement.bindString(7, pars.note);
        }
        // nếu command_stack == null
        // nếu null
        if (pars.command_stack == null) {
            // bind giá trị thứ 8 là null
            mInsertSudokuStatement.bindNull(8);
        } else {
            // bind giá trị thứ 8 là command_stack
            mInsertSudokuStatement.bindString(8, pars.command_stack);
        }

        // khai báo rowId và sau khi thực thi query thì gắn id của dòng vừa được insert
        // vào rowId
        long rowId = mInsertSudokuStatement.executeInsert();
        if (rowId > 0) {
            // trả về rowId
            return rowId;
        }
        // nếu câu lệnh sql lỗi, trả về SQLexception
        throw new SQLException("Failed to insert sudoku.");
    }

    // dùng để trả về list bàn chơi sudoku để exportr theo folderId
    public Cursor exportFolder(long folderID) {
        // khai báo query
        String query = "select f._id as folder_id, f.name as folder_name, f.created as folder_created, s.created, s.state, s.time, s.last_played, s.data, s.puzzle_note, s.command_stack from folder f left outer join sudoku s on f._id = s.folder_id";

        // khởi tạo sqlitedatabase để lấy thông tin trong database
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        // nếu folderId có giá trị != -1
        if (folderID != -1) {
            // thêm vào query lệnh where để query theo folderId
            query += " where f._id = ?";
        }
        // trả về con trỏ sau khi thực hiện query với lệnh query ở trên
        return db.rawQuery(query, folderID != -1 ? new String[] { String.valueOf(folderID) } : null);
    }

    // dùng để trả bàn chơi sudoku để exportr theo sudokuId
    public Cursor exportSudoku(long sudokuID) {
        // khai báo query
        String query = "select f._id as folder_id, f.name as folder_name, f.created as folder_created, s.created, s.state, s.time, s.last_played, s.data, s.puzzle_note, s.command_stack from sudoku s inner join folder f on s.folder_id = f._id where s._id = ?";

        // khởi tạo sqlitedatabase để lấy thông tin trong database
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        // trả về con trỏ sau khi thực hiện query với lệnh query ở trên
        return db.rawQuery(query, new String[] { String.valueOf(sudokuID) });
    }

    // dùng để cập nhật bàn chơi vào database
    public void updateSudoku(SudokuGame sudoku) {
        // khai báo contentValue và put dữ liệu vào contentValue
        ContentValues values = new ContentValues();
        values.put(SudokuColumns.DATA, sudoku.getCells().serialize());
        values.put(SudokuColumns.LAST_PLAYED, sudoku.getLastPlayed());
        values.put(SudokuColumns.STATE, sudoku.getState());
        values.put(SudokuColumns.TIME, sudoku.getTime());
        values.put(SudokuColumns.PUZZLE_NOTE, sudoku.getNote());

        // khai báo null cho command_stack
        String command_stack = null;
        // kiểm tra nếu state đang là playing
        // nếu đúng
        if (sudoku.getState() == SudokuGame.GAME_STATE_PLAYING) {
            // gắn command_stack
            command_stack = sudoku.getCommandStack().serialize();
        }
        // put command_stack vào contentValue
        values.put(SudokuColumns.COMMAND_STACK, command_stack);

        // khởi tạo sqlitedatabase để viết và thực thi query vào database
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        // thực thi query với method update với SUDOKU_TABLE_NAME, value được put ở
        // trên, tại id folderID
        db.update(SUDOKU_TABLE_NAME, values, SudokuColumns._ID + "=" + sudoku.getId(), null);
    }

    // dùng để xóa bàn chơi sudoku theo sudokuId
    public void deleteSudoku(long sudokuID) {
        // khởi tạo sqlitedatabase để viết và thực thi query vào database
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        // thực thi việc xóa bàn chơi theo id
        db.delete(SUDOKU_TABLE_NAME, SudokuColumns._ID + "=" + sudokuID, null);
    }

    // dùng để đóng statement
    public void close() {
        // nếu statement không null
        if (mInsertSudokuStatement != null) {
            // close statement
            mInsertSudokuStatement.close();
        }
        // close DB helper
        mOpenHelper.close();
    }

    // dùng để xác định bắt đầu transaction
    public void beginTransaction() {
        mOpenHelper.getWritableDatabase().beginTransaction();
    }

    // dùng để gắn transaction successful
    public void setTransactionSuccessful() {
        mOpenHelper.getWritableDatabase().setTransactionSuccessful();
    }

    // dùng để xác định kết thúc transaction
    public void endTransaction() {
        mOpenHelper.getWritableDatabase().endTransaction();
    }
}
