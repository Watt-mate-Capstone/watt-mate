package Wattmate.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "`user`") // DB의 User 테이블과 매핑
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;

    @Column(nullable = false, unique = true, length = 50)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 30)
    private String nickname;

    @Column(name = "kepco_cust_no", nullable = false, length = 20)
    private String kepcoCustNo;

    @Column(name = "household_count", nullable = false)
    private Integer householdCount;

    @Column(name = "energy_temp")
    private Float energyTemp = 36.5f; // 기본값

    @Column(name = "current_point")
    private Integer currentPoint = 0;

    @Column(name = "total_point")
    private Integer totalPoint = 0;

    @Column(name = "title_id")
    private Integer titleId;

    // 기본 생성자
    public User() {}

    // Getters and Setters
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getKepcoCustNo() { return kepcoCustNo; }
    public void setKepcoCustNo(String kepcoCustNo) { this.kepcoCustNo = kepcoCustNo; }

    public Integer getHouseholdCount() { return householdCount; }
    public void setHouseholdCount(Integer householdCount) { this.householdCount = householdCount; }

    public Float getEnergyTemp() { return energyTemp; }
    public void setEnergyTemp(Float energyTemp) { this.energyTemp = energyTemp; }

    public Integer getCurrentPoint() { return currentPoint; }
    public void setCurrentPoint(Integer currentPoint) { this.currentPoint = currentPoint; }

    public Integer getTotalPoint() { return totalPoint; }
    public void setTotalPoint(Integer totalPoint) { this.totalPoint = totalPoint; }

    public Integer getTitleId() { return titleId; }
    public void setTitleId(Integer titleId) { this.titleId = titleId; }
}