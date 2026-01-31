package com.groupunix.drivewireserver;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.groupunix.drivewireserver.testclient.DriveWireTestClient;

public class DriveWireProtocolTest {

    private static Thread serverThread;
    private static final String TEST_CONFIG = "target/test-data/test-config.xml";
    private static final String TEST_DSK = "target/test-data/test.dsk";
    private static final int TEST_PORT = 6801;

    @BeforeAll
    public static void setUp() throws Exception {
        // Ensure test data exists
        File dataDir = new File("target/test-data");
        if (!dataDir.exists())
            dataDir.mkdirs();

        File dskFile = new File(TEST_DSK);
        if (!dskFile.exists()) {
            byte[] blank = new byte[256 * 630];
            try (FileOutputStream fos = new FileOutputStream(dskFile)) {
                fos.write(blank);
            }
        }
        String absoluteDskPath = dskFile.getAbsolutePath();

        // Dynamically generate config with absolute path
        String configContent = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                "<drivewire-config xmlns=\"http://groupunix.org/DriveWireConfig\">\n" +
                "    <LogLevel>DEBUG</LogLevel>\n" +
                "    <LogToConsole>true</LogToConsole>\n" +
                "    <UIEnabled>false</UIEnabled>\n" +
                "    <ConfigAutosave>false</ConfigAutosave>\n" +
                "    <instance>\n" +
                "        <Name>Test Instance</Name>\n" +
                "        <DeviceType>tcp</DeviceType>\n" +
                "        <TCPServerPort>6801</TCPServerPort>\n" +
                "        <Drive0Path>" + absoluteDskPath + "</Drive0Path>\n" +
                "        <AutoStart>true</AutoStart>\n" +
                "        <CocoModel>3</CocoModel>\n" +
                "    </instance>\n" +
                "</drivewire-config>\n";

        try (FileOutputStream fos = new FileOutputStream(TEST_CONFIG)) {
            fos.write(configContent.getBytes());
        }

        // Run server in a separate thread
        serverThread = new Thread(() -> {
            try {
                DriveWireServer.init(new String[] { "-config", TEST_CONFIG });
                DriveWireServer.startProtoHandlers();
                while (true) {
                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();

        // Give it a moment to start
        Thread.sleep(2000);
    }

    @AfterAll
    public static void tearDown() throws Exception {
        DriveWireServer.serverShutdown();
        if (serverThread != null) {
            serverThread.interrupt();
        }
    }

    @Test
    public void testHandshake() throws IOException {
        DriveWireTestClient client = new DriveWireTestClient("localhost", TEST_PORT);
        client.connect();
        int serverVersion = client.dwInit(4);
        assertEquals(4, serverVersion, "Server should respond with protocol version 4");
        client.disconnect();
    }

    @Test
    public void testReadWriteSector() throws IOException {
        DriveWireTestClient client = new DriveWireTestClient("localhost", TEST_PORT);
        client.connect();
        client.dwInit(4);

        int drive = 0;
        int lsn = 10;
        byte[] testData = new byte[256];
        Arrays.fill(testData, (byte) 0xAA);
        testData[0] = (byte) 0xDE;
        testData[1] = (byte) 0xAD;
        testData[2] = (byte) 0xBE;
        testData[3] = (byte) 0xEF;

        // Write sector
        client.writeSector(drive, lsn, testData);

        // Read back sector
        byte[] readData = client.readSector(drive, lsn, 256);

        assertArrayEquals(testData, readData, "Read data should match written data");

        client.disconnect();
    }
}
