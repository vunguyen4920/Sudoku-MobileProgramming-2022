package hcmute.vtv_18110069_18110051_18110070.game_sudoku.gui;

import java.util.Formatter;

/**
 *
 * Class TimerFormat giúp cho việc format thời gian
 * trong trường hợp này chỉ format thành định dạng mm:ss
 *
 */
public class TimerFormat {
    private static final int TIME_99_99 = 99 * 99 * 1000;

    private StringBuilder mTimeText = new StringBuilder();
    private Formatter mGameTimeFormatter = new Formatter(mTimeText);

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
