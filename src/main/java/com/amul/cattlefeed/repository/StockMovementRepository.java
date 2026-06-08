package com.amul.cattlefeed.repository;

import com.amul.cattlefeed.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {


    @Query("""
        SELECT sm.stock.cattleFeed.feedId, SUM(sm.quantity)
        FROM StockMovement sm
        WHERE sm.movementType = 'OUT'
        AND MONTH(sm.movementDate) = MONTH(CURRENT_DATE)
        GROUP BY sm.stock.cattleFeed.feedId
        """)
    List<Object[]> getMonthlyFeedConsumption();


    @Query("""
    SELECT COUNT(sm) > 0
    FROM StockMovement sm
    WHERE sm.stock.zone = :zone
    AND sm.stock.district = :district
    AND sm.stock.cattleFeed.feedId = :feedId
    AND sm.cycle = :cycle
    AND sm.movementType = 'IN'
    """)
    boolean existsNormalStockForCycleAndZoneAndDistrict(
            @Param("zone") String zone,
            @Param("district") String district,
            @Param("cycle") String cycle,
            @Param("feedId") Long feedId   // ✅ 4th param added
    );
}