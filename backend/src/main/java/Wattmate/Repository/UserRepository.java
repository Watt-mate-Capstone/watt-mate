package Wattmate.Repository;

import Wattmate.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    boolean existsByNickname(String nickname);

    boolean existsByEmail(String email);

    Optional<User> findByNickname(String nickname);

    Optional<User> findByEmail(String email);
}