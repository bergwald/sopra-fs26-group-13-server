package ch.uzh.ifi.hase.soprafs26.repository;

import org.springframework.stereotype.Repository;

import ch.uzh.ifi.hase.soprafs26.entity.SessionUser;

import java.util.UUID;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

@Repository("sessionUserRepository")
public interface SessionUserRepository extends JpaRepository<SessionUser, Long> {
    SessionUser findById(String id);
    List<SessionUser> findBySessionId(UUID sessionId);
}
