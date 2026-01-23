package until.the.eternity.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import until.the.eternity.common.filter.GatewayAuthFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class SecurityConfig {

    private final GatewayAuthFilter gatewayAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(
                        authorize ->
                                authorize
                                        // Actuator 및 헬스체크 엔드포인트는 공개
                                        .requestMatchers("/actuator/**", "/health")
                                        .permitAll()
                                        // Swagger 문서는 공개
                                        .requestMatchers(
                                                "/swagger-ui/**", "/v3/api-docs/**", "/docs/**")
                                        .permitAll()
                                        // API 엔드포인트는 공개
                                        // TODO: API endpoint 정리 후 matcher 수정
                                        // TODO: 권한 관련 기능 개발 완료 후 hasRole 추가
                                        .requestMatchers(
                                                "/api/**",
                                                "/auction-history/**",
                                                "/statistics/**",
                                                "/horn-bugle/**")
                                        .permitAll()
                                        // 나머지 요청은 인증 필요
                                        .anyRequest()
                                        .authenticated())
                .addFilterBefore(gatewayAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
