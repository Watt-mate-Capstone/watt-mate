package Wattmate.Service;

import Wattmate.DTO.LoginRequest;
import Wattmate.DTO.SignupRequest;
import Wattmate.Entity.HouseholdType;
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
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new RuntimeException("이미 존재하는 닉네임입니다.");
        }

        TitleMaster defaultTitle = titleMasterRepository.findById(1)
                .orElseGet(() -> {
                    TitleMaster newTitle = new TitleMaster();
                    newTitle.setTitleName("에너지 새싹");
                    return titleMasterRepository.save(newTitle);
                });

        User user = new User();
        user.setEmail(request.getUsername());
        user.setPassword(request.getPassword());
        user.setNickname(request.getNickname());
        user.setKepcoCustNo("TEMP_" + System.currentTimeMillis());

        if (request.getHouseType() != null && request.getHouseType().contains("1인")) {
            user.setHouseholdType(HouseholdType.LIGHT);
        } else if (request.getHouseType() != null && request.getHouseType().contains("2인")) {
            user.setHouseholdType(HouseholdType.MIDDLE);
        } else {
            user.setHouseholdType(HouseholdType.HEAVY);
        }

        user.setEnergyTemp(36.5f);
        user.setCurrentPoint(0);
        user.setTotalPoint(0);
        user.setTitle(defaultTitle);

        userRepository.save(user);
    }

    public String login(LoginRequest request) {
        User user = userRepository.findByNickname(request.getUsername())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 닉네임입니다."));

        if (!user.getPassword().equals(request.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        return jwtTokenProvider.createToken(user.getNickname());
    }
}