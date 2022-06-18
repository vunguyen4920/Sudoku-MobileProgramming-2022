package hcmute.vtv_18110069_18110051_18110070.game_sudoku.game;

import android.content.Context;

import hcmute.vtv_18110069_18110051_18110070.game_sudoku.R;

//Là class dùng như thư mục, để chứa các màn chơi tùy theo độ khó
public class FolderInfo {


    public long id;
    public String name;

    //Thông tin về số màn chơi, số giải nhanh mà độ khó đó có và có bao nhiêu màn đang dừng chơi
    public int puzzleCount;
    public int solvedCount;
    public int playingCount;

    public FolderInfo() {

    }
    //lấy thông tin về độ khó
    public FolderInfo(long id, String name) {
        this.id = id;
        this.name = name;
    }
    //Lấy các thông tin chi tiết của độ khó và cũng check xem tất cả các màn đã được giải hay chưa
    public String getDetail(Context c) {
        StringBuilder sb = new StringBuilder();

        if (puzzleCount == 0) {
            // no puzzles in folder
            sb.append(c.getString(R.string.no_puzzles));
        } else {
            // there are some puzzles
            sb.append(puzzleCount == 1 ? c.getString(R.string.one_puzzle) : c.getString(R.string.n_puzzles, puzzleCount));

            int unsolvedCount = puzzleCount - solvedCount;

            if (playingCount != 0 || unsolvedCount != 0) {
                sb.append(" (");

                if (playingCount != 0) {
                    sb.append(c.getString(R.string.n_playing, playingCount));
                    if (unsolvedCount != 0) {
                        sb.append(", ");
                    }
                }

                if (unsolvedCount != 0) {
                    sb.append(c.getString(R.string.n_unsolved, unsolvedCount));
                }

                sb.append(")");
            }

            if (unsolvedCount == 0 && puzzleCount != 0) {
                sb.append(" (").append(c.getString(R.string.all_solved)).append(")");
            }

        }

        return sb.toString();

    }

}
