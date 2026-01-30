package com.groupunix.drivewireserver;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.Date;

public class VersionTest {

    @Test
    public void testVersionComparison() {
        Version v1 = new Version(4, 3, 6, "");
        Version v2 = new Version(4, 3, 7, "");
        Version v3 = new Version(5, 0, 0, "");
        Version v1a = new Version(4, 3, 6, "a");

        assertTrue(v2.newerThan(v1));
        assertTrue(v3.newerThan(v2));
        assertTrue(v1a.newerThan(v1));
        assertFalse(v1.newerThan(v2));
    }

    @Test
    public void testEqualsVersion() {
        Version v1 = new Version(4, 3, 6, "SNAPSHOT");
        Version v2 = new Version(4, 3, 6, "SNAPSHOT");
        Version v3 = new Version(4, 3, 6, "RELEASE");

        assertTrue(v1.equalsVersion(v2));
        assertFalse(v1.equalsVersion(v3));
    }

    @Test
    public void testToStringWithBuildNumber() {
        Version v = new Version(4, 3, 6, "");
        v.setBuildNumber("42");
        assertEquals("4.3.6 (build 42)", v.toString());

        v.setBuildNumber(null);
        assertEquals("4.3.6", v.toString());

        v.setBuildNumber("");
        assertEquals("4.3.6", v.toString());
    }

    @Test
    public void testOS9DateMethods() {
        // Date(year, month, day) - year since 1900, month 0-11
        @SuppressWarnings("deprecation")
        Date date = new Date(124, 0, 30); // 2024-01-30
        Version v = new Version(4, 3, 6, "", date);

        assertEquals((byte) 124, v.getOS9Year());
        assertEquals((byte) 0, v.getOS9Month());
        assertEquals((byte) 30, v.getOS9Day());
    }
}
