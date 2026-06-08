package com.amul.cattlefeed.repository;

import com.amul.cattlefeed.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long> {

    Optional<Stock> findByCattleFeed_FeedIdAndZoneAndDistrict(
            Long feedId,
            String zone,
            String district
    );
    List<Stock> findByZoneAndDistrict(String zone, String district);

    @Query("SELECT SUM(s.quantity) FROM Stock s")
    Double getTotalStock();
}