package bg.fmi.mjt.splitwise.commands.creators;

import bg.fmi.mjt.splitwise.commands.Command;

@FunctionalInterface
public interface CommandCreator {

    Command create(String input);

}
