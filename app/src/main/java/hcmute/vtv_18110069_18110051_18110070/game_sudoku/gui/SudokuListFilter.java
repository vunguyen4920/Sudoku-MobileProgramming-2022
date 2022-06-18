package hcmute.vtv_18110069_18110051_18110070.game_sudoku.gui;

import android.content.Context;

import hcmute.vtv_18110069_18110051_18110070.game_sudoku.R;
import hcmute.vtv_18110069_18110051_18110070.game_sudoku.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Class hoạt động như 1 class đa dụng dùng để
 * chứa xử lý cho việc lọc màn chơi theo
 * tiêu chí đã chơi, đang chơi, hay chưa chơi
 *
 */
public class SudokuListFilter {

    public boolean showStateNotStarted = true;
    public boolean showStatePlaying = true;
    public boolean showStateCompleted = true;
    private Context mContext;

    public SudokuListFilter(Context context) {
        mContext = context;
    }

    @Override
    public String toString() {
        List<String> visibleStates = new ArrayList<>();
        if (showStateNotStarted) {
            visibleStates.add(mContext.getString(R.string.not_started));
        }
        if (showStatePlaying) {
            visibleStates.add(mContext.getString(R.string.playing));
        }
        if (showStateCompleted) {
            visibleStates.add(mContext.getString(R.string.solved));
        }
        return StringUtils.join(visibleStates, ",");
    }
}
