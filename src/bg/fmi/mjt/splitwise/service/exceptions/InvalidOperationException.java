package bg.fmi.mjt.splitwise.service.exceptions;

public class InvalidOperationException extends ServiceException {

    public InvalidOperationException(String message) {
        super(message);
    }

    public InvalidOperationException(String message, Throwable cause) {
        super(message, cause);
    }

}
