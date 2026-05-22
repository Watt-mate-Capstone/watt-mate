package Wattmate.Security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                // 1. CORS 설정 활성화
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 2. CSRF 비활성화 (JWT 사용 시 필수)
                .csrf(csrf -> csrf.disable())
                // 3. 세션 사용 안함 (Stateless)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // 4. 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // 브라우저의 사전 요청(OPTIONS)은 무조건 허용 (CORS 해결의 핵심)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // 인증 없이 접근 가능한 경로 설정
                        .requestMatchers("/api/auth/**", "/api/login", "/api/check-duplicate").permitAll()
                        .requestMatchers("/api/users").authenticated()
                        .anyRequest().permitAll()
                )
                // 5. 기타 설정 비활성화
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                // 6. JWT 필터 추가
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 7. CORS 상세 설정 (리액트와의 통신 허용)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 리액트 개발 서버 주소 허용
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",
                "https://watt-mate-capstone.github.io" ,
                "https://watt-mate.vercel.app"
        ));

        // 허용할 HTTP 메서드
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // 모든 헤더 허용
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // 브라우저가 Authorization 헤더를 읽을 수 있도록 노출
        configuration.setExposedHeaders(Arrays.asList("Authorization"));

        // 쿠키 및 인증 헤더 허용
        configuration.setAllowCredentials(true);

        // Preflight 요청 캐싱 시간 (1시간)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}