package com.adamo.vrspfab.payments;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/**
 * Factory class for retrieving a specific PaymentProvider implementation at runtime.
 * This class holds a map of all available payment providers and allows for their
 * dynamic selection based on a provider name.
 */
@Component
public class PaymentProviderFactory {

    private final Map<String, PaymentProvider> providerMap;

    public PaymentProviderFactory(Map<String, PaymentProvider> providerMap) {
        this.providerMap = providerMap;
    }

    /**
     * Retrieves a payment provider by its designated name.
     *
     * @param providerName The name of the provider to retrieve (e.g., "stripePaymentProvider").
     * @return An Optional containing the PaymentProvider if found, otherwise an empty Optional.
     */
    public Optional<PaymentProvider> getProvider(String providerName) {
        return Optional.ofNullable(providerMap.get(providerName));
    }
}
