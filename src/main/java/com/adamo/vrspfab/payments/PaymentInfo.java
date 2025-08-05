package com.adamo.vrspfab.payments;

import jakarta.persistence.*;
import lombok.*;

// This entity remains valid. It holds specific details about the payment method used.
@Entity
@Table(name = "payment_infos")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PaymentInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false, unique = true)
    private Payment payment;

    @Column(name = "payment_method_brand", nullable = false) // e.g., "visa", "paypal"
    private String paymentMethodBrand;

    @Column(nullable = false)
    private String lastFourDigits; // e.g., "4242" for cards

    @Column
    private String expiryDate; // e.g., "12/25", can be null for methods like PayPal
}
