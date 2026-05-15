package Wattmate.Controller;

import Wattmate.Entity.PowerData;
import Wattmate.Repository.PowerDataRepository;
import Wattmate.Service.PowerDataService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;

@RestController
@RequestMapping("/api/power")
@CrossOrigin(origins = "*") // 테스트용 전체 허용
public class PowerDataController {

    private final PowerDataRepository powerDataRepository;
    private final PowerDataService powerDataService;

    public PowerDataController(PowerDataRepository powerDataRepository, PowerDataService powerDataService) {
        this.powerDataRepository = powerDataRepository;
        this.powerDataService = powerDataService;
    }

    // 파일 업로드 및 분석 (기존)
    @PostMapping("/upload")
    public Map<String, Object> upload(@RequestParam("userId") Integer userId, @RequestParam("file") MultipartFile file) throws Exception {
        return powerDataService.analyzeAndSave(userId, file);
    }

    // 시간별 조회
    @GetMapping("/hourly")
    public List<PowerData> getHourly(@RequestParam Integer userId, @RequestParam String date) {
        return powerDataRepository.findHourlyUsage(userId, date);
    }

    // 일별 조회
    @GetMapping("/daily")
    public List<Map<String, Object>> getDaily(@RequestParam Integer userId, @RequestParam Integer month, @RequestParam Integer year) {
        return powerDataRepository.findDailyUsage(userId, month, year);
    }

    // 월별 조회
    @GetMapping("/monthly")
    public List<Map<String, Object>> getMonthly(@RequestParam Integer userId, @RequestParam Integer year) {
        return powerDataRepository.findMonthlyUsage(userId, year);
    }
}