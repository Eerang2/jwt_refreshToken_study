package jwt_study.user.exceptions;

public class RefreshTokenNotFoundException extends RuntimeException {
    public RefreshTokenNotFoundException() {
        super("리프레시 토큰이 존재하지 않습니다.");
    }
}
