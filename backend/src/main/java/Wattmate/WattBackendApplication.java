package Wattmate; // 기존 패키지 선언 그대로 유지

import jakarta.annotation.PostConstruct; // 🌟 이 줄 추가
import java.util.TimeZone;              // 🌟 이 줄 추가
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WattBackendApplication {

	// 🌟 여기에서 JVM 타임존을 한국 시간축으로 변경하는 어노테이션과 메서드를 추가합니다!
	@PostConstruct
	public void setTimeZone() {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
	}

	public static void main(String[] args) {
		SpringApplication.run(WattBackendApplication.class, args);
	}
}