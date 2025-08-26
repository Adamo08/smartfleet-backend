package com.adamo.vrspfab.payments;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event fired when a payment is completed successfully.
 * This event is used to trigger business logic like updating reservation status.
 */
@Getter
public class PaymentCompletedEvent extends ApplicationEvent {
    
    private final Long paymentId;
    private final Long reservationId;
    private final String transactionId;
    private final PaymentStatus status;
    
    public PaymentCompletedEvent(Object source, Long paymentId, Long reservationId, String transactionId, PaymentStatus status) {
        super(source);
        this.paymentId = paymentId;
        this.reservationId = reservationId;
        this.transactionId = transactionId;
        this.status = status;
    }
}
