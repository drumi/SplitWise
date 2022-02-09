package bg.fmi.mjt.splitwise.storage.models;

import bg.fmi.mjt.splitwise.storage.dao.Identifiable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class User implements Identifiable {

    private final String username;
    private final String password;
    private final Set<String> friendIds;
    private final Set<String> groupNames;
    private final Set<String> paymentIds;
    private final List<String> unreadNotifications;
    private final Map<String, Double> friendsIdsToLevsOwed;

    public User(String username, String password, Set<String> friendIds, Set<String> groupNames,
                Set<String> paymentIds, List<String> unreadNotifications, Map<String, Double> friendsIdsToLevsOwed) {
        this.username = username;
        this.password = password;
        this.friendIds = friendIds;
        this.groupNames = groupNames;
        this.paymentIds = paymentIds;
        this.unreadNotifications = unreadNotifications;
        this.friendsIdsToLevsOwed = friendsIdsToLevsOwed;
    }

    public static User of(String username, String password) {
        return new User(
            username,
            password,
            Collections.emptySet(),
            Collections.emptySet(),
            Collections.emptySet(),
            Collections.emptyList(),
            Collections.emptyMap()
        );
    }

    public User withAddedFriendId(String id) {
        var tmp = new HashSet<>(friendIds);
        tmp.add(id);

        return new User(
            username,
            password,
            tmp,
            groupNames,
            paymentIds,
            unreadNotifications,
            friendsIdsToLevsOwed
        );
    }

    public User withEmptyUnreadNotifications() {
        return new User(
            username,
            password,
            friendIds,
            groupNames,
            paymentIds,
            Collections.emptyList(),
            friendsIdsToLevsOwed
        );
    }

    public User withNewUnreadNotification(String notification) {
        var tmp = new ArrayList<>(unreadNotifications);
        tmp.add(notification);

        return new User(
            username,
            password,
            friendIds,
            groupNames,
            paymentIds,
            tmp,
            friendsIdsToLevsOwed
        );
    }

    public User withAddedGroupName(String name) {
        var tmp = new HashSet<>(groupNames);
        tmp.add(name);

        return new User(
            username,
            password,
            friendIds,
            tmp,
            paymentIds,
            unreadNotifications,
            friendsIdsToLevsOwed
        );
    }

    public User withPaymentIds(Set<String> paymentIds) {
        return new User(
            username,
            password,
            friendIds,
            groupNames,
            paymentIds,
            unreadNotifications,
            friendsIdsToLevsOwed
        );
    }

    public User withNewPaymentId(String paymentId) {
        var tmp = new HashSet<>(paymentIds);
        tmp.add(paymentId);

        return new User(
            username,
            password,
            friendIds,
            groupNames,
            tmp,
            unreadNotifications,
            friendsIdsToLevsOwed
        );
    }

    public User withFriendsIdsToLevsOwed(Map<String, Double> map) {
        return new User(
            username,
            password,
            friendIds,
            groupNames,
            paymentIds,
            unreadNotifications,
            map
        );
    }


    @Override
    public String id() {
        return username;
    }



    public String username() {
        return username;
    }

    public String password() {
        return password;
    }

    public Set<String> friendIds() {
        return friendIds;
    }

    public Set<String> groupNames() {
        return groupNames;
    }

    public Set<String> paymentIds() {
        return paymentIds;
    }

    public List<String> unreadNotifications() {
        return unreadNotifications;
    }

    public Map<String, Double> friendsIdsToLevsOwed() {
        return friendsIdsToLevsOwed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        User user = (User) o;

        return username.equals(user.username);
    }

    @Override
    public int hashCode() {
        return username.hashCode();
    }

    @Override
    public String toString() {
        return "User{" +
            "username='" + username + '\'' +
            ", password='" + password + '\'' +
            ", friendIds=" + friendIds +
            ", groupNames=" + groupNames +
            ", paymentIds=" + paymentIds +
            ", unreadNotifications=" + unreadNotifications +
            ", friendsIdsToLevsOwed=" + friendsIdsToLevsOwed +
            '}';
    }
}
