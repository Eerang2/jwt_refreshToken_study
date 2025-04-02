package jwt_study.user.repository.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refresh_token_key")
    private Long key;

    private Long userKey;

    private String refreshToken;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime expiredAt;

    public RefreshToken of(Long userKey, String refreshToken) {
        return RefreshToken.builder()
                .userKey(userKey)
                .refreshToken(refreshToken)
                .createdAt(LocalDateTime.now())
                .expiredAt(null)
                .build();

    }

    public RefreshToken(Long userKey, String refreshToken) {
        this.userKey = userKey;
        this.refreshToken = refreshToken;
    }
}
