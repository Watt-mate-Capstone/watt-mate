package Wattmate.DTO;

public class LoginResponse {
    private String token;
    private String message;
    private Integer userId;   // 🌟 DB 진짜 유저 ID 필드 추가
    private String nickname;  // 🌟 유저 닉네임 필드 추가

    // 기존 생성자가 있다면 유지하거나 아래처럼 확장해 주세요.
    public LoginResponse(String token, String message, Integer userId, String nickname) {
        this.token = token;
        this.message = message;
        this.userId = userId;
        this.nickname = nickname;
    }

    // Getter 메서드들...
    public String getToken() { return token; }
    public String getMessage() { return message; }
    public Integer getUserId() { return userId; }
    public String getNickname() { return nickname; }
}