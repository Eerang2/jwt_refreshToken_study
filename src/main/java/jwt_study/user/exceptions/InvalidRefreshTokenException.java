package jwt_study.user.exceptions;

public class InvalidRefreshTokenException extends RuntimeException {
    public InvalidRefreshTokenException() {
        super("리프레시 토큰이 일치하지 않습니다.");
    }
}
