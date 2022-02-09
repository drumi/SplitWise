package bg.fmi.mjt.splitwise.commands;

import java.util.HashMap;
import java.util.Map;

public enum CommandType {

    ADD_FRIEND("add-friend", "add-friend <username>"),
    CREATE_GROUP("create-group", "create-group <group_name> <username> <username> ... <username>"),
    GET_STATUS("get-status", "get-status"),
    LOGIN("login", "login <username> <password>"),
    LOGOUT("logout", "logout"),
    PAYED("payed", "payed <amount> <username>"),
    REGISTER("register", "register <username> <password>"),
    SPLIT("split", "split <amount> <username> <reason_for_payment>"),
    SPLIT_GROUP("split-group", "split-group <amount> <group_name> <reason_for_payment>"),
    GET_PAYMENT_HISTORY("get-payment-history", "get-payment-history"),
    HELP("help", "help");

    private static final Map<String, CommandType> MAP = new HashMap<>();

    private final String name;
    private final String usage;

    static {
        for (CommandType t : CommandType.values()) {
            MAP.put(t.name, t);
        }
    }

    CommandType(String name, String usage) {
        this.name = name;
        this.usage = usage;
    }

    public static CommandType fromString(String token) {
        return MAP.get(token);
    }

    public String cmdName() {
        return name;
    }

    public String getUsage() {
        return usage;
    }

    public static class Indexes {
        public static final int ADD_FRIEND_FRIEND_USERNAME_INDEX = 0;

        public static final int CREATE_CROUP_GROUP_NAME_INDEX = 0;
        public static final  int CREATE_CROUP_USERNAMES_START_INDEX = 1;

        public static final int LOGIN_USERNAME_INDEX = 0;
        public static final  int LOGIN_PASSWORD_INDEX = 1;

        public static final int REGISTER_USERNAME_INDEX = 0;
        public static final int REGISTER_PASSWORD_INDEX = 1;

        public static final int PAYED_AMOUNT_INDEX = 0;
        public static final  int PAYED_FRIEND_USERNAME_INDEX = 1;

        public static final int SPLIT_AMOUNT_INDEX = 0;
        public static final int SPLIT_FRIEND_USERNAME_INDEX = 1;
        public static final int SPLIT_REASON_FOR_PAYMENT_INDEX = 2;

        public static final int SPLIT_GROUP_AMOUNT_INDEX = 0;
        public static final int SPLIT_GROUP_NAME_INDEX = 1;
        public static final int SPLIT_GROUP_REASON_FOR_PAYMENT_INDEX = 2;
    }

}

