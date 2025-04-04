package jwt_study.user.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class ValidAccessTokenException extends RuntimeException {

    public ValidAccessTokenException() {
        super("유효하지않은 엑세스 토큰입니다.");
    }
}
