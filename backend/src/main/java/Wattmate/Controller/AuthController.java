package Wattmate.Controller;

import Wattmate.DTO.SignupRequest;
import Wattmate.DTO.SignupResponse;
import Wattmate.Service.AuthService;
import org.springframework.web.bind.annotation.*;
import Wattmate.DTO.LoginRequest;
import Wattmate.DTO.LoginResponse;

@RestController
@RequestMapping("/api")
public class    AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public SignupResponse signup(@RequestBody SignupRequest request) {

        authService.signup(request);

        return new SignupResponse("회원가입 성공");
    }
    
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {

        String token = authService.login(request);

        return new LoginResponse(token, "로그인 성공");
    }
}