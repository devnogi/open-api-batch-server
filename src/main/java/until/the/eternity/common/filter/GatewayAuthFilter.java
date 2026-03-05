package until.the.eternity.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import until.the.eternity.common.entity.CustomWebAuthenticationDetails;
import until.the.eternity.common.enums.UserRole;
import until.the.eternity.common.util.IpAddressUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Gateway에서 전달한 인증 헤더(X-Auth-*)를 기반으로 Spring Security의 Authentication을 생성하는 필터
 *
 * <p>Gateway에서 전달하는 헤더: - X-Auth-User-Id: 사용자 ID (Long) - X-Auth-Username: 사용자 이메일/username
 * (String) - X-Auth-Roles: 사용자 역할 (예: user, admin, super_admin)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GatewayAuthFilter extends OncePerRequestFilter {

    private final IpAddressUtil ipAddressUtil;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String userIdHeader = request.getHeader("X-Auth-User-Id");
        String usernameHeader = request.getHeader("X-Auth-Username");
        String rolesHeader = request.getHeader("X-Auth-Roles");

        UsernamePasswordAuthenticationToken authentication =
                getAuthentication(userIdHeader, rolesHeader);

        String clientIp = ipAddressUtil.getClientIp(request);

        CustomWebAuthenticationDetails webAuthenticationDetails =
                new CustomWebAuthenticationDetails(request, clientIp);

        authentication.setDetails(webAuthenticationDetails);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        log.debug(
                "Authentication set - Principal: {}, Username: {}, Authorities: {}",
                authentication.getPrincipal(),
                usernameHeader,
                authentication.getAuthorities());

        filterChain.doFilter(request, response);
    }

    private UsernamePasswordAuthenticationToken getAuthentication(
            String userIdHeader, String rolesHeader) {

        Long userId = null;
        try {
            if (userIdHeader != null) {
                userId = Long.parseLong(userIdHeader);
            }
        } catch (NumberFormatException e) {
            log.warn("Invalid user ID header: {}", userIdHeader);
        }

        if (rolesHeader == null || rolesHeader.isEmpty()) {
            log.debug("No roles found, creating anonymous authentication");
            return new UsernamePasswordAuthenticationToken(userId, null);
        }

        String roleCode = rolesHeader.trim().toLowerCase();
        UserRole userRole = UserRole.fromCode(roleCode).orElse(null);

        if (userRole == null) {
            log.warn("Unknown role code: '{}', creating anonymous authentication", roleCode);
            return new UsernamePasswordAuthenticationToken(userId, null);
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + userRole.name()));

        log.debug("Created authentication for userId: {}, role: {}", userId, userRole.name());

        return new UsernamePasswordAuthenticationToken(userId, null, authorities);
    }
}
