package bg.fmi.mjt.splitwise.responses;

import java.util.Map;

public class OwesResponse {

    private final Map<String, Double> userToLevs; // positive means the user owes you that much

    public OwesResponse(Map<String, Double> userToLevs) {
        this.userToLevs = userToLevs;
    }

    public Map<String, Double> getUserToLevs() {
        return userToLevs;
    }

}
