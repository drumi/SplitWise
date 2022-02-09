package bg.fmi.mjt.splitwise.commands.validators;

import bg.fmi.mjt.splitwise.commands.Command;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultCommandValidatorTest {

    private static final String VALID_COMMAND_MSG = "This command should be valid. Command: ";

    CommandValidator validator = new DefaultCommandValidator();

    @Test
    void testIsValidReturnsTrueOnGetPaymentHistoryCommand() {
        var cmd = new Command("get-payment-history", new String[]{});

        assertTrue(validator.isValid(cmd), VALID_COMMAND_MSG + cmd);


    }

    @Test
    void testIsValidReturnsTrueOnGetStatusCommand() {
        var cmd = new Command("get-status", new String[]{});

        assertTrue(validator.isValid(cmd), VALID_COMMAND_MSG + cmd);
    }

    @Test
    void testIsValidReturnsTrueOnAddFriendCommand() {
        var cmd = new Command("add-friend", new String[]{"ivan"});

        assertTrue(validator.isValid(cmd), VALID_COMMAND_MSG + cmd);
    }

    @Test
    void testIsValidReturnsTrueOnCreateGroupCommand() {
        var cmd = new Command("create-group", new String[]{"group", "ivan", "ivan2"});

        assertTrue(validator.isValid(cmd), VALID_COMMAND_MSG + cmd);
    }

    @Test
    void testIsValidReturnsTrueOnRegisterCommand() {
        var cmd = new Command("register", new String[]{"username", "password"});

        assertTrue(validator.isValid(cmd), VALID_COMMAND_MSG + cmd);
    }

    @Test
    void testIsValidReturnsTrueOnLoginCommand() {
        var cmd = new Command("login", new String[]{"username", "password"});

        assertTrue(validator.isValid(cmd), VALID_COMMAND_MSG + cmd);
    }

    @Test
    void testIsValidReturnsTrueOnLogoutCommand() {
        var cmd = new Command("logout", new String[]{});

        assertTrue(validator.isValid(cmd), VALID_COMMAND_MSG + cmd);
    }

    @Test
    void testIsValidReturnsTrueOnPayedCommand() {
        var cmd = new Command("payed", new String[]{"10.43", "ivan"});

        assertTrue(validator.isValid(cmd), VALID_COMMAND_MSG + cmd);
    }

    @Test
    void testIsValidReturnsTrueOnSplitCommand() {
        var cmd = new Command("split", new String[]{"1234", "petar", "the reason"});

        assertTrue(validator.isValid(cmd), VALID_COMMAND_MSG + cmd);
    }

    @Test
    void testIsValidReturnsTrueOnSplitGroupCommand() {
        var cmd = new Command("split-group", new String[]{"11.32", "group", "the reason"});

        assertTrue(validator.isValid(cmd), VALID_COMMAND_MSG + cmd);
    }

    @Test
    void testIsValidReturnsTrueOnHelpCommand() {
        var cmd = new Command("help", new String[]{});

        assertTrue(validator.isValid(cmd), VALID_COMMAND_MSG + cmd);
    }

    @Test
    void testIsValidReturnsFalseOnHelpWithWrongArgumentsCount() {
        var cmd = new Command("help", new String[]{"arg"});

        assertFalse(validator.isValid(cmd), "Wrong number of arguments on help but returned true");
    }

    @Test
    void testIsValidReturnsFalseOnHelpWithNullArguments() {
        var cmd = new Command("help", null);

        assertFalse(validator.isValid(cmd), "Null arguments on help but returned true");
    }

    @Test
    void testIsValidReturnsFalseOnNullCommandName() {
        var cmd = new Command(null, new String[]{});

        assertFalse(validator.isValid(cmd), "Null name on command but returned true");
    }

    @Test
    void testIsValidReturnsFalseOnNegativeAmount() {
        var cmd = new Command("split-group", new String[]{"-11.32", "group", "the reason"});

        assertFalse(validator.isValid(cmd), "Negative amount but returned true");
    }

    @Test
    void testIsValidReturnsFalseOnWhiteSpaceArgument() {
        var cmd = new Command("payed", new String[]{"10.43", " "});

        assertFalse(validator.isValid(cmd), "Contains whitespace only argument but returned true");
    }

    @Test
    void testIsValidReturnsFalseOnEmptyStringArgument() {
        var cmd = new Command("payed", new String[]{"10.43", ""});

        assertFalse(validator.isValid(cmd), "Contains whitespace only argument but returned true");
    }

    @Test
    void testIsValidReturnsFalseOnWrongOrderArgument() {
        var cmd = new Command("split-group", new String[]{"group", "10.00", "the reason"});

        assertFalse(validator.isValid(cmd), "Swapped <amount> and <group_name> arguments but returned true");
    }

    @Test
    void testIsValidThrowsOnNull() {
        assertThrows(NullPointerException.class, () -> validator.isValid(null), "Did not throw on null");
    }
}