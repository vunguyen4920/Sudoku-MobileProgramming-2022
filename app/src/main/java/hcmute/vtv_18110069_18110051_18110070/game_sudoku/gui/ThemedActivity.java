package hcmute.vtv_18110069_18110051_18110070.game_sudoku.gui;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import hcmute.vtv_18110069_18110051_18110070.game_sudoku.utils.ThemeUtils;

public class ThemedActivity extends AppCompatActivity {
    private int mThemeId = 0;
    private long mTimestampWhenApplyingTheme = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ThemeUtils.setThemeFromPreferences(this);
        mTimestampWhenApplyingTheme = System.currentTimeMillis();
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (ThemeUtils.sTimestampOfLastThemeUpdate > mTimestampWhenApplyingTheme) {
            recreate();
        }
    }
}
