package Wattmate.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "PowerData")
public class PowerData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer dataId;

    @Column(nullable = false)
    private Integer userId;

    @Column(nullable = false)
    private LocalDateTime recordedAt;

    private Float realUsageKwh; // 과거 실측 사용량
    private Float predUsageKwh; // AI가 예측한 사용량

    public PowerData() {}

    // Getters and Setters
    public Integer getDataId() { return dataId; }
    public void setDataId(Integer dataId) { this.dataId = dataId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }

    public Float getRealUsageKwh() { return realUsageKwh; }
    public void setRealUsageKwh(Float realUsageKwh) { this.realUsageKwh = realUsageKwh; }

    public Float getPredUsageKwh() { return predUsageKwh; }
    public void setPredUsageKwh(Float predUsageKwh) { this.predUsageKwh = predUsageKwh; }
}