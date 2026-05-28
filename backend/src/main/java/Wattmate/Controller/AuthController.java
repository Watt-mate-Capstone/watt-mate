package Wattmate.Controller;

import Wattmate.DTO.SignupRequest;
import Wattmate.DTO.LoginRequest;
import Wattmate.DTO.LoginResponse;
import Wattmate.Entity.User;                  // 🌟 추가
import Wattmate.Repository.UserRepository;    // 🌟 추가
import Wattmate.Service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository; // 🌟 유저 정보 직접 조회를 위한 인젝션

    // 🌟 생성자에 userRepository 추가
    public AuthController(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        authService.signup(request);
        return ResponseEntity.ok("회원가입 성공");
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        // 1. 기존 로그인 인증 수행 및 토큰 발행
        String token = authService.login(request);

        // 2. 🌟 [핵심] 로그인 요청온 이메일로 DB에서 실제 유저 엔티티를 통째로 끄집어냅니다.
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        // 3. 🌟 프론트엔드가 애타게 찾던 진짜 user_id와 nickname을 Response에 실어서 던집니다!
        return new LoginResponse(token, "로그인 성공", user.getUserId(), user.getNickname());
    }
}