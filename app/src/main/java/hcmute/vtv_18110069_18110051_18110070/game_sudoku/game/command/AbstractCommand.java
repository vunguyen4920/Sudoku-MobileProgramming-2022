package hcmute.vtv_18110069_18110051_18110070.game_sudoku.game.command;

import java.util.StringTokenizer;

//Class lệnh chung cho các lệnh khác
public abstract class AbstractCommand {

    //Khai báo các command
    private static final CommandDef[] commands = {
            new CommandDef(ClearAllNotesCommand.class.getSimpleName(), "c1",
                    ClearAllNotesCommand::new),
            new CommandDef(EditCellNoteCommand.class.getSimpleName(), "c2",
                    EditCellNoteCommand::new),
            new CommandDef(FillInNotesCommand.class.getSimpleName(), "c3",
                    FillInNotesCommand::new),
            new CommandDef(SetCellValueCommand.class.getSimpleName(), "c4",
                    SetCellValueCommand::new),
            new CommandDef(CheckpointCommand.class.getSimpleName(), "c5",
                    CheckpointCommand::new),
            new CommandDef(SetCellValueAndRemoveNotesCommand.class.getSimpleName(), "c6",
                    SetCellValueAndRemoveNotesCommand::new),
            new CommandDef(FillInNotesWithAllValuesCommand.class.getSimpleName(), "c7",
                    FillInNotesWithAllValuesCommand::new)
    };
    //Dùng để tạo chuỗi độc nhất
    public static AbstractCommand deserialize(StringTokenizer data) {
        String cmdShortName = data.nextToken();
        for (CommandDef cmdDef : commands) {
            if (cmdDef.getShortName().equals(cmdShortName)) {
                AbstractCommand cmd = cmdDef.create();
                cmd._deserialize(data);
                return cmd;
            }
        }
        throw new IllegalArgumentException(String.format("Unknown command class '%s'.", cmdShortName));
    }

    protected void _deserialize(StringTokenizer data) {

    }

    public void serialize(StringBuilder data) {
        String cmdLongName = getCommandClass();
        for (CommandDef cmdDef : commands) {
            if (cmdDef.getLongName().equals(cmdLongName)) {
                data.append(cmdDef.getShortName()).append("|");
                return;
            }
        }

        throw new IllegalArgumentException(String.format("Unknown command class '%s'.", cmdLongName));
    }

    public String getCommandClass() {
        return getClass().getSimpleName();
    }

    //Thực thi lệnh
    abstract void execute();

    //Để undo lại lệnh
    abstract void undo();

    private interface CommandCreatorFunction {
        AbstractCommand create();
    }
    //Định nghĩa các command được sử dụng
    private static class CommandDef {
        String mLongName;
        String mShortName;
        CommandCreatorFunction mCreator;

        public CommandDef(String longName, String shortName, CommandCreatorFunction creator) {
            mLongName = longName;
            mShortName = shortName;
            mCreator = creator;
        }

        public AbstractCommand create() {
            return mCreator.create();
        }

        public String getLongName() {
            return mLongName;
        }

        public String getShortName() {
            return mShortName;
        }
    }

}
