package bg.fmi.mjt.splitwise.responses;

import java.util.List;

public class PaymentHistoryResponse {

    private final List<Payment> paymentHistory;

    public PaymentHistoryResponse(List<Payment> paymentHistory) {
        this.paymentHistory = paymentHistory;
    }

    public List<Payment> getPaymentHistory() {
        return paymentHistory;
    }

}
