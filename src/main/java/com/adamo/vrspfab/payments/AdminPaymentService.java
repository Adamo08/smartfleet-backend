package com.adamo.vrspfab.payments;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service providing business logic for administrator-specific payment operations.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminPaymentService {

    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final RefundMapper refundMapper;
    private final PaymentMapper paymentMapper;

    public Page<PaymentDetailsDto> findAllPayments(Pageable pageable) {
        Page<Payment> paymentsPage = paymentRepository.findAll(pageable);
        return paymentsPage.map(paymentMapper::toPaymentDetailsDto);
    }

    public PaymentDetailsDto findPaymentById(Long paymentId) {
        Payment payment = paymentRepository.findWithDetailsById(paymentId)
                .orElseThrow(() -> new PaymentException("Payment not found with ID: " + paymentId));
        return paymentMapper.toPaymentDetailsDto(payment);
    }

    public List<RefundDetailsDto> findRefundsByPaymentId(Long paymentId) {
        var refunds = refundRepository.findByPaymentId(paymentId);
        if (refunds.isEmpty()) {
            throw new PaymentException("No refunds found for payment ID: " + paymentId);
        }

        return refunds.stream()
                .map(refundMapper::toRefundDetailsDto)
                .toList();
    }
}
