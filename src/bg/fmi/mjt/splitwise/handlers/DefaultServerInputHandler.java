package bg.fmi.mjt.splitwise.handlers;

import bg.fmi.mjt.splitwise.commands.Command;
import bg.fmi.mjt.splitwise.commands.executors.CommandExecutor;
import bg.fmi.mjt.splitwise.requests.CommandRequest;
import com.google.gson.Gson;

import java.util.Objects;


public class DefaultServerInputHandler implements InputHander {

    private final Gson gson = new Gson();

    private final CommandExecutor executor;

    public DefaultServerInputHandler(CommandExecutor executor) {
        this.executor = executor;
    }

    @Override
    public String handle(String input) {
        Objects.requireNonNull(input, "input cannot be null");

        CommandRequest request = gson.fromJson(input, CommandRequest.class);

        return executor.execute(Command.ofNewOwner(request.getCommand(), request.getAuthToken()));
    }
}
