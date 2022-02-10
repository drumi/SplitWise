package bg.fmi.mjt.splitwise.commands.executors;

import bg.fmi.mjt.splitwise.client.ClientServer;
import bg.fmi.mjt.splitwise.commands.Command;
import bg.fmi.mjt.splitwise.commands.CommandType;
import bg.fmi.mjt.splitwise.commands.validators.CommandValidator;
import bg.fmi.mjt.splitwise.responses.CommandResponse;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientCommandExecutorTest {

    private static final String SUCCESSFUL_OPERATION_MSG = "***Success***";
    private static final String SERIALIZED_SUCCESS_RESPONSE = new Gson().toJson(CommandResponse.ofSuccess());

    @Mock
    ClientServer clientServer;

    @Mock
    CommandValidator validator;

    @InjectMocks
    ClientCommandExecutor executor;

    @Test
    void testExecuteThrowsOnNull() {
        assertThrows(NullPointerException.class, () -> executor.execute(null), "Did not throw on null");
    }

    @Test
    void testExecuteRegisterReturnsSuccess() throws IOException {
        when(clientServer.recv()).thenReturn(SERIALIZED_SUCCESS_RESPONSE);
        when(validator.isValid(any())).thenReturn(true);

        Command addFriend = new Command(CommandType.ADD_FRIEND.cmdName(), new String[]{"ivan"});

        var result = executor.execute(addFriend);
        assertEquals(SUCCESSFUL_OPERATION_MSG, result, "Did not return success message on successful operation");
    }
}