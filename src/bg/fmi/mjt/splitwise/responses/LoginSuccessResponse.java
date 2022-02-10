package bg.fmi.mjt.splitwise.responses;

import java.util.List;

public class LoginSuccessResponse {

    private final String authToken;
    private final List<String> notifications;

    public LoginSuccessResponse(String authToken, List<String> notifications) {
        this.authToken = authToken;
        this.notifications = notifications;
    }

    public String getAuthToken() {
        return authToken;
    }

    public List<String> getNotifications() {
        return notifications;
    }

}
