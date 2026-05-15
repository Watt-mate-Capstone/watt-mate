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

    /**
     * 1. 데이터 초기화 (삭제)
     * 새 파일을 올릴 때 기존 데이터를 한 줄씩 지우지 않고 한 번에(Bulk) 삭제합니다.
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM PowerData p WHERE p.userId = :userId")
    void deleteByUserId(@Param("userId") Integer userId);

    /**
     * 2. 시간대별 데이터 조회 (HourlyStats용)
     * 특정 유저가 선택한 '날짜'의 00시~23시 데이터를 가져옵니다.
     */
    @Query(value = "SELECT * FROM power_data " +
            "WHERE user_id = :userId AND DATE(recorded_at) = :date " +
            "ORDER BY recorded_at ASC", nativeQuery = true)
    List<PowerData> findHourlyUsage(@Param("userId") Integer userId, @Param("date") String date);

    /**
     * 3. 일별 사용량 합계 조회 (DailyStats용)
     * 특정 월의 데이터를 날짜별(1일~31일)로 합산하여 가져옵니다.
     */
    @Query(value = "SELECT DATE(p.recorded_at) as date, SUM(p.real_usage_kwh) as usage " +
            "FROM power_data p " +
            "WHERE p.user_id = :userId AND MONTH(p.recorded_at) = :month " +
            "GROUP BY DATE(p.recorded_at) " +
            "ORDER BY date", nativeQuery = true)
    List<Map<String, Object>> findDailyUsage(@Param("userId") Integer userId, @Param("month") Integer month);

    /**
     * 4. 월별 사용량 합계 조회 (MonthlyStats용)
     * 특정 연도의 데이터를 월별(1월~12월)로 합산하여 가져옵니다.
     */
    @Query(value = "SELECT MONTH(p.recorded_at) as month, SUM(p.real_usage_kwh) as usage " +
            "FROM power_data p " +
            "WHERE p.user_id = :userId AND YEAR(p.recorded_at) = :year " +
            "GROUP BY MONTH(p.recorded_at) " +
            "ORDER BY month", nativeQuery = true)
    List<Map<String, Object>> findMonthlyUsage(@Param("userId") Integer userId, @Param("year") Integer year);

    /**
     * 5. 예측 데이터 조회 (차트 출력용)
     * AI가 생성한 미래 예측 데이터(predUsageKwh > 0)만 가져옵니다.
     */
    @Query("SELECT p FROM PowerData p WHERE p.userId = :userId AND p.predUsageKwh > 0 ORDER BY p.recordedAt ASC")
    List<PowerData> findFuturePredictions(@Param("userId") Integer userId);
}