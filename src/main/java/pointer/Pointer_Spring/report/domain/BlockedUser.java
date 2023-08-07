package pointer.Pointer_Spring.report.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import pointer.Pointer_Spring.config.BaseEntity;

import javax.persistence.*;

@Getter
@Entity(name = "BlockedUser")
@NoArgsConstructor
public class BlockedUser extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "blocked_user_id", unique = true)
    private Long blockedUserId;
    private String email;
    private String id;

    public BlockedUser(String email, String id) {
        this.email = email;
        this.id = id;
    }
}
