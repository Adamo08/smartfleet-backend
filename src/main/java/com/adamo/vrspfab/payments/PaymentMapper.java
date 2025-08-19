package com.adamo.vrspfab.payments;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Mapper (componentModel = "spring")
public interface PaymentMapper {
    @Mapping(target = "id", source = "id")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "currency", source = "currency")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "provider", source = "provider")
    @Mapping(target = "transactionId", source = "transactionId")
    @Mapping(target = "captureId", source = "captureId")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "reservationId", source = "reservation.id")
    @Mapping(target = "userId", source = "reservation.user.id")
    @Mapping(target = "userEmail", source = "reservation.user.email")
    PaymentDetailsDto toPaymentDetailsDto(Payment payment);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "reservationId", source = "reservation.id")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "currency", source = "currency")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "transactionId", source = "transactionId")
    @Mapping(target = "captureId", source = "captureId")
    @Mapping(target = "provider", source = "provider")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    PaymentDto toPaymentDto(Payment payment);
}
