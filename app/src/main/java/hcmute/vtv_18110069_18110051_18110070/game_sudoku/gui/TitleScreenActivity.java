package hcmute.vtv_18110069_18110051_18110070.game_sudoku.gui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.preference.PreferenceManager;

import java.util.Random;

import hcmute.vtv_18110069_18110051_18110070.game_sudoku.R;
import hcmute.vtv_18110069_18110051_18110070.game_sudoku.db.SudokuDatabase;
import hcmute.vtv_18110069_18110051_18110070.game_sudoku.game.SudokuGame;

/**
 *
 * Class này là activity dùng để xử lí sự kiện cho screen trang bắt đầu
 *
 * Người dùng có thể tiếp tục game họ đã chơi ở đây
 * và luôn là màn chơi gần nhất --> Resume
 * hoặc chọn chơi màn khác --> New Game
 * hoặc chỉnh sửa thiết lập --> Settings
 *
 */
public class TitleScreenActivity extends ThemedActivity {

    private Button mResumeBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_title_screen);

        mResumeBtn = findViewById(R.id.resume_button);
        Button mNewGameBtn = findViewById(R.id.new_game_button);
        Button mSudokuListBtn = findViewById(R.id.sudoku_list_button);
        Button mSettingsBtn = findViewById(R.id.settings_button);

        setupResumeButton();

        Random random = new Random();
        int sudokuId = random.nextInt(90 - 1) + 1;
        Intent intentNewGame = new Intent(TitleScreenActivity.this, PlayActivity.class);
        intentNewGame.putExtra(PlayActivity.EXTRA_SUDOKU_ID, (long) sudokuId);
        mNewGameBtn.setOnClickListener((view) ->
                startActivity(intentNewGame));

        mSudokuListBtn.setOnClickListener((view) ->
                startActivity(new Intent(this, DifficultiesListActivity.class)));

        mSettingsBtn.setOnClickListener((view) ->
                startActivity(new Intent(this, GameSettingsActivity.class)));

        SharedPreferences gameSettings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean showSudokuFolderListOnStartup = gameSettings.getBoolean("show_sudoku_lists_on_startup", false);
        if (showSudokuFolderListOnStartup) {
            startActivity(new Intent(this, DifficultiesListActivity.class));
        }
    }

    private boolean canResume(long mSudokuGameID) {
        SudokuDatabase mDatabase = new SudokuDatabase(getApplicationContext());
        SudokuGame mSudokuGame = mDatabase.getSudoku(mSudokuGameID);
        if (mSudokuGame != null) {
            return mSudokuGame.getState() != SudokuGame.GAME_STATE_COMPLETED;
        }
        return false;
    }

    private void setupResumeButton() {
        SharedPreferences gameSettings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        long mSudokuGameID = gameSettings.getLong("most_recently_played_sudoku_id", 0);
        if (canResume(mSudokuGameID)) {
            mResumeBtn.setVisibility(View.VISIBLE);
            mResumeBtn.setOnClickListener((view) -> {
                Intent intentToPlay = new Intent(TitleScreenActivity.this, PlayActivity.class);
                intentToPlay.putExtra(PlayActivity.EXTRA_SUDOKU_ID, mSudokuGameID);
                startActivity(intentToPlay);
            });
        } else {
            mResumeBtn.setVisibility(View.GONE);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();

        setupResumeButton();
    }
}
