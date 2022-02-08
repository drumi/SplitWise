package bg.fmi.mjt.splitwise.responses;

public class CommandResponse {

    private boolean wasSuccessful;
    private String errorMessage;
    private String data;

    public static CommandResponse ofError(String message) {
        return new CommandResponse(false, message, null);
    }

    public static CommandResponse ofSuccess(String data) {
        return new CommandResponse(true, null, data);
    }

    public static CommandResponse ofSuccess() {
        return new CommandResponse(true, null, null);
    }

    private CommandResponse(boolean wasSuccessful, String errorMessage, String data) {
        this.wasSuccessful = wasSuccessful;
        this.errorMessage = errorMessage;
        this.data = data;
    }

    public boolean wasSuccessful() {
        return wasSuccessful;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getData() {
        return data;
    }
}
