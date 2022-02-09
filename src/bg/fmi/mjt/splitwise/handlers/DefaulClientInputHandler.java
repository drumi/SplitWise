package bg.fmi.mjt.splitwise.handlers;

import bg.fmi.mjt.splitwise.commands.Command;
import bg.fmi.mjt.splitwise.commands.creators.CommandCreator;
import bg.fmi.mjt.splitwise.commands.executors.CommandExecutor;

import java.util.Objects;

public class DefaulClientInputHandler implements InputHander {

    private final CommandExecutor executor;
    private final CommandCreator creator;

    public DefaulClientInputHandler(CommandExecutor executor, CommandCreator creator) {
        this.executor = executor;
        this.creator = creator;
    }

    @Override
    public String handle(String input) {
        Objects.requireNonNull(input, "input cannot be null");

        Command command = creator.create(input);

        return executor.execute(command);
    }

}
