package bg.fmi.mjt.splitwise.service.exceptions;

public class UnauthorizedOperationException extends ServiceException {

    public UnauthorizedOperationException(String message) {
        super(message);
    }

    public UnauthorizedOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
