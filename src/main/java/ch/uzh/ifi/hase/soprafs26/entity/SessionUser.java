package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "session_user")
public class SessionUser implements Serializable {

    @Id
    private Long userId;

    @OneToOne(fetch = FetchType.EAGER, optional = false, cascade = CascadeType.PERSIST)
    @MapsId
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "session_id", referencedColumnName = "id", nullable = false)
    private Session session;

    // score field
    @Column(nullable = false)
    private long score = 0L;

    public Long getUserId() {
        return this.userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getSessionId() {
        return this.session.getId().toString();
    }

    public void setSessionId(UUID sessionId) {
        this.session.setId(sessionId);
    }

    public Long getScore() {
        return this.score;
    }

    public void setScore(Long score) {
        this.score = score;
    }

}
