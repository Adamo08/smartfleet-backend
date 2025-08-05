package com.adamo.vrspfab.payments;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentProviderFactory providerFactory;
    private final PaymentRepository paymentRepository;
    private final Cache<String, PaymentResponseDto> idempotencyCache;

    @Transactional
    public SessionResponseDto createPaymentSession(SessionRequestDto requestDto) {
        PaymentProvider provider = providerFactory.getProvider(requestDto.getProviderName())
                .orElseThrow(() -> new IllegalArgumentException("Invalid payment provider: " + requestDto.getProviderName()));
        return provider.createPaymentSession(requestDto);
    }

    @Transactional
    public PaymentResponseDto processPayment(PaymentRequestDto requestDto, String idempotencyKey) {
        if (idempotencyKey != null) {
            PaymentResponseDto cached = idempotencyCache.asMap().get(idempotencyKey);
            if (cached != null) {
                return cached;
            }
        }
        PaymentProvider provider = providerFactory.getProvider(requestDto.getProviderName())
                .orElseThrow(() -> new PaymentException("Invalid payment provider: " + requestDto.getProviderName()));
        PaymentResponseDto response = provider.processPayment(requestDto);
        if (idempotencyKey != null) {
            idempotencyCache.put(idempotencyKey, response);
        }
        return response;
    }

    @Transactional(readOnly = true)
    public PaymentResponseDto getPaymentStatus(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException("Payment not found with ID: " + paymentId));

        // Assuming the provider name is stored on the payment entity
        PaymentProvider provider = providerFactory.getProvider(payment.getProvider())
                .orElseThrow(() -> new IllegalStateException("Provider not found for payment: " + paymentId));

        return provider.getPaymentStatus(paymentId);
    }
}
