package bg.fmi.mjt.splitwise.service.exceptions;

public class UsernameNotFoundException extends ServiceException {

    public UsernameNotFoundException(String message) {
        super(message);
    }

    public UsernameNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
