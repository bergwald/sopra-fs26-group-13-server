package ch.uzh.ifi.hase.soprafs26.entity;

import org.junit.jupiter.api.Test;

import jakarta.persistence.Column;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class UserTest {

	@Test
	public void usernameColumn_isNotUpdatable() throws NoSuchFieldException {
		Field usernameField = User.class.getDeclaredField("username");
		Column column = usernameField.getAnnotation(Column.class);

		assertNotNull(column);
		assertFalse(column.updatable());
	}
}
