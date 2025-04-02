package jwt_study.user.repository.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@Table(name = "TEST_USER")
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_key")
    private Long key;

    private String userId;
    private String pwd;

    public User(Long key, String id) {
        this.key = key;
        this.userId = id;
    }
}
