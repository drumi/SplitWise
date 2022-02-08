package bg.fmi.mjt.splitwise.responses;

import java.time.LocalDateTime;

public class Payment {

    private final String payedTo;
    private final double levs;
    private final LocalDateTime payedOn;

    public Payment(String payedTo, double levs, LocalDateTime payedOn) {
        this.payedTo = payedTo;
        this.levs = levs;
        this.payedOn = payedOn;
    }

    public String getPayedTo() {
        return payedTo;
    }

    public double getLevs() {
        return levs;
    }

    public LocalDateTime getPayedOn() {
        return payedOn;
    }

}
