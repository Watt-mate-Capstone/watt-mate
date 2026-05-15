package Wattmate.Controller;

import Wattmate.Service.PowerDataService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@RestController
@RequestMapping("/api/power")
public class PowerDataController {

    private final PowerDataService powerDataService;

    public PowerDataController(PowerDataService powerDataService) {
        this.powerDataService = powerDataService;
    }

    @PostMapping("/upload")
    public Map<String, Object> upload(@RequestParam("userId") Integer userId,
                                      @RequestParam("file") MultipartFile file) {
        try {
            System.out.println("==== [Controller] 파일 업로드 요청 받음 (UserId: " + userId + ") ====");
            return powerDataService.analyzeAndSave(userId, file);
        } catch (Exception e) {
            System.err.println("[Controller] 에러 발생: " + e.getMessage());
            return Map.of("status", "error", "message", e.getMessage());
        }
    }
}