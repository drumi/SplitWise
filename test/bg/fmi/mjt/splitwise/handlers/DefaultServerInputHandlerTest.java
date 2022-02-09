package bg.fmi.mjt.splitwise.handlers;

import bg.fmi.mjt.splitwise.commands.Command;
import bg.fmi.mjt.splitwise.commands.executors.CommandExecutor;
import bg.fmi.mjt.splitwise.requests.CommandRequest;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultServerInputHandlerTest {

    static final String AUTH_TOKEN = "token";
    static final String COMMAND_OWNER= "owner";
    static final String COMMAND_NAME = "commandName";
    static final String[] COMMAND_ARGUMENTS = new String[]{"arg0"};
    static final Command COMMAND = new Command(COMMAND_OWNER, COMMAND_NAME, COMMAND_ARGUMENTS);
    static final CommandRequest COMMAND_REQUEST =
        new CommandRequest(COMMAND, AUTH_TOKEN);
    static final String SERIALIZED_COMMAND_REQUEST = new Gson().toJson(COMMAND_REQUEST);

    @Mock
    CommandExecutor executor;

    @InjectMocks
    DefaultServerInputHandler handler;

    @Test
    void testHandle() {
        when(executor.execute(Command.ofNewOwner(COMMAND, AUTH_TOKEN))).thenReturn("output");

        String output = handler.handle(SERIALIZED_COMMAND_REQUEST);

        assertEquals("output", output, "Did not return correct result");
    }

    @Test
    void testNullThrows() {
        assertThrows(NullPointerException.class, () -> handler.handle(null), "Did not throw on null argument");
    }
}