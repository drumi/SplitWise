package bg.fmi.mjt.splitwise.commands.executors;

import bg.fmi.mjt.splitwise.client.ClientServer;
import bg.fmi.mjt.splitwise.commands.CommandType;
import bg.fmi.mjt.splitwise.commands.Command;
import bg.fmi.mjt.splitwise.commands.validators.CommandValidator;
import bg.fmi.mjt.splitwise.gson.adapters.LocalDateTimeAdapter;
import bg.fmi.mjt.splitwise.logger.Level;
import bg.fmi.mjt.splitwise.logger.LogUtils;
import bg.fmi.mjt.splitwise.logger.Logger;
import bg.fmi.mjt.splitwise.logger.LoggerFactory;
import bg.fmi.mjt.splitwise.requests.CommandRequest;
import bg.fmi.mjt.splitwise.responses.CommandResponse;
import bg.fmi.mjt.splitwise.responses.LoginSuccessResponse;
import bg.fmi.mjt.splitwise.responses.OwesResponse;
import bg.fmi.mjt.splitwise.responses.PaymentHistoryResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ClientCommandExecutor implements CommandExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientCommandExecutor.class);

    private static final String SUCCESSFUL_OPERATION_MSG = "***Success***";

    private static final String COMMUNICATION_ERROR_MSG =
        "There was an error communicating with server. Please contact support and provide logs in " +
            LoggerFactory.getLogsPath().normalize().toAbsolutePath();

    private final static String NO_NOTIFICATIONS_MSG = "No new notifications to show";
    private final static String NO_OWES_MSG = "No one owes you and you do not owe anyone";
    private final static String YOU_OWE_TEMPLATE = "You owe %s an amount of %.2f levs";
    private final static String USER_OWES_YOU_TEMPLATE = "User %s owes you an amount of %.2f levs";
    private final static String NO_PAYMENT_HISTORY_MSG = "You haven't payed anyone";
    private final static String PAYMENT_TEMPLATE = "You payed %s an amount of %.2f levs on %tF";

    private final Gson gson =
        new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).create();
    private final CommandValidator validator;
    private final ClientServer server;

    private String authToken;

    public ClientCommandExecutor(CommandValidator validator, ClientServer clientServer) {
        this.validator = validator;
        this.server = clientServer;

        connectServer();
    }

    private void connectServer() {
        try {
            server.connect();
        } catch (IOException e) {
            LOGGER.log(Level.ERROR, "Could not connect to the server. Error: " + LogUtils.stringifyThrowable(e));
        }
    }

    @Override
    public String execute(Command cmd) {
        Objects.requireNonNull(cmd, "Command cannot be null");

        if (!validator.isValid(cmd)) {
            return help();
        }

        try {
            return dispatch(cmd);
        } catch (IOException e) {
            LOGGER.log(Level.ERROR, "Communication error: " + LogUtils.stringifyThrowable(e));
            return COMMUNICATION_ERROR_MSG;
        }
    }

    private String dispatch(Command cmd) throws IOException {
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
            case HELP -> help();
        };
    }

    private String getPaymentHistory(Command cmd) throws IOException {
        return handle(
            cmd,
            this::simpleErrorHandler,
            response -> handlePaymentHistoryResponse(fromJson(response.getData(), PaymentHistoryResponse.class))
        );
    }

    private String handlePaymentHistoryResponse(PaymentHistoryResponse response) {
        var payments = response.getPaymentHistory();

        if (payments == null || payments.size() == 0) {
            return NO_PAYMENT_HISTORY_MSG;
        }

        return payments.stream().map(
            payment -> String.format(
                PAYMENT_TEMPLATE,
                payment.getPayedTo(),
                payment.getLevs(),
                payment.getPayedOn()
            )
        ).collect(Collectors.joining(System.lineSeparator()));
    }

    private String getStatus(Command cmd) throws IOException {
        return handle(
            cmd,
            this::simpleErrorHandler,
            response -> handleOwesResponse(fromJson(response.getData(), OwesResponse.class))
        );
    }

    private String handleOwesResponse(OwesResponse data) {
        var userToLevs = data.getUserToLevs();

        if (userToLevs == null || userToLevs.size() == 0) {
            return NO_OWES_MSG;
        }

        return userToLevs.entrySet().stream().map( e -> {
            var key = e.getKey();
            var value = e.getValue();

            if (e.getValue() > 0) {
                return String.format(USER_OWES_YOU_TEMPLATE, key, value);
            } else  {
                return String.format(YOU_OWE_TEMPLATE, key, -value);
            }
        }).collect(Collectors.joining(System.lineSeparator()));
    }

    private String addFriend(Command cmd) throws IOException {
        return handle(cmd, this::simpleErrorHandler, this::simpleSuccessHandler);
    }

    private String createGroup(Command cmd) throws IOException {
        return handle(cmd, this::simpleErrorHandler, this::simpleSuccessHandler);
    }

    private String login(Command cmd) throws IOException {
        return handle(
            cmd,
            this::simpleErrorHandler,
            response -> handleLoginSuccessResponse(fromJson(response.getData(), LoginSuccessResponse.class))
        );
    }

    private String handle(Command cmd,
                          Function<CommandResponse, String> errorHandler,
                          Function<CommandResponse, String> successHandler) throws IOException {

        var request = new CommandRequest(cmd, authToken);

        CommandResponse response = sendAndRecv(request);

        return response.wasSuccessful() ? successHandler.apply(response) : errorHandler.apply(response);
    }

    private String handleLoginSuccessResponse(LoginSuccessResponse data) {
        authToken = data.getAuthToken();
        var notifications = data.getNotifications();

        if (notifications == null || notifications.size() == 0) {
            return NO_NOTIFICATIONS_MSG;
        }
        return notifications.stream().collect(Collectors.joining(System.lineSeparator()));
    }

    private String logout(Command cmd) throws IOException {
        return handle(
            cmd,
            this::simpleErrorHandler,
            response -> {
                authToken = null;
                return simpleSuccessHandler(response);
            });
    }

    private String payed(Command cmd) throws IOException {
        return handle(cmd, this::simpleErrorHandler, this::simpleSuccessHandler);
    }

    private String split(Command cmd) throws IOException {
        return handle(cmd, this::simpleErrorHandler, this::simpleSuccessHandler);
    }

    private String splitGroup(Command cmd) throws IOException {
        return handle(cmd, this::simpleErrorHandler, this::simpleSuccessHandler);
    }

    private String register(Command cmd) throws IOException {
        return handle(cmd, this::simpleErrorHandler, this::simpleSuccessHandler);
    }

    private String help() {
        var sb = new StringBuilder();

        for (var c : CommandType.values()) {
            sb.append(c.getUsage()).append(System.lineSeparator());
        }

        return sb.toString();
    }

    private String simpleErrorHandler(CommandResponse cr) {
        return cr.getErrorMessage();
    }

    private String simpleSuccessHandler(CommandResponse cr) {
        return SUCCESSFUL_OPERATION_MSG;
    }

    private String asJson(Object o) {
        return gson.toJson(o);
    }

    private <T> T fromJson(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }

    private String sendAndRecv(String message) throws IOException {
        server.send(message);
        return server.recv();
    }

    private CommandResponse sendAndRecv(CommandRequest request) throws IOException {
        var response =  sendAndRecv(asJson(request));
        return fromJson(response, CommandResponse.class);
    }

}
