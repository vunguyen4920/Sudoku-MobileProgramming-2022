package hcmute.vtv_18110069_18110051_18110070.game_sudoku.gui;

import java.util.Formatter;

/**
 * Game time formatter.
 *
 * @author romario
 */
public class GameTimeFormat {
    private static final int TIME_99_99 = 99 * 99 * 1000;

    private StringBuilder mTimeText = new StringBuilder();
    private Formatter mGameTimeFormatter = new Formatter(mTimeText);

    /**
     * Formats time to format of mm:ss, hours are
     * never displayed, only total number of minutes.
     *
     * @param time Time in milliseconds.
     * @return
     */
    public String format(long time) {
        mTimeText.setLength(0);
        if (time > TIME_99_99) {
            mGameTimeFormatter.format("%d:%02d", time / 60000, time / 1000 % 60);
        } else {
            mGameTimeFormatter.format("%02d:%02d", time / 60000, time / 1000 % 60);
        }
        return mTimeText.toString();
    }

}
