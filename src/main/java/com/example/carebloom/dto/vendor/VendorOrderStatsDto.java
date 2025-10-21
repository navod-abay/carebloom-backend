package com.example.carebloom.dto.vendor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorOrderStatsDto {
    private Long totalOrders;           // Total unique orders (grouped by userId + date)
    private Long pendingOrders;         // Orders with status = "pending"
    private Double thisMonthRevenue;    // Sum of delivered orders this month
}
