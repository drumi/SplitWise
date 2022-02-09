package bg.fmi.mjt.splitwise.handlers;

import bg.fmi.mjt.splitwise.commands.Command;
import bg.fmi.mjt.splitwise.commands.creators.CommandCreator;
import bg.fmi.mjt.splitwise.commands.executors.CommandExecutor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaulClientInputHandlerTest {

    static final String COMMAND_NAME = "commandName";
    static final String[] COMMAND_ARGUMENTS = new String[]{"arg0"};
    static final Command RETURNED_COMMAND = new Command(COMMAND_NAME, COMMAND_ARGUMENTS);

    @Mock
    CommandExecutor executor;

    @Mock
    CommandCreator creator;

    @InjectMocks
    DefaulClientInputHandler handler;

    @Test
    void testHandle() {
        when(creator.create("input")).thenReturn(RETURNED_COMMAND);
        when(executor.execute(RETURNED_COMMAND)).thenReturn("output");

        String output = handler.handle("input");

        assertEquals("output", output, "Did not return correct result");
    }

    @Test
    void testNullThrows() {
        assertThrows(NullPointerException.class, () -> handler.handle(null), "Did not throw on null argument");
    }
}