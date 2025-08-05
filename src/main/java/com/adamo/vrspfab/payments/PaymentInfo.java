package com.adamo.vrspfab.payments;

import jakarta.persistence.*;
import lombok.*;

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

    @Column(name = "payment_method", nullable = false)
    private String paymentMethod; // e.g., "CARD", "PAYPAL"

    @Column(nullable = false)
    private String lastFourDigits; // e.g., "1234" for security

    @Column(nullable = false)
    private String expiryDate; // e.g., "12/25"
}