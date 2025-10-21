package com.example.carebloom.services.vendors;

import com.example.carebloom.models.CartItem;
import com.example.carebloom.models.Product;
import com.example.carebloom.repositories.CartRepository;
import com.example.carebloom.repositories.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
public class VendorDashboardService {
    private static final Logger logger = LoggerFactory.getLogger(VendorDashboardService.class);

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    // Monthly analysis - comparing current year months with previous year
    public Map<String, Object> getMonthlyCustomerRate(String vendorId) {
        logger.info("=== GETTING MONTHLY CUSTOMER RATE FOR VENDOR: {} ===", vendorId);
        return getMonthlyAnalysis(vendorId, "customer-rate");
    }

    public Map<String, Object> getMonthlyItemsSold(String vendorId) {
        logger.info("=== GETTING MONTHLY ITEMS SOLD FOR VENDOR: {} ===", vendorId);
        return getMonthlyAnalysis(vendorId, "items-sold");
    }

    public Map<String, Object> getMonthlyRevenue(String vendorId) {
        return getMonthlyAnalysis(vendorId, "revenue");
    }
    // New method for monthly order status

    public Map<String, Object> getMonthlyOrderStatus(String vendorId) {
        return getMonthlyProductStatusAnalysis(vendorId);
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
        return getYearlyProductStatusAnalysis(vendorId);
    }

    // Special method for product status analysis (monthly)
    private Map<String, Object> getMonthlyProductStatusAnalysis(String vendorId) {
        try {
            LocalDateTime now = LocalDateTime.now();
            int currentYear = now.getYear();
            int previousYear = currentYear - 1;

            Map<String, Object> response = new HashMap<>();
            response.put("currentYear", currentYear);
            response.put("previousYear", previousYear);

            // Get product status data for both years
            List<Map<String, Object>> currentYearMonths = getMonthlyProductStatusData(vendorId, currentYear);
            List<Map<String, Object>> previousYearMonths = getMonthlyProductStatusData(vendorId, previousYear);

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
            logger.error("Error getting monthly product status analysis: ", e);
            return createErrorResponse("Failed to get monthly product status analysis");
        }
    }

    // Special method for product status analysis (yearly)
    private Map<String, Object> getYearlyProductStatusAnalysis(String vendorId) {
        try {
            LocalDateTime now = LocalDateTime.now();
            YearMonth currentMonth = YearMonth.from(now);
            YearMonth previousMonth = currentMonth.minusMonths(1);

            Map<String, Object> response = new HashMap<>();
            response.put("currentMonth", currentMonth.getMonth().name());
            response.put("previousMonth", previousMonth.getMonth().name());

            // Get daily product status data for both months
            List<Map<String, Object>> currentMonthDays = getDailyProductStatusData(vendorId, currentMonth);
            List<Map<String, Object>> previousMonthDays = getDailyProductStatusData(vendorId, previousMonth);

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
            logger.error("Error getting yearly product status analysis: ", e);
            return createErrorResponse("Failed to get yearly product status analysis");
        }
    }

    private Map<String, Object> getMonthlyAnalysis(String vendorId, String analysisType) {
        logger.info("VendorDashboardService.getMonthlyAnalysis() - Starting monthly analysis for vendorId: {}, analysisType: {}", vendorId, analysisType);
        
        try {
            LocalDateTime now = LocalDateTime.now();
            int currentYear = now.getYear();
            int previousYear = currentYear - 1;
            
            logger.info("VendorDashboardService.getMonthlyAnalysis() - Analysis period - Current year: {}, Previous year: {}", currentYear, previousYear);

            Map<String, Object> response = new HashMap<>();
            response.put("currentYear", currentYear);
            response.put("previousYear", previousYear);

            // Get data for both years
            logger.info("VendorDashboardService.getMonthlyAnalysis() - Fetching monthly data for current year: {}", currentYear);
            List<Map<String, Object>> currentYearMonths = getMonthlyData(vendorId, currentYear, analysisType);
            logger.info("VendorDashboardService.getMonthlyAnalysis() - Current year months data: {}", currentYearMonths);
            
            logger.info("VendorDashboardService.getMonthlyAnalysis() - Fetching monthly data for previous year: {}", previousYear);
            List<Map<String, Object>> previousYearMonths = getMonthlyData(vendorId, previousYear, analysisType);
            logger.info("VendorDashboardService.getMonthlyAnalysis() - Previous year months data: {}", previousYearMonths);

            response.put("currentYearMonths", currentYearMonths);
            response.put("previousYearMonths", previousYearMonths);

            // Calculate totals
            int currentYearTotal = currentYearMonths.stream()
                    .mapToInt(month -> ((Number) month.get("count")).intValue())
                    .sum();
            logger.info("VendorDashboardService.getMonthlyAnalysis() - Current year total calculated: {}", currentYearTotal);

            int previousYearTotal = previousYearMonths.stream()
                    .mapToInt(month -> ((Number) month.get("count")).intValue())
                    .sum();
            logger.info("VendorDashboardService.getMonthlyAnalysis() - Previous year total calculated: {}", previousYearTotal);

            response.put("currentYearTotal", currentYearTotal);
            response.put("previousYearTotal", previousYearTotal);

            // Calculate difference and percentage change
            int difference = currentYearTotal - previousYearTotal;
            double percentageChange = previousYearTotal == 0 ? 0 : ((double) difference / previousYearTotal) * 100;
            
            logger.info("VendorDashboardService.getMonthlyAnalysis() - Calculated difference: {}, percentage change: {}%", difference, percentageChange);

            response.put("difference", difference);
            response.put("percentageChange", Math.round(percentageChange * 100.0) / 100.0);

            logger.info("VendorDashboardService.getMonthlyAnalysis() - Final response structure: {}", response);
            return response;
        } catch (Exception e) {
            logger.error("VendorDashboardService.getMonthlyAnalysis() - Error getting monthly analysis for vendorId: {}, analysisType: {}, Exception: {}", vendorId, analysisType, e.getMessage(), e);
            return createErrorResponse("Failed to get monthly analysis");
        }
    }

    private Map<String, Object> getYearlyAnalysis(String vendorId, String analysisType) {
        logger.info("VendorDashboardService.getYearlyAnalysis() - Starting yearly analysis for vendorId: {}, analysisType: {}", vendorId, analysisType);
        
        try {
            LocalDateTime now = LocalDateTime.now();
            YearMonth currentMonth = YearMonth.from(now);
            YearMonth previousMonth = currentMonth.minusMonths(1);
            
            logger.info("VendorDashboardService.getYearlyAnalysis() - Analysis period - Current month: {}, Previous month: {}", currentMonth.getMonth().name(), previousMonth.getMonth().name());

            Map<String, Object> response = new HashMap<>();
            response.put("currentMonth", currentMonth.getMonth().name());
            response.put("previousMonth", previousMonth.getMonth().name());

            // Get daily data for both months
            logger.info("VendorDashboardService.getYearlyAnalysis() - Fetching daily data for current month: {}", currentMonth.getMonth().name());
            List<Map<String, Object>> currentMonthDays = getDailyData(vendorId, currentMonth, analysisType);
            logger.info("VendorDashboardService.getYearlyAnalysis() - Current month days data: {}", currentMonthDays);
            
            logger.info("VendorDashboardService.getYearlyAnalysis() - Fetching daily data for previous month: {}", previousMonth.getMonth().name());
            List<Map<String, Object>> previousMonthDays = getDailyData(vendorId, previousMonth, analysisType);
            logger.info("VendorDashboardService.getYearlyAnalysis() - Previous month days data: {}", previousMonthDays);

            response.put("currentMonthDays", currentMonthDays);
            response.put("previousMonthDays", previousMonthDays);

            // Calculate totals
            int currentMonthTotal = currentMonthDays.stream()
                    .mapToInt(day -> ((Number) day.get("count")).intValue())
                    .sum();
            logger.info("VendorDashboardService.getYearlyAnalysis() - Current month total calculated: {}", currentMonthTotal);

            int previousMonthTotal = previousMonthDays.stream()
                    .mapToInt(day -> ((Number) day.get("count")).intValue())
                    .sum();
            logger.info("VendorDashboardService.getYearlyAnalysis() - Previous month total calculated: {}", previousMonthTotal);

            response.put("currentMonthTotal", currentMonthTotal);
            response.put("previousMonthTotal", previousMonthTotal);

            // Calculate difference and percentage change
            int difference = currentMonthTotal - previousMonthTotal;
            double percentageChange = previousMonthTotal == 0 ? 0 : ((double) difference / previousMonthTotal) * 100;
            
            logger.info("VendorDashboardService.getYearlyAnalysis() - Calculated difference: {}, percentage change: {}%", difference, percentageChange);

            response.put("difference", difference);
            response.put("percentageChange", Math.round(percentageChange * 100.0) / 100.0);

            logger.info("VendorDashboardService.getYearlyAnalysis() - Final response structure: {}", response);
            return response;
        } catch (Exception e) {
            logger.error("VendorDashboardService.getYearlyAnalysis() - Error getting yearly analysis for vendorId: {}, analysisType: {}, Exception: {}", vendorId, analysisType, e.getMessage(), e);
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

    private List<Map<String, Object>> getMonthlyProductStatusData(String vendorId, int year) {
        List<Map<String, Object>> monthlyData = new ArrayList<>();

        for (Month month : Month.values()) {
            // For product status, we'll count products by different statuses
            // Since products don't have time-based creation per month in this context,
            // we'll count current active products for consistency
            List<Product> products = productRepository.findByVendorId(vendorId);
            int count = products.size();

            Map<String, Object> monthData = new HashMap<>();
            monthData.put("month", month.name());
            monthData.put("count", count);
            monthlyData.add(monthData);
        }

        return monthlyData;
    }

    private List<Map<String, Object>> getDailyProductStatusData(String vendorId, YearMonth yearMonth) {
        List<Map<String, Object>> dailyData = new ArrayList<>();

        int daysInMonth = yearMonth.lengthOfMonth();

        for (int day = 1; day <= daysInMonth; day++) {
            // For product status, we'll count current products
            // This could be enhanced to track daily changes if needed
            List<Product> products = productRepository.findByVendorId(vendorId);
            int count = products.size();

            Map<String, Object> dayData = new HashMap<>();
            dayData.put("day", day);
            dayData.put("count", count);
            dailyData.add(dayData);
        }

        return dailyData;
    }

    private int calculateMetric(String vendorId, LocalDateTime start, LocalDateTime end, String analysisType) {
        logger.info("VendorDashboardService.calculateMetric() - Calculating metric for vendorId: {}, analysisType: {}, period: {} to {}", vendorId, analysisType, start, end);
        
        switch (analysisType) {
            case "customer-rate":
                // Count distinct customers who added items to cart in this period
                logger.info("VendorDashboardService.calculateMetric() - Calculating customer-rate metric");
                List<CartItem> cartItems = cartRepository.findByVendorIdAndAddedAtBetween(vendorId, start, end);
                logger.info("VendorDashboardService.calculateMetric() - Found {} cart items for customer-rate calculation", cartItems.size());
                
                int distinctCustomers = (int) cartItems.stream()
                        .map(CartItem::getUserId)
                        .distinct()
                        .count();
                logger.info("VendorDashboardService.calculateMetric() - Customer-rate result: {} distinct customers", distinctCustomers);
                return distinctCustomers;

            case "items-sold":
                // Count total items that have been delivered/sold
                logger.info("VendorDashboardService.calculateMetric() - Calculating items-sold metric");
                List<CartItem> soldItems = cartRepository.findSoldItemsByVendorIdAndDeliveredAtBetween(vendorId, start, end);
                logger.info("VendorDashboardService.calculateMetric() - Found {} sold items for items-sold calculation", soldItems.size());
                
                int totalItemsSold = soldItems.stream()
                        .mapToInt(item -> item.getQuantity() != null ? item.getQuantity() : 0)
                        .sum();
                logger.info("VendorDashboardService.calculateMetric() - Items-sold result: {} total items", totalItemsSold);
                return totalItemsSold;

            case "revenue":
                // Sum total revenue from delivered items
                logger.info("VendorDashboardService.calculateMetric() - Calculating revenue metric");
                List<CartItem> deliveredItems = cartRepository.findSoldItemsByVendorIdAndDeliveredAtBetween(vendorId, start, end);
                logger.info("VendorDashboardService.calculateMetric() - Found {} delivered items for revenue calculation", deliveredItems.size());
                
                double totalRevenue = deliveredItems.stream()
                        .mapToDouble(item -> {
                            double price = item.getPriceAtAdd() != null ? item.getPriceAtAdd() : 0.0;
                            int quantity = item.getQuantity() != null ? item.getQuantity() : 0;
                            double itemRevenue = price * quantity;
                            logger.debug("VendorDashboardService.calculateMetric() - Item revenue: price={}, quantity={}, total={}", price, quantity, itemRevenue);
                            return itemRevenue;
                        })
                        .sum();
                int roundedRevenue = (int) Math.round(totalRevenue);
                logger.info("VendorDashboardService.calculateMetric() - Revenue result: {} (rounded from {})", roundedRevenue, totalRevenue);
                return roundedRevenue;

            case "order-status":
                // Count products by different statuses for better insights
                logger.info("VendorDashboardService.calculateMetric() - Calculating order-status metric");
                List<Product> products = productRepository.findByVendorId(vendorId);
                logger.info("VendorDashboardService.calculateMetric() - Found {} products for order-status calculation", products.size());

                // You can enhance this to count by specific status
                // For now, we'll count active products
                int activeProducts = (int) products.stream()
                        .filter(product -> "active".equals(product.getStatus()) ||
                                (product.getIsActive() != null && product.getIsActive()))
                        .count();
                logger.info("VendorDashboardService.calculateMetric() - Order-status result: {} active products", activeProducts);
                return activeProducts;

            default:
                logger.warn("VendorDashboardService.calculateMetric() - Unknown analysis type: {}, returning 0", analysisType);
                return 0;
        }
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", message);
        error.put("timestamp", LocalDateTime.now());
        return error;
    }

    // Additional method to get detailed product status breakdown
    public Map<String, Object> getProductStatusBreakdown(String vendorId) {
        try {
            List<Product> products = productRepository.findByVendorId(vendorId);

            Map<String, Long> statusCounts = new HashMap<>();
            statusCounts.put("active", products.stream().filter(p -> "active".equals(p.getStatus())).count());
            statusCounts.put("inactive", products.stream().filter(p -> "inactive".equals(p.getStatus())).count());
            statusCounts.put("out-of-stock",
                    products.stream().filter(p -> "out-of-stock".equals(p.getStatus())).count());

            Map<String, Object> response = new HashMap<>();
            response.put("statusBreakdown", statusCounts);
            response.put("totalProducts", products.size());
            response.put("timestamp", LocalDateTime.now());

            return response;
        } catch (Exception e) {
            logger.error("Error getting product status breakdown: ", e);
            return createErrorResponse("Failed to get product status breakdown");
        }
    }
}
