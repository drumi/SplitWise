package bg.fmi.mjt.splitwise.server;

import bg.fmi.mjt.splitwise.logger.Level;
import bg.fmi.mjt.splitwise.logger.LogUtils;
import bg.fmi.mjt.splitwise.logger.Logger;
import bg.fmi.mjt.splitwise.logger.LoggerFactory;
import bg.fmi.mjt.splitwise.handlers.InputHander;
import bg.fmi.mjt.splitwise.server.exceptions.ServerException;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class Server implements Runnable {

    private static final int BUFFER_SIZE = 8192;
    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);
    private static final Charset TRANSPORTATION_CHARSET = StandardCharsets.UTF_8;

    private final String hostname;
    private final int port;
    private final InputHander inputHander;
    private final ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);

    private Selector selector;
    private ServerSocketChannel serverChannel;
    private boolean isRunning = false;

    public Server(String hostname, int port, InputHander inputHander) {
        this.hostname = hostname;
        this.port = port;
        this.inputHander = inputHander;

        init();
    }

    private void init() {
        LOGGER.log(
            Level.INFO, String.format("Starting a server with hostname '%s' and port '%d'...", hostname, port)
        );

        initSelector();
        initServer();

        registerInSelector(serverChannel, SelectionKey.OP_ACCEPT);

        LOGGER.log(
            Level.INFO, String.format("Server with hostname '%s' and port '%d' successfully started", hostname, port)
        );
    }

    private void initSelector() {
        LOGGER.log(Level.DEBUG, "Initializing selector...");

        try {
            selector = Selector.open();
        } catch (IOException e) {
            throw new ServerException("Cannot open a selector", e);
        }

        LOGGER.log(Level.DEBUG, "Selector finished initializing.");
    }

    private void initServer() {
        LOGGER.log(Level.DEBUG, "Starting server socket...");

        try {
            serverChannel = ServerSocketChannel.open();
            serverChannel.bind(new InetSocketAddress(hostname, port));
            serverChannel.configureBlocking(false);
        } catch (IOException e) {
            throw new ServerException("Cannot initialize the server", e);
        }

        LOGGER.log(Level.DEBUG, "Server socket successfully started");
    }

    private void registerInSelector(SelectableChannel channel, int operations) {
        try {
            channel.register(selector, operations);
        } catch (ClosedChannelException e) {
            LOGGER.log(Level.INFO, "Tried to register an already closed channel.");
        }
    }

    private void close(Closeable c) {
        try {
            c.close();
        } catch (IOException e) {
            LOGGER.log(Level.WARN, "Closing a resource failed. " + LogUtils.stringifyThrowable(e));
        }
    }

    @Override
    public void run() {
        isRunning = true;

        while (isRunning) {

            int readyChannelsCount;

            try {
                readyChannelsCount = selector.select();
            } catch (IOException e) {
                throw new ServerException("Selector's select method failed", e);
            }

            if (readyChannelsCount == 0) {
                continue;
            }

            var keyIter = selector.selectedKeys()
                                  .iterator();

            while (keyIter.hasNext()) {
                SelectionKey key = keyIter.next();

                if (key.isAcceptable()) {
                    handleAccept((ServerSocketChannel) key.channel());
                } else if (key.isReadable()) {
                    handleRead((SocketChannel) key.channel());
                }

                keyIter.remove();
            }
        }

        shutDown();

        LOGGER.log(Level.INFO, "Server shutdown was successful");
    }

    private void handleAccept(ServerSocketChannel server) {
        SocketChannel client;

        LOGGER.log(Level.INFO, "Attempting to accept a client...");

        try {
            client = server.accept();
        } catch (IOException e) {
            LOGGER.log(
                Level.INFO,
                String.format(
                    "Could not accept a client connection. %s",
                    LogUtils.stringifyThrowable(e)
                )
            );

            return;
        }

        LOGGER.log(Level.DEBUG, "Attempting to register a client into selector...");

        try {
            client.configureBlocking(false);
            client.register(selector, SelectionKey.OP_READ);
        } catch (IOException e) {
            throw new ServerException("Registering a client channel into the selector failed", e);
        }

        LOGGER.log(
            Level.INFO,
            String.format("A client successfully connected with address: %s", client.socket().getInetAddress())
        );
    }

    private void handleRead(SocketChannel client) {
        try {
            buffer.clear();
            int r = client.read(buffer);

            if (r == -1) {
                LOGGER.log(
                    Level.INFO,
                    String.format("Client with address: %s disconnected", client.socket().getInetAddress())
                );

                close(client);
            } else if (r > 0) {
                buffer.flip();
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);

                String input = new String(bytes, TRANSPORTATION_CHARSET);
                String result = inputHander.handle(input);

                buffer.clear();
                buffer.put(result.getBytes(TRANSPORTATION_CHARSET));
                buffer.flip();
                client.write(buffer);
            }
        } catch (IOException e) {
            String inet = client.socket().getInetAddress().toString();
            LOGGER.log(
                Level.WARN,
                String.format("Communicating with client: %s failed. Disconnecting client...", inet)
            );

            close(client);

            LOGGER.log(
                Level.INFO, String.format("Successfully disconnected client: %s", inet)
            );
        }
    }

    public void stop() {
        LOGGER.log(Level.INFO, "Shutting down server...");
        isRunning = false;

        if (selector.isOpen()) {
            selector.wakeup();
        }
    }

    private void shutDown() {
        for (var key : selector.keys()) {
            close(key.channel());
        }
    }

}
