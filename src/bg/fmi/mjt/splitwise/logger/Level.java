package bg.fmi.mjt.splitwise.logger;

public enum Level {

    ERROR("ERROR", 3),
    WARN("WARN", 2),
    INFO("INFO", 1),
    DEBUG("DEBUG", 0);

    private final String displayName;
    private final int priority;

    Level(String displayName, int priority) {
        this.displayName = displayName;
        this.priority = priority;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getPriority() {
        return priority;
    }

}