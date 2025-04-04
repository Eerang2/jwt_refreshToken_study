package jwt_study.user.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jwt_study.user.exceptions.ValidAccessTokenException;
import jwt_study.user.service.UserService;
import jwt_study.user.service.dto.TokenRes;
import jwt_study.user.util.CookieUtil;
import jwt_study.user.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    private final UserService authService;
    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;

    public JwtAuthInterceptor(JwtUtil jwtUtil, UserService authService, CookieUtil cookieUtil) {
        this.jwtUtil = jwtUtil;
        this.authService = authService;
        this.cookieUtil = cookieUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        } else {
            return true;
        }

        if (jwtUtil.isTokenExpired(token)) {
            // ✅ CookieUtil을 통해 리프레시 토큰 추출
            String refreshToken = cookieUtil.extractRefreshToken(request);

            if (refreshToken != null) {
                String newAccessToken = authService.reissueAccessToken(refreshToken).getAccessToken();
                response.setHeader("Authorization", "Bearer " + newAccessToken);
            }
        }

        return true;
    }
}
