package hcmute.vtv_18110069_18110051_18110070.game_sudoku.gui;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import androidx.core.graphics.ColorUtils;

import net.margaritov.preference.colorpicker.ColorPickerDialog;
import net.margaritov.preference.colorpicker.ColorPickerPreference;

import hcmute.vtv_18110069_18110051_18110070.game_sudoku.R;
import hcmute.vtv_18110069_18110051_18110070.game_sudoku.utils.ThemeUtils;

import java.util.Arrays;

/**
 * A {@link Preference} that allows for setting and previewing a custom Sudoku Board theme.
 */
public class SudokuBoardCustomThemePreferenceGroup extends PreferenceGroup implements
        PreferenceManager.OnActivityDestroyListener,
        ListView.OnItemClickListener,
        SharedPreferences.OnSharedPreferenceChangeListener {
    private SudokuBoardView mBoard;
    private Dialog mDialog;
    private ListView mListView;
    private Dialog mCopyFromExistingThemeDialog;
    private SharedPreferences mGameSettings = PreferenceManager.getDefaultSharedPreferences(getContext());

    public SudokuBoardCustomThemePreferenceGroup(Context context, AttributeSet attrs) {
        super(context, attrs, android.R.attr.preferenceScreenStyle);
        mGameSettings = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public SudokuBoardCustomThemePreferenceGroup(Context context) {
        this(context, null);
    }

    @Override
    protected boolean isOnSameScreenAsChildren() {
        return false;
    }

    @Override
    protected void onClick() {
        if (mDialog != null && mDialog.isShowing()) {
            return;
        }

        showDialog();
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setPositiveButton(R.string.close, null);

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View sudokuPreviewView = inflater.inflate(R.layout.preference_dialog_sudoku_board_theme, null);
        prepareSudokuPreviewView(sudokuPreviewView);
        builder.setCustomTitle(sudokuPreviewView);

        mListView = new ListView(getContext());
        mListView.setAdapter(new CustomThemeListAdapter(this));
        mListView.setOnItemClickListener(this);
        builder.setView(mListView);

        mGameSettings.registerOnSharedPreferenceChangeListener(this);

        mDialog = builder.create();
        mDialog.setOnDismissListener((dialog) -> {
            mGameSettings.unregisterOnSharedPreferenceChangeListener(this);
            mDialog = null;
            mListView = null;
            commitLightThemeOrDarkThemeChanges();
        });
        mDialog.show();
    }

    private void commitLightThemeOrDarkThemeChanges() {
        SwitchPreference preference = (SwitchPreference) getPreference(0);
        SharedPreferences.Editor settingsEditor = mGameSettings.edit();
        String newTheme = preference.isChecked() ? "custom_light" : "custom";
        if (!mGameSettings.getString("theme", "opensudoku").equals(newTheme)) {
            settingsEditor.putString("theme", newTheme);
        }
        settingsEditor.apply();
        ThemeUtils.sTimestampOfLastThemeUpdate = System.currentTimeMillis();
        callChangeListener(null);
    }

    private void showCopyFromExistingThemeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.select_theme);
        builder.setNegativeButton(android.R.string.cancel, null);

        String[] themeNames = getContext().getResources().getStringArray(R.array.theme_names);
        String[] themeNamesWithoutCustomTheme = Arrays.copyOfRange(themeNames, 0, themeNames.length - 1);
        builder.setItems(themeNamesWithoutCustomTheme, (dialog, which) -> {
            copyFromExistingThemeIndex(which);
            mCopyFromExistingThemeDialog.dismiss();
        });

        mCopyFromExistingThemeDialog = builder.create();
        mCopyFromExistingThemeDialog.setOnDismissListener((dialog) -> mCopyFromExistingThemeDialog = null);
        mCopyFromExistingThemeDialog.show();
    }

    private void copyFromExistingThemeIndex(int which) {
        String theme = getContext().getResources().getStringArray(R.array.theme_codes)[which];
        ContextThemeWrapper themeWrapper = new ContextThemeWrapper(getContext(), ThemeUtils.getThemeResourceIdFromString(theme));

        ((SwitchPreference) getPreference(0)).setChecked(ThemeUtils.isLightTheme(theme));

        int[] attributes = {
                R.attr.colorPrimary,
                R.attr.colorPrimaryDark,
                R.attr.colorAccent,
                R.attr.colorButtonNormal,
                R.attr.lineColor,
                R.attr.sectorLineColor,
                R.attr.textColor,
                R.attr.textColorReadOnly,
                R.attr.textColorNote,
                R.attr.backgroundColor,
                R.attr.backgroundColorSecondary,
                R.attr.backgroundColorReadOnly,
                R.attr.backgroundColorTouched,
                R.attr.backgroundColorSelected,
                R.attr.backgroundColorHighlighted
        };

        TypedArray themeColors = themeWrapper.getTheme().obtainStyledAttributes(attributes);
        for (int i = 0; i < attributes.length; i++) {
            ((ColorPickerPreference) getPreference(i + 1)).onColorChanged(themeColors.getColor(i, Color.GRAY));
        }
    }

    private void showCreateFromSingleColorDialog() {
        ColorPickerDialog colorDialog = new ColorPickerDialog(getContext(), mGameSettings.getInt("custom_theme_colorPrimary", Color.WHITE));
        colorDialog.setAlphaSliderVisible(false);
        colorDialog.setHexValueEnabled(true);
        colorDialog.setOnColorChangedListener(this::createCustomThemeFromSingleColor);
        colorDialog.show();
    }

    private void createCustomThemeFromSingleColor(int colorPrimary) {
        double whiteContrast = ColorUtils.calculateContrast(colorPrimary, Color.WHITE);
        double blackContrast = ColorUtils.calculateContrast(colorPrimary, Color.BLACK);
        boolean isLightTheme = whiteContrast < blackContrast;
        ((SwitchPreference) findPreference("custom_theme_isLightTheme")).setChecked(isLightTheme);

        float[] colorAsHSL = new float[3];
        ColorUtils.colorToHSL(colorPrimary, colorAsHSL);

        float[] tempHSL = colorAsHSL.clone();
        tempHSL[0] = (colorAsHSL[0] + 180f) % 360.0f;
        int colorAccent = ColorUtils.HSLToColor(tempHSL);

        tempHSL = colorAsHSL.clone();
        tempHSL[2] += isLightTheme ? -0.1f : 0.1f;
        int colorPrimaryDark = ColorUtils.HSLToColor(tempHSL);

        int textColor = isLightTheme ? Color.BLACK : Color.WHITE;
        int backgroundColor = isLightTheme ? Color.WHITE : Color.BLACK;

        ((ColorPickerPreference) findPreference("custom_theme_colorPrimary")).onColorChanged(colorPrimary);
        ((ColorPickerPreference) findPreference("custom_theme_colorPrimaryDark")).onColorChanged(colorPrimaryDark);
        ((ColorPickerPreference) findPreference("custom_theme_colorAccent")).onColorChanged(colorAccent);
        ((ColorPickerPreference) findPreference("custom_theme_colorButtonNormal")).onColorChanged(isLightTheme ? Color.LTGRAY : Color.DKGRAY);
        ((ColorPickerPreference) findPreference("custom_theme_lineColor")).onColorChanged(colorPrimaryDark);
        ((ColorPickerPreference) findPreference("custom_theme_sectorLineColor")).onColorChanged(colorPrimaryDark);
        ((ColorPickerPreference) findPreference("custom_theme_textColor")).onColorChanged(textColor);
        ((ColorPickerPreference) findPreference("custom_theme_textColorReadOnly")).onColorChanged(textColor);
        ((ColorPickerPreference) findPreference("custom_theme_textColorNote")).onColorChanged(textColor);
        ((ColorPickerPreference) findPreference("custom_theme_backgroundColor")).onColorChanged(backgroundColor);
        ((ColorPickerPreference) findPreference("custom_theme_backgroundColorSecondary")).onColorChanged(backgroundColor);
        ((ColorPickerPreference) findPreference("custom_theme_backgroundColorReadOnly")).onColorChanged(ColorUtils.setAlphaComponent(colorPrimaryDark, 64));
        ((ColorPickerPreference) findPreference("custom_theme_backgroundColorTouched")).onColorChanged(colorAccent);
        ((ColorPickerPreference) findPreference("custom_theme_backgroundColorSelected")).onColorChanged(colorPrimaryDark);
        ((ColorPickerPreference) findPreference("custom_theme_backgroundColorHighlighted")).onColorChanged(colorPrimary);
    }

    @Override
    protected void onAttachedToActivity() {
        for (int i = 1; i < getPreferenceCount(); i ++) {
            ((ColorPickerPreference) getPreference(i)).setHexValueEnabled(true);
        }
        super.onAttachedToActivity();
    }

    public void onActivityDestroy() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }

        if (mCopyFromExistingThemeDialog != null && mCopyFromExistingThemeDialog.isShowing()) {
            mCopyFromExistingThemeDialog.dismiss();
        }
    }

    private void prepareSudokuPreviewView(View view) {
        mBoard = view.findViewById(R.id.sudoku_board);
        mBoard.setOnCellSelectedListener((cell) -> {
            if (cell != null) {
                mBoard.setHighlightedValue(cell.getValue());
            } else {
                mBoard.setHighlightedValue(0);
            }
        });
        ThemeUtils.prepareSudokuPreviewView(mBoard);
        updateThemePreview();
    }

    private void updateThemePreview() {
        String themeName = mGameSettings.getString("theme", "opensudoku");
        ThemeUtils.applyThemeToSudokuBoardViewFromContext(themeName, mBoard, getContext());
    }

    private void quantizeCustomAppColorPreferences() {
        SharedPreferences.Editor settingsEditor = mGameSettings.edit();
        settingsEditor.putInt("custom_theme_colorPrimary", ThemeUtils.findClosestMaterialColor(mGameSettings.getInt("custom_theme_colorPrimary", Color.GRAY)));
        settingsEditor.putInt("custom_theme_colorPrimaryDark", ThemeUtils.findClosestMaterialColor(mGameSettings.getInt("custom_theme_colorPrimaryDark", Color.GRAY)));
        settingsEditor.putInt("custom_theme_colorAccent", ThemeUtils.findClosestMaterialColor(mGameSettings.getInt("custom_theme_colorAccent", Color.WHITE)));
        settingsEditor.putInt("custom_theme_colorButtonNormal", ThemeUtils.findClosestMaterialColor(mGameSettings.getInt("custom_theme_colorButtonNormal", Color.GRAY)));
        settingsEditor.apply();
        ThemeUtils.sTimestampOfLastThemeUpdate = System.currentTimeMillis();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.contains("custom_theme_color")) {
            quantizeCustomAppColorPreferences();
        }
        updateThemePreview();
        if (mListView != null) {
            mListView.invalidateViews();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position == parent.getCount() - 1) {
            showCreateFromSingleColorDialog();
        } else if (position == parent.getCount() - 2) {
            showCopyFromExistingThemeDialog();
        } else if (position == 0) {
            SwitchPreference preference = (SwitchPreference) getPreference(position);
            preference.setChecked(!preference.isChecked());
        } else {
            ((ColorPickerPreference) getPreference(position)).onPreferenceClick(null);
        }
    }

    private static class CustomThemeListAdapter extends BaseAdapter implements ListAdapter {
        private SudokuBoardCustomThemePreferenceGroup mPreferenceGroup;
        private Preference mCopyFromExistingThemePreference;
        private Preference mCreateFromColorPreference;

        CustomThemeListAdapter(SudokuBoardCustomThemePreferenceGroup preferenceGroup) {
            mPreferenceGroup = preferenceGroup;
            mCopyFromExistingThemePreference = new Preference(preferenceGroup.getContext());
            mCopyFromExistingThemePreference.setTitle(R.string.copy_from_existing_theme);
            mCreateFromColorPreference = new Preference(preferenceGroup.getContext());
            mCreateFromColorPreference.setTitle(R.string.create_from_single_color);
        }

        @Override
        public boolean areAllItemsEnabled() {
            return true;
        }

        @Override
        public boolean isEnabled(int position) {
            return true;
        }

        @Override
        public int getCount() {
            return mPreferenceGroup.getPreferenceCount() + 2;
        }

        @Override
        public Object getItem(int position) {
            if (position == getCount() - 2) {
                return mCopyFromExistingThemePreference;
            } else if (position == getCount() - 1) {
                return mCreateFromColorPreference;
            } else {
                return mPreferenceGroup.getPreference(position);
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Preference preference = ((Preference) getItem(position));

            // we pass convertView as null for the first and final elements to make sure we don't
            // have a color preview on the list view items that don't edit colors
            return (position == 0 || position >= getCount() - 2) ? preference.getView(null, parent) : preference.getView(convertView, parent);
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }
}
