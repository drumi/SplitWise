package bg.fmi.mjt.splitwise.commands.creators;

import bg.fmi.mjt.splitwise.commands.Command;

import java.util.Arrays;

public class DefaultCommandCreator implements CommandCreator {

    // Match me greedily at least one whitespaces that have even number(or 0) of quotes after them
    private static final String IGNORE_QUOTED_SPLIT_REGEX = "\\s+(?=([^\"]*\"[^\"]*\"[^\"]*)*[^\"]*$)";

    @Override
    public Command create(String input) {
        var tokens = Arrays.stream(input.trim().split(IGNORE_QUOTED_SPLIT_REGEX))
                           .map(s -> s.replace("\"", "").trim())
                           .toArray(String[]::new);

        return new Command(
            tokens[0],
            Arrays.copyOfRange(tokens, 1, tokens.length)
        );
    }
}