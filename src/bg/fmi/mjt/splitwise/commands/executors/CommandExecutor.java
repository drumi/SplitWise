package bg.fmi.mjt.splitwise.commands.executors;

import bg.fmi.mjt.splitwise.commands.Command;

@FunctionalInterface
public interface CommandExecutor {

    String execute(Command cmd);

}
