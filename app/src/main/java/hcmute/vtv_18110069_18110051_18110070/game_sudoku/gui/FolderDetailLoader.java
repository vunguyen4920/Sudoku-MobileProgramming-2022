package hcmute.vtv_18110069_18110051_18110070.game_sudoku.gui;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import hcmute.vtv_18110069_18110051_18110070.game_sudoku.db.SudokuDatabase;
import hcmute.vtv_18110069_18110051_18110070.game_sudoku.game.FolderInfo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FolderDetailLoader {

    private static final String TAG = "FolderDetailLoader";

    private SudokuDatabase mDatabase;
    private Handler mGuiHandler;
    private ExecutorService mLoaderService = Executors.newSingleThreadExecutor();

    public FolderDetailLoader(Context context) {
        mDatabase = new SudokuDatabase(context);
        mGuiHandler = new Handler();
    }

    public void loadDetailAsync(long folderID, FolderDetailCallback loadedCallback) {
        final long folderIDFinal = folderID;
        final FolderDetailCallback loadedCallbackFinal = loadedCallback;
        mLoaderService.execute(() -> {
            try {
                final FolderInfo folderInfo = mDatabase.getFolderInfoFull(folderIDFinal);

                mGuiHandler.post(() -> loadedCallbackFinal.onLoaded(folderInfo));
            } catch (Exception e) {
                Log.e(TAG, "Error occurred while loading full folder info.", e);
            }
        });
    }

    public void destroy() {
        mLoaderService.shutdownNow();
        mDatabase.close();
    }

    public interface FolderDetailCallback {
        void onLoaded(FolderInfo folderInfo);
    }
}
