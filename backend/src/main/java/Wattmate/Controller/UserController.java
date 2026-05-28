package Wattmate.Controller;

import Wattmate.Entity.User;
import Wattmate.Repository.UserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users") // 1. 기본 베이스 주소를 /api/users 로 설정
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping //
    public List<User> getUsers() {
        return userRepository.findAll();
    }
}