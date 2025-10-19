package com.example.carebloom.services.vendors;

import com.example.carebloom.models.Order;
import com.example.carebloom.repositories.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
public class VendorDashboardService {
    private static final Logger logger = LoggerFactory.getLogger(VendorDashboardService.class);

    @Autowired
    private OrderRepository orderRepository;

    // Monthly analysis - comparing current year months with previous year
    public Map<String, Object> getMonthlyCustomerRate(String vendorId) {
        return getMonthlyAnalysis(vendorId, "customer-rate");
    }

    public Map<String, Object> getMonthlyItemsSold(String vendorId) {
        return getMonthlyAnalysis(vendorId, "items-sold");
    }

    public Map<String, Object> getMonthlyRevenue(String vendorId) {
        return getMonthlyAnalysis(vendorId, "revenue");
    }

    public Map<String, Object> getMonthlyOrderStatus(String vendorId) {
        return getMonthlyAnalysis(vendorId, "order-status");
    }

    // Yearly analysis - comparing current month days with previous month
    public Map<String, Object> getYearlyCustomerRate(String vendorId) {
        return getYearlyAnalysis(vendorId, "customer-rate");
    }

    public Map<String, Object> getYearlyItemsSold(String vendorId) {
        return getYearlyAnalysis(vendorId, "items-sold");
    }

    public Map<String, Object> getYearlyRevenue(String vendorId) {
        return getYearlyAnalysis(vendorId, "revenue");
    }

    public Map<String, Object> getYearlyOrderStatus(String vendorId) {
        return getYearlyAnalysis(vendorId, "order-status");
    }

    private Map<String, Object> getMonthlyAnalysis(String vendorId, String analysisType) {
        try {
            LocalDateTime now = LocalDateTime.now();
            int currentYear = now.getYear();
            int previousYear = currentYear - 1;

            Map<String, Object> response = new HashMap<>();
            response.put("currentYear", currentYear);
            response.put("previousYear", previousYear);

            // Get data for both years
            List<Map<String, Object>> currentYearMonths = getMonthlyData(vendorId, currentYear, analysisType);
            List<Map<String, Object>> previousYearMonths = getMonthlyData(vendorId, previousYear, analysisType);

            response.put("currentYearMonths", currentYearMonths);
            response.put("previousYearMonths", previousYearMonths);

            // Calculate totals
            int currentYearTotal = currentYearMonths.stream()
                    .mapToInt(month -> ((Number) month.get("count")).intValue())
                    .sum();

            int previousYearTotal = previousYearMonths.stream()
                    .mapToInt(month -> ((Number) month.get("count")).intValue())
                    .sum();

            response.put("currentYearTotal", currentYearTotal);
            response.put("previousYearTotal", previousYearTotal);

            // Calculate difference and percentage change
            int difference = currentYearTotal - previousYearTotal;
            double percentageChange = previousYearTotal == 0 ? 0 : ((double) difference / previousYearTotal) * 100;

            response.put("difference", difference);
            response.put("percentageChange", Math.round(percentageChange * 100.0) / 100.0);

            return response;
        } catch (Exception e) {
            logger.error("Error getting monthly analysis: ", e);
            return createErrorResponse("Failed to get monthly analysis");
        }
    }

    private Map<String, Object> getYearlyAnalysis(String vendorId, String analysisType) {
        try {
            LocalDateTime now = LocalDateTime.now();
            YearMonth currentMonth = YearMonth.from(now);
            YearMonth previousMonth = currentMonth.minusMonths(1);

            Map<String, Object> response = new HashMap<>();
            response.put("currentMonth", currentMonth.getMonth().name());
            response.put("previousMonth", previousMonth.getMonth().name());

            // Get daily data for both months
            List<Map<String, Object>> currentMonthDays = getDailyData(vendorId, currentMonth, analysisType);
            List<Map<String, Object>> previousMonthDays = getDailyData(vendorId, previousMonth, analysisType);

            response.put("currentMonthDays", currentMonthDays);
            response.put("previousMonthDays", previousMonthDays);

            // Calculate totals
            int currentMonthTotal = currentMonthDays.stream()
                    .mapToInt(day -> ((Number) day.get("count")).intValue())
                    .sum();

            int previousMonthTotal = previousMonthDays.stream()
                    .mapToInt(day -> ((Number) day.get("count")).intValue())
                    .sum();

            response.put("currentMonthTotal", currentMonthTotal);
            response.put("previousMonthTotal", previousMonthTotal);

            // Calculate difference and percentage change
            int difference = currentMonthTotal - previousMonthTotal;
            double percentageChange = previousMonthTotal == 0 ? 0 : ((double) difference / previousMonthTotal) * 100;

            response.put("difference", difference);
            response.put("percentageChange", Math.round(percentageChange * 100.0) / 100.0);

            return response;
        } catch (Exception e) {
            logger.error("Error getting yearly analysis: ", e);
            return createErrorResponse("Failed to get yearly analysis");
        }
    }

    private List<Map<String, Object>> getMonthlyData(String vendorId, int year, String analysisType) {
        List<Map<String, Object>> monthlyData = new ArrayList<>();

        for (Month month : Month.values()) {
            LocalDateTime startOfMonth = LocalDateTime.of(year, month, 1, 0, 0, 0);
            LocalDateTime endOfMonth = startOfMonth.with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59)
                    .withSecond(59);

            int count = calculateMetric(vendorId, startOfMonth, endOfMonth, analysisType);

            Map<String, Object> monthData = new HashMap<>();
            monthData.put("month", month.name());
            monthData.put("count", count);
            monthlyData.add(monthData);
        }

        return monthlyData;
    }

    private List<Map<String, Object>> getDailyData(String vendorId, YearMonth yearMonth, String analysisType) {
        List<Map<String, Object>> dailyData = new ArrayList<>();

        int daysInMonth = yearMonth.lengthOfMonth();

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDateTime startOfDay = LocalDateTime.of(yearMonth.getYear(), yearMonth.getMonth(), day, 0, 0, 0);
            LocalDateTime endOfDay = startOfDay.withHour(23).withMinute(59).withSecond(59);

            int count = calculateMetric(vendorId, startOfDay, endOfDay, analysisType);

            Map<String, Object> dayData = new HashMap<>();
            dayData.put("day", day);
            dayData.put("count", count);
            dailyData.add(dayData);
        }

        return dailyData;
    }

    private int calculateMetric(String vendorId, LocalDateTime start, LocalDateTime end, String analysisType) {
        List<Order> orders = orderRepository.findByVendorIdAndCreatedAtBetween(vendorId, start, end);

        switch (analysisType) {
            case "customer-rate":
                // Count distinct customers
                return (int) orders.stream()
                        .map(Order::getCustomerId)
                        .distinct()
                        .count();

            case "items-sold":
                // Count total items sold
                return orders.stream()
                        .flatMap(order -> order.getOrderItems().stream())
                        .mapToInt(item -> item.getQuantity() != null ? item.getQuantity() : 0)
                        .sum();

            case "revenue":
                // Sum total revenue (converted to int for count field)
                BigDecimal totalRevenue = orders.stream()
                        .map(Order::getTotalAmount)
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                return totalRevenue.intValue();

            case "order-status":
                // Count total orders (regardless of status)
                return orders.size();

            default:
                return 0;
        }
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", message);
        error.put("timestamp", LocalDateTime.now());
        return error;
    }
}
