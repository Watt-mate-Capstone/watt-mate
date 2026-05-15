package Wattmate.Service;

import Wattmate.DTO.LoginRequest;
import Wattmate.DTO.SignupRequest;
import Wattmate.Entity.TitleMaster;
import Wattmate.Entity.User;
import Wattmate.Repository.TitleMasterRepository;
import Wattmate.Repository.UserRepository;
import Wattmate.Security.JwtTokenProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final TitleMasterRepository titleMasterRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserRepository userRepository,
                       TitleMasterRepository titleMasterRepository,
                       JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.titleMasterRepository = titleMasterRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public void signup(SignupRequest request) {
        System.out.println("EMAIL = " + request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("이미 존재하는 이메일입니다.");
        }

        if (userRepository.existsByNickname(request.getNickname())) {
            throw new RuntimeException("이미 존재하는 닉네임입니다.");
        }

        TitleMaster defaultTitle = titleMasterRepository.findById(1)
                .orElseGet(() -> {
                    TitleMaster newTitle = new TitleMaster();
                    newTitle.setTitleName("에너지 새싹");
                    newTitle.setMinRank(0);
                    newTitle.setMaxRank(999999);
                    newTitle.setColorCode("#00C853");
                    return titleMasterRepository.save(newTitle);
                });

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setNickname(request.getNickname());
        user.setKepcoCustNo(request.getKepcoCustNo());
        user.setHouseholdCount(request.getHouseholdCount());

        user.setEnergyTemp(36.5f);
        user.setCurrentPoint(0);
        user.setTotalPoint(0);
        user.setTitleId(defaultTitle.getTitleId());

        userRepository.save(user);
    }

    public String login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 이메일입니다."));

        if (!user.getPassword().equals(request.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        return jwtTokenProvider.createToken(user.getEmail());
    }
}