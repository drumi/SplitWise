package bg.fmi.mjt.splitwise.service.exceptions;

public class UnsuccessfulLoginException extends ServiceException {

    public UnsuccessfulLoginException(String message) {
        super(message);
    }

    public UnsuccessfulLoginException(String message, Throwable cause) {
        super(message, cause);
    }

}
