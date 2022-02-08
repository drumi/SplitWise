package bg.fmi.mjt.splitwise.requests;

import bg.fmi.mjt.splitwise.commands.Command;

public class CommandRequest {

    private final Command command;
    private final String authToken;

    public CommandRequest(Command command, String authToken) {
        this.command = command;
        this.authToken = authToken;
    }

    public Command getCommand() {
        return command;
    }

    public String getAuthToken() {
        return authToken;
    }
}
