package com.goatfarm.security;

import com.goatfarm.exception.InvalidTokenException;
import com.goatfarm.model.AuthUser;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Authenticates requests carrying {@code Authorization: Bearer <jwt>}.
 *
 * <p>A bad token never blows up the filter: the request simply stays unauthenticated and the
 * reason is stored for {@link JwtAuthenticationEntryPoint}. Public endpoints therefore keep working
 * even if the client sends a stale token, and protected ones answer 401.
 */
@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            authenticate(request, header.substring(BEARER_PREFIX.length()));
        }
        filterChain.doFilter(request, response);
    }

    private void authenticate(HttpServletRequest request, String token) {
        try {
            Claims claims = jwtUtil.validateToken(token);
            AuthUser user = new AuthUser(longClaim(claims, "userId"), longClaim(claims, "farmId"));
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(user, null, List.of()));
        } catch (InvalidTokenException ex) {
            SecurityContextHolder.clearContext();
            request.setAttribute(JwtAuthenticationEntryPoint.AUTH_ERROR_ATTRIBUTE, ex.getMessage());
        }
    }

    private static Long longClaim(Claims claims, String name) {
        Object value = claims.get(name);
        return value instanceof Number number ? number.longValue() : null;
    }
}
