package bg.fmi.mjt.splitwise.commands.executors;

import bg.fmi.mjt.splitwise.commands.Command;
import bg.fmi.mjt.splitwise.commands.CommandType;
import bg.fmi.mjt.splitwise.commands.validators.CommandValidator;
import bg.fmi.mjt.splitwise.logger.Logger;
import bg.fmi.mjt.splitwise.logger.LoggerFactory;
import bg.fmi.mjt.splitwise.service.Service;
import bg.fmi.mjt.splitwise.service.exceptions.ServiceException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServerCommandExecutorTest {

    private static MockedStatic<LoggerFactory> loggerMock = mockStatic(LoggerFactory.class);

    @Mock
    CommandValidator validator;

    @Mock
    Service service;

    @InjectMocks
    ServerCommandExecutor executor;


    @BeforeAll
    static void beforeAll() {
        loggerMock.when(() -> LoggerFactory.getLogger(ArgumentMatchers.any())).thenReturn(mock(Logger.class));
    }

    @AfterAll
    static void afterAll() {
        loggerMock.close();
    }

    @Test
    void testExecuteThrowsOnNull() {
        assertThrows(NullPointerException.class, () -> executor.execute(null), "Did not throw on null");
    }

    @Test
    void testExecuteCallsAddFriendOnly() throws ServiceException {
        when(validator.isValid(any())).thenReturn(true);

        executor.execute(new Command(CommandType.ADD_FRIEND.cmdName(), new String[]{"ivan"}));

        verify(service, times(1)).addFriend(any(), any());
        verifyNoMoreInteractions(service);
    }

    @Test
    void testExecuteCallsCreateGroupOnly() throws ServiceException {
        when(validator.isValid(any())).thenReturn(true);

        executor.execute(new Command(CommandType.CREATE_GROUP.cmdName(), new String[]{"groupName", "friend1", "friend2"}));

        verify(service, times(1)).createGroup(any(), any(), any());
        verifyNoMoreInteractions(service);
    }

    @Test
    void testExecuteCallsGetPaymentHistoryOnly() throws ServiceException {
        when(validator.isValid(any())).thenReturn(true);

        executor.execute(new Command(CommandType.GET_PAYMENT_HISTORY.cmdName(), new String[]{}));

        verify(service, times(1)).getPaymentHistory(any());
        verifyNoMoreInteractions(service);
    }

    @Test
    void testExecuteCallsGetStatusOnly() throws ServiceException {
        when(validator.isValid(any())).thenReturn(true);

        executor.execute(new Command(CommandType.GET_STATUS.cmdName(), new String[]{}));

        verify(service, times(1)).getStatus(any());
        verifyNoMoreInteractions(service);
    }

    @Test
    void testExecuteCallsLoginAndNotificationsOnly() throws ServiceException {
        when(validator.isValid(any())).thenReturn(true);

        executor.execute(new Command(CommandType.LOGIN.cmdName(), new String[]{"username", "password"}));

        verify(service, times(1)).login(any(), any());
        verify(service, times(1)).getAndDeleteNotifications(any());
        verifyNoMoreInteractions(service);
    }

    @Test
    void testExecuteCallsLogoutsOnly() {
        when(validator.isValid(any())).thenReturn(true);

        executor.execute(new Command(CommandType.LOGOUT.cmdName(), new String[]{}));

        verify(service, times(1)).logout(any());
        verifyNoMoreInteractions(service);
    }

    @Test
    void testExecuteCallsPayedOnly() throws ServiceException {
        when(validator.isValid(any())).thenReturn(true);

        executor.execute(new Command(CommandType.PAYED.cmdName(), new String[]{"10.00", "friend"}));

        verify(service, times(1)).payed(any(), any(), anyDouble());
        verifyNoMoreInteractions(service);
    }

    @Test
    void testExecuteCallRegisterOnly() throws ServiceException {
        when(validator.isValid(any())).thenReturn(true);

        executor.execute(new Command(CommandType.REGISTER.cmdName(), new String[]{"username", "password"}));

        verify(service, times(1)).register(any(), any());
        verifyNoMoreInteractions(service);
    }

    @Test
    void testExecuteCallSplitOnly() throws ServiceException {
        when(validator.isValid(any())).thenReturn(true);

        executor.execute(new Command(CommandType.SPLIT.cmdName(), new String[]{"10.12", "ivan", "reason"}));

        verify(service, times(1)).split(any(), any(), anyDouble(), any());
        verifyNoMoreInteractions(service);
    }

    @Test
    void testExecuteCallSplitGroupOnly() throws ServiceException {
        when(validator.isValid(any())).thenReturn(true);

        executor.execute(new Command(CommandType.SPLIT_GROUP.cmdName(), new String[]{"10.12", "group", "reason"}));

        verify(service, times(1)).splitGroup(any(), any(), anyDouble(), any());
        verifyNoMoreInteractions(service);
    }


}