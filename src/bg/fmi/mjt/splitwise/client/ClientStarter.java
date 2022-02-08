package bg.fmi.mjt.splitwise.client;

import bg.fmi.mjt.splitwise.commands.creators.DefaultCommandCreator;
import bg.fmi.mjt.splitwise.commands.executors.ClientCommandExecutor;
import bg.fmi.mjt.splitwise.commands.validators.DefaultCommandValidator;
import bg.fmi.mjt.splitwise.client.gui.CLI;
import bg.fmi.mjt.splitwise.handlers.DefaulClientInputHandler;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class ClientStarter {

    private static final String HOST = "localhost";
    private static final int PORT = 7777;

    public static void main(String[] args) {

        var clientServer = new ClientServer(HOST, PORT);

        var validator = new DefaultCommandValidator();
        var executor = new ClientCommandExecutor(validator, clientServer);
        var creator = new DefaultCommandCreator();

        var handler = new DefaulClientInputHandler(executor, creator);

        var cli = new CLI(new InputStreamReader(System.in), new OutputStreamWriter(System.out), handler);

        cli.run();
    }
}
