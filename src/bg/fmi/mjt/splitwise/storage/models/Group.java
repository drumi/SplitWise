package bg.fmi.mjt.splitwise.storage.models;

import bg.fmi.mjt.splitwise.storage.dao.Identifiable;

import java.util.Set;

public final class Group implements Identifiable {

    private final String groupName;
    private final Set<String> participantIds;

    public Group(String groupName, Set<String> participantIds) {
        this.groupName = groupName;
        this.participantIds = participantIds;
    }

    public static Group of(String groupName, Set<String> participantIds) {
        return new Group(groupName, participantIds);
    }

    @Override
    public String id() {
        return groupName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Group group = (Group) o;

        return groupName.equals(group.groupName);
    }

    @Override
    public int hashCode() {
        return groupName.hashCode();
    }

    public String groupName() {
        return groupName;
    }

    public Set<String> participantIds() {
        return participantIds;
    }

    @Override
    public String toString() {
        return "Group[" +
            "groupName=" + groupName + ", " +
            "participantIds=" + participantIds + ']';
    }


}
