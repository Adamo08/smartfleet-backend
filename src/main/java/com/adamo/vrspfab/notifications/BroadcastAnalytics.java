package com.adamo.vrspfab.notifications;

import lombok.Data;

@Data
public class BroadcastAnalytics {
    
    private Long totalUsers;
    private Long sentCount;
    private Long failedCount;
    private Long readCount;
    private Long clickCount;
    private Double deliveryRate;
    private Double readRate;
    private Double clickRate;
    
    public BroadcastAnalytics() {
        this.totalUsers = 0L;
        this.sentCount = 0L;
        this.failedCount = 0L;
        this.readCount = 0L;
        this.clickCount = 0L;
        this.deliveryRate = 0.0;
        this.readRate = 0.0;
        this.clickRate = 0.0;
    }
    
    public void calculateRates() {
        if (sentCount > 0) {
            this.deliveryRate = ((double) (sentCount - failedCount) / sentCount) * 100;
            this.readRate = ((double) readCount / sentCount) * 100;
            this.clickRate = ((double) clickCount / sentCount) * 100;
        }
    }
}
