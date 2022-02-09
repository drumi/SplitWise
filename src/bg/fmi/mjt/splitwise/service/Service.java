package bg.fmi.mjt.splitwise.service;

import bg.fmi.mjt.splitwise.service.exceptions.GroupAlreadyExistsException;
import bg.fmi.mjt.splitwise.service.exceptions.GroupNotFoundException;
import bg.fmi.mjt.splitwise.service.exceptions.InvalidOperationException;
import bg.fmi.mjt.splitwise.service.exceptions.ServiceException;
import bg.fmi.mjt.splitwise.service.exceptions.UnauthorizedOperationException;
import bg.fmi.mjt.splitwise.service.exceptions.UnsuccessfulLoginException;
import bg.fmi.mjt.splitwise.service.exceptions.UsernameAlreadyExistsException;
import bg.fmi.mjt.splitwise.service.exceptions.UsernameNotFoundException;
import bg.fmi.mjt.splitwise.storage.dao.Dao;
import bg.fmi.mjt.splitwise.storage.models.Group;
import bg.fmi.mjt.splitwise.storage.models.Payment;
import bg.fmi.mjt.splitwise.storage.models.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Service {

    private static final String FRIEND_ADD_TEMPLATE_MSG = "%s added you as friend!";
    private static final String GROUP_ADD_TEMPLATE_MSG = "%s added you to group %s!";
    private static final String OWE_TEMPLATE_MSG = "%s made a payment and you owe %.2f levs. Reason: %s";
    private static final String PAYMENT_APPROVAL_TEMPLATE_MSG = "%s approved your payment of amount: %.2f levs";

    private static final int MIN_GROUP_SIZE = 3;

    private final Dao<User> userDao;
    private final Dao<Group> groupDao;
    private final Dao<Payment> paymentDao;

    private final Map<String, String> tokenToUserId = new HashMap<>();
    private final Map<String, String> userIdToToken = new HashMap<>();

    private final Supplier<String> idSupplier;
    private final Supplier<String> authTokenSupplier;

    public Service(Dao<User> userDao, Dao<Group> groupDao, Dao<Payment> paymentDao,
                   Supplier<String> idSupplier, Supplier<String> authTokenSupplier) {
        this.userDao = userDao;
        this.groupDao = groupDao;
        this.paymentDao = paymentDao;
        this.idSupplier = idSupplier;
        this.authTokenSupplier = authTokenSupplier;
    }

    public void register(String username, String password) throws ServiceException {
        checkNull(username, "username");
        checkNull(password, "password");

        if (userDao.find(username) != null) {
            throw new UsernameAlreadyExistsException("Username is already taken");
        }

        userDao.insert(User.of(username, password));
    }

    private void checkNull(Object o, String arg) {
        Objects.requireNonNull(o, String.format("%s cannot be null", arg));
    }

    private void validatePaymentAmount(double d) throws ServiceException {
        if (d <= 0) {
            throw new InvalidOperationException("Amount must be positive");
        }
    }

    public String login(String username, String password) throws ServiceException {
        checkNull(username, "username");
        checkNull(password, "password");

        User user = userDao.find(username);

        if (user == null || !user.password().equals(password)) {
            throw new UnsuccessfulLoginException("Could not login with the provided credentials");
        } else  {
            return login(user.id());
        }
    }

    private String login(String userId) {
        String authToken = authTokenSupplier.get();
        tokenToUserId.put(authToken, userId);

        String previousToken = userIdToToken.get(userId);

        if (previousToken != null) {
            tokenToUserId.remove(previousToken);
        }

        userIdToToken.put(userId, authToken);

        return authToken;
    }

    public void logout(String token) {
        var id = tokenToUserId.get(token);
        tokenToUserId.remove(token);
        userIdToToken.remove(id);
    }

    public void addFriend(String token, String friendUsername) throws ServiceException {
        checkNull(friendUsername, "friendUsername");

        var id = idTokenAuthFilter(token);

        var friend = userDao.find(friendUsername);

        if (friend == null) {
            throw new UsernameNotFoundException("The username is not registered in the system");
        }

        var user = userDao.find(id);

        if (user.friendIds().contains(friend.id())) {
            return;
        }

        if (user.id().equals(friend.id())) {
            throw new InvalidOperationException("You cannot add yourself as a friend");
        }

        addAsFriends(user.id(), friend.id());

        notify(friend.id(), String.format(FRIEND_ADD_TEMPLATE_MSG, user.username()));
    }

    private void addAsFriends(String id1, String id2) {
        addToFriends(id1, id2);
        addToFriends(id2, id1);
    }

    private void addToFriends(String userId, String friendId) {
        userDao.update(userDao.find(userId).withAddedFriendId(friendId));
    }

    private void notify(String id, String message) {
        userDao.update(userDao.find(id).withNewUnreadNotification(message));
    }

    public void createGroup(String token, String groupName, Set<String> friendUsernames) throws ServiceException {
        checkNull(groupName, "groupName");
        checkNull(friendUsernames, "friendUsernames");

        var id = idTokenAuthFilter(token);

        if (friendUsernames.size() < MIN_GROUP_SIZE - 1) {
            throw new InvalidOperationException(
                String.format("Cannot create a group with less than %d members", MIN_GROUP_SIZE)
            );
        }

        if (groupDao.find(groupName) != null) {
            throw new GroupAlreadyExistsException("Group with that name already exists");
        }

        var user = userDao.find(id);

        // prepare all users for adding into group
        var usersToAdd = new HashSet<User>();
        usersToAdd.add(user);
        for (var username : friendUsernames) {
            var pendingUser = userDao.find(username);

            if (pendingUser == null) {
                throw new UsernameNotFoundException(String.format("username %s does not exists", username));
            } else if (!pendingUser.friendIds().contains(id)) {
                throw new UnauthorizedOperationException("You can only add your friends to a group");
            }
            usersToAdd.add(pendingUser);
        }

        // create new group
        var userIds = usersToAdd.stream()
                                .map(User::id)
                                .collect(Collectors.toSet());
        groupDao.insert(Group.of(groupName, userIds));

        // update user group names
        usersToAdd.forEach(u -> userDao.update(u.withAddedGroupName(groupName)));

        // notify each user
        usersToAdd.stream()
                  .filter(u -> !u.equals(user))
                  .forEach(u -> notify(u.id(), String.format(GROUP_ADD_TEMPLATE_MSG, user.username(), groupName)));

    }

    public void split(String token, String friendUsername, double amountInLevs, String reason)
        throws ServiceException {
        checkNull(friendUsername, "friendUsername");
        checkNull(reason, "reason");
        validatePaymentAmount(amountInLevs);

        var id = idTokenAuthFilter(token);
        var user = userDao.find(id);
        var friend = userDao.find(friendUsername);

        if (friend == null) {
            throw new UsernameNotFoundException("Friends username was not found in the system");
        } else if (!friend.friendIds().contains(id)) {
            throw new UnauthorizedOperationException("Cannot split money with people that are not on your friend list");
        }

        splitBetweenAndNotifyUsers(user.id(), Set.of(friend.id()), amountInLevs, reason);
    }

    public void splitGroup(String token, String groupName, double amountInLevs, String reason)
        throws ServiceException {
        checkNull(groupName, "groupName");
        checkNull(reason, "reason");
        validatePaymentAmount(amountInLevs);

        var id = idTokenAuthFilter(token);
        var user = userDao.find(id);
        var group = groupDao.find(groupName);

        if (group == null) {
            throw new GroupNotFoundException("Group does not exist");
        }

        var debtors = group.participantIds()
                           .stream()
                           .filter(pid -> !pid.equals(user.id()))
                           .collect(Collectors.toSet());

        splitBetweenAndNotifyUsers(user.id(), debtors, amountInLevs, reason);
    }

    private void splitBetweenAndNotifyUsers(String ownerId, Set<String> debtorIds, double amountInLevs, String reason) {
        double amountPerUser = amountInLevs / (debtorIds.size() + 1);

        var owner = userDao.find(ownerId);

        debtorIds.forEach(debtorId -> {
            updateOwedAmountBetween(debtorId, ownerId, amountPerUser);
            notify(debtorId, String.format(OWE_TEMPLATE_MSG, owner.username(), amountPerUser, reason));
        });
    }

    public void payed(String token, String friendUsernameThatPayed, double amountInLevs) throws ServiceException {
        checkNull(friendUsernameThatPayed, "friendUsernameThatPayed");
        validatePaymentAmount(amountInLevs);

        var id = idTokenAuthFilter(token);
        var user = userDao.find(id);
        var friend = userDao.find(friendUsernameThatPayed);

        String paymentId = idSupplier.get();
        paymentDao.insert(Payment.of(paymentId, friend.id(), user.id(), amountInLevs, LocalDateTime.now()));

        userDao.update(friend.withNewPaymentId(paymentId));
        updateOwedAmountBetween(user.id(), friend.id(), amountInLevs);

        notify(
            friend.id(),
            String.format(PAYMENT_APPROVAL_TEMPLATE_MSG, user.username(), amountInLevs)
        );
    }

    /**
     * adds a debt of amountInLevs in <b>fromId</b> account and reduces debt in <b>toId</> account
     */

    private void updateOwedAmountBetween(String fromId, String toId, double amountInLevs) {
        addOwedAmount(fromId, toId, amountInLevs);
        addOwedAmount(toId, fromId, -amountInLevs);
    }

    private void addOwedAmount(String fromId, String toId, double amountInLevs) {
        var user = userDao.find(toId);

        var owed = new HashMap<>(user.friendsIdsToLevsOwed());
        owed.merge(fromId, amountInLevs, (old, cur) -> old + cur == 0 ? null : old + cur);

        userDao.update(user.withFriendsIdsToLevsOwed(owed));
    }

    public Map<String, Double> getStatus(String token) throws ServiceException {
        var id = idTokenAuthFilter(token);

        return Collections.unmodifiableMap(
            userDao.find(id).friendsIdsToLevsOwed()
        );
    }

    public List<Payment> getPaymentHistory(String token) throws ServiceException {
        var id = idTokenAuthFilter(token);

        return userDao.find(id).paymentIds().stream()
                      .map(paymentDao::find)
                      .toList();
    }

    public List<String> getAndDeleteNotifications(String token) throws ServiceException {
        var id = idTokenAuthFilter(token);
        var user =  userDao.find(id);

        var notifications = Collections.unmodifiableList(
            user.unreadNotifications()
        );

        userDao.update(user.withEmptyUnreadNotifications());

        return notifications;
    }

    private String idTokenAuthFilter(String token) throws ServiceException {
        var id = tokenToUserId.get(token);

        if (id == null) {
            throw new UnauthorizedOperationException("The auth token is not valid");
        }

        return id;
    }

}
