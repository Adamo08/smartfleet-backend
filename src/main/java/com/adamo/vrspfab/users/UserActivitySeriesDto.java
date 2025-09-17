package com.adamo.vrspfab.users;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserActivitySeriesDto {
    private List<String> months; // e.g., ["Apr", "May", ...]
    private List<Long> monthlyReservationCounts;
    private List<BigDecimal> monthlySpending; // completed payment amounts per month
}


