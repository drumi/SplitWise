package bg.fmi.mjt.splitwise.logger;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public final class LoggerFactory {

    private static final Path PATH_TO_LOGS = Paths.get(".", "logs");
    private static final Level MIN_LOG_LEVEL = Level.DEBUG;
    private static final Map<Class<?>, Logger> LOGGERS = new HashMap<>();

    private LoggerFactory() {
    }

    public static Logger getLogger(Class<?> clazz) {
        Objects.requireNonNull(clazz, "clazz cannot be null");

        LOGGERS.computeIfAbsent(clazz, k -> new DefaultLogger(new LoggerOptions(clazz, PATH_TO_LOGS, MIN_LOG_LEVEL)));
        return LOGGERS.get(clazz);
    }

    public static Path getLogsPath() {
        return PATH_TO_LOGS;
    }

}
