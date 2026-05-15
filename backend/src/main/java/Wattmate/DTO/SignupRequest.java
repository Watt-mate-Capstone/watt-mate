package Wattmate.DTO;

public class SignupRequest {
    private String username;    // 이메일
    private String password;
    private String nickname;    // 사용자 이름
    private String houseType;   // 가구 유형
    private String region;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getHouseType() { return houseType; }
    public void setHouseType(String houseType) { this.houseType = houseType; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
}