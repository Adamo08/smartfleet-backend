package com.adamo.vrspfab.payments;

import com.adamo.vrspfab.reservations.Reservation;
import com.adamo.vrspfab.reservations.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service("onSitePaymentProvider")
@RequiredArgsConstructor
public class OnSitePaymentProvider implements PaymentProvider {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final RefundRepository refundRepository;

    @Override
    @Transactional
    public PaymentResponseDto processPayment(PaymentRequestDto requestDto) {
        Payment payment = paymentRepository.findByReservationId(requestDto.getReservationId())
                .orElseThrow(() -> new PaymentException("No pending payment found for reservation: " + requestDto.getReservationId()));

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            return new PaymentResponseDto(payment.getId(), payment.getTransactionId(), payment.getStatus().name(), null);
        }

        if (!payment.getAmount().equals(requestDto.getAmount()) || !payment.getCurrency().equals(requestDto.getCurrency())) {
            throw new PaymentException("Request amount/currency does not match pending payment.");
        }

        String transactionRef = requestDto.getPaymentMethodId() != null && !requestDto.getPaymentMethodId().isBlank()
                ? requestDto.getPaymentMethodId()
                : ("ONSITE-" + payment.getId() + "-" + System.currentTimeMillis());

        payment.setTransactionId(transactionRef);
        payment.setStatus(PaymentStatus.COMPLETED);
        paymentRepository.save(payment);

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

        Refund refund = new Refund();
        refund.setPayment(payment);
        refund.setRefundTransactionId("ONSITE-RFD-" + payment.getId() + "-" + System.currentTimeMillis());
        refund.setAmount(requestDto.getAmount());
        refund.setCurrency(payment.getCurrency());
        refund.setReason(requestDto.getReason());
        refund.setRefundMethod(requestDto.getRefundMethod() != null ? requestDto.getRefundMethod() : "ONSITE");
        refund.setAdditionalNotes(requestDto.getAdditionalNotes());
        refund.setContactEmail(requestDto.getContactEmail());
        refund.setContactPhone(requestDto.getContactPhone());
        refund.setStatus(RefundStatus.PROCESSED);
        refund.setProcessedAt(LocalDateTime.now());
        refundRepository.save(refund);

        payment.setStatus(PaymentStatus.REFUNDED);
        paymentRepository.save(payment);

        return new RefundResponseDto(refund.getId(), refund.getRefundTransactionId(), refund.getStatus());
    }

    @Override
    public String getProviderName() {
        return "onSitePaymentProvider";
    }
}


