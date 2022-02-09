package bg.fmi.mjt.splitwise.commands;

import java.util.Arrays;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Command command = (Command) o;

        if (!Objects.equals(owner, command.owner)) {
            return false;
        }
        if (!Objects.equals(name, command.name)) {
            return false;
        }

        return Arrays.equals(args, command.args);
    }

    @Override
    public int hashCode() {
        int result = owner != null ? owner.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(args);
        return result;
    }

    @Override
    public String toString() {
        return "Command{" +
            "owner='" + owner + '\'' +
            ", name='" + name + '\'' +
            ", args=" + Arrays.toString(args) +
            '}';
    }
}
