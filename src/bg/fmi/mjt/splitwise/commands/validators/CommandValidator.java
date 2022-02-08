package bg.fmi.mjt.splitwise.commands.validators;

import bg.fmi.mjt.splitwise.commands.Command;

@FunctionalInterface
public interface CommandValidator {

    boolean isValid(Command cmd);

}
