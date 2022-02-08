package bg.fmi.mjt.splitwise.client.gui;

import bg.fmi.mjt.splitwise.handlers.InputHander;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Scanner;

public class CLI implements Runnable {

    private final Scanner reader;
    private final PrintWriter writer;

    private final InputHander inputHander;
    private boolean isRunning = false;

    public CLI(Reader reader, Writer writer, InputHander inputHander) {
        this.reader = new Scanner(new BufferedReader(reader));
        this.writer = new PrintWriter(writer, true);
        this.inputHander = inputHander;
    }

    @Override
    public void run() {
        isRunning = true;

        while (isRunning) {
            String clientCommand = readLine();
            String commandResult = evaluate(clientCommand);
            print(commandResult);
        }
    }

    public void stop() {
        isRunning = false;
    }

    private String readLine() {
        return reader.nextLine();
    }

    private String evaluate(String command) {
        return inputHander.handle(command);
    }

    private void print(String s) {
        writer.println(s);
    }
}
