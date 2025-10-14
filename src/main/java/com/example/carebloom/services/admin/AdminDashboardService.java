package com.example.carebloom.services.admin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.carebloom.models.Mother;
import com.example.carebloom.models.MOHOffice;
import com.example.carebloom.repositories.MotherRepository;
import com.example.carebloom.repositories.MOHOfficeRepository;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdminDashboardService {
    private static final Logger logger = LoggerFactory.getLogger(AdminDashboardService.class);

    @Autowired
    private MotherRepository motherRepository;

    @Autowired
    private MOHOfficeRepository mohOfficeRepository;

    /**
     * Get total count of mothers with accepted registration statuses
     */
    public long getTotalMothersCount() {
        return motherRepository.totalMothersCount();
    }

    /**
     * Get year-wise comparison of mother registrations with monthly breakdown
     */
    public YearlyComparisonStats getYearlyComparison() {
        int currentYear = LocalDateTime.now().getYear();
        int previousYear = currentYear - 1;

        // Get monthly breakdown for current year
        List<MonthlyData> currentYearMonths = new ArrayList<>();
        long currentYearTotal = 0;

        for (int month = 1; month <= 12; month++) {
            LocalDateTime monthStart = LocalDateTime.of(currentYear, month, 1, 0, 0, 0);
            LocalDateTime monthEnd = monthStart.plusMonths(1).minusSeconds(1);

            long monthCount = motherRepository.countByAcceptedStatusesAndCreatedAtBetween(monthStart, monthEnd);
            currentYearMonths.add(new MonthlyData(Month.of(month).name(), monthCount));
            currentYearTotal += monthCount;
        }

        // Get monthly breakdown for previous year
        List<MonthlyData> previousYearMonths = new ArrayList<>();
        long previousYearTotal = 0;

        for (int month = 1; month <= 12; month++) {
            LocalDateTime monthStart = LocalDateTime.of(previousYear, month, 1, 0, 0, 0);
            LocalDateTime monthEnd = monthStart.plusMonths(1).minusSeconds(1);

            long monthCount = motherRepository.countByAcceptedStatusesAndCreatedAtBetween(monthStart, monthEnd);
            previousYearMonths.add(new MonthlyData(Month.of(month).name(), monthCount));
            previousYearTotal += monthCount;
        }

        logger.info("Yearly comparison - Current year ({}): {}, Previous year ({}): {}",
                currentYear, currentYearTotal, previousYear, previousYearTotal);

        return new YearlyComparisonStats(currentYear, currentYearMonths, currentYearTotal,
                previousYear, previousYearMonths, previousYearTotal);
    }

    /**
     * Get monthly comparison of mother registrations with daily breakdown
     */
    public MonthlyComparisonStats getMonthlyComparison() {
        LocalDateTime now = LocalDateTime.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();

        // Current month: First day to last day
        LocalDateTime currentMonthStart = LocalDateTime.of(currentYear, currentMonth, 1, 0, 0, 0);

        // Get daily breakdown for current month
        List<DailyData> currentMonthDays = new ArrayList<>();
        long currentMonthTotal = 0;
        int daysInCurrentMonth = currentMonthStart.toLocalDate().lengthOfMonth();

        for (int day = 1; day <= daysInCurrentMonth; day++) {
            LocalDateTime dayStart = LocalDateTime.of(currentYear, currentMonth, day, 0, 0, 0);
            LocalDateTime dayEnd = LocalDateTime.of(currentYear, currentMonth, day, 23, 59, 59);

            long dayCount = motherRepository.countByAcceptedStatusesAndCreatedAtBetween(dayStart, dayEnd);
            currentMonthDays.add(new DailyData(day, dayCount));
            currentMonthTotal += dayCount;
        }

        // Previous month
        LocalDateTime previousMonthStart = currentMonthStart.minusMonths(1);

        // Get daily breakdown for previous month
        List<DailyData> previousMonthDays = new ArrayList<>();
        long previousMonthTotal = 0;
        int daysInPreviousMonth = previousMonthStart.toLocalDate().lengthOfMonth();

        for (int day = 1; day <= daysInPreviousMonth; day++) {
            LocalDateTime dayStart = LocalDateTime.of(previousMonthStart.getYear(), previousMonthStart.getMonthValue(),
                    day, 0, 0, 0);
            LocalDateTime dayEnd = LocalDateTime.of(previousMonthStart.getYear(), previousMonthStart.getMonthValue(),
                    day, 23, 59, 59);

            long dayCount = motherRepository.countByAcceptedStatusesAndCreatedAtBetween(dayStart, dayEnd);
            previousMonthDays.add(new DailyData(day, dayCount));
            previousMonthTotal += dayCount;
        }

        String currentMonthName = now.getMonth().name();
        String previousMonthName = previousMonthStart.getMonth().name();

        logger.info("Monthly comparison - Current month ({}): {}, Previous month ({}): {}",
                currentMonthName, currentMonthTotal, previousMonthName, previousMonthTotal);

        return new MonthlyComparisonStats(currentMonthName, currentMonthDays, currentMonthTotal,
                previousMonthName, previousMonthDays, previousMonthTotal);
    }

    /**
     * Get complete dashboard statistics
     */
    public DashboardStats getCompleteStats() {
        long totalCount = getTotalMothersCount();
        YearlyComparisonStats yearlyStats = getYearlyComparison();
        MonthlyComparisonStats monthlyStats = getMonthlyComparison();

        return new DashboardStats(totalCount, yearlyStats, monthlyStats);
    }

    /**
     * Get monthly registration status totals for current month
     */
    public RegistrationStatusTotals getMonthlyRegistrationStatusTotals() {
        LocalDateTime now = LocalDateTime.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();

        LocalDateTime monthStart = LocalDateTime.of(currentYear, currentMonth, 1, 0, 0, 0);
        LocalDateTime monthEnd = monthStart.plusMonths(1).minusSeconds(1);

        long completeCount = motherRepository.countByRegistrationStatusAndCreatedAtBetween("complete", monthStart,
                monthEnd);
        long normalCount = motherRepository.countByRegistrationStatusAndCreatedAtBetween("normal", monthStart,
                monthEnd);
        long acceptedCount = motherRepository.countByRegistrationStatusAndCreatedAtBetween("accepted", monthStart,
                monthEnd);

        String monthName = now.getMonth().name();

        logger.info("Monthly status totals for {} - Complete: {}, Normal: {}, Accepted: {}",
                monthName, completeCount, normalCount, acceptedCount);

        return new RegistrationStatusTotals(monthName + " " + currentYear, completeCount, normalCount, acceptedCount);
    }

    /**
     * Get yearly registration status totals for current year
     */
    public RegistrationStatusTotals getYearlyRegistrationStatusTotals() {
        int currentYear = LocalDateTime.now().getYear();

        LocalDateTime yearStart = LocalDateTime.of(currentYear, 1, 1, 0, 0, 0);
        LocalDateTime yearEnd = LocalDateTime.of(currentYear, 12, 31, 23, 59, 59);

        long completeCount = motherRepository.countByRegistrationStatusAndCreatedAtBetween("complete", yearStart,
                yearEnd);
        long normalCount = motherRepository.countByRegistrationStatusAndCreatedAtBetween("normal", yearStart, yearEnd);
        long acceptedCount = motherRepository.countByRegistrationStatusAndCreatedAtBetween("accepted", yearStart,
                yearEnd);

        logger.info("Yearly status totals for {} - Complete: {}, Normal: {}, Accepted: {}",
                currentYear, completeCount, normalCount, acceptedCount);

        return new RegistrationStatusTotals(String.valueOf(currentYear), completeCount, normalCount, acceptedCount);
    }

    /**
     * Get monthly district-wise totals for current month
     */
    public DistrictWiseTotals getMonthlyDistrictWiseTotals() {
        LocalDateTime now = LocalDateTime.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();

        LocalDateTime monthStart = LocalDateTime.of(currentYear, currentMonth, 1, 0, 0, 0);
        LocalDateTime monthEnd = monthStart.plusMonths(1).minusSeconds(1);

        // Get all mothers within the date range to extract distinct districts
        List<Mother> mothers = motherRepository.findDistinctDistrictsByAcceptedStatusesAndCreatedAtBetween(monthStart,
                monthEnd);

        // Extract unique districts
        List<String> distinctDistricts = mothers.stream()
                .map(Mother::getDistrict)
                .filter(district -> district != null && !district.trim().isEmpty())
                .distinct()
                .sorted()
                .toList();

        // Get count for each district
        List<DistrictData> districtCounts = new ArrayList<>();
        for (String district : distinctDistricts) {
            long count = motherRepository.countByDistrictAndAcceptedStatusesAndCreatedAtBetween(district, monthStart,
                    monthEnd);
            if (count > 0) {
                districtCounts.add(new DistrictData(district, count));
            }
        }

        String monthName = now.getMonth().name();

        logger.info("Monthly district-wise totals for {} - {} districts found", monthName, districtCounts.size());

        return new DistrictWiseTotals(monthName + " " + currentYear, districtCounts);
    }

    /**
     * Get yearly district-wise totals for current year
     */
    public DistrictWiseTotals getYearlyDistrictWiseTotals() {
        int currentYear = LocalDateTime.now().getYear();

        LocalDateTime yearStart = LocalDateTime.of(currentYear, 1, 1, 0, 0, 0);
        LocalDateTime yearEnd = LocalDateTime.of(currentYear, 12, 31, 23, 59, 59);

        // Get all mothers within the date range to extract distinct districts
        List<Mother> mothers = motherRepository.findDistinctDistrictsByAcceptedStatusesAndCreatedAtBetween(yearStart,
                yearEnd);

        // Extract unique districts
        List<String> distinctDistricts = mothers.stream()
                .map(Mother::getDistrict)
                .filter(district -> district != null && !district.trim().isEmpty())
                .distinct()
                .sorted()
                .toList();

        // Get count for each district
        List<DistrictData> districtCounts = new ArrayList<>();
        for (String district : distinctDistricts) {
            long count = motherRepository.countByDistrictAndAcceptedStatusesAndCreatedAtBetween(district, yearStart,
                    yearEnd);
            if (count > 0) {
                districtCounts.add(new DistrictData(district, count));
            }
        }

        logger.info("Yearly district-wise totals for {} - {} districts found", currentYear, districtCounts.size());

        return new DistrictWiseTotals(String.valueOf(currentYear), districtCounts);
    }

    /**
     * Get monthly MOH office registrations with daily breakdown (current month vs
     * previous month)
     */
    public MOHOfficeRegistrationStats getMonthlyMOHOfficeRegistrations() {
        LocalDateTime now = LocalDateTime.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();

        // Current month: First day to last day
        LocalDateTime currentMonthStart = LocalDateTime.of(currentYear, currentMonth, 1, 0, 0, 0);

        // Get daily breakdown for current month
        List<DailyData> currentMonthDays = new ArrayList<>();
        long currentMonthTotal = 0;
        int daysInCurrentMonth = currentMonthStart.toLocalDate().lengthOfMonth();

        for (int day = 1; day <= daysInCurrentMonth; day++) {
            LocalDateTime dayStart = LocalDateTime.of(currentYear, currentMonth, day, 0, 0, 0);
            LocalDateTime dayEnd = LocalDateTime.of(currentYear, currentMonth, day, 23, 59, 59);

            long dayCount = mohOfficeRepository.countByCreatedAtBetween(dayStart, dayEnd);
            currentMonthDays.add(new DailyData(day, dayCount));
            currentMonthTotal += dayCount;
        }

        // Previous month
        LocalDateTime previousMonthStart = currentMonthStart.minusMonths(1);

        // Get daily breakdown for previous month
        List<DailyData> previousMonthDays = new ArrayList<>();
        long previousMonthTotal = 0;
        int daysInPreviousMonth = previousMonthStart.toLocalDate().lengthOfMonth();

        for (int day = 1; day <= daysInPreviousMonth; day++) {
            LocalDateTime dayStart = LocalDateTime.of(previousMonthStart.getYear(), previousMonthStart.getMonthValue(),
                    day, 0, 0, 0);
            LocalDateTime dayEnd = LocalDateTime.of(previousMonthStart.getYear(), previousMonthStart.getMonthValue(),
                    day, 23, 59, 59);

            long dayCount = mohOfficeRepository.countByCreatedAtBetween(dayStart, dayEnd);
            previousMonthDays.add(new DailyData(day, dayCount));
            previousMonthTotal += dayCount;
        }

        String currentMonthName = now.getMonth().name();
        String previousMonthName = previousMonthStart.getMonth().name();

        logger.info("Monthly MOH office registrations - Current month ({}): {}, Previous month ({}): {}",
                currentMonthName, currentMonthTotal, previousMonthName, previousMonthTotal);

        return new MOHOfficeRegistrationStats(currentMonthName, currentMonthDays, currentMonthTotal,
                previousMonthName, previousMonthDays, previousMonthTotal);
    }

    /**
     * Get yearly MOH office registrations with monthly breakdown (current year vs
     * previous year)
     */
    public MOHOfficeYearlyStats getYearlyMOHOfficeRegistrations() {
        int currentYear = LocalDateTime.now().getYear();
        int previousYear = currentYear - 1;

        // Get monthly breakdown for current year
        List<MonthlyData> currentYearMonths = new ArrayList<>();
        long currentYearTotal = 0;

        for (int month = 1; month <= 12; month++) {
            LocalDateTime monthStart = LocalDateTime.of(currentYear, month, 1, 0, 0, 0);
            LocalDateTime monthEnd = monthStart.plusMonths(1).minusSeconds(1);

            long monthCount = mohOfficeRepository.countByCreatedAtBetween(monthStart, monthEnd);
            currentYearMonths.add(new MonthlyData(Month.of(month).name(), monthCount));
            currentYearTotal += monthCount;
        }

        // Get monthly breakdown for previous year
        List<MonthlyData> previousYearMonths = new ArrayList<>();
        long previousYearTotal = 0;

        for (int month = 1; month <= 12; month++) {
            LocalDateTime monthStart = LocalDateTime.of(previousYear, month, 1, 0, 0, 0);
            LocalDateTime monthEnd = monthStart.plusMonths(1).minusSeconds(1);

            long monthCount = mohOfficeRepository.countByCreatedAtBetween(monthStart, monthEnd);
            previousYearMonths.add(new MonthlyData(Month.of(month).name(), monthCount));
            previousYearTotal += monthCount;
        }

        logger.info("Yearly MOH office registrations - Current year ({}): {}, Previous year ({}): {}",
                currentYear, currentYearTotal, previousYear, previousYearTotal);

        return new MOHOfficeYearlyStats(currentYear, currentYearMonths, currentYearTotal,
                previousYear, previousYearMonths, previousYearTotal);
    }

    /**
     * Get monthly MOH office district-wise registrations for current month
     */
    public DistrictWiseTotals getMonthlyMOHOfficeDistrictWiseRegistrations() {
        LocalDateTime now = LocalDateTime.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();

        LocalDateTime monthStart = LocalDateTime.of(currentYear, currentMonth, 1, 0, 0, 0);
        LocalDateTime monthEnd = monthStart.plusMonths(1).minusSeconds(1);

        // Get all MOH offices within the date range to extract distinct districts
        List<MOHOffice> mohOffices = mohOfficeRepository.findDistinctDistrictsByCreatedAtBetween(monthStart, monthEnd);

        // Extract unique districts
        List<String> distinctDistricts = mohOffices.stream()
                .map(MOHOffice::getDistrict)
                .filter(district -> district != null && !district.trim().isEmpty())
                .distinct()
                .sorted()
                .toList();

        // Get count for each district
        List<DistrictData> districtCounts = new ArrayList<>();
        for (String district : distinctDistricts) {
            long count = mohOfficeRepository.countByDistrictAndCreatedAtBetween(district, monthStart, monthEnd);
            if (count > 0) {
                districtCounts.add(new DistrictData(district, count));
            }
        }

        String monthName = now.getMonth().name();

        logger.info("Monthly MOH office district-wise registrations for {} - {} districts found", monthName, districtCounts.size());

        return new DistrictWiseTotals(monthName + " " + currentYear, districtCounts);
    }

    /**
     * Get yearly MOH office district-wise registrations for current year
     */
    public DistrictWiseTotals getYearlyMOHOfficeDistrictWiseRegistrations() {
        int currentYear = LocalDateTime.now().getYear();

        LocalDateTime yearStart = LocalDateTime.of(currentYear, 1, 1, 0, 0, 0);
        LocalDateTime yearEnd = LocalDateTime.of(currentYear, 12, 31, 23, 59, 59);

        // Get all MOH offices within the date range to extract distinct districts
        List<MOHOffice> mohOffices = mohOfficeRepository.findDistinctDistrictsByCreatedAtBetween(yearStart, yearEnd);

        // Extract unique districts
        List<String> distinctDistricts = mohOffices.stream()
                .map(MOHOffice::getDistrict)
                .filter(district -> district != null && !district.trim().isEmpty())
                .distinct()
                .sorted()
                .toList();

        // Get count for each district
        List<DistrictData> districtCounts = new ArrayList<>();
        for (String district : distinctDistricts) {
            long count = mohOfficeRepository.countByDistrictAndCreatedAtBetween(district, yearStart, yearEnd);
            if (count > 0) {
                districtCounts.add(new DistrictData(district, count));
            }
        }

        logger.info("Yearly MOH office district-wise registrations for {} - {} districts found", currentYear, districtCounts.size());

        return new DistrictWiseTotals(String.valueOf(currentYear), districtCounts);
    }

    // Inner classes for response DTOs
    public static class YearlyComparisonStats {
        private int currentYear;
        private List<MonthlyData> currentYearMonths;
        private long currentYearTotal;
        private int previousYear;
        private List<MonthlyData> previousYearMonths;
        private long previousYearTotal;
        private long difference;
        private double percentageChange;

        public YearlyComparisonStats(int currentYear, List<MonthlyData> currentYearMonths, long currentYearTotal,
                int previousYear, List<MonthlyData> previousYearMonths, long previousYearTotal) {
            this.currentYear = currentYear;
            this.currentYearMonths = currentYearMonths;
            this.currentYearTotal = currentYearTotal;
            this.previousYear = previousYear;
            this.previousYearMonths = previousYearMonths;
            this.previousYearTotal = previousYearTotal;
            this.difference = currentYearTotal - previousYearTotal;
            this.percentageChange = previousYearTotal > 0 ? ((double) difference / previousYearTotal) * 100 : 0;
        }

        // Getters
        public int getCurrentYear() {
            return currentYear;
        }

        public List<MonthlyData> getCurrentYearMonths() {
            return currentYearMonths;
        }

        public long getCurrentYearTotal() {
            return currentYearTotal;
        }

        public int getPreviousYear() {
            return previousYear;
        }

        public List<MonthlyData> getPreviousYearMonths() {
            return previousYearMonths;
        }

        public long getPreviousYearTotal() {
            return previousYearTotal;
        }

        public long getDifference() {
            return difference;
        }

        public double getPercentageChange() {
            return percentageChange;
        }
    }

    public static class MonthlyComparisonStats {
        private String currentMonth;
        private List<DailyData> currentMonthDays;
        private long currentMonthTotal;
        private String previousMonth;
        private List<DailyData> previousMonthDays;
        private long previousMonthTotal;
        private long difference;
        private double percentageChange;

        public MonthlyComparisonStats(String currentMonth, List<DailyData> currentMonthDays, long currentMonthTotal,
                String previousMonth, List<DailyData> previousMonthDays, long previousMonthTotal) {
            this.currentMonth = currentMonth;
            this.currentMonthDays = currentMonthDays;
            this.currentMonthTotal = currentMonthTotal;
            this.previousMonth = previousMonth;
            this.previousMonthDays = previousMonthDays;
            this.previousMonthTotal = previousMonthTotal;
            this.difference = currentMonthTotal - previousMonthTotal;
            this.percentageChange = previousMonthTotal > 0 ? ((double) difference / previousMonthTotal) * 100 : 0;
        }

        // Getters
        public String getCurrentMonth() {
            return currentMonth;
        }

        public List<DailyData> getCurrentMonthDays() {
            return currentMonthDays;
        }

        public long getCurrentMonthTotal() {
            return currentMonthTotal;
        }

        public String getPreviousMonth() {
            return previousMonth;
        }

        public List<DailyData> getPreviousMonthDays() {
            return previousMonthDays;
        }

        public long getPreviousMonthTotal() {
            return previousMonthTotal;
        }

        public long getDifference() {
            return difference;
        }

        public double getPercentageChange() {
            return percentageChange;
        }
    }

    public static class DashboardStats {
        private long totalMothersCount;
        private YearlyComparisonStats yearlyComparison;
        private MonthlyComparisonStats monthlyComparison;

        public DashboardStats(long totalMothersCount, YearlyComparisonStats yearlyComparison,
                MonthlyComparisonStats monthlyComparison) {
            this.totalMothersCount = totalMothersCount;
            this.yearlyComparison = yearlyComparison;
            this.monthlyComparison = monthlyComparison;
        }

        // Getters
        public long getTotalMothersCount() {
            return totalMothersCount;
        }

        public YearlyComparisonStats getYearlyComparison() {
            return yearlyComparison;
        }

        public MonthlyComparisonStats getMonthlyComparison() {
            return monthlyComparison;
        }
    }

    // Helper classes for detailed data
    public static class MonthlyData {
        private String month;
        private long count;

        public MonthlyData(String month, long count) {
            this.month = month;
            this.count = count;
        }

        // Getters
        public String getMonth() {
            return month;
        }

        public long getCount() {
            return count;
        }
    }

    public static class DailyData {
        private int day;
        private long count;

        public DailyData(int day, long count) {
            this.day = day;
            this.count = count;
        }

        // Getters
        public int getDay() {
            return day;
        }

        public long getCount() {
            return count;
        }
    }

    // Class for registration status totals
    public static class RegistrationStatusTotals {
        private String period;
        private long completeCount;
        private long normalCount;
        private long acceptedCount;
        private long totalCount;

        public RegistrationStatusTotals(String period, long completeCount, long normalCount, long acceptedCount) {
            this.period = period;
            this.completeCount = completeCount;
            this.normalCount = normalCount;
            this.acceptedCount = acceptedCount;
            this.totalCount = completeCount + normalCount + acceptedCount;
        }

        // Getters
        public String getPeriod() {
            return period;
        }

        public long getCompleteCount() {
            return completeCount;
        }

        public long getNormalCount() {
            return normalCount;
        }

        public long getAcceptedCount() {
            return acceptedCount;
        }

        public long getTotalCount() {
            return totalCount;
        }
    }

    // Class for district-wise totals
    public static class DistrictWiseTotals {
        private String period;
        private List<DistrictData> districts;
        private long totalCount;

        public DistrictWiseTotals(String period, List<DistrictData> districts) {
            this.period = period;
            this.districts = districts;
            this.totalCount = districts.stream().mapToLong(DistrictData::getCount).sum();
        }

        // Getters
        public String getPeriod() {
            return period;
        }

        public List<DistrictData> getDistricts() {
            return districts;
        }

        public long getTotalCount() {
            return totalCount;
        }
    }

    // Class for individual district data
    public static class DistrictData {
        private String district;
        private long count;

        public DistrictData(String district, long count) {
            this.district = district;
            this.count = count;
        }

        // Getters
        public String getDistrict() {
            return district;
        }

        public long getCount() {
            return count;
        }
    }

    // Class for MOH office monthly registration statistics
    public static class MOHOfficeRegistrationStats {
        private String currentMonth;
        private List<DailyData> currentMonthDays;
        private long currentMonthTotal;
        private String previousMonth;
        private List<DailyData> previousMonthDays;
        private long previousMonthTotal;
        private long difference;
        private double percentageChange;

        public MOHOfficeRegistrationStats(String currentMonth, List<DailyData> currentMonthDays, long currentMonthTotal,
                String previousMonth, List<DailyData> previousMonthDays, long previousMonthTotal) {
            this.currentMonth = currentMonth;
            this.currentMonthDays = currentMonthDays;
            this.currentMonthTotal = currentMonthTotal;
            this.previousMonth = previousMonth;
            this.previousMonthDays = previousMonthDays;
            this.previousMonthTotal = previousMonthTotal;
            this.difference = currentMonthTotal - previousMonthTotal;
            this.percentageChange = previousMonthTotal > 0 ? ((double) difference / previousMonthTotal) * 100 : 0;
        }

        // Getters
        public String getCurrentMonth() {
            return currentMonth;
        }

        public List<DailyData> getCurrentMonthDays() {
            return currentMonthDays;
        }

        public long getCurrentMonthTotal() {
            return currentMonthTotal;
        }

        public String getPreviousMonth() {
            return previousMonth;
        }

        public List<DailyData> getPreviousMonthDays() {
            return previousMonthDays;
        }

        public long getPreviousMonthTotal() {
            return previousMonthTotal;
        }

        public long getDifference() {
            return difference;
        }

        public double getPercentageChange() {
            return percentageChange;
        }
    }

    // Class for MOH office yearly registration statistics
    public static class MOHOfficeYearlyStats {
        private int currentYear;
        private List<MonthlyData> currentYearMonths;
        private long currentYearTotal;
        private int previousYear;
        private List<MonthlyData> previousYearMonths;
        private long previousYearTotal;
        private long difference;
        private double percentageChange;

        public MOHOfficeYearlyStats(int currentYear, List<MonthlyData> currentYearMonths, long currentYearTotal,
                int previousYear, List<MonthlyData> previousYearMonths, long previousYearTotal) {
            this.currentYear = currentYear;
            this.currentYearMonths = currentYearMonths;
            this.currentYearTotal = currentYearTotal;
            this.previousYear = previousYear;
            this.previousYearMonths = previousYearMonths;
            this.previousYearTotal = previousYearTotal;
            this.difference = currentYearTotal - previousYearTotal;
            this.percentageChange = previousYearTotal > 0 ? ((double) difference / previousYearTotal) * 100 : 0;
        }

        // Getters
        public int getCurrentYear() {
            return currentYear;
        }

        public List<MonthlyData> getCurrentYearMonths() {
            return currentYearMonths;
        }

        public long getCurrentYearTotal() {
            return currentYearTotal;
        }

        public int getPreviousYear() {
            return previousYear;
        }

        public List<MonthlyData> getPreviousYearMonths() {
            return previousYearMonths;
        }

        public long getPreviousYearTotal() {
            return previousYearTotal;
        }

        public long getDifference() {
            return difference;
        }

        public double getPercentageChange() {
            return percentageChange;
        }
    }
}
