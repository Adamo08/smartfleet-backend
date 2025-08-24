package com.adamo.vrspfab.payments;

import com.adamo.vrspfab.common.SecurityRules;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

@Component
public class PaymentSecurityRules implements SecurityRules {

    private static final String ADMIN = "ADMIN";
    private static final String CUSTOMER = "CUSTOMER";

    /**
     * Configures security rules for the /payments endpoints.
     *
     * @param registry The Spring Security registry to configure.
     */
    @Override
    public void configure(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry) {
        registry
                // Customer and Admin can create payments
                .requestMatchers(HttpMethod.POST, "/payments/session").hasAnyRole(CUSTOMER, ADMIN)
                .requestMatchers(HttpMethod.POST, "/payments/process").hasAnyRole(CUSTOMER, ADMIN)
                .requestMatchers(HttpMethod.POST, "/payments/confirm/{sessionId}").hasAnyRole(CUSTOMER, ADMIN)
                .requestMatchers(HttpMethod.GET, "/payments/{paymentId}/status").hasAnyRole(CUSTOMER, ADMIN)

                // Payment details endpoints - authenticated users can view their own payments
                .requestMatchers(HttpMethod.GET, "/payments/{paymentId}").authenticated()
                .requestMatchers(HttpMethod.GET, "/payments/reservation/{reservationId}").authenticated()

                // Payment history endpoints - authenticated users can view their own history
                .requestMatchers(HttpMethod.GET, "/payments/history").authenticated()
                .requestMatchers(HttpMethod.GET, "/payments/history/filtered").authenticated()

                // Payment management endpoints - authenticated users can manage their own payments
                .requestMatchers(HttpMethod.POST, "/payments/{paymentId}/cancel").hasAnyRole(CUSTOMER, ADMIN)

                // Refund endpoints - can be initiated by customers or managed by admins
                .requestMatchers(HttpMethod.POST, "/payments/refund").hasAnyRole(CUSTOMER, ADMIN)
                .requestMatchers(HttpMethod.GET, "/payments/refund/{refundId}").hasAnyRole(CUSTOMER, ADMIN)

                // Payment methods and validation endpoints - authenticated users
                .requestMatchers(HttpMethod.GET, "/payments/methods").authenticated()
                .requestMatchers(HttpMethod.GET, "/payments/methods/{methodId}/validate").authenticated()

                // Analytics endpoint restricted to Admins
                .requestMatchers(HttpMethod.GET, "/payments/analytics").hasRole(ADMIN)

                // Admin-only payment management endpoints
                .requestMatchers("/admin/payments/**").hasRole(ADMIN)

                // Webhook endpoints must be public to be accessible by external services like Stripe/PayPal
                .requestMatchers(HttpMethod.POST, "/webhooks/**").permitAll();
    }
}
