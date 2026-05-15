package Wattmate.Repository;

import Wattmate.Entity.PowerData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PowerDataRepository extends JpaRepository<PowerData, Integer> {
    // 새 데이터를 넣기 전 기존 데이터를 지우기 위한 메서드
    void deleteByUserId(Integer userId);
}