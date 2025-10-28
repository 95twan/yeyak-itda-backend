package com.rodemtree.yeyakitda.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rodemtree.yeyakitda.dto.response.ApiResponseDto;
import com.rodemtree.yeyakitda.dto.response.ResponseErrorCode;
import com.rodemtree.yeyakitda.exception.InvalidTokenException;
import com.rodemtree.yeyakitda.service.AuthService;
import com.rodemtree.yeyakitda.common.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTHORIZATION_HEADER = "Authorization";

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final AuthService authService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = authorizationHeader.substring(BEARER_PREFIX.length());

        try {
            if (authService.isBlacklist(accessToken)) {
                throw new InvalidTokenException();
            }

            if (jwtUtil.isTokenValid(accessToken) && "access".equals(jwtUtil.getType(accessToken))) {
                String email = jwtUtil.getEmail(accessToken);
                String role = jwtUtil.getRole(accessToken);

                CustomUserDetails userDetails = new CustomUserDetails(email, null, List.of(new SimpleGrantedAuthority(role)));

                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        } catch (Exception e) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");

            ApiResponseDto<?> responseDto = ApiResponseDto.of(ResponseErrorCode.INVALID_TOKEN);
            response.getWriter().write(objectMapper.writeValueAsString(responseDto));
            return;
        }

        filterChain.doFilter(request, response);

    }
}
