package bg.fmi.mjt.splitwise.client;

import bg.fmi.mjt.splitwise.logger.Logger;
import bg.fmi.mjt.splitwise.logger.LoggerFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class ClientServerTest {

    static final String HOST = "localhost";
    static final int PORT = 51508;

    static MockedStatic<LoggerFactory> loggerMock = mockStatic(LoggerFactory.class);

    @BeforeAll
    static void beforeAll() {
        loggerMock.when(() -> LoggerFactory.getLogger(any())).thenReturn(mock(Logger.class));
    }

    @AfterAll
    static void afterAll() {
        loggerMock.close();
    }

    @Test
    void echoTest() throws IOException {
        var socket = new ServerSocket();
        socket.bind(new InetSocketAddress(HOST, PORT));

        assertTimeoutPreemptively(
            Duration.ofSeconds(10),
            () -> {
                var clientServer = new ClientServer(HOST, PORT);
                clientServer.connect();

                var clientConnection = socket.accept();

                var message = "the message to echo";
                clientServer.send(message);

                var in = clientConnection.getInputStream();
                var out = clientConnection.getOutputStream();

                byte[] bytes = new byte[64];
                int bytesRead = in.read(bytes);
                out.write(bytes, 0, bytesRead);
                out.flush();

                var receivedMessage = clientServer.recv();
                assertEquals(message, receivedMessage, "Did not echo the correct message");

                clientServer.close();
                clientConnection.close();
                socket.close();
            },
            "Server did not echo the data in the specified time period"
        );

    }
}