package jwt_study.util;

import jwt_study.user.repository.entity.RefreshToken;
import jwt_study.user.service.dto.TokenRes;
import jwt_study.user.util.JwtProperties;
import jwt_study.user.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@TestPropertySource(locations = "classpath:application-test.properties") // 테스트용 설정 파일 로드
public class JwtUtilTest {

    @Mock
    private JwtProperties jwtProperties;

    @InjectMocks
    private JwtUtil jwtUtil;

    private final Long userId = 1L;

    @Autowired
    private Environment environment; // Environment 사용

    private String secretKey; // @Value 대신 직접 설정

    @BeforeEach
    void setUp() {
        secretKey = environment.getProperty("jwt.secret-key"); // 환경 변수에서 직접 가져오기
        System.out.println(secretKey);
        lenient().when(jwtProperties.getAccessTokenExpireTime()).thenReturn(3600000L);
        lenient().when(jwtProperties.getRefreshTokenExpireTime()).thenReturn(86400000L);
        ReflectionTestUtils.setField(jwtUtil, "secretKeyString", secretKey);
    }

    @Test
    @DisplayName("액세스 토큰 생성 - 정상적으로 생성")
    void testGenerateAccessToken() {
        String token = jwtUtil.generateAccessToken(userId);
        assertNotNull(token);
    }

    @Test
    @DisplayName("리프레시 토큰 생성 - 정상적으로 생성")
    void testGenerateRefreshToken() {
        String token = jwtUtil.generateRefreshToken(userId);
        assertNotNull(token);
    }

    @Test
    @DisplayName("토큰에서 사용자 ID 추출 - 올바른 ID가 반환")
    void testExtractUsername() {
        String token = jwtUtil.generateAccessToken(userId);
        Long extractedUserId = jwtUtil.extractUserKey(token);
        assertEquals(userId, extractedUserId);
    }


    @Test
    @DisplayName("토큰 유효성 검사 - 유효한 토큰은 true를 반환")
    void testValidateToken() {
        String token = jwtUtil.generateAccessToken(userId);
        assertTrue(jwtUtil.validateToken(token));
    }
}

