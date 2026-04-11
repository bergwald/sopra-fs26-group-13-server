package ch.uzh.ifi.hase.soprafs26.entity;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertTrue;


public class SessionUserTest {
	@Test
	public void joinTable_annotations() throws NoSuchFieldException {
		Field userId = SessionUser.class.getDeclaredField("user");
		Field session = SessionUser.class.getDeclaredField("session");

        assertTrue(userId.isAnnotationPresent(jakarta.persistence.OneToOne.class));
        assertTrue(session.isAnnotationPresent(jakarta.persistence.ManyToOne.class));
	}
    
}
