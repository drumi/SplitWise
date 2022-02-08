package bg.fmi.mjt.splitwise.logger;

import java.util.Arrays;

public class LogUtils {

    private LogUtils() {
    }

    public static String stringifyThrowable(Throwable t) {
        return String.format(
            "{ Exception class: %s; Message: %s; StackTrace: %s }",
            t.getClass().getCanonicalName(),
            t.getMessage(),
            Arrays.toString(t.getStackTrace())
        );
    }
}
