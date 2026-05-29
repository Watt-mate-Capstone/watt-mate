package Wattmate.Controller;

import Wattmate.Entity.PointLog;
import Wattmate.Repository.PointLogRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/point-logs")
public class PointLogController {

    private final PointLogRepository pointLogRepository;

    public PointLogController(PointLogRepository pointLogRepository) {
        this.pointLogRepository = pointLogRepository;
    }

    @GetMapping
    public ResponseEntity<List<PointLog>> getAllPointLogs() {

        List<PointLog> logs = pointLogRepository.findAll();

        return ResponseEntity.ok(logs);
    }
}