package bg.fmi.mjt.splitwise.logger;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Objects;

class DefaultLogger implements Logger, Closeable {

    private static final String LOG_FORMAT = "[%s]|%s|%s|%s";
    private static final String LOG_FILE_NAME = "logs.txt";

    private final LoggerOptions options;

    private PrintWriter writer;

    public DefaultLogger(LoggerOptions options) {
        this.options = options;

        File logFile = options.logDirectory().resolve(LOG_FILE_NAME).toFile();

        try {
            Files.createDirectories(options.logDirectory());
            writer = new PrintWriter(new BufferedWriter(new FileWriter(logFile, true)));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not open/create log file", e);
        }
    }

    @Override
    public void log(Level level, String message) {
        Objects.requireNonNull(message, "Message cannot be null");
        Objects.requireNonNull(level, "Level cannot be null");

        if (level.getPriority() < options.minLogLevel().getPriority()) {
            return;
        }

        synchronized (Logger.class) {
            writer.println(
                String.format(
                    LOG_FORMAT, level.getDisplayName(), LocalDateTime.now(), options.clazz().getCanonicalName(), message
                )
            );
            writer.flush();
        }
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}
