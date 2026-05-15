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
    @Query("DELETE FROM PowerData p WHERE p.userId = :userId")
    void deleteByUserId(@Param("userId") Integer userId);

    // [시간별] 특정 날짜 데이터
    @Query(value = "SELECT * FROM PowerData WHERE user_id = :userId AND DATE(recorded_at) = :date ORDER BY recorded_at ASC", nativeQuery = true)
    List<PowerData> findHourlyUsage(@Param("userId") Integer userId, @Param("date") String date);

    // [일별] 특정 월의 날짜별 합계
    @Query(value = "SELECT DATE(recorded_at) as date, SUM(real_usage_kwh) as usage " +
            "FROM PowerData WHERE user_id = :userId AND MONTH(recorded_at) = :month AND YEAR(recorded_at) = :year " +
            "GROUP BY DATE(recorded_at) ORDER BY date", nativeQuery = true)
    List<Map<String, Object>> findDailyUsage(@Param("userId") Integer userId, @Param("month") Integer month, @Param("year") Integer year);

    // [월별] 특정 연도의 월별 합계
    @Query(value = "SELECT MONTH(recorded_at) as month, SUM(real_usage_kwh) as usage " +
            "FROM PowerData WHERE user_id = :userId AND YEAR(recorded_at) = :year " +
            "GROUP BY MONTH(recorded_at) ORDER BY month", nativeQuery = true)
    List<Map<String, Object>> findMonthlyUsage(@Param("userId") Integer userId, @Param("year") Integer year);
}