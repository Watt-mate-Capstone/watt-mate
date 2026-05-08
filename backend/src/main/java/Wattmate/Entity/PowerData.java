package Wattmate.Entity;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "power_data")
public class PowerData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer dataId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private static User user;

    @Column(name = "real_usage_kwh", nullable = false)
    private Float realUsageKwh;

    @Column(name = "pred_usage_kwh")
    private Float predUsageKwh;

    @Column(name = "recorded_at", nullable = false)
    private Timestamp recordedAt;

    public PowerData() {}

    public Integer getDataId() { return dataId; }
    public void setDataId(Integer dataId) { this.dataId = dataId; }

    public static User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Float getRealUsageKwh() { return realUsageKwh; }
    public void setRealUsageKwh(Float realUsageKwh) { this.realUsageKwh = realUsageKwh; }

    public Float getPredUsageKwh() { return predUsageKwh; }
    public void setPredUsageKwh(Float predUsageKwh) { this.predUsageKwh = predUsageKwh; }

    public Timestamp getRecordedAt() { return recordedAt; }
    public void setRecordedAt(Timestamp recordedAt) { this.recordedAt = recordedAt; }
}