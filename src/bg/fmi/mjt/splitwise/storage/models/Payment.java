package bg.fmi.mjt.splitwise.storage.models;

import bg.fmi.mjt.splitwise.storage.dao.Identifiable;

import java.time.LocalDateTime;

public final class Payment implements Identifiable {

    private final String id;
    private final String fromId;
    private final String toId;
    private final double levs;
    private final LocalDateTime datePayed;

    public Payment(String id, String fromId, String toId, double levs, LocalDateTime datePayed) {
        this.id = id;
        this.fromId = fromId;
        this.toId = toId;
        this.levs = levs;
        this.datePayed = datePayed;
    }

    public static Payment of(String id, String fromId, String toId, double levs, LocalDateTime datePayed) {
        return new Payment(id, fromId, toId, levs, datePayed);
    }

    public String id() {
        return id;
    }

    public String fromId() {
        return fromId;
    }

    public String toId() {
        return toId;
    }

    public double levs() {
        return levs;
    }

    public LocalDateTime datePayed() {
        return datePayed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Payment payment = (Payment) o;

        return id.equals(payment.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Payment{" +
            "id='" + id + '\'' +
            ", fromId='" + fromId + '\'' +
            ", toId='" + toId + '\'' +
            ", levs=" + levs +
            ", datePayed=" + datePayed +
            '}';
    }
}
