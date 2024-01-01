package com.groupunix.drivewireserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;


public class DriveWireServerTest {
    DriveWireServer dws = new DriveWireServer();

    @Test
    public void testIncrementConfigSerial() {
        // Arrange
        int initialConfigSerial = DriveWireServer.getConfigSerial();

        // Act
        DriveWireServer.incrementConfigSerial();

        // Assert
        int updatedConfigSerial = DriveWireServer.getConfigSerial();
        assertEquals(initialConfigSerial + 1, updatedConfigSerial);
    }

    @Test

    private Object when(long numDiskOps) {
        return null;
    }

    @Test
    public void testGetAvailableSerialPorts() {
        // Arrange

        // Act
        ArrayList<String> availableSerialPorts = DriveWireServer.getAvailableSerialPorts();

        // Assert
        assertNotNull(availableSerialPorts);
        // Add additional assertions based on your requirements
    }

}
