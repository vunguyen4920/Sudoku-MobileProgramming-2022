package hcmute.vtv_18110069_18110051_18110070.game_sudoku.gui;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;

import hcmute.vtv_18110069_18110051_18110070.game_sudoku.R;
import hcmute.vtv_18110069_18110051_18110070.game_sudoku.utils.ThemeUtils;

public class GameSettingsActivity extends PreferenceActivity {

    private long mTimestampWhenApplyingTheme;
    private CheckBoxPreference mHighlightSimilarNotesPreference;
    private OnPreferenceChangeListener mShowHintsChanged = (preference, newValue) -> {
        boolean newVal = (Boolean) newValue;

        HintsQueue hm = new HintsQueue(GameSettingsActivity.this);
        if (newVal) {
            hm.resetOneTimeHints();
        }
        return true;
    };
    private OnPreferenceChangeListener mHighlightSimilarCellsChanged = (preference, newValue) -> {
        mHighlightSimilarNotesPreference.setEnabled((Boolean) newValue);
        return true;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.setThemeFromPreferences(this);
        mTimestampWhenApplyingTheme = System.currentTimeMillis();
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.game_settings);

        findPreference("show_hints").setOnPreferenceChangeListener(mShowHintsChanged);
        findPreference("theme").setOnPreferenceChangeListener(((preference, newValue) -> {
            recreate();
            return true;
        }));

        mHighlightSimilarNotesPreference = (CheckBoxPreference) findPreference("highlight_similar_notes");
        CheckBoxPreference highlightSimilarCellsPreference = (CheckBoxPreference) findPreference("highlight_similar_cells");
        highlightSimilarCellsPreference.setOnPreferenceChangeListener(mHighlightSimilarCellsChanged);
        mHighlightSimilarNotesPreference.setEnabled(highlightSimilarCellsPreference.isChecked());
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (ThemeUtils.sTimestampOfLastThemeUpdate > mTimestampWhenApplyingTheme) {
            recreate();
        }
    }
}
