package hcmute.vtv_18110069_18110051_18110070.game_sudoku.gui.importing;

import hcmute.vtv_18110069_18110051_18110070.game_sudoku.db.SudokuInvalidFormatException;

/**
 * Handles import of puzzles via intent's extras.
 *
 * @author romario
 */
public class ExtrasImportTask extends AbstractImportTask {

    private String mFolderName;
    private String mGames;
    private boolean mAppendToFolder;

    public ExtrasImportTask(String folderName, String games, boolean appendToFolder) {
        mFolderName = folderName;
        mGames = games;
        mAppendToFolder = appendToFolder;
    }

    @Override
    protected void processImport() throws SudokuInvalidFormatException {
        if (mAppendToFolder) {
            appendToFolder(mFolderName);
        } else {
            importFolder(mFolderName);
        }

        for (String game : mGames.split("\n")) {
            importGame(game);
        }
    }

}
