package bg.fmi.mjt.splitwise.commands.validators;

import bg.fmi.mjt.splitwise.commands.CommandType;
import bg.fmi.mjt.splitwise.commands.Command;

import java.util.Objects;

public class DefaultCommandValidator implements CommandValidator {

    private final static int ADD_FRIEND_ARG_COUNT = 1;
    private final static int GET_STATUS_ARG_COUNT = 0;
    private final static int LOGIN_ARG_COUNT = 2;
    private final static int LOGOUT_ARG_COUNT = 0;
    private final static int PAYED_ARG_COUNT = 2;
    private final static int REGISTER_ARG_COUNT = 2;
    private final static int SPLIT_ARG_COUNT = 3;
    private final static int SPLIT_GROUP_ARG_COUNT = 3;
    private final static int GET_PAYMENT_HISTORY_ARG_COUNT = 0;
    private final static int HELP_ARG_COUNT = 0;

    private final static int CREATE_GROUP_MIN_ARG_COUNT = 3;

    private final static String LEVS_REGEX = "\\d+(\\.\\d\\d?)?";
    private final static int PAYED_LEVS_INDEX = 0;
    private final static int SPLIT_LEVS_INDEX = 0;
    private final static int SPLIT_GROUP_LEVS_INDEX = 0;

    @Override
    public boolean isValid(Command cmd) {
        Objects.requireNonNull(cmd, "Command cannot be null");

        CommandType cmdType = CommandType.fromString(cmd.name());

        if (cmdType == null || cmd.args() == null) {
            return false;
        }

        for (var arg : cmd.args()) {
            if (arg == null || arg.isBlank()) {
                return false;
            }
        }

        return switch (CommandType.fromString(cmd.name())) {
            case ADD_FRIEND -> cmd.args().length == ADD_FRIEND_ARG_COUNT;
            case CREATE_GROUP -> cmd.args().length >= CREATE_GROUP_MIN_ARG_COUNT;
            case GET_STATUS -> cmd.args().length == GET_STATUS_ARG_COUNT;
            case LOGIN -> cmd.args().length == LOGIN_ARG_COUNT;
            case LOGOUT -> cmd.args().length == LOGOUT_ARG_COUNT;
            case PAYED -> cmd.args().length == PAYED_ARG_COUNT &&
                    cmd.args()[PAYED_LEVS_INDEX].matches(LEVS_REGEX);
            case REGISTER -> cmd.args().length == REGISTER_ARG_COUNT;
            case SPLIT -> cmd.args().length == SPLIT_ARG_COUNT &&
                    cmd.args()[SPLIT_LEVS_INDEX].matches(LEVS_REGEX);
            case SPLIT_GROUP -> cmd.args().length == SPLIT_GROUP_ARG_COUNT &&
                    cmd.args()[SPLIT_GROUP_LEVS_INDEX].matches(LEVS_REGEX);
            case GET_PAYMENT_HISTORY -> cmd.args().length == GET_PAYMENT_HISTORY_ARG_COUNT;
            case HELP -> cmd.args().length == HELP_ARG_COUNT;
        };
    }
}
