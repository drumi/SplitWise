package bg.fmi.mjt.splitwise.service;

import bg.fmi.mjt.splitwise.service.exceptions.ServiceException;
import bg.fmi.mjt.splitwise.storage.dao.Dao;
import bg.fmi.mjt.splitwise.storage.models.Group;
import bg.fmi.mjt.splitwise.storage.models.Payment;
import bg.fmi.mjt.splitwise.storage.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class ServiceTest {

    private final static double DOUBLE_PRECISION = 0.001;

    private final String INVALID_TOKEN = "invalid";

    private final String USER_1 = "misho";
    private final String USER_2 = "gosho";
    private final String USER_3 = "tosho";
    private final String USER_4 = "ivan";

    private final String USER_1_PASSWORD = "mishoPassword";
    private final String USER_2_PASSWORD = "goshoPassword";
    private final String USER_3_PASSWORD = "toshoPassword";
    private final String USER_4_PASSWORD = "goshoPassword";

    private final String USER_4_PAYMENT_ID = "pid";
    private final double USER_4_PAYMENT_AMOUNT = 123.45;
    private final LocalDateTime USER_4_PAYMENT_DATE =  LocalDateTime.of(2021, Month.APRIL, 1, 12, 44);

    private final double USER_2_OWES_USER_1 = 200.0;
    private final double USER_2_OWES_USER_4 = 50.0;

    private final String USER_3_NOTIFICATION_MESSAGE = "Someone payed you";

    private final String GROUP_NAME = "fmi";

    private final Supplier<String> idSupplier = () -> UUID.randomUUID().toString();
    private final Supplier<String> authTokenSupplier = () -> UUID.randomUUID().toString();

    private Dao<User> userDao;
    private Dao<Group> groupDao;
    private Dao<Payment> paymentDao;

    private Service service;

    @BeforeEach
    void setUp() {
        Map<String, Double> user1FriendDebts = Map.of(USER_2, USER_2_OWES_USER_1);
        Map<String, Double> user2FriendDebts = Map.of(USER_1, -USER_2_OWES_USER_1, USER_4, -USER_2_OWES_USER_4);
        Map<String, Double> user3FriendDebts = Map.of();
        Map<String, Double> user4FriendDebts = Map.of(USER_2, USER_2_OWES_USER_4);

        var user1 = new User(USER_1, USER_1_PASSWORD, Set.of(USER_2), Set.of(), Set.of(), List.of(), user1FriendDebts);

        var user2 = new User(USER_2, USER_2_PASSWORD, Set.of(USER_4, USER_1), Set.of(GROUP_NAME),
                             Set.of(), List.of(), user2FriendDebts);

        var user3 = new User(USER_3, USER_3_PASSWORD, Set.of(USER_4), Set.of(GROUP_NAME),
                             Set.of(), List.of(USER_3_NOTIFICATION_MESSAGE), user3FriendDebts);

        var user4 = new User(USER_4, USER_4_PASSWORD, Set.of(USER_3, USER_2), Set.of(GROUP_NAME),
                             Set.of(USER_4_PAYMENT_ID), List.of(), user4FriendDebts);

        var userDb = Map.of(
            USER_1, user1,
            USER_2, user2,
            USER_3, user3,
            USER_4, user4
        );

        var groupDb = Map.of(
            GROUP_NAME, new Group(GROUP_NAME, Set.of(USER_2, USER_3, USER_4))
        );

        var paymentDb = Map.of(
            USER_4_PAYMENT_ID,
            Payment.of(USER_4_PAYMENT_ID, USER_4, USER_3, USER_4_PAYMENT_AMOUNT, USER_4_PAYMENT_DATE)
        );

        userDao = new DaoStub<>(userDb);
        groupDao = new DaoStub<>(groupDb);
        paymentDao = new DaoStub<>(paymentDb);

        service = new Service(userDao, groupDao, paymentDao, idSupplier, authTokenSupplier);
    }

    @Test
    void testRegisterDoesNotThrow() {
        var username = "username";
        var password = "password";

        assertDoesNotThrow(() -> service.register(username, password), "Should not throw when there are no collisions");
    }

    @Test
    void testRegisterThrowsOnNullPassword() {
        var username = "username";

        assertThrows(NullPointerException.class, () -> service.register(username, null), "Should throw on null password");
    }

    @Test
    void testRegisterThrowsOnNullUsername() {
        var password = "password";

        assertThrows(NullPointerException.class, () -> service.register(null, password), "Should throw on null username");
    }

    @Test
    void testRegisterWritesToDb() throws ServiceException {
        var username = "username";
        var password = "password";

        service.register(username, password);

        assertNotNull(userDao.find(username), "did not write user to db");
    }

    @Test
    void testRegisterThrowsOnExistingUsername() {
        var username = USER_1;
        var password = "password";

        assertThrows(ServiceException.class, () -> service.register(username, password), "did not throw on name collision");
    }

    @Test
    void testLoginThrowsOnNullPassword() {
        var username = "username";

        assertThrows(NullPointerException.class, () -> service.login(username, null), "Should throw on null password");
    }

    @Test
    void testLoginThrowsOnNullUsername() {
        var password = "password";

        assertThrows(NullPointerException.class, () -> service.login(null, password), "Should throw on null username");
    }

    @Test
    void testLoginDoesNotThrow() {
        assertDoesNotThrow(() -> service.login(USER_1, USER_1_PASSWORD), "Should not throw on valid login");
    }

    @Test
    void testLoginDoesNotLoginOnFalseCredentials() {
        assertDoesNotThrow(() -> service.login(USER_1, USER_1_PASSWORD), "Should throw on wrong login");
    }

    @Test
    void testLoginReturnsToken() throws ServiceException {
        var token = service.login(USER_1, USER_1_PASSWORD);

        assertNotNull(token, "did not generate id token");
    }

    @Test
    void testAddFriendThrowsOnNull() throws ServiceException {
        var token = service.login(USER_1, USER_1_PASSWORD);

        assertThrows(NullPointerException.class, () -> service.addFriend(token, null), "Should throw on null username");
    }

    @Test
    void addFriendThrowsOnInvalidToken() {
        assertThrows(ServiceException.class, () -> service.addFriend(INVALID_TOKEN, USER_4), "Should throw on invalid token");
    }

    @Test
    void testAddFriendWritesChanges() throws ServiceException {
        var token = service.login(USER_1, USER_1_PASSWORD);

        service.addFriend(token, USER_4);

        assertTrue(userDao.find(USER_1).friendIds().contains(USER_4), "Did not add users to friend list");
        assertTrue(userDao.find(USER_4).friendIds().contains(USER_1), "Did not add users to friend list");
    }

    @Test
    void testAddFriendThrowsOnSelfAdd() throws ServiceException {
        var token = service.login(USER_1, USER_1_PASSWORD);

        assertThrows(ServiceException.class, () -> service.addFriend(token, USER_1), "Did not throw on self add");
    }

    @Test
    void testAddFriendThrowsOnUnregisteredUser() throws ServiceException {
        var token = service.login(USER_1, USER_1_PASSWORD);

        assertThrows(ServiceException.class, () -> service.addFriend(token, "nonexistant"), "Did not throw on nonexistant user add");
    }

    @Test
    void testCreateGroupThrowsOnLessThan3Users() throws ServiceException {
        var token = service.login(USER_4, USER_4_PASSWORD);

        assertThrows(ServiceException.class, () -> service.createGroup(token, "newGroup", Set.of(USER_2)), "Did not throw on a group with less than 3 members");
    }

    @Test
    void testCreateGroupThrowsWhenUsersAreNotOnYourFriendList() throws ServiceException {
        var token = service.login(USER_4, USER_4_PASSWORD);

        assertThrows(ServiceException.class, () -> service.createGroup(token, "newGroup", Set.of(USER_1, USER_3)), "Did not throw on a group with a stranger");
    }

    @Test
    void testCreateGroupThrowsWhenSelfAdding() throws ServiceException {
        var token = service.login(USER_4, USER_4_PASSWORD);

        assertThrows(ServiceException.class, () -> service.createGroup(token, "newGroup", Set.of(USER_2, USER_4)), "Did not throw on self add");
    }

    @Test
    void testCreateGroupThowsOnDublicateName() throws ServiceException {
        var token = service.login(USER_4, USER_4_PASSWORD);

        assertThrows(ServiceException.class, () -> service.createGroup(token, GROUP_NAME, Set.of(USER_2, USER_3)), "Did not throw on a dublicate name group");
    }

    @Test
    void testCreateGroupWritesToDb() throws ServiceException {
        var token = service.login(USER_4, USER_4_PASSWORD);

        service.createGroup(token, "newGroup", Set.of(USER_2, USER_3));

        var group = groupDao.find("newGroup");

        assertNotNull(group, "Did not write the group to db");
    }

    @Test
    void testCreateGroupThrowsOnNullGroup() throws ServiceException {
        var token = service.login(USER_4, USER_4_PASSWORD);

        assertThrows(NullPointerException.class, () -> service.createGroup(token, null, Set.of(USER_2, USER_4)), "Did not throw on null group name");
    }

    @Test
    void testCreateGroupThrowsOnNullSet() throws ServiceException {
        var token = service.login(USER_4, USER_4_PASSWORD);

        assertThrows(NullPointerException.class, () -> service.createGroup(token, "newGroup", null), "Did not throw on null set of friends");
    }

    @Test
    void testSplitUpdatesDebt() throws ServiceException {
        var token = service.login(USER_4, USER_4_PASSWORD);

        var amount = 100.0;
        service.split(token, USER_3, amount, "Reason");

        var newAmount = userDao.find(USER_4).friendsIdsToLevsOwed().get(USER_3);
        assertEquals(amount / 2, newAmount, DOUBLE_PRECISION, "Did not set the correct amount");

        newAmount = userDao.find(USER_3).friendsIdsToLevsOwed().get(USER_4);
        assertEquals(-amount / 2, newAmount, DOUBLE_PRECISION, "Did not set the correct amount");
    }

    @Test
    void testSplitThrowsOnNegativeAmount() throws ServiceException {
        var token = service.login(USER_4, USER_4_PASSWORD);

        assertThrows(ServiceException.class, () -> service.split(token, USER_3, -100.0, "Reason"), "Did not throw on null username");
    }

    @Test
    void testSplitThrowsOnNullReason() throws ServiceException {
        var token = service.login(USER_4, USER_4_PASSWORD);

        assertThrows(NullPointerException.class, () -> service.split(token, USER_3, 100.0, null), "Did not throw on null reason");
    }

    @Test
    void testSplitThrowsOnNullUsername() throws ServiceException {
        var token = service.login(USER_4, USER_4_PASSWORD);

        assertThrows(NullPointerException.class, () -> service.split(token, null, 100.0, "Reason"), "Did not throw on null username");
    }

    @Test
    void testSplitThrowsOnInvalidToken() {
        assertThrows(ServiceException.class, () -> service.split(INVALID_TOKEN, USER_3, 100.0, "Reason"), "Did not throw on negative amount");
    }


    @Test
    void testSplitGroupThrowsOnNullName() throws ServiceException {
        var token = service.login(USER_4, USER_4_PASSWORD);

        assertThrows(NullPointerException.class, () -> service.splitGroup(token, null, 100.0, "Reason"), "Did not throw on null group name");
    }

    @Test
    void testSplitGroupThrowsOnNullReason() throws ServiceException {
        var token = service.login(USER_4, USER_4_PASSWORD);

        assertThrows(NullPointerException.class, () -> service.splitGroup(token, GROUP_NAME, 100.0, null), "Did not throw on null reason");
    }

    @Test
    void testSplitGroupThrowsOnNegativeAmount() throws ServiceException {
        var token = service.login(USER_4, USER_4_PASSWORD);

        assertThrows(ServiceException.class, () -> service.splitGroup(token, GROUP_NAME, -100.0, "Reason"), "Did not throw on negative amount");
    }

    @Test
    void testSplitGroupUpdatesDebt() throws ServiceException {
        var token = service.login(USER_4, USER_4_PASSWORD);

        var amount = 100.0;
        service.splitGroup(token, GROUP_NAME, amount, "Reason");

        var newAmount = userDao.find(USER_4).friendsIdsToLevsOwed().get(USER_3);
        assertEquals(amount / 3, newAmount, DOUBLE_PRECISION, "Did not set the correct amount");

        newAmount = userDao.find(USER_4).friendsIdsToLevsOwed().get(USER_2);
        assertEquals(amount / 3, newAmount - USER_2_OWES_USER_4, DOUBLE_PRECISION, "Did not set the correct amount");

        newAmount = userDao.find(USER_2).friendsIdsToLevsOwed().get(USER_4);
        assertEquals(-amount / 3, newAmount + USER_2_OWES_USER_4, DOUBLE_PRECISION, "Did not set the correct amount");

        newAmount = userDao.find(USER_3).friendsIdsToLevsOwed().get(USER_4);
        assertEquals(-amount / 3, newAmount, DOUBLE_PRECISION, "Did not set the correct amount");
    }

    @Test
    void testSplitGroupThrowsoninvalidToken() {
        assertThrows(ServiceException.class, () -> service.splitGroup(INVALID_TOKEN, GROUP_NAME, 100.0, "Reason"), "did not throw on invalid token");
    }

    @Test
    void testPayedThrowsOnNullUsername() throws ServiceException {
        var token = service.login(USER_1, USER_1_PASSWORD);

        assertThrows(NullPointerException.class, () -> service.payed(token, null, USER_2_OWES_USER_1), "did not throw on null username");
    }

    @Test
    void testPayedRemovesDebt() throws ServiceException {
        var token = service.login(USER_1, USER_1_PASSWORD);

        service.payed(token, USER_2, USER_2_OWES_USER_1);

        Double expectedNull = userDao.find(USER_1).friendsIdsToLevsOwed().get(USER_2);
        assertNull(expectedNull, "Did not remove debt record");

        expectedNull = userDao.find(USER_2).friendsIdsToLevsOwed().get(USER_1);
        assertNull(expectedNull, "Did not remove debt record");
    }

    @Test
    void testPayedThrowsOnNegativeAmount() throws ServiceException {
        var token = service.login(USER_1, USER_1_PASSWORD);

        var negativeAmount = -2 * USER_2_OWES_USER_1;

        assertThrows(ServiceException.class, () -> service.payed(token, USER_2, negativeAmount), "Did not throw on negative payment");
    }

    @Test
    void testPayedThrowsOnInvalidToken() {
        assertThrows(ServiceException.class, () -> service.payed(INVALID_TOKEN, USER_2, USER_2_OWES_USER_1), "Did not throw on negative payment");
    }

    @Test
    void testGetStatusReturnsCorrectStatus() throws ServiceException {
        var token = service.login(USER_2, USER_2_PASSWORD);

        Map<String, Double> status = service.getStatus(token);

        assertEquals(-USER_2_OWES_USER_4, status.get(USER_4), "Did not return correct debt");
        assertEquals(-USER_2_OWES_USER_1, status.get(USER_1), "Did not return correct debt");
    }

    @Test
    void testGetStatusReturnsEmptyMapWhenNooneOwesYouAndYouDoNotOweAnyone() throws ServiceException {
        var token = service.login(USER_3, USER_3_PASSWORD);

        Map<String, Double> status = service.getStatus(token);

        assertEquals(status, Map.of(), "Did not return empty map on a user without debts or friends with debts");
    }

    @Test
    void testGetStatusThrowsOnInvalidToken() {
        assertThrows(ServiceException.class, () -> service.getStatus(INVALID_TOKEN), "Did not throw on invalid token");
    }

    @Test
    void testGetPaymentHistoryReturnsCorrectData() throws ServiceException {
        var token = service.login(USER_4, USER_4_PASSWORD);

        List<Payment> payments = service.getPaymentHistory(token);

        assertTrue(payments.size() == 1 && payments.contains(paymentDao.find(USER_4_PAYMENT_ID)), "Did not return correct payments");
    }

    @Test
    void testGetPaymentHistoryThrowsOnInvalidToken() {
        assertThrows(ServiceException.class, () -> service.getPaymentHistory(INVALID_TOKEN), "Did not throw on invalid token");
    }

    @Test
    void testGetAndDeleteNotification() throws ServiceException {
        var token = service.login(USER_3, USER_3_PASSWORD);

        List<String> notifications = service.getAndDeleteNotifications(token);

        assertEquals(USER_3_NOTIFICATION_MESSAGE, notifications.get(0), "Did not return correct notification");
        assertEquals(1, notifications.size(), "Did not return correct size");

        var expectedDeletedNotifications = service.getAndDeleteNotifications(token);

        assertEquals(List.of(), expectedDeletedNotifications, "Did not delete notifications");
    }

    @Test
    void testGetAndDeleteNotificationsThrowsOnInvalidToken() {
        assertThrows(ServiceException.class, () -> service.getAndDeleteNotifications(INVALID_TOKEN), "Did not throw on invalid token");
    }
}