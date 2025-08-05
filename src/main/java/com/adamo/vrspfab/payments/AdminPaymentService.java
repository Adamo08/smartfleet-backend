package com.adamo.vrspfab.payments;

import com.adamo.vrspfab.payments.*;
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

    public Page<Payment> findAllPayments(Pageable pageable) {
        return paymentRepository.findAll(pageable);
    }

    public Payment findPaymentById(Long paymentId) {
        return paymentRepository.findWithDetailsById(paymentId)
                .orElseThrow(() -> new PaymentException("Payment not found with ID: " + paymentId));
    }

    public List<Refund> findRefundsByPaymentId(Long paymentId) {
        return refundRepository.findByPaymentId(paymentId);
    }
}
