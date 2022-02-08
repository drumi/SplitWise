package bg.fmi.mjt.splitwise.service.exceptions;

public class GroupAlreadyExistsException extends ServiceException {

    public GroupAlreadyExistsException(String message) {
        super(message);
    }

    public GroupAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }

}
