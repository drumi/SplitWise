package bg.fmi.mjt.splitwise.server;

import bg.fmi.mjt.splitwise.handlers.InputHander;
import bg.fmi.mjt.splitwise.logger.Logger;
import bg.fmi.mjt.splitwise.logger.LoggerFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServerTest {

    static final Charset TRANSPORTATION_CHARSET = StandardCharsets.UTF_8;

    static final String HOST = "localhost";
    static final int PORT = 51509;

    static final int READ_PORT = 51510;

    static MockedStatic<LoggerFactory> loggerMock = mockStatic(LoggerFactory.class);

    @Mock
    InputHander hander;

    @BeforeAll
    static void setUp() {
        loggerMock.when(() -> LoggerFactory.getLogger(any())).thenReturn(mock(Logger.class));
    }

    @AfterAll
    static void afterAll() {
        loggerMock.close();
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

    @Test
    void serverReadsSendedData() {
        String sended = "The string to send";
        when(hander.handle(sended)).thenReturn(sended);

        var server = new Server(HOST, READ_PORT, hander);
        var thread = new Thread(server);

        assertTimeoutPreemptively(
            Duration.ofSeconds(10),
            () -> {
                thread.start();

                var socket = new Socket(HOST, READ_PORT);
                var out = socket.getOutputStream();

                out.write(sended.getBytes(TRANSPORTATION_CHARSET));
                out.flush();

                var in = socket.getInputStream();

                var bytes = new byte[64];
                var r = in.read(bytes);
                String read = new String(bytes, 0, r, TRANSPORTATION_CHARSET);
                server.stop();

                assertEquals(sended, read);

                out.close();
                in.close();

                thread.join();
            },
            "Server did not echo the data in the specified time period"
        );
    }
}