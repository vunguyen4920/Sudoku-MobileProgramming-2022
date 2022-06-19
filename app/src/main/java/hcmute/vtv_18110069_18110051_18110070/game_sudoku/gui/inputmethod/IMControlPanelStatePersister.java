package hcmute.vtv_18110069_18110051_18110070.game_sudoku.gui.inputmethod;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import androidx.preference.PreferenceManager;

/**
 * Class này giúp cho việc lưu state người dùng chọn cách nhập dữ liệu nào
 */
public class IMControlPanelStatePersister {

    private static final String PREFIX = IMControlPanel.class.getName();

    private SharedPreferences mPreferences;

    public IMControlPanelStatePersister(Context context) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void saveState(IMControlPanel controlPanel) {
//       Lưu state của control panel này
        StateBundle cpState = new StateBundle(mPreferences, PREFIX + "", true);
        cpState.putInt("activeMethodIndex", controlPanel.getActiveMethodIndex());
        cpState.commit();

        // Chạy vòng lặp để lưu state cho các input method
        for (InputMethod im : controlPanel.getInputMethods()) {
            StateBundle outState = new StateBundle(mPreferences, PREFIX + "" + im.getInputMethodName(), true);
            im.onSaveState(outState);
            outState.commit();
        }
    }

    public void restoreState(IMControlPanel controlPanel) {
        // Tương tự như khi save thì ta restore lại state cũng như thé
//        Restore state của Control panel
        StateBundle cpState = new StateBundle(mPreferences, PREFIX + "", false);
        int methodId = cpState.getInt("activeMethodIndex", 0);
        if (methodId != -1) {
            controlPanel.activateInputMethod(methodId);
        }

//       restore state của các input method
        for (InputMethod im : controlPanel.getInputMethods()) {
            StateBundle savedState = new StateBundle(mPreferences, PREFIX + "" + im.getInputMethodName(), false);
            im.onRestoreState(savedState);
        }
    }

    public static class StateBundle {

        private final SharedPreferences mPreferences;
        private final Editor mPrefEditor;
        private final String mPrefix;
        private final boolean mEditable;

        public StateBundle(SharedPreferences preferences, String prefix,
                           boolean editable) {
            mPreferences = preferences;
            mPrefix = prefix;
            mEditable = editable;

            if (mEditable) {
                mPrefEditor = preferences.edit();
            } else {
                mPrefEditor = null;
            }
        }

        public boolean getBoolean(String key, boolean defValue) {
            return mPreferences.getBoolean(mPrefix + key, defValue);
        }

        public float getFloat(String key, float defValue) {
            return mPreferences.getFloat(mPrefix + key, defValue);
        }

        public int getInt(String key, int defValue) {
            return mPreferences.getInt(mPrefix + key, defValue);
        }

        public String getString(String key, String defValue) {
            return mPreferences.getString(mPrefix + key, defValue);
        }

        public void putBoolean(String key, boolean value) {
            if (!mEditable) {
                throw new IllegalStateException("StateBundle is not editable");
            }
            mPrefEditor.putBoolean(mPrefix + key, value);
        }

        public void putFloat(String key, float value) {
            if (!mEditable) {
                throw new IllegalStateException("StateBundle is not editable");
            }
            mPrefEditor.putFloat(mPrefix + key, value);
        }

        public void putInt(String key, int value) {
            if (!mEditable) {
                throw new IllegalStateException("StateBundle is not editable");
            }
            mPrefEditor.putInt(mPrefix + key, value);
        }

        public void putString(String key, String value) {
            if (!mEditable) {
                throw new IllegalStateException("StateBundle is not editable");
            }
            mPrefEditor.putString(mPrefix + key, value);
        }

        public void commit() {
            if (!mEditable) {
                throw new IllegalStateException("StateBundle is not editable");
            }
            mPrefEditor.commit();
        }

    }

}
