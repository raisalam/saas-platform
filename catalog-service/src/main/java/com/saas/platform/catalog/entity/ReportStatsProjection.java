package com.saas.platform.catalog.entity;

public interface ReportStatsProjection {
    // Summary Stats
    Double getTotalGenerated();
    Double getTotalUsed();
    Double getTotalValue();
    Double getTotalDiscount();

    // Trend Data (Grouped)
    java.time.LocalDate getStatDate();    Long getDailyGenerated();
    Long getDailyUsed();
}