package ch.uzh.ifi.hase.soprafs26.repository;

import org.springframework.stereotype.Repository;

import java.util.UUID;
import ch.uzh.ifi.hase.soprafs26.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository("sessionRepository")
public interface SessionRepository extends JpaRepository<Session, UUID> {
    Session findById(String id);

}
