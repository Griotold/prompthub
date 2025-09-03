package com.griotold.prompthub.adapter.security.config;

import com.griotold.prompthub.adapter.security.jwt.JwtAuthenticationFilter;
import com.griotold.prompthub.adapter.security.CustomAccessDeniedHandler;
import com.griotold.prompthub.adapter.security.CustomAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .formLogin(auth -> auth.disable())
                .httpBasic(auth -> auth.disable())
                .authorizeHttpRequests(authorize -> authorize
                        // 기본 경로
                        .requestMatchers("/").permitAll()
                        // Swagger 관련 경로
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**").permitAll()
                        // 인증이 필요없는 소셜 로그인 API
                        .requestMatchers("/api/v1/auth/google/login").permitAll()
                        .requestMatchers("/api/v1/auth/kakao/login").permitAll()
                        .requestMatchers("/api/v1/auth/naver/login").permitAll()
                        // 콜백 경로들
                        .requestMatchers("/auth/google/callback").permitAll()
                        .requestMatchers("/auth/kakao/callback").permitAll()
                        .requestMatchers("/auth/naver/callback").permitAll()
                        // 토큰 관련 API
                        .requestMatchers("/api/v1/auth/refresh").permitAll()
                        // 공개 API
                        .requestMatchers("/api/v1/public/**").permitAll()
                        // /api/v1/admin 으로 시작하는 엔드포인트는 ADMIN 권한 필요
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        // 나머지는 인증(USER 권한) 필요
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOriginPattern("*"); // 모든 Origin 허용
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS")); // 모든 메서드 허용
        configuration.setAllowedHeaders(Arrays.asList("*")); // 모든 헤더 허용
        configuration.setAllowCredentials(true); // 인증 정보 포함 허용

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}