package org.example.tastereview.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 인증 없는 Security 구성 (REQ-NF-005~007, ADR-005).
 * 모든 경로 permitAll, formLogin/httpBasic/logout 해제, CSRF·보안헤더 기본값 유지.
 * 기본 사용자 생성을 막기 위해 메인 클래스에서 UserDetailsServiceAutoConfiguration 을 제외한다.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        http.formLogin(AbstractHttpConfigurer::disable);
        http.httpBasic(AbstractHttpConfigurer::disable);
        http.logout(AbstractHttpConfigurer::disable);
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder(AppProperties appProperties) {
        return new BCryptPasswordEncoder(appProperties.getSecurity().getBcryptStrength());
    }
}
