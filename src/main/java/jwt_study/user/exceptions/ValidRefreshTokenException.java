package jwt_study.user.exceptions;

public class ValidRefreshTokenException extends RuntimeException {
    public ValidRefreshTokenException() {
        super("리프레시 토큰이 유효하지 않습니다.");
    }
}
