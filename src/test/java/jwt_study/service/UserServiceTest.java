package jwt_study.service;

import jwt_study.user.repository.RefreshTokenRepository;
import jwt_study.user.repository.UserRepository;
import jwt_study.user.repository.entity.RefreshToken;
import jwt_study.user.repository.entity.User;
import jwt_study.user.service.UserService;
import jwt_study.user.service.dto.TokenRes;
import jwt_study.user.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) // ✅ Strict Stubbing 비활성화
class UserServiceTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private UserService userService;

    private final Long userKey = 1L;
    private final String userId = "test001";
    private final String accessToken = "mockAccessToken";
    private final String refreshToken = "mockRefreshToken";

    private User mockUser;
    private RefreshToken mockRefreshTokenEntity;

    @BeforeEach
    void setUp() {
        mockUser = new User(userKey, userId); // User 객체 생성
        mockRefreshTokenEntity = new RefreshToken(userKey, refreshToken); // 추가

        // ✅ userKey(Long)으로 조회하도록 수정
        // ✅ `findByUserId(userId)` 사용 (타입 일치)
        when(userRepository.findByUserId(userId)).thenReturn(Optional.of(mockUser));

        // ✅ `findById(userKey)`도 따로 Stubbing (필요한 경우)
        when(userRepository.findById(userKey)).thenReturn(Optional.of(mockUser));

        // ✅ JWT 가짜 토큰 설정
        when(jwtUtil.generateAccessToken(userKey)).thenReturn("mockAccessToken");
        when(jwtUtil.generateRefreshToken(userKey)).thenReturn("mockRefreshToken");
    }

    @Test
    @DisplayName("사용자 토큰 생성 테스트")
    void testExtractToken() {


        System.out.println("userKey: " + userKey);  // ← 이 로그가 null이면 문제 발생
        // ✅ 테스트 내부에서 불필요한 when() 호출 제거
        TokenRes tokenRes = userService.extractToken(mockUser);

        assertNotNull(tokenRes);
        assertEquals(accessToken, tokenRes.getAccessToken());
        assertEquals(refreshToken, tokenRes.getRefreshToken());
    }

    @Test
    @DisplayName("리프레시 토큰 저장 테스트")
    void testInsertRefreshToken() {
        userService.insertRefreshToken(refreshToken, mockUser.getUserId());
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("리프레시 토큰을 사용한 액세스 토큰 갱신 테스트")
    void testRefreshAccessToken() {
        // 1️⃣ 리프레시 토큰이 유효한지 검증 (true 반환)
        when(jwtUtil.validateToken(refreshToken))
                .thenReturn(true);

        // 2️⃣ 리프레시 토큰에서 userKey(사용자 식별자)를 추출
        when(jwtUtil.extractUserKey(refreshToken))
                .thenReturn(userKey);

        // 3️⃣ DB에서 userKey를 사용해 저장된 리프레시 토큰을 찾음
        when(refreshTokenRepository.findByUserKey(userKey))
                .thenReturn(Optional.of(mockRefreshTokenEntity));

        // 4️⃣ userKey를 기반으로 새로운 액세스 토큰 생성
        when(jwtUtil.generateAccessToken(userKey))
                .thenReturn("newAccessToken");

        // 5️⃣ 리프레시 토큰을 사용하여 새로운 액세스 토큰 발급 요청
        TokenRes result = userService.reissueAccessToken(refreshToken);

        // 6️⃣ 발급된 액세스 토큰이 null이 아닌지 확인
        assertNotNull(result.getAccessToken());

        // 7️⃣ 새로 발급된 액세스 토큰이 예상한 값("newAccessToken")과 동일한지 확인
        assertEquals("newAccessToken", result.getAccessToken());

        // 8️⃣ 리프레시 토큰은 변하지 않고 유지되는지 확인
        assertEquals(refreshToken, result.getRefreshToken());
    }

}
