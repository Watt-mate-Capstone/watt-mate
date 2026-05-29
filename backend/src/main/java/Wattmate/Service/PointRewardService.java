package Wattmate.Service;

import Wattmate.Entity.LogType;
import Wattmate.Entity.User;
import Wattmate.Entity.PointLog; // enum('SAVE','SPEND') 구조 맞춤
import Wattmate.Repository.UserRepository;
import Wattmate.Repository.PointLogRepository;
import lombok.Getter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PointRewardService {

    private final UserRepository userRepository;
    private final PointLogRepository pointLogRepository;
    @Getter
    private final PowerService powerService; // 전력량 가져오는 서비스 (기존 서비스 주입)

    public PointRewardService(UserRepository userRepository, PointLogRepository pointLogRepository, PowerService powerService) {
        this.userRepository = userRepository;
        this.pointLogRepository = pointLogRepository;
        this.powerService = powerService;
    }

    // 🌟 매달 1일 새벽 0시 0분에 자동으로 실행되는 정산 시스템 (시연 시 수동 호출 API를 따로 뚫어도 좋습니다)
    @Scheduled(cron = "0 0 0 1 * *")
    @Transactional
    public void calculateMonthlyRewards() {
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) return;

        // 1. 유저별 절약 수치 연산 및 정렬 로직 (프론트에서 하던 정렬을 백엔드가 수행)
        // (중략: 각 유저의 전월 대비 saving 계산 후 내림차순 리스트업)

        int totalUsers = users.size();

        for (int i = 0; i < totalUsers; i++) {
            User user = users.get(i);
            int rank = i + 1;
            double percentile = ((double) rank / totalUsers) * 100; // 상위 몇 %인지 계산

            int rewardPoint = 0;
            String rankTier = "";


            if (percentile <= 5.0) { rewardPoint = 3000; rankTier = "상위 1~5%"; }
            else if (percentile <= 15.0) { rewardPoint = 2000; rankTier = "상위 6~15%"; }
            else if (percentile <= 30.0) { rewardPoint = 1000; rankTier = "상위 16~30%"; }
            else if (percentile <= 50.0) { rewardPoint = 500; rankTier = "상위 31~50%"; }

            if (rewardPoint > 0) {
                // 2. user 테이블의 진짜 총 포인트(current_point)에 더하기
                user.setCurrentPoint(user.getCurrentPoint() + rewardPoint);
                userRepository.save(user);

                // 3. point_log 테이블에 영수증(INSERT) 강제 적재
                PointLog log = new PointLog();
                log.setUser(user);
                log.setAmount(rewardPoint);
                log.setLogType(LogType.valueOf("SAVE")); // 적립
                log.setDescription("월간 절약 리그 리워드 지급 (" + rankTier + " - " + rank + "위)");
                log.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
                pointLogRepository.save(log);
            }
        }
    }

}