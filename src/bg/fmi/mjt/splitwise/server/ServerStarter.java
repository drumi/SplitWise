package bg.fmi.mjt.splitwise.server;

import bg.fmi.mjt.splitwise.commands.executors.ServerCommandExecutor;
import bg.fmi.mjt.splitwise.commands.validators.DefaultCommandValidator;
import bg.fmi.mjt.splitwise.gson.adapters.LocalDateTimeAdapter;
import bg.fmi.mjt.splitwise.handlers.DefaultServerInputHandler;
import bg.fmi.mjt.splitwise.service.Service;
import bg.fmi.mjt.splitwise.storage.dao.FileDao;
import bg.fmi.mjt.splitwise.storage.models.Group;
import bg.fmi.mjt.splitwise.storage.models.Payment;
import bg.fmi.mjt.splitwise.storage.models.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.nio.file.Path;
import java.time.LocalDateTime;

public class ServerStarter {

    private static final Path PATH_TO_USER_DB = Path.of("db", "users");
    private static final Path PATH_TO_GROUP_DB = Path.of("db", "groups");
    private static final Path PATH_TO_PAYMENT_DB = Path.of("db", "payments");

    private static final String HOST = "localhost";
    private static final int PORT = 7777;

    private static final Gson GSON =
        new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).create();

    public static void main(String[] args) {
        var userDao = new FileDao<>(PATH_TO_USER_DB, User.class);
        var groupDao = new FileDao<>(PATH_TO_GROUP_DB, Group.class);
        var paymentDao = new FileDao<>(PATH_TO_PAYMENT_DB, GSON, Payment.class);

        var service = new Service(userDao, groupDao, paymentDao);

        var validator = new DefaultCommandValidator();

        var executor = new ServerCommandExecutor(validator, service);

        var handler = new DefaultServerInputHandler(executor);

        var server = new Server(HOST, PORT, handler);

        server.run();

    }

}
