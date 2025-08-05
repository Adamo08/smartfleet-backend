package com.adamo.vrspfab.payments;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefundService {

    private final PaymentProviderFactory providerFactory;
    private final RefundRepository refundRepository;
    private final PaymentRepository paymentRepository;

    @Transactional
    public RefundResponseDto processRefund(RefundRequestDto requestDto) {
        validateRefundRequest(requestDto);

        Payment payment = paymentRepository.findById(requestDto.getPaymentId())
                .orElseThrow(() -> new PaymentException("Payment not found with ID: " + requestDto.getPaymentId()));

        PaymentProvider provider = providerFactory.getProvider(payment.getProvider())
                .orElseThrow(() -> new IllegalStateException("Provider not found for payment: " + payment.getId()));

        return provider.processRefund(requestDto);
    }

    @Transactional(readOnly = true)
    public Refund getRefundDetails(Long refundId) {
        return refundRepository.findById(refundId)
                .orElseThrow(() -> new PaymentException("Refund not found: " + refundId));
    }

    private void validateRefundRequest(RefundRequestDto requestDto) {
        if (requestDto.getAmount() <= 0) {
            throw new IllegalArgumentException("Refund amount must be positive.");
        }
        if (requestDto.getReason() == null || requestDto.getReason().trim().isEmpty()) {
            throw new IllegalArgumentException("Refund reason is required.");
        }
    }
}
