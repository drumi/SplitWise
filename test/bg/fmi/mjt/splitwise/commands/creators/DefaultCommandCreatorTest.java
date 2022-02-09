package bg.fmi.mjt.splitwise.commands.creators;

import bg.fmi.mjt.splitwise.commands.Command;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultCommandCreatorTest {

    CommandCreator creator = new DefaultCommandCreator();

    @Test
    void testCreateWithSimpleArguments() {
        var commandString = "delete disk1 disk2 disk3";

        var expectedCommand =
            new Command("delete", new String[] {"disk1", "disk2", "disk3"});

        Command returnedCommand = creator.create(commandString);

        assertEquals(expectedCommand, returnedCommand, "Returned command does not match expected one");
    }

    @Test
    void testCreateWithQuotedArguments() {
        var commandString = "delete disk1 disk2 disk3 \"Cleaning up some space\"";

        // creator should remove quotes
        var expectedCommand =
            new Command("delete", new String[] {"disk1", "disk2", "disk3", "Cleaning up some space"});

        Command returnedCommand = creator.create(commandString);

        assertEquals(expectedCommand, returnedCommand, "Returned command does not match expected one");
    }

    @Test
    void testThrowsOnNull() {
        assertThrows(NullPointerException.class, () -> creator.create(null), "Did not throw on null");
    }
}