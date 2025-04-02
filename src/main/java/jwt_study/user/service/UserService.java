package jwt_study.user.service;

import jwt_study.user.exceptions.InvalidRefreshTokenException;
import jwt_study.user.exceptions.RefreshTokenNotFoundException;
import jwt_study.user.exceptions.ValidRefreshTokenException;
import jwt_study.user.repository.RefreshTokenRepository;
import jwt_study.user.repository.entity.RefreshToken;
import jwt_study.user.service.dto.TokenRes;
import jwt_study.user.util.JwtUtil;
import jwt_study.user.repository.entity.User;
import jwt_study.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public TokenRes extractToken(User user) {
        User existUser = userRepository.findByUserId(user.getUserId())
                .orElseThrow(RuntimeException::new);

        // 엑세스 토큰 추출
        String accessToken = jwtUtil.generateAccessToken(existUser.getKey());

        // 리프레시 토큰 추출
        String refreshToken = jwtUtil.generateRefreshToken(existUser.getKey());


        return TokenRes.of(accessToken, refreshToken);
    }

    @Transactional
    public void insertRefreshToken(String refreshToken, String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(RuntimeException::new);

        RefreshToken refreshTokenEntity = new RefreshToken();

        refreshTokenRepository.save(refreshTokenEntity.of(user.getKey(), refreshToken));
    }

    @Transactional
    public TokenRes refreshAccessToken(String refreshToken) {
        //  리프레시 토큰 유효성 검사
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new ValidRefreshTokenException();
        }

        // 리프레시 토큰에서 userKey 추출
        Long userKey = jwtUtil.extractUserKey(refreshToken);

        // 저장된 리프레시 토큰 가져오기
        RefreshToken storedToken = refreshTokenRepository.findByUserKey(userKey)
                .orElseThrow(RefreshTokenNotFoundException::new);

        // 토큰이 null 이거나 일치하지 않으면 예외 처리
        if (storedToken.getRefreshToken() == null || !storedToken.getRefreshToken().equals(refreshToken)) {
            throw new InvalidRefreshTokenException();
        }

        // 새로운 액세스 토큰 발급
        String newAccessToken = jwtUtil.generateAccessToken(userKey);

        return TokenRes.of(newAccessToken, refreshToken); // 기존 리프레시 토큰 유지
    }
}
