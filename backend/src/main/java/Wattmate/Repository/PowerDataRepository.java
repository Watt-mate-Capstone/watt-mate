package Wattmate.Repository;

import Wattmate.Entity.PowerData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Repository
public interface PowerDataRepository extends JpaRepository<PowerData, Integer> {

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM power_data WHERE user_id = :userId", nativeQuery = true)
    void deleteByUserId(@Param("userId") Integer userId);

    @Query(value = "SELECT * FROM power_data " +
            "WHERE user_id = :userId AND DATE(recorded_at) = :date " +
            "ORDER BY recorded_at ASC", nativeQuery = true)
    List<PowerData> findHourlyUsage(@Param("userId") Integer userId,
                                    @Param("date") String date);

    // 🌟 [수정본] 일별 집계 쿼리에 실측(real)과 예측(pred) 컬럼을 함께 더하도록 대폭 수정
    @Query(value = "SELECT DATE(recorded_at) AS date, SUM(real_usage_kwh + pred_usage_kwh) AS `usage` " +
            "FROM power_data WHERE user_id = :userId " +
            "AND MONTH(recorded_at) = :month AND YEAR(recorded_at) = :year " +
            "GROUP BY DATE(recorded_at) ORDER BY date", nativeQuery = true)
    List<Map<String, Object>> findDailyUsage(@Param("userId") Integer userId,
                                             @Param("month") Integer month,
                                             @Param("year") Integer year);

    // 🌟 [수정본] 나중을 위해 월별 집계 쿼리도 두 컬럼을 합산하도록 미리 수정해 둡니다.
    @Query(value = "SELECT MONTH(recorded_at) AS month, SUM(real_usage_kwh + pred_usage_kwh) AS `usage` " +
            "FROM power_data WHERE user_id = :userId AND YEAR(recorded_at) = :year " +
            "GROUP BY MONTH(recorded_at) ORDER BY month", nativeQuery = true)
    List<Map<String, Object>> findMonthlyUsage(@Param("userId") Integer userId,
                                               @Param("year") Integer year);
}