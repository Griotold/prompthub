package com.griotold.prompthub.adapter.security.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            String jwt = resolveToken(request);
            if (jwt != null) {
                authenticateJwt(jwt, request);
            }
        } catch (ExpiredJwtException e) {
            handleTokenException(request, "EXPIRED_TOKEN", e);
        } catch (MalformedJwtException | SignatureException e) {
            handleTokenException(request, "INVALID_TOKEN", e);
        } catch (Exception e) {
            handleTokenException(request, "UNKNOWN_ERROR", e);
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            request.setAttribute("exception", "NOT_TOKEN");
            return null;
        }
        return authHeader.substring(7);
    }

    private void authenticateJwt(String jwt, HttpServletRequest request) {
        String username = jwtTokenProvider.getUsername(jwt);
        if (username == null || SecurityContextHolder.getContext().getAuthentication() != null) {
            return;
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (!jwtTokenProvider.isExpired(jwt)) {
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);

            log.debug("JWT 인증 성공 - 사용자: {}", username);
        }
    }

    private void handleTokenException(HttpServletRequest request, String exceptionType, Exception e) {
        request.setAttribute("exception", exceptionType);
        log.warn("JWT 인증 예외 발생 [{}]: {}", exceptionType, e.getMessage());
    }
}