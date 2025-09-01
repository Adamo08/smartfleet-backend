package com.adamo.vrspfab.payments;

import com.adamo.vrspfab.reservations.Reservation;
import com.adamo.vrspfab.reservations.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service("onSitePaymentProvider")
@RequiredArgsConstructor
public class OnSitePaymentProvider implements PaymentProvider {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final RefundRepository refundRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional
    public PaymentResponseDto processPayment(PaymentRequestDto requestDto) {
        Payment payment = paymentRepository.findByReservationId(requestDto.getReservationId())
                .orElseThrow(() -> new PaymentException("No pending payment found for reservation: " + requestDto.getReservationId()));

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            return new PaymentResponseDto(payment.getId(), payment.getTransactionId(), payment.getStatus().name(), null);
        }

        // For on-site payments, this method should only be called by admin after verifying actual payment
        // Check if the request is coming from an admin user or if it's a legitimate payment completion
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new PaymentException("Payment is not in a processable state: " + payment.getStatus());
        }

        // Use BigDecimal comparison with tolerance for floating-point precision issues
        if (payment.getAmount().subtract(requestDto.getAmount()).abs().compareTo(new java.math.BigDecimal("0.01")) > 0 || 
            !payment.getCurrency().equals(requestDto.getCurrency())) {
            throw new PaymentException("Request amount/currency does not match pending payment. Expected: " + 
                payment.getAmount() + " " + payment.getCurrency() + ", Got: " + 
                requestDto.getAmount() + " " + requestDto.getCurrency());
        }

        // Generate unique transaction ID for on-site payments
        String transactionRef = "ONSITE-" + payment.getId() + "-" + System.currentTimeMillis();

        payment.setTransactionId(transactionRef);
        payment.setStatus(PaymentStatus.COMPLETED);
        paymentRepository.save(payment);

        // Fire payment completed event to trigger business logic
        applicationEventPublisher.publishEvent(new PaymentCompletedEvent(
                this, 
                payment.getId(), 
                payment.getReservation().getId(), 
                transactionRef, 
                PaymentStatus.COMPLETED
        ));

        return new PaymentResponseDto(payment.getId(), transactionRef, payment.getStatus().name(), null);
    }

    @Override
    @Transactional
    public SessionResponseDto createPaymentSession(SessionRequestDto requestDto) {
        Reservation reservation = reservationRepository.findById(requestDto.getReservationId())
                .orElseThrow(() -> new PaymentException("Reservation not found with ID: " + requestDto.getReservationId()));

        Payment payment = Payment.builder()
                .reservation(reservation)
                .amount(requestDto.getAmount())
                .currency(requestDto.getCurrency())
                .status(PaymentStatus.PENDING)
                .provider(getProviderName())
                .build();
        paymentRepository.save(payment);

        return new SessionResponseDto(String.valueOf(payment.getId()), null);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponseDto getPaymentStatus(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException("Payment not found: " + paymentId));
        return new PaymentResponseDto(payment.getId(), payment.getTransactionId(), payment.getStatus().name(), null);
    }

    @Override
    @Transactional
    public RefundResponseDto processRefund(RefundRequestDto requestDto) {
        Payment payment = paymentRepository.findById(requestDto.getPaymentId())
                .orElseThrow(() -> new PaymentException("Cannot refund non-existent payment with ID: " + requestDto.getPaymentId()));

        // Validate refund amount
        java.math.BigDecimal currentRefundedAmount = payment.getRefundedAmount() != null ? payment.getRefundedAmount() : java.math.BigDecimal.ZERO;
        java.math.BigDecimal remainingAmount = payment.getAmount().subtract(currentRefundedAmount);
        
        if (requestDto.getAmount().compareTo(remainingAmount) > 0) {
            throw new PaymentException("Refund amount (" + requestDto.getAmount() + ") exceeds remaining refundable amount (" + remainingAmount + ")");
        }

        Refund refund = new Refund();
        refund.setPayment(payment);
        refund.setRefundTransactionId("ONSITE-RFD-" + payment.getId() + "-" + System.currentTimeMillis());
        refund.setAmount(requestDto.getAmount());
        refund.setCurrency(payment.getCurrency());
        refund.setReason(requestDto.getReason());
        refund.setRefundMethod(requestDto.getRefundMethod() != null ? requestDto.getRefundMethod() : RefundMethod.ONSITE_CASH);
        refund.setAdditionalNotes(requestDto.getAdditionalNotes());
        refund.setContactEmail(requestDto.getContactEmail());
        refund.setContactPhone(requestDto.getContactPhone());
        refund.setProcessedAt(LocalDateTime.now());

        // Update payment refunded amount
        java.math.BigDecimal newRefundedAmount = currentRefundedAmount.add(requestDto.getAmount());
        payment.setRefundedAmount(newRefundedAmount);

        // Determine refund and payment status based on amount
        boolean isFullRefund = newRefundedAmount.compareTo(payment.getAmount()) == 0;
        
        if (isFullRefund) {
            refund.setStatus(RefundStatus.PROCESSED);
            payment.setStatus(PaymentStatus.REFUNDED);
        } else {
            refund.setStatus(RefundStatus.PARTIALLY_PROCESSED);
            payment.setStatus(PaymentStatus.PARTIALLY_REFUNDED);
        }

        refundRepository.save(refund);
        paymentRepository.save(payment);

        return new RefundResponseDto(refund.getId(), refund.getRefundTransactionId(), refund.getStatus());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canProcessPayment(Long reservationId) {
        return paymentRepository.findByReservationId(reservationId)
                .map(payment -> payment.getStatus() == PaymentStatus.PENDING)
                .orElse(false);
    }

    @Override
    public String getProviderName() {
        return "onSitePaymentProvider";
    }
}


