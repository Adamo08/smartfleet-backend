package com.adamo.vrspfab.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "refunds")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Refund {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency;

    @Column(name = "reason", nullable = false, length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RefundStatus status;

    @Column(name = "requested_at", nullable = false, updatable = false)
    private Timestamp requestedAt;

    @Column(name = "processed_at")
    private Timestamp processedAt;
}