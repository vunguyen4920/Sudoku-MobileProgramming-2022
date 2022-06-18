package hcmute.vtv_18110069_18110051_18110070.game_sudoku.game;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

//Dùng cho việc ghi chú các giá trị có thể đúng của cell
public class CellNote {

    public static final CellNote EMPTY = new CellNote();
    private final short mNotedNumbers;

    //Để ghi chú là rỗng
    public CellNote() {
        mNotedNumbers = 0;
    }

    //Lưu ghi chú
    private CellNote(short notedNumbers) {
        mNotedNumbers = notedNumbers;
    }

    //Độc nhất hóa giá trị để sử dụng cho command
    public static CellNote deserialize(String note) {
        return deserialize(note, CellCollection.DATA_VERSION);
    }

    public static CellNote deserialize(String note, int version) {

        int noteValue = 0;
        if (note != null && !note.equals("") && !note.equals("-")) {
            if (version == CellCollection.DATA_VERSION_1) {
                StringTokenizer tokenizer = new StringTokenizer(note, ",");
                while (tokenizer.hasMoreTokens()) {
                    String value = tokenizer.nextToken();
                    if (!value.equals("-")) {
                        int number = Integer.parseInt(value);
                        noteValue |= (1 << (number - 1));
                    }
                }
            } else {
                //CellCollection.DATA_VERSION_2
                noteValue = Integer.parseInt(note);
            }
        }

        return new CellNote((short) noteValue);
    }


    //Tạo instance mới cho ghi chú từ chuỗi Interger đưa vào
    public static CellNote fromIntArray(Integer[] notedNums) {
        int notedNumbers = 0;

        for (Integer n : notedNums) {
            notedNumbers = (short) (notedNumbers | (1 << (n - 1)));
        }

        return new CellNote((short) notedNumbers);
    }


    //Độc nhất hóa giá trị
    public void serialize(StringBuilder data) {
        data.append(mNotedNumbers);
        data.append("|");
    }

    public String serialize() {
        StringBuilder sb = new StringBuilder();
        serialize(sb);
        return sb.toString();
    }

    //Trả các ghi chú được lưu
    public List<Integer> getNotedNumbers() {

        List<Integer> result = new ArrayList<>();
        int c = 1;
        for (int i = 0; i < 9; i++) {
            if ((mNotedNumbers & (short) c) != 0) {
                result.add(i + 1);
            }
            c = (c << 1);
        }

        return result;
    }

    //Trong chế độ Toggles: Để thêm ghi chú nếu nó chưa có, còn nếu có rồi thì gỡ
    public CellNote toggleNumber(int number) {
        if (number < 1 || number > 9)
            throw new IllegalArgumentException("Number must be between 1-9.");

        return new CellNote((short) (mNotedNumbers ^ (1 << (number - 1))));
    }

    //Thêm số vào ghi chú nếu số đó chưa có sẵn
    public CellNote addNumber(int number) {
        if (number < 1 || number > 9)
            throw new IllegalArgumentException("Number must be between 1-9.");

        return new CellNote((short) (mNotedNumbers | (1 << (number - 1))));
    }

    //Xóa số từ ghi chú
    public CellNote removeNumber(int number) {
        if (number < 1 || number > 9)
            throw new IllegalArgumentException("Number must be between 1-9.");

        return new CellNote((short) (mNotedNumbers & ~(1 << (number - 1))));
    }

    //Check xem giá trị của ghi chú có phải từ 1-9 không
    public boolean hasNumber(int number) {
        if (number < 1 || number > 9) {
            return false;
        }

        return (mNotedNumbers & (1 << (number - 1))) != 0;
    }

    //Xóa sạch ghi chú
    public CellNote clear() {
        return new CellNote();
    }

    //Xen thử xem ghi chú có trống hay không
    public boolean isEmpty() {
        return mNotedNumbers == 0;
    }

}
