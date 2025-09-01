package com.adamo.vrspfab.payments;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper (componentModel = "spring")
public interface RefundMapper {


    /**
     * Maps a Refund entity to a RefundResponseDto.
     *
     * @param refund the Refund entity to map
     * @return the mapped RefundResponseDto
     */
    @Mapping(target = "refundRecordId", source = "id")
    @Mapping(target = "refundTransactionId", source = "refundTransactionId")
    @Mapping(target = "status", source = "status")
    RefundResponseDto toRefundResponseDto(Refund refund);

    /**
     * Maps a Refund entity to a RefundDetailsDto.
     *
     * @param refund the Refund entity to map
     * @return the mapped RefundDetailsDto
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "paymentId", source = "payment.id")
    @Mapping(target = "refundTransactionId", source = "refundTransactionId")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "currency", source = "currency")
    @Mapping(target = "reason", expression = "java(refund.getReason() != null ? refund.getReason().getDescription() : null)")
    @Mapping(target = "refundMethod", expression = "java(refund.getRefundMethod() != null ? refund.getRefundMethod().getDisplayName() : null)")
    @Mapping(target = "additionalNotes", source = "additionalNotes")
    @Mapping(target = "contactEmail", source = "contactEmail")
    @Mapping(target = "contactPhone", source = "contactPhone")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "requestedAt", source = "requestedAt")
    @Mapping(target = "processedAt", source = "processedAt")
    @Mapping(target = "reservationId", source = "payment.reservation.id")
    @Mapping(target = "userId", source = "payment.reservation.user.id")
    @Mapping(target = "userEmail", source = "payment.reservation.user.email")
    RefundDetailsDto toRefundDetailsDto(Refund refund);
}
