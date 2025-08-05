package com.adamo.vrspfab.payments;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
public class PaymentConfig {

    /**
     * Creates a factory bean that holds a map of all available PaymentProvider implementations.
     * The key is the provider's name (e.g., "stripePaymentProvider"), and the value is the bean instance.
     * This allows for dynamic selection of a payment provider at runtime.
     *
     * @param providers A list of all beans that implement the PaymentProvider interface, injected by Spring.
     * @return A PaymentProviderFactory containing a map of all available providers.
     */
    @Bean
    public PaymentProviderFactory paymentProviderFactory(List<PaymentProvider> providers) {
        Map<String, PaymentProvider> providerMap = providers.stream()
                .collect(Collectors.toMap(
                        PaymentProvider::getProviderName,
                        Function.identity()
                ));
        return new PaymentProviderFactory(providerMap);
    }
}
