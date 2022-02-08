package bg.fmi.mjt.splitwise.server;

import bg.fmi.mjt.splitwise.handlers.InputHander;
import bg.fmi.mjt.splitwise.logger.Logger;
import bg.fmi.mjt.splitwise.logger.LoggerFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class ServerTest {

    static final String HOST = "localhost";
    static final int PORT = 51509;

    static MockedStatic<LoggerFactory> loggerMock = mockStatic(LoggerFactory.class);

    @Mock
    InputHander hander;

    @BeforeAll
    static void setUp() {
        loggerMock.when(() -> LoggerFactory.getLogger(ArgumentMatchers.any())).thenReturn(mock(Logger.class));
    }

    @Test
    void serverStartsAndStopsSuccessfully() {
        var server = new Server(HOST, PORT, hander);
        var thread = new Thread(server);

        assertTimeoutPreemptively(
            Duration.ofSeconds(10),
            () -> {
                thread.start();
                server.stop();
                thread.join();
            },
            "Server did not start and stop in the specified time period"
        );

    }
}