package bg.fmi.mjt.splitwise.logger;

import java.nio.file.Path;

public record LoggerOptions(Class<?> clazz, Path logDirectory, Level minLogLevel) {
}
