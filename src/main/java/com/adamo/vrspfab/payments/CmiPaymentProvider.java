package com.adamo.vrspfab.payments;

import com.adamo.vrspfab.reservations.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service("cmiPaymentProvider")
@RequiredArgsConstructor
public class CmiPaymentProvider implements PaymentProvider {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final RefundRepository refundRepository;
    private final RestTemplate restTemplate;

    @Value("${cmi.api.baseUrl:https://example-cmi}" )
    private String cmiBaseUrl;

    @Override
    @Transactional
    public PaymentResponseDto processPayment(PaymentRequestDto requestDto) {
        Payment payment = paymentRepository.findByReservationId(requestDto.getReservationId())
                .orElseThrow(() -> new PaymentException("No pending payment found for reservation: " + requestDto.getReservationId()));

        if (!payment.getAmount().equals(requestDto.getAmount()) || !payment.getCurrency().equals(requestDto.getCurrency())) {
            throw new PaymentException("Request amount/currency does not match pending payment.");
        }

        // Capture/confirm via CMI (placeholder HTTP call; replace with actual CMI spec)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(Map.of(
                "orderId", payment.getTransactionId(),
                "amount", requestDto.getAmount(),
                "currency", requestDto.getCurrency()
        ), headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(cmiBaseUrl + "/payments/capture", entity, Map.class);
        String status = String.valueOf(response.getBody().getOrDefault("status", "COMPLETED"));

        if (!"COMPLETED".equalsIgnoreCase(status)) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            throw new PaymentException("CMI capture failed. Status: " + status);
        }

        payment.setStatus(PaymentStatus.COMPLETED);
        paymentRepository.save(payment);
        return new PaymentResponseDto(payment.getId(), payment.getTransactionId(), payment.getStatus().name(), null);
    }

    @Override
    @Transactional
    public SessionResponseDto createPaymentSession(SessionRequestDto requestDto) {
        var reservation = reservationRepository.findById(requestDto.getReservationId())
                .orElseThrow(() -> new PaymentException("Reservation not found with ID: " + requestDto.getReservationId()));

        Payment payment = Payment.builder()
                .reservation(reservation)
                .amount(requestDto.getAmount())
                .currency(requestDto.getCurrency())
                .status(PaymentStatus.PENDING)
                .provider(getProviderName())
                .build();
        paymentRepository.save(payment);

        // Create CMI session (placeholder; replace with real spec)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(Map.of(
                "amount", requestDto.getAmount(),
                "currency", requestDto.getCurrency(),
                "successUrl", requestDto.getSuccessUrl(),
                "cancelUrl", requestDto.getCancelUrl(),
                "reference", payment.getId()
        ), headers);

        ResponseEntity<Map> resp = restTemplate.postForEntity(cmiBaseUrl + "/payments/session", entity, Map.class);
        String sessionId = String.valueOf(resp.getBody().getOrDefault("sessionId", UUID.randomUUID().toString()));
        String checkoutUrl = String.valueOf(resp.getBody().getOrDefault("checkoutUrl", ""));

        payment.setTransactionId(sessionId);
        paymentRepository.save(payment);

        return new SessionResponseDto(sessionId, checkoutUrl);
    }

    @Override
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
        refund.setRefundTransactionId("CMI-RFD-" + payment.getId() + "-" + System.currentTimeMillis());
        refund.setAmount(requestDto.getAmount());
        refund.setCurrency(payment.getCurrency());
        refund.setReason(requestDto.getReason());
        refund.setStatus(RefundStatus.PROCESSED);
        refund.setProcessedAt(LocalDateTime.now());
        refundRepository.save(refund);

        payment.setStatus(PaymentStatus.REFUNDED);
        paymentRepository.save(payment);

        return new RefundResponseDto(refund.getId(), refund.getRefundTransactionId(), refund.getStatus());
    }

    @Override
    public String getProviderName() {
        return "cmiPaymentProvider";
    }
}


