package bg.fmi.mjt.splitwise.commands;

public final class Command {

    private final String owner;
    private final String name;
    private final String[] args;

    public static Command ofNewOwner(Command cmd, String owner) {
        return new Command(owner, cmd.name, cmd.args);
    }

    public Command(String name, String[] args) {
        this.name = name;
        this.args = args;

        owner = null;
    }

    public Command(String owner, String name, String[] args) {
        this.name = name;
        this.args = args;
        this.owner = owner;
    }

    public String name() {
        return name;
    }

    public String[] args() {
        return args;
    }

    public String owner() {
        return owner;
    }
}
