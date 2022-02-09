package bg.fmi.mjt.splitwise.commands.executors;

import bg.fmi.mjt.splitwise.commands.Command;
import bg.fmi.mjt.splitwise.commands.CommandType;
import bg.fmi.mjt.splitwise.commands.validators.CommandValidator;
import bg.fmi.mjt.splitwise.gson.adapters.LocalDateTimeAdapter;
import bg.fmi.mjt.splitwise.logger.Level;
import bg.fmi.mjt.splitwise.logger.Logger;
import bg.fmi.mjt.splitwise.logger.LoggerFactory;
import bg.fmi.mjt.splitwise.responses.CommandResponse;
import bg.fmi.mjt.splitwise.responses.LoginSuccessResponse;
import bg.fmi.mjt.splitwise.responses.OwesResponse;
import bg.fmi.mjt.splitwise.responses.Payment;
import bg.fmi.mjt.splitwise.responses.PaymentHistoryResponse;
import bg.fmi.mjt.splitwise.service.Service;
import bg.fmi.mjt.splitwise.service.exceptions.ServiceException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static bg.fmi.mjt.splitwise.commands.CommandType.Indexes.ADD_FRIEND_FRIEND_USERNAME_INDEX;
import static bg.fmi.mjt.splitwise.commands.CommandType.Indexes.CREATE_CROUP_GROUP_NAME_INDEX;
import static bg.fmi.mjt.splitwise.commands.CommandType.Indexes.CREATE_CROUP_USERNAMES_START_INDEX;
import static bg.fmi.mjt.splitwise.commands.CommandType.Indexes.LOGIN_PASSWORD_INDEX;
import static bg.fmi.mjt.splitwise.commands.CommandType.Indexes.LOGIN_USERNAME_INDEX;
import static bg.fmi.mjt.splitwise.commands.CommandType.Indexes.PAYED_AMOUNT_INDEX;
import static bg.fmi.mjt.splitwise.commands.CommandType.Indexes.PAYED_FRIEND_USERNAME_INDEX;
import static bg.fmi.mjt.splitwise.commands.CommandType.Indexes.REGISTER_PASSWORD_INDEX;
import static bg.fmi.mjt.splitwise.commands.CommandType.Indexes.REGISTER_USERNAME_INDEX;
import static bg.fmi.mjt.splitwise.commands.CommandType.Indexes.SPLIT_AMOUNT_INDEX;
import static bg.fmi.mjt.splitwise.commands.CommandType.Indexes.SPLIT_FRIEND_USERNAME_INDEX;
import static bg.fmi.mjt.splitwise.commands.CommandType.Indexes.SPLIT_GROUP_AMOUNT_INDEX;
import static bg.fmi.mjt.splitwise.commands.CommandType.Indexes.SPLIT_GROUP_NAME_INDEX;
import static bg.fmi.mjt.splitwise.commands.CommandType.Indexes.SPLIT_GROUP_REASON_FOR_PAYMENT_INDEX;
import static bg.fmi.mjt.splitwise.commands.CommandType.Indexes.SPLIT_REASON_FOR_PAYMENT_INDEX;

public class ServerCommandExecutor implements CommandExecutor {

    private static final Gson GSON =
        new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).create();

    private static final String LOGGER_DEBUG_COMMAND_FORMAT = "Executing \"%s\" command...";
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerCommandExecutor.class);

    private static final String INVALID_COMMAND_MESSAGE = "This command is invalid!";
    private static final String INVALID_COMMAND_RESPONSE = new Gson().toJson(
        CommandResponse.ofError(INVALID_COMMAND_MESSAGE)
    );
    private static final String SUCCESSFUL_COMMAND_RESPONSE = new Gson().toJson(
        CommandResponse.ofSuccess()
    );

    private final CommandValidator validator;
    private final Service service;


    public ServerCommandExecutor(CommandValidator validator, Service service) {
        this.validator = validator;
        this.service = service;
    }

    @Override
    public String execute(Command cmd) {
        Objects.requireNonNull(cmd, "Command cannot be null");

        if (!validator.isValid(cmd)) {
            return INVALID_COMMAND_RESPONSE;
        }

        return dispatch(cmd);
    }

    private String dispatch(Command cmd) {
        return switch (CommandType.fromString(cmd.name())) {
            case ADD_FRIEND -> addFriend(cmd);
            case CREATE_GROUP -> createGroup(cmd);
            case GET_STATUS -> getStatus(cmd);
            case LOGIN -> login(cmd);
            case LOGOUT -> logout(cmd);
            case PAYED -> payed(cmd);
            case REGISTER -> register(cmd);
            case SPLIT -> split(cmd);
            case SPLIT_GROUP -> splitGroup(cmd);
            case GET_PAYMENT_HISTORY -> getPaymentHistory(cmd);
            default -> INVALID_COMMAND_RESPONSE;
        };
    }

    private String addFriend(Command cmd) {
        LOGGER.log(Level.DEBUG, String.format(LOGGER_DEBUG_COMMAND_FORMAT, "addFriend"));

        String friendUsername = cmd.args()[ADD_FRIEND_FRIEND_USERNAME_INDEX];

        try {
            service.addFriend(cmd.owner(), friendUsername);
            return SUCCESSFUL_COMMAND_RESPONSE;
        } catch (ServiceException e) {
            return GSON.toJson(CommandResponse.ofError(e.getMessage()));
        }
    }

    private String createGroup(Command cmd) {
        LOGGER.log(Level.DEBUG, String.format(LOGGER_DEBUG_COMMAND_FORMAT, "createGroup"));

        String groupName = cmd.args()[CREATE_CROUP_GROUP_NAME_INDEX];
        Set<String> friendUsernames = Arrays.stream(cmd.args())
                                            .skip(CREATE_CROUP_USERNAMES_START_INDEX)
                                            .collect(Collectors.toSet());
        try {
            service.createGroup(
                cmd.owner(),
                groupName,
                friendUsernames
            );
            return SUCCESSFUL_COMMAND_RESPONSE;
        } catch (ServiceException e) {
            return GSON.toJson(CommandResponse.ofError(e.getMessage()));
        }
    }

    private String getStatus(Command cmd) {
        LOGGER.log(Level.DEBUG, String.format(LOGGER_DEBUG_COMMAND_FORMAT, "getStatus"));

        try {
            Map<String, Double> debts = service.getStatus(cmd.owner());

            String data = GSON.toJson(new OwesResponse(debts));

            return GSON.toJson(CommandResponse.ofSuccess(data));
        } catch (ServiceException e) {
            return GSON.toJson(CommandResponse.ofError(e.getMessage()));
        }
    }

    private String login(Command cmd) {
        LOGGER.log(Level.DEBUG, String.format(LOGGER_DEBUG_COMMAND_FORMAT, "login"));

        String username = cmd.args()[LOGIN_USERNAME_INDEX];
        String password = cmd.args()[LOGIN_PASSWORD_INDEX];

        String authToken;

        try {
            authToken = service.login(username, password);

            List<String> notifications = service.getAndDeleteNotifications(authToken);

            var data = GSON.toJson(new LoginSuccessResponse(authToken, notifications));

            return GSON.toJson(CommandResponse.ofSuccess(data));
        } catch (ServiceException e) {
            return GSON.toJson(CommandResponse.ofError(e.getMessage()));
        }
    }

    private String logout(Command cmd) {
        LOGGER.log(Level.DEBUG, String.format(LOGGER_DEBUG_COMMAND_FORMAT, "logout"));

        service.logout(cmd.owner());
        return SUCCESSFUL_COMMAND_RESPONSE;
    }

    private String payed(Command cmd) {
        LOGGER.log(Level.DEBUG, String.format(LOGGER_DEBUG_COMMAND_FORMAT, "payed"));

        double amount = Double.parseDouble(cmd.args()[PAYED_AMOUNT_INDEX]);
        String friendUsername = cmd.args()[PAYED_FRIEND_USERNAME_INDEX];

        try {
            service.payed(cmd.owner(), friendUsername, amount);
            return SUCCESSFUL_COMMAND_RESPONSE;
        } catch (ServiceException e) {
            return GSON.toJson(CommandResponse.ofError(e.getMessage()));
        }
    }

    private String register(Command cmd) {
        LOGGER.log(Level.DEBUG, String.format(LOGGER_DEBUG_COMMAND_FORMAT, "register"));

        String username = cmd.args()[REGISTER_USERNAME_INDEX];
        String password = cmd.args()[REGISTER_PASSWORD_INDEX];

        try {
            service.register(username, password);
            return SUCCESSFUL_COMMAND_RESPONSE;
        } catch (ServiceException e) {
            return GSON.toJson(CommandResponse.ofError(e.getMessage()));
        }
    }

    private String split(Command cmd) {
        LOGGER.log(Level.DEBUG, String.format(LOGGER_DEBUG_COMMAND_FORMAT, "split"));

        double amount = Double.parseDouble(cmd.args()[SPLIT_AMOUNT_INDEX]);
        String friendUsername = cmd.args()[SPLIT_FRIEND_USERNAME_INDEX];
        String reason = cmd.args()[SPLIT_REASON_FOR_PAYMENT_INDEX];

        try {
            service.split(
                cmd.owner(),
                friendUsername,
                amount,
                reason
            );
            return SUCCESSFUL_COMMAND_RESPONSE;
        } catch (ServiceException e) {
            return GSON.toJson(CommandResponse.ofError(e.getMessage()));
        }
    }

    private String splitGroup(Command cmd) {
        LOGGER.log(Level.DEBUG, String.format(LOGGER_DEBUG_COMMAND_FORMAT, "splitGroup"));

        double amount = Double.parseDouble(cmd.args()[SPLIT_GROUP_AMOUNT_INDEX]);
        String groupName = cmd.args()[SPLIT_GROUP_NAME_INDEX];
        String reason = cmd.args()[SPLIT_GROUP_REASON_FOR_PAYMENT_INDEX];

        try {
            service.splitGroup(
                cmd.owner(),
                groupName,
                amount,
                reason
            );
            return SUCCESSFUL_COMMAND_RESPONSE;
        } catch (ServiceException e) {
            return GSON.toJson(CommandResponse.ofError(e.getMessage()));
        }
    }

    private String getPaymentHistory(Command cmd) {
        LOGGER.log(Level.DEBUG, String.format(LOGGER_DEBUG_COMMAND_FORMAT, "getPaymentHistory"));

        try {
            List<bg.fmi.mjt.splitwise.storage.models.Payment> payments = service.getPaymentHistory(cmd.owner());

            var responsePayments =
                payments.stream()
                        .map(payment -> new Payment(payment.toId(), payment.levs(), payment.datePayed()))
                        .toList();

            var data = GSON.toJson(new PaymentHistoryResponse(responsePayments));

            return GSON.toJson(CommandResponse.ofSuccess(data));
        } catch (ServiceException e) {
            return GSON.toJson(CommandResponse.ofError(e.getMessage()));
        }
    }

}
