package Wattmate.Controller;

import Wattmate.DTO.PowerAnalysisResponse;
import Wattmate.Service.PowerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/power")
@RequiredArgsConstructor
public class PowerController {

    private final PowerService powerService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadPowerData(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("파일이 비어있습니다.");
        }

        // 서비스 호출
        PowerAnalysisResponse result = powerService.analyzeAndSave(file);
        return ResponseEntity.ok(result);
    }
}