package hcmute.vtv_18110069_18110051_18110070.game_sudoku.gui;

import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;

import hcmute.vtv_18110069_18110051_18110070.game_sudoku.R;

import java.util.LinkedList;
import java.util.Queue;

/**
 *
 * Là 1 UI Class giúp cho việc show dialog gợi ý hoặc hướng dẫn cho người chơi
 *
 * Cơ bản thì vẫn là 1 dạng dialog
 * Nhưng về mặt UI thì dialog này giúp cho người chơi biết
 * đây là thông tin hương dẫn từ hệ thống
 *
 */
public class HintsQueue {
    private static final String PREF_NAME = "hints";
    private final Queue<Message> mMessages;
    private final AlertDialog mHintDialog;
    private Context mContext;
    private SharedPreferences mPrefs;
    private boolean mOneTimeHintsEnabled;

    public HintsQueue(Context context) {
        mContext = context;
        mPrefs = mContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        SharedPreferences gameSettings = PreferenceManager.getDefaultSharedPreferences(context);
        gameSettings.registerOnSharedPreferenceChangeListener((sharedPreferences, key) -> {
            if (key.equals("show_hints")) {
                mOneTimeHintsEnabled = sharedPreferences.getBoolean("show_hints", true);
            }
        });
        mOneTimeHintsEnabled = gameSettings.getBoolean("show_hints", true);

        //processQueue();
        OnClickListener mHintClosed = (dialog, which) -> {
            //processQueue();
        };
        mHintDialog = new AlertDialog.Builder(context)
                .setIcon(R.drawable.ic_info)
                .setTitle(R.string.hint)
                .setMessage("")
                .setPositiveButton(R.string.close, mHintClosed).create();

        mHintDialog.setOnDismissListener(dialog -> processQueue());

        mMessages = new LinkedList<>();
    }

    private void addHint(Message hint) {
        synchronized (mMessages) {
            mMessages.add(hint);
        }

        synchronized (mHintDialog) {
            if (!mHintDialog.isShowing()) {
                processQueue();
            }
        }
    }

    private void processQueue() {
        Message hint;

        synchronized (mMessages) {
            hint = mMessages.poll();
        }

        if (hint != null) {
            showHintDialog(hint);
        }
    }

    private void showHintDialog(Message hint) {
        synchronized (mHintDialog) {
            mHintDialog.setTitle(mContext.getString(hint.titleResID));
            mHintDialog.setMessage(mContext.getText(hint.messageResID));
            mHintDialog.show();
        }
    }

    public void showHint(int titleResID, int messageResID, Object... args) {
        Message hint = new Message();
        hint.titleResID = titleResID;
        hint.messageResID = messageResID;
        addHint(hint);
    }

    public void showOneTimeHint(String key, int titleResID, int messageResID, Object... args) {
        if (mOneTimeHintsEnabled) {
            if (legacyHintsWereDisplayed()) {
                return;
            }

            String hintKey = "hint_" + key;
            if (!mPrefs.getBoolean(hintKey, false)) {
                showHint(titleResID, messageResID, args);
                Editor editor = mPrefs.edit();
                editor.putBoolean(hintKey, true);
                editor.apply();
            }
        }

    }

    public boolean legacyHintsWereDisplayed() {
        return mPrefs.getBoolean("hint_2131099727", false) &&
                mPrefs.getBoolean("hint_2131099730", false) &&
                mPrefs.getBoolean("hint_2131099726", false) &&
                mPrefs.getBoolean("hint_2131099729", false) &&
                mPrefs.getBoolean("hint_2131099728", false);
    }

    public void resetOneTimeHints() {
        Editor editor = mPrefs.edit();
        editor.clear();
        editor.apply();
    }

    private static class Message {
        int titleResID;
        int messageResID;
    }

}
