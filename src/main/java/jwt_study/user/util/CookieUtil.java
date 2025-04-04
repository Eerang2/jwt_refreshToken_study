package jwt_study.user.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CookieUtil {

    private final JwtProperties jwtProperties;

    // ✅ **쿠키에 리프레시 토큰 저장**
    public void addRefreshTokenToCookie(String refreshToken, HttpServletResponse response) {
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true); // XSS 공격 방지
        cookie.setSecure(true);   // HTTPS에서만 전송
        cookie.setPath("/");      // 모든 경로에서 사용 가능
        cookie.setMaxAge((int) (jwtProperties.getRefreshTokenExpireTime() / 1000)); // 만료 시간 설정

        response.addCookie(cookie);
    }

    public String extractRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;

        for (Cookie cookie : cookies) {
            if ("refreshToken".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }

}
