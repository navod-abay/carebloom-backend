package com.example.carebloom.controllers.admin;

import com.example.carebloom.services.admin.AdminDashboardService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/")
@CrossOrigin(origins = "${app.cors.admin-origin}")
public class AdminDashboardController {
    @Autowired
    private AdminDashboardService dashboardService;

    /**
     * Get total count of mothers with accepted registration statuses
     */
    @GetMapping("/get-mothers-count")
    public ResponseEntity<Long> getTotalMothersCount() {
        try {
            long count = dashboardService.getTotalMothersCount();
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get yearly comparison stats (current year vs previous year)
     */
    @GetMapping("/yearly-comparison")
    public ResponseEntity<AdminDashboardService.YearlyComparisonStats> getYearlyComparison() {
        try {
            AdminDashboardService.YearlyComparisonStats stats = dashboardService.getYearlyComparison();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get monthly comparison stats (current month vs previous month)
     */
    @GetMapping("/monthly-comparison")
    public ResponseEntity<AdminDashboardService.MonthlyComparisonStats> getMonthlyComparison() {
        try {
            AdminDashboardService.MonthlyComparisonStats stats = dashboardService.getMonthlyComparison();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get complete dashboard statistics including total, yearly and monthly
     * comparisons
     */
    @GetMapping("/dashboard-stats")
    public ResponseEntity<AdminDashboardService.DashboardStats> getDashboardStats() {
        try {
            AdminDashboardService.DashboardStats stats = dashboardService.getCompleteStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get monthly registration status totals (complete, normal, accepted counts for
     * current month)
     */
    @GetMapping("/month-total-registrationstatuses")
    public ResponseEntity<AdminDashboardService.RegistrationStatusTotals> getMonthlyRegistrationStatusTotals() {
        try {
            AdminDashboardService.RegistrationStatusTotals stats = dashboardService
                    .getMonthlyRegistrationStatusTotals();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get yearly registration status totals (complete, normal, accepted counts for
     * current year)
     */
    @GetMapping("/year-total-registrationstatuses")
    public ResponseEntity<AdminDashboardService.RegistrationStatusTotals> getYearlyRegistrationStatusTotals() {
        try {
            AdminDashboardService.RegistrationStatusTotals stats = dashboardService.getYearlyRegistrationStatusTotals();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get monthly district-wise totals (count of mothers by district for current
     * month)
     */
    @GetMapping("/month-districtwise-total-mothers")
    public ResponseEntity<AdminDashboardService.DistrictWiseTotals> getMonthlyDistrictWiseTotals() {
        try {
            AdminDashboardService.DistrictWiseTotals stats = dashboardService.getMonthlyDistrictWiseTotals();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get yearly district-wise totals (count of mothers by district for current
     * year)
     */
    @GetMapping("/year-districtwise-total-mothers")
    public ResponseEntity<AdminDashboardService.DistrictWiseTotals> getYearlyDistrictWiseTotals() {
        try {
            AdminDashboardService.DistrictWiseTotals stats = dashboardService.getYearlyDistrictWiseTotals();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
