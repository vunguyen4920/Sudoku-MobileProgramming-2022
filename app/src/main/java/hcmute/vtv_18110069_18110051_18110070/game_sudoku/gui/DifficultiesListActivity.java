package hcmute.vtv_18110069_18110051_18110070.game_sudoku.gui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;

import hcmute.vtv_18110069_18110051_18110070.game_sudoku.R;
import hcmute.vtv_18110069_18110051_18110070.game_sudoku.db.FolderColumns;
import hcmute.vtv_18110069_18110051_18110070.game_sudoku.db.SudokuDatabase;

/**
 *
 * Ở trang chọn game này người chơi sẽ chọn màn chơi
 *
 * Những màn chơi đã được thêm vào từ trước trong db.
 *
 */
public class DifficultiesListActivity extends ThemedActivity {
//    Setup hằng số để tiện cho việc gọi cũng như sụ dễ dàng xuyên suốt quá trình code
    public static final int MENU_ITEM_SETTINGS = Menu.FIRST;
    private Cursor mCursor;
    private SudokuDatabase mDatabase;
    private FolderListViewBinder mFolderListBinder;
    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.folder_list);
        setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);

        mDatabase = new SudokuDatabase(getApplicationContext());
        mCursor = mDatabase.getFolderList();
        startManagingCursor(mCursor);
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.folder_list_item,
                mCursor, new String[]{FolderColumns.NAME, FolderColumns._ID},
                new int[]{R.id.name, R.id.detail});
        mFolderListBinder = new FolderListViewBinder(this);
        adapter.setViewBinder(mFolderListBinder);

        mListView = findViewById(android.R.id.list);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener((parent, view, position, id) -> {
            Intent i = new Intent(getApplicationContext(), SudokuListActivity.class);
            i.putExtra(SudokuListActivity.EXTRA_FOLDER_ID, id);
            startActivity(i);
        });
        registerForContextMenu(mListView);
    }

    @Override
    protected void onStart() {
        super.onStart();

        mCursor.requery();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDatabase.close();
        mFolderListBinder.destroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add(0, MENU_ITEM_SETTINGS, 2, R.string.settings)
                .setShortcut('6', 's')
                .setIcon(R.drawable.ic_settings);


        Intent intent = new Intent(null, getIntent().getData());
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
                new ComponentName(this, DifficultiesListActivity.class), null, intent, 0, null);

        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case MENU_ITEM_SETTINGS:
                intent = new Intent();
                intent.setClass(this, GameSettingsActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private static class FolderListViewBinder implements ViewBinder {
        private Context mContext;
        private FolderDetailLoader mDetailLoader;


        public FolderListViewBinder(Context context) {
            mContext = context;
            mDetailLoader = new FolderDetailLoader(context);
        }

        @Override
        public boolean setViewValue(View view, Cursor c, int columnIndex) {

            switch (view.getId()) {
                case R.id.name:
                    ((TextView) view).setText(c.getString(columnIndex));
                    break;
                case R.id.detail:
                    final long folderID = c.getLong(columnIndex);
                    final TextView detailView = (TextView) view;
                    detailView.setText(mContext.getString(R.string.loading));
                    mDetailLoader.loadDetailAsync(folderID, folderInfo -> {
                        if (folderInfo != null)
                            detailView.setText(folderInfo.getDetail(mContext));
                    });
            }
            return true;
        }

        public void destroy() {
            mDetailLoader.destroy();
        }
    }
}
