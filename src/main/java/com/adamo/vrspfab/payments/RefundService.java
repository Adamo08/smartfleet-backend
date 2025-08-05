package com.adamo.vrspfab.payments;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefundService {

    private final PaymentProvider paymentProvider;
    private final RefundRepository refundRepository;

    @Transactional
    public RefundResponseDto processRefund(RefundRequestDto requestDto) {
        validateRefundRequest(requestDto);
        RefundResponseDto response = paymentProvider.processRefund(requestDto);
        return response;
    }

    @Transactional(readOnly = true)
    public Refund getRefundDetails(Long refundId) {
        return refundRepository.findById(refundId)
                .orElseThrow(() -> new IllegalArgumentException("Refund not found: " + refundId));
    }

    private void validateRefundRequest(RefundRequestDto requestDto) {
        if (requestDto.getAmount() <= 0) {
            throw new IllegalArgumentException("Refund amount must be positive");
        }
        if (requestDto.getReason() == null || requestDto.getReason().trim().isEmpty()) {
            throw new IllegalArgumentException("Refund reason is required");
        }
    }
}