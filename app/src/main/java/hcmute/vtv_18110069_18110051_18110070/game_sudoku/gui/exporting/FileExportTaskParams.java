package hcmute.vtv_18110069_18110051_18110070.game_sudoku.gui.exporting;

import java.io.OutputStream;

public class FileExportTaskParams {

    /**
     * Id of folder to export. Set to -1, if you want to export all folders.
     */
    public Long folderID;
    /**
     * Id of sudoku puzzle to export.
     */
    public Long sudokuID;

    /**
     * File where data should be saved.
     */
    public OutputStream file;
    public String filename;

}
