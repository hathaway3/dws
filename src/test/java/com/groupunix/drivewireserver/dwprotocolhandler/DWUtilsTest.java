package com.groupunix.drivewireserver.dwprotocolhandler;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class DWUtilsTest {

    @Test
    public void testInt2() {
        assertEquals(0x1234, DWUtils.int2(new byte[] { 0x12, 0x34 }));
        assertEquals(0x00FF, DWUtils.int2(new byte[] { 0x00, (byte) 0xFF }));
        assertEquals(0xFFFF, DWUtils.int2(new byte[] { (byte) 0xFF, (byte) 0xFF }));
    }

    @Test
    public void testInt3() {
        assertEquals(0x123456, DWUtils.int3(new byte[] { 0x12, 0x34, 0x56 }));
        assertEquals(0xFFFFFF, DWUtils.int3(new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF }));
    }

    @Test
    public void testInt4() {
        // This test will likely fail due to the << 32 bug if it's meant to be 4-byte
        // int
        assertEquals(0x12345678, DWUtils.int4(new byte[] { 0x12, 0x34, 0x56, 0x78 }));
    }

    @Test
    public void testReverseByte() {
        // reverseByte(int b) -> reverse bits?
        // Let's see what it does. It uses Integer.reverse(b) then Integer.reverseBytes
        // Logic: 0x01 -> 1b000...0 -> reverseBytes -> 0x80 (if b is treated as 8 bits)
        // Wait, Integer.reverse(1) is 0x80000000.
        // Integer.reverseBytes(0x80000000) is 0x00000080.
        // So it reverses bits in a byte.
        assertEquals((byte) 0x80, DWUtils.reverseByte(0x01));
        assertEquals((byte) 0x01, DWUtils.reverseByte(0x80));
        assertEquals((byte) 0xAA, DWUtils.reverseByte(0x55));
    }

    @Test
    public void testHexStringToByteArray() {
        byte[] expected = new byte[] { 0x12, (byte) 0xAB, 0x34 };
        byte[] actual = DWUtils.hexStringToByteArray("12AB34");
        // This is expected to fail currently as it only parses 1 char per byte
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testByteArrayToHexString() {
        byte[] data = new byte[] { 0x12, (byte) 0xAB, 0x34 };
        // It appends colon according to the code: out.append(':');
        assertEquals("12:AB:34:", DWUtils.byteArrayToHexString(data));
    }

    @Test
    public void testDropFirstToken() {
        assertEquals("two three", DWUtils.dropFirstToken("one two three"));
        assertEquals("", DWUtils.dropFirstToken("one"));
        assertEquals("", DWUtils.dropFirstToken(""));
        assertNull(DWUtils.dropFirstToken(null));
    }

    @Test
    public void testIsStringBool() {
        assertTrue(DWUtils.isStringTrue("true"));
        assertTrue(DWUtils.isStringTrue("yes"));
        assertTrue(DWUtils.isStringTrue("1"));
        assertFalse(DWUtils.isStringTrue("false"));

        assertTrue(DWUtils.isStringFalse("false"));
        assertTrue(DWUtils.isStringFalse("no"));
        assertTrue(DWUtils.isStringFalse("0"));
        assertFalse(DWUtils.isStringFalse("true"));
    }
}
