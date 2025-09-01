package com.adamo.vrspfab.reservations;

import lombok.Data;

/**
 * Contains additional booking context information for reservations.
 * This includes slot type, duration, calculation method, and user preferences.
 */
@Data
public class ReservationBookingContext {
    
    /**
     * The type of slot selected for the reservation.
     */
    private String slotType; // HOURLY, DAILY, WEEKLY, CUSTOM
    
    /**
     * Duration of the reservation in hours.
     */
    private Integer duration;
    
    /**
     * Method used for calculating the reservation amount.
     */
    private String calculationMethod; // SLOT_BASED, DATE_RANGE, DURATION_BASED
    
    /**
     * Optional array of selected slot IDs if using slot-based booking.
     */
    private Long[] selectedSlotIds;
    
    /**
     * Original calculated amount before any modifications.
     */
    private Double originalAmount;
    
    /**
     * User preferences and special requests.
     */
    private BookingPreferences preferences;
    
    /**
     * Nested class for booking preferences.
     */
    @Data
    public static class BookingPreferences {
        /**
         * User's preferred payment method.
         */
        private String preferredPaymentMethod;
        
        /**
         * Any special requests or additional notes.
         */
        private String specialRequests;
    }
}
