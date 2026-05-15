package Wattmate.DTO;

public class SignupRequest {
    private String email;           // 기존 username 대신 email로 명칭 통일
    private String password;
    private String nickname;
    private String kepcoCustNo;     // 한전 고객번호 (필수)
    private Integer householdCount; // 가구원 수 (숫자 타입)

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
}