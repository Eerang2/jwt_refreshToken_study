package jwt_study.user.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
@Slf4j
public class JwtUtil {

    private final String secretKeyString; // 환경 변수에서 불러온 SECRET_KEY (32바이트 이상 필수)

    private final JwtProperties jwtProperties;

    public JwtUtil( @Value("${jwt.secret}") String secretKeyString, JwtProperties jwtProperties) {
        this.secretKeyString = secretKeyString;
        this.jwtProperties = jwtProperties;
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKeyString.getBytes());
    }


    // ✅ **액세스 토큰 생성 (userKey만 저장)**
    public String generateAccessToken(Long userKey) {
        return Jwts.builder()
                .setSubject(userKey.toString()) // userKey 저장
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getAccessTokenExpireTime()))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ✅ **리프레시 토큰 생성 (userKey만 저장)**
    public String generateRefreshToken(Long userKey) {
        return Jwts.builder()
                .setSubject(userKey.toString()) // userKey 저장
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getRefreshTokenExpireTime()))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ✅ **토큰에서 userKey 추출 (Long 타입 반환)**
    public Long extractUserKey(String token) {
        String userKeyStr = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
        return Long.parseLong(userKeyStr); // String → Long 변환
    }

    // ✅ **토큰 유효성 검사**
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("토큰이 만료됨: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("지원하지 않는 JWT 형식: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("잘못된 JWT: {}", e.getMessage());
        } catch (SignatureException e) {
            log.warn("서명 검증 실패: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims가 비어있음: {}", e.getMessage());
        }
        return false;
    }

    // ✅ **토큰 만료 여부 확인**
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration();
            return expiration.before(new Date()); // 만료 시간이 현재 시간보다 이전이면 true
        } catch (Exception e) {
            return true; // 파싱 중 에러 발생하면 만료된 것으로 간주
        }
    }
}
