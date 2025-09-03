package com.griotold.prompthub.adapter.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.griotold.prompthub.adapter.webapi.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        String exception = (String) request.getAttribute("exception");

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ErrorResponse errorResponse;

        if ("NOT_TOKEN".equals(exception)) {
            errorResponse = new ErrorResponse("액세스 토큰이 없습니다.");
        } else if ("NOT_VALID_TOKEN".equals(exception)) {
            errorResponse = new ErrorResponse("유효하지 않은 액세스 토큰입니다.");
        } else {
            errorResponse = new ErrorResponse("인증이 필요합니다.");
        }

        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}