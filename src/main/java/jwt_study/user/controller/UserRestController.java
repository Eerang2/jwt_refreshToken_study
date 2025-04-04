package jwt_study.user.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jwt_study.user.controller.dto.UserReq;
import jwt_study.user.exceptions.InvalidRefreshTokenException;
import jwt_study.user.service.UserService;
import jwt_study.user.service.dto.TokenRes;
import jwt_study.user.util.CookieUtil;
import jwt_study.user.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserRestController {

    private final UserService userService;
    private final CookieUtil cookieUtil;

    @PostMapping("/api/user/login")
    public void login(@RequestBody UserReq.Login user, HttpServletResponse response) {

        // 유저확인과 토큰 추출
        TokenRes login = userService.extractToken(user.toModel());

        // refresh token 저장
        userService.insertRefreshToken(login.getRefreshToken(), user.getUserId());

        // http 쿠키 저장
        cookieUtil.addRefreshTokenToCookie(login.getRefreshToken(), response);
    }


    @PostMapping("/api/refresh")
    public ResponseEntity<?> refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = cookieUtil.extractRefreshToken(request);

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No refresh token");
        }

        String newAccessToken = userService.reissueAccessToken(refreshToken).getAccessToken();
        return ResponseEntity.ok().header("Authorization", "Bearer " + newAccessToken).build();
    }
}
