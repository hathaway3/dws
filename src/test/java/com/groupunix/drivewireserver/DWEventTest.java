package com.groupunix.drivewireserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
public class DWEventTest {

    private DWEvent dwEvent;

    @BeforeEach
    public void setUp() {
        dwEvent = new DWEvent((byte) 1, 2);
    }

    @Test
    public void testSetParamWithStringValue() {
        // Arrange
        String key = "paramKey";
        String value = "paramValue";

        // Act
        dwEvent.setParam(key, value);

        // Assert
        assertTrue(dwEvent.hasParam(key));
        assertEquals(value, dwEvent.getParam(key));
    }

    @Test
    public void testSetParamWithByteValue() {
        // Arrange
        String key = "paramKey";
        byte value = 0x0A;

        // Act
        dwEvent.setParam(key, value);

        // Assert
        assertTrue(dwEvent.hasParam(key));
        assertEquals("0a", dwEvent.getParam(key));
    }

    @Test
    public void testSetParamWithByteArrayValue() {
        // Arrange
        String key = "paramKey";
        byte[] value = { 0x0A, 0x0B, 0x0C };

        // Act
        dwEvent.setParam(key, value);

        // Assert
        assertTrue(dwEvent.hasParam(key));
        assertEquals("0a0b0c", dwEvent.getParam(key));
    }

    @Test
    public void testHasParam() {
        // Arrange
        String key = "paramKey";
        String value = "paramValue";
        dwEvent.setParam(key, value);

        // Act & Assert
        assertTrue(dwEvent.hasParam(key));
        assertFalse(dwEvent.hasParam("nonExistentKey"));
    }

    @Test
    public void testGetParam() {
        // Arrange
        String key = "paramKey";
        String value = "paramValue";
        dwEvent.setParam(key, value);

        // Act & Assert
        assertEquals(value, dwEvent.getParam(key));
        assertNull(dwEvent.getParam("nonExistentKey"));
    }

    @Test
    public void testGetParamKeys() {
        // Arrange
        String key1 = "paramKey1";
        String key2 = "paramKey2";
        dwEvent.setParam(key1, "value1");
        dwEvent.setParam(key2, "value2");

        // Act
        Set<String> paramKeys = dwEvent.getParamKeys();

        // Assert
        assertNotNull(paramKeys);
        assertEquals(2, paramKeys.size());
        assertTrue(paramKeys.contains(key1));
        assertTrue(paramKeys.contains(key2));
    }

    @Test
    public void testGetEventType() {
        // Arrange
        byte eventType = 1;
        dwEvent.setEventType(eventType);

        // Act & Assert
        assertEquals(eventType, dwEvent.getEventType());
    }

    @Test
    public void testGetEventInstance() {
        // Arrange
        int eventInstance = -1;
        dwEvent.setEventInstance(eventInstance);

        // Act & Assert
        assertEquals(eventInstance, dwEvent.getEventInstance());
    }
}