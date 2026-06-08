package com.amul.cattlefeed.repository;

import com.amul.cattlefeed.entity.Farmer;
import com.amul.cattlefeed.entity.FeedRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FeedRequestRepository extends JpaRepository<FeedRequest, Long> {

    List<FeedRequest> findByFarmer_FarmerId(Long farmerId);
    boolean existsByFarmer_FarmerIdAndCycle(Long farmerId, String cycle);
    List<FeedRequest> findByStatus(String status);

    List<FeedRequest> findByFarmer_ZoneAndFarmer_District(String zone, String district);

    List<FeedRequest> findByStatusAndFarmer_ZoneAndFarmer_District(
            String status, String zone, String district);

    // Society-scoped queries (preferred for secretary isolation)
    List<FeedRequest> findByStatusAndFarmer_SocietyCode(String status, String societyCode);
    List<FeedRequest> findByFarmer_SocietyCode(String societyCode);

    @Query("""
    SELECT f.farmer.zone,
           SUM(CASE WHEN f.status = 'APPROVED' THEN 1 ELSE 0 END),
           SUM(CASE WHEN f.status = 'REJECTED' THEN 1 ELSE 0 END)
    FROM FeedRequest f
    GROUP BY f.farmer.zone
    """)
    List<Object[]> getZoneWiseStatusCount();

    List<FeedRequest> findByFarmerZone(String zone);

    long countByStatusAndFarmer_ZoneAndFarmer_District(
            String status, String zone, String district);
    long countByStatus(String status);

    @Query("""
        SELECT MONTH(f.requestDate), COUNT(f)
        FROM FeedRequest f
        GROUP BY MONTH(f.requestDate)
        ORDER BY MONTH(f.requestDate)
        """)
    List<Object[]> getMonthlyFeedRequests();

    List<FeedRequest> findByFarmer(Farmer farmer);
}
