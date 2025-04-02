package jwt_study.user.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenRes {

    private String accessToken;
    private String refreshToken;

    public static TokenRes of(String accessToken, String refreshToken) {
        return new TokenRes(accessToken, refreshToken);
    }
}
