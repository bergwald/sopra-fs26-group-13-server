package ch.uzh.ifi.hase.soprafs26.entity;

import org.junit.jupiter.api.Test;

import jakarta.persistence.Column;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SessionTest {
    @Test
    public void columns_areNotNullable() throws NoSuchFieldException {
        Field sessionId = Session.class.getDeclaredField("id");
        Field sessionExpiryDate = Session.class.getDeclaredField("sessionExpiryDateTime");
        Field sessionRoundNumber = Session.class.getDeclaredField("roundNumber");

        Column sessionExpiryDateColumn = sessionExpiryDate.getAnnotation(Column.class);
        Column sessionIdColumn = sessionId.getAnnotation(Column.class);
        Column sessionRoundNumberColumn = sessionRoundNumber.getAnnotation(Column.class);

        assertNotNull(sessionExpiryDateColumn);
        assertNotNull(sessionId);
        assertNotNull(sessionRoundNumber);

        assertFalse(sessionExpiryDateColumn.nullable() && sessionIdColumn.nullable()
                && sessionRoundNumberColumn.nullable());

    }

}
