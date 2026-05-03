package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;

import java.io.Serializable;

import ch.uzh.ifi.hase.soprafs26.constant.UserSessionRole;

@Entity
@Table(name = "sessionuser")
public class SessionUser implements Serializable {

    @Id
    @GeneratedValue
    private Long id;

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

    @Column(nullable = false)
    private UserSessionRole userRole = UserSessionRole.OWNER;

    @Column
    private double guessLongitude = -1;

    @Column
    private double guessLatitude = -1;

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Session getSession() {
        return this.session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public Long getScore() {
        return this.score;
    }

    public void setScore(Long score) {
        this.score = score;
    }

    public UserSessionRole getUserRole() {
        return this.userRole;
    }

    public void setUserRole(UserSessionRole userRole) {
        this.userRole = userRole;
    }
    
    public double getGuessLatitude() {
        return this.guessLatitude;
    }

    public void setGuessLatitude(double guessLatitude) {
        this.guessLatitude = guessLatitude;
    }

    public double getGuessLongitude() {
        return this.guessLongitude;
    }

    public void setGuessLongitude(double guessLongitude) {
        this.guessLongitude = guessLongitude;
    }
}