package bg.fmi.mjt.splitwise.logger;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class DefaultLoggerTest {

    private static final String DIRECTORY = "tmp3";
    private static final String LOG_FILE_NAME = "logs.txt";
    private static final Path DIRECTORY_PATH = Path.of(DIRECTORY);

    private static final String LOGS_DELIMITER_REGEX = "\\|";

    private static final int LEVEL_INDEX = 0;
    private static final int TIMESTAMP_INDEX = 1;
    private static final int PACKAGE_INDEX = 2;
    private static final int MESSAGE_INDEX = 3;

    private static final int SECTIONS_COUNT = 4;

    static final LocalDateTime RETURNED_DATE = LocalDateTime.of(2021, 12, 12, 10, 30);

    static final MockedStatic<LocalDateTime> datetimeMock = Mockito.mockStatic(LocalDateTime.class);
    DefaultLogger logger;

    @BeforeAll
    static void setUp() throws IOException {
        datetimeMock.when(() -> LocalDateTime.now()).thenReturn(RETURNED_DATE);
        try {
            Files.createDirectory(DIRECTORY_PATH);
        } catch (DirectoryNotEmptyException e) {
            deleteFiles();
        }
    }

    @AfterAll
    static void tearDown() throws IOException {
        datetimeMock.close();
        Files.delete(DIRECTORY_PATH);
    }

    @BeforeEach
    void beforeEach() {
        logger = new DefaultLogger(new LoggerOptions(Object.class, DIRECTORY_PATH, Level.DEBUG));
    }

    @AfterEach
    void afterEach() throws IOException {
        logger.close();

        deleteFiles();
    }

    private static void deleteFiles() throws IOException {
        DirectoryStream<Path> files = Files.newDirectoryStream(DIRECTORY_PATH, "*.txt");

        for (var file : files) {
            Files.deleteIfExists(file);
        }
    }

    @Test
    void testThrowsOnNullMessage() {
        assertThrows(NullPointerException.class, () -> logger.log(Level.INFO, null), "log should throw on null message");
    }

    @Test
    void testThrowsOnNullLevel() {
        assertThrows(NullPointerException.class, () -> logger.log(null, "message"), "log should throw on null level");
    }

    @Test
    void testLogDebug() throws IOException {
        final String toLog = "Something happened";

        logger.log(Level.DEBUG, toLog);
        logger.close();

        var lines = Files.readAllLines(DIRECTORY_PATH.resolve(LOG_FILE_NAME));
        String[] tokens = lines.get(0).split(LOGS_DELIMITER_REGEX, SECTIONS_COUNT);

        assertEquals(toLog, tokens[MESSAGE_INDEX], "Logged message is not equal to the original one");
        assertEquals(RETURNED_DATE.toString(), tokens[TIMESTAMP_INDEX], "Timestamp is not equal to the mocked one");
        assertEquals("[" + Level.DEBUG.getDisplayName() + "]", tokens[LEVEL_INDEX], "Level is not same as the logged one");
        assertEquals(Object.class.getCanonicalName(), tokens[PACKAGE_INDEX], "Package name is not equal to the original one");
    }

    @Test
    void testLogInfo() throws IOException {
        final String toLog = "Something happened";

        logger.log(Level.INFO, toLog);
        logger.close();

        var lines = Files.readAllLines(DIRECTORY_PATH.resolve(LOG_FILE_NAME));
        String[] tokens = lines.get(0).split(LOGS_DELIMITER_REGEX, SECTIONS_COUNT);

        assertEquals(toLog, tokens[MESSAGE_INDEX], "Logged message is not equal to the original one");
        assertEquals(RETURNED_DATE.toString(), tokens[TIMESTAMP_INDEX], "Timestamp is not equal to the mocked one");
        assertEquals("[" + Level.INFO.getDisplayName() + "]", tokens[LEVEL_INDEX], "Level is not same as the logged one");
        assertEquals(Object.class.getCanonicalName(), tokens[PACKAGE_INDEX], "Package name is not equal to the original one");
    }

    @Test
    void testLogWarn() throws IOException {
        final String toLog = "Something happened";

        logger.log(Level.WARN, toLog);
        logger.close();

        var lines = Files.readAllLines(DIRECTORY_PATH.resolve(LOG_FILE_NAME));
        String[] tokens = lines.get(0).split(LOGS_DELIMITER_REGEX, SECTIONS_COUNT);

        assertEquals(toLog, tokens[MESSAGE_INDEX], "Logged message is not equal to the original one");
        assertEquals(RETURNED_DATE.toString(), tokens[TIMESTAMP_INDEX], "Timestamp is not equal to the mocked one");
        assertEquals("[" + Level.WARN.getDisplayName() + "]", tokens[LEVEL_INDEX], "Level is not same as the logged one");
        assertEquals(Object.class.getCanonicalName(), tokens[PACKAGE_INDEX], "Package name is not equal to the original one");
    }

    @Test
    void testLogError() throws IOException {
        final String toLog = "Something happened";

        logger.log(Level.ERROR, toLog);
        logger.close();

        var lines = Files.readAllLines(DIRECTORY_PATH.resolve(LOG_FILE_NAME));
        String[] tokens = lines.get(0).split(LOGS_DELIMITER_REGEX, SECTIONS_COUNT);

        assertEquals(toLog, tokens[MESSAGE_INDEX], "Logged message is not equal to the original one");
        assertEquals(RETURNED_DATE.toString(), tokens[TIMESTAMP_INDEX], "Timestamp is not equal to the mocked one");
        assertEquals("[" + Level.ERROR.getDisplayName() + "]", tokens[LEVEL_INDEX], "Level is not same as the logged one");
        assertEquals(Object.class.getCanonicalName(), tokens[PACKAGE_INDEX], "Package name is not equal to the original one");
    }

    @Test
    void testMinLogLevel() throws IOException {
        DefaultLogger logger = new DefaultLogger(new LoggerOptions(Object.class, DIRECTORY_PATH, Level.WARN));

        final String debugLog = "Debug log message";
        final String infoLog = "Info log message";
        final String warnLog = "Warn log message";
        final String errorLog = "Error log message";

        logger.log(Level.DEBUG, debugLog);
        logger.log(Level.INFO, infoLog);
        logger.log(Level.WARN, warnLog);
        logger.log(Level.ERROR, errorLog);
        logger.close();

        var lines = Files.readAllLines(DIRECTORY_PATH.resolve(LOG_FILE_NAME));
        String[][] tokens = lines.stream()
                                 .map(line -> line.split(LOGS_DELIMITER_REGEX, SECTIONS_COUNT))
                                 .toArray(String[][]::new);

        assertEquals(warnLog, tokens[0][MESSAGE_INDEX], "Logged message is not equal to the expected one");
        assertEquals(RETURNED_DATE.toString(), tokens[0][TIMESTAMP_INDEX], "Timestamp is not equal to the mocked one");
        assertEquals("[" + Level.WARN.getDisplayName() + "]", tokens[0][LEVEL_INDEX], "Level is not same as the expected one");
        assertEquals(Object.class.getCanonicalName(), tokens[0][PACKAGE_INDEX], "Package name is not equal to the expected one");

        assertEquals(errorLog, tokens[1][MESSAGE_INDEX], "Logged message is not equal to the expected one");
        assertEquals(RETURNED_DATE.toString(), tokens[1][TIMESTAMP_INDEX], "Timestamp is not equal to the mocked one");
        assertEquals("[" + Level.ERROR.getDisplayName() + "]", tokens[1][LEVEL_INDEX], "Level is not same as the expected one");
        assertEquals(Object.class.getCanonicalName(), tokens[1][PACKAGE_INDEX], "Package name is not equal to the expected one");
    }

    @Test
    void testWithMultipleLogging() throws IOException {

        final String debugLog = "Debug log message";
        final String infoLog = "Info log message";
        final String warnLog = "Warn log message";
        final String errorLog = "Error log message";

        logger.log(Level.DEBUG, debugLog);
        logger.log(Level.INFO, infoLog);
        logger.log(Level.WARN, warnLog);
        logger.log(Level.ERROR, errorLog);
        logger.close();

        var lines = Files.readAllLines(DIRECTORY_PATH.resolve(LOG_FILE_NAME));
        String[][] tokens = lines.stream()
                                 .map(line -> line.split(LOGS_DELIMITER_REGEX, SECTIONS_COUNT))
                                 .toArray(String[][]::new);

        assertEquals(debugLog, tokens[0][MESSAGE_INDEX], "Logged message is not equal to the original one");
        assertEquals(RETURNED_DATE.toString(), tokens[0][TIMESTAMP_INDEX], "Timestamp is not equal to the mocked one");
        assertEquals("[" + Level.DEBUG.getDisplayName() + "]", tokens[0][LEVEL_INDEX], "Level is not same as the logged one");
        assertEquals(Object.class.getCanonicalName(), tokens[0][PACKAGE_INDEX], "Package name is not equal to the original one");

        assertEquals(infoLog, tokens[1][MESSAGE_INDEX], "Logged message is not equal to the original one");
        assertEquals(RETURNED_DATE.toString(), tokens[1][TIMESTAMP_INDEX], "Timestamp is not equal to the mocked one");
        assertEquals("[" + Level.INFO.getDisplayName() + "]", tokens[1][LEVEL_INDEX], "Level is not same as the logged one");
        assertEquals(Object.class.getCanonicalName(), tokens[1][PACKAGE_INDEX], "Package name is not equal to the original one");

        assertEquals(warnLog, tokens[2][MESSAGE_INDEX], "Logged message is not equal to the original one");
        assertEquals(RETURNED_DATE.toString(), tokens[2][TIMESTAMP_INDEX], "Timestamp is not equal to the mocked one");
        assertEquals("[" + Level.WARN.getDisplayName() + "]", tokens[2][LEVEL_INDEX], "Level is not same as the logged one");
        assertEquals(Object.class.getCanonicalName(), tokens[2][PACKAGE_INDEX], "Package name is not equal to the original one");

        assertEquals(errorLog, tokens[3][MESSAGE_INDEX], "Logged message is not equal to the original one");
        assertEquals(RETURNED_DATE.toString(), tokens[3][TIMESTAMP_INDEX], "Timestamp is not equal to the mocked one");
        assertEquals("[" + Level.ERROR.getDisplayName() + "]", tokens[3][LEVEL_INDEX], "Level is not same as the logged one");
        assertEquals(Object.class.getCanonicalName(), tokens[3][PACKAGE_INDEX], "Package name is not equal to the original one");
    }
}