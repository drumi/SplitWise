package bg.fmi.mjt.splitwise.service.exceptions;

public class GroupNotFoundException extends ServiceException {

    public GroupNotFoundException(String message) {
        super(message);
    }

    public GroupNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}
