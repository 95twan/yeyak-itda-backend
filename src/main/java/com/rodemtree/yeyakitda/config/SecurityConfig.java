package com.rodemtree.yeyakitda.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rodemtree.yeyakitda.dto.response.ApiResponseDto;
import com.rodemtree.yeyakitda.dto.response.ResponseErrorCode;
import com.rodemtree.yeyakitda.security.JwtAuthenticationFilter;
import com.rodemtree.yeyakitda.security.LoginAuthenticationFilter;
import com.rodemtree.yeyakitda.service.AuthService;
import com.rodemtree.yeyakitda.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@RequiredArgsConstructor
@Configuration
public class SecurityConfig {

    private final ObjectMapper objectMapper;
    private final JwtUtil jwtUtil;
    private final AuthService authService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/reissue", "/api/auth/register").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/restaurants", "/api/restaurants/**", "/api/events/**", "/api/restaurant-groups").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(customAuthenticationEntryPoint()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter(jwtUtil, objectMapper), LoginAuthenticationFilter.class)
                .addFilterAt(loginAuthenticationFilter(authenticationManager), UsernamePasswordAuthenticationFilter.class)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 요청을 허용할 출처(프론트엔드 주소)를 명시합니다.
        // Synology NAS의 외부 접속 IP와 로컬 개발용 주소를 모두 추가해주는 것이 좋습니다.
        configuration.setAllowedOrigins(List.of("http://yeyak-itda.rodemtree.synology.me", "http://116.123.110.162:80", "http://192.168.1.18:8888"));

        // 허용할 HTTP 메서드를 지정합니다. (GET, POST, PUT, DELETE 등)
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // 요청 헤더에 모든 종류를 허용합니다.
        configuration.setAllowedHeaders(List.of("*"));

        // 자격 증명(쿠키, 인증 헤더 등)을 포함한 요청을 허용합니다.
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 모든 경로(/api/**)에 대해 위에서 정의한 CORS 설정을 적용합니다.
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationEntryPoint customAuthenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");

            ApiResponseDto<?> responseDto = ApiResponseDto.of(ResponseErrorCode.UNAUTHORIZED);

            response.getWriter().write(objectMapper.writeValueAsString(responseDto));
        };
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public LoginAuthenticationFilter loginAuthenticationFilter(AuthenticationManager authenticationManager) throws Exception {
        LoginAuthenticationFilter filter = new LoginAuthenticationFilter(jwtUtil, objectMapper, authService);
        filter.setAuthenticationManager(authenticationManager);
        return filter;
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtUtil jwtUtil, ObjectMapper objectMapper) {
        return new JwtAuthenticationFilter(jwtUtil, objectMapper);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
