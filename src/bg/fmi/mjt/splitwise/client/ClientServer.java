package bg.fmi.mjt.splitwise.client;

import bg.fmi.mjt.splitwise.logger.Level;
import bg.fmi.mjt.splitwise.logger.Logger;
import bg.fmi.mjt.splitwise.logger.LoggerFactory;
import bg.fmi.mjt.splitwise.server.Server;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class ClientServer implements Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientServer.class);
    private static final int BUFFER_SIZE = 8192;
    private static final Charset TRANSPORTATION_CHARSET = StandardCharsets.UTF_8;

    private final ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
    private final InetSocketAddress serverAddress;

    private SocketChannel server;

    private boolean isConnected = false;

    public ClientServer(String serverHostname, int serverPort) {
        serverAddress = new InetSocketAddress(serverHostname, serverPort);
    }


    public void connect() throws IOException {
        if (isConnected) {
            return;
        }

        LOGGER.log(Level.INFO, "Connecting to the server...");

        server = SocketChannel.open();
        server.connect(serverAddress);
        isConnected = true;

        LOGGER.log(Level.INFO, "Connected to the server");
    }

    public void send(String message) throws IOException {
        LOGGER.log(Level.DEBUG, "Sending data to server...");

        buffer.clear();
        buffer.put(message.getBytes(TRANSPORTATION_CHARSET));
        buffer.flip();
        server.write(buffer);
    }

    public String recv() throws IOException {
        LOGGER.log(Level.DEBUG, "Receiving data from server...");

        buffer.clear();
        server.read(buffer);
        buffer.flip();

        byte[] byteArray = new byte[buffer.remaining()];
        buffer.get(byteArray);
        return new String(byteArray, TRANSPORTATION_CHARSET);
    }

    @Override
    public void close() throws IOException {
        LOGGER.log(Level.DEBUG, "Closing client...");

        server.close();

        LOGGER.log(Level.DEBUG, "Closed client...");
    }
}
