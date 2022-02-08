package bg.fmi.mjt.splitwise.service.exceptions;

public class UsernameAlreadyExistsException extends ServiceException {

    public UsernameAlreadyExistsException(String message) {
        super(message);
    }

    public UsernameAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }

}
