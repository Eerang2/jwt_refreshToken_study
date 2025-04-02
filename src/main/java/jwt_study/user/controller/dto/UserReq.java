package jwt_study.user.controller.dto;

import jwt_study.user.repository.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
public class UserReq {

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Login {
        private String userId;
        private String password;

        public User toModel() {
            return User.builder()
                    .userId(this.userId)
                    .pwd(this.password)
                    .build();
        }
    }


}
