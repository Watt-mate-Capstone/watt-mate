package Wattmate.Controller;

import Wattmate.Entity.PowerData;
import Wattmate.Repository.PowerDataRepository;
import Wattmate.Service.PowerDataService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;

@RestController
@RequestMapping("/api/power")
@CrossOrigin(origins = "*")
public class PowerDataController {

    private final PowerDataRepository powerDataRepository;
    private final PowerDataService powerDataService;

    public PowerDataController(PowerDataRepository powerDataRepository, PowerDataService powerDataService) {
        this.powerDataRepository = powerDataRepository;
        this.powerDataService = powerDataService;
    }

    @PostMapping("/upload")
    public Map<String, Object> upload(@RequestParam("userId") Integer userId, @RequestParam("file") MultipartFile file) throws Exception {
        return powerDataService.analyzeAndSave(userId, file);
    }

    @GetMapping("/hourly")
    public List<PowerData> getHourly(@RequestParam Integer userId, @RequestParam String date) {
        // Repository의 findHourlyUsage 호출
        return powerDataRepository.findHourlyUsage(userId, date);
    }

    @GetMapping("/daily")
    public List<Map<String, Object>> getDaily(@RequestParam Integer userId, @RequestParam Integer month, @RequestParam Integer year) {
        // Repository의 findDailyUsage 호출
        return powerDataRepository.findDailyUsage(userId, month, year);
    }

    @GetMapping("/monthly")
    public List<Map<String, Object>> getMonthly(@RequestParam Integer userId, @RequestParam Integer year) {
        // Repository의 findMonthlyUsage 호출 (이제 에러 안 남!)
        return powerDataRepository.findMonthlyUsage(userId, year);
    }
}