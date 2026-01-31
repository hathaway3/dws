package com.groupunix.drivewireserver.testclient;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * A test client for the DriveWire protocol.
 * Emulates a DriveWire hardware/firmware client (e.g., a CoCo).
 */
public class DriveWireTestClient {
    private String host;
    private int port;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    public DriveWireTestClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void connect() throws IOException {
        socket = new Socket(host, port);
        socket.setTcpNoDelay(true);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
    }

    public void disconnect() throws IOException {
        if (socket != null) {
            socket.close();
        }
    }

    /**
     * OP_DWINIT (0x5A)
     * 
     * @param version Client version
     * @return Server version
     */
    public int dwInit(int version) throws IOException {
        out.writeByte(90); // OP_DWINIT
        out.writeByte(version);
        out.flush();
        return in.readUnsignedByte();
    }

    /**
     * OP_READ (0x52)
     * 
     * @param drive      Drive number (0-255)
     * @param lsn        Logical Sector Number (24-bit)
     * @param sectorSize Expected sector size
     * @return Sector data
     */
    public byte[] readSector(int drive, int lsn, int sectorSize) throws IOException {
        out.writeByte(82); // OP_READ
        out.writeByte(drive);
        out.writeByte((lsn >> 16) & 0xFF);
        out.writeByte((lsn >> 8) & 0xFF);
        out.writeByte(lsn & 0xFF);
        out.flush();

        int response = in.readUnsignedByte();
        if (response != 0) {
            throw new IOException("Server returned error code: " + response);
        }

        byte[] sector = new byte[sectorSize];
        in.readFully(sector);

        int serverChecksum = in.readUnsignedShort();
        int calculatedChecksum = computeChecksum(sector);
        if (serverChecksum != calculatedChecksum) {
            throw new IOException(
                    "Checksum mismatch! Server: " + serverChecksum + ", Calculated: " + calculatedChecksum);
        }

        return sector;
    }

    /**
     * OP_WRITE (0x57)
     * 
     * @param drive Drive number (0-255)
     * @param lsn   Logical Sector Number (24-bit)
     * @param data  Sector data
     */
    public void writeSector(int drive, int lsn, byte[] data) throws IOException {
        out.writeByte(87); // OP_WRITE
        out.writeByte(drive);
        out.writeByte((lsn >> 16) & 0xFF);
        out.writeByte((lsn >> 8) & 0xFF);
        out.writeByte(lsn & 0xFF);
        out.write(data);

        int checksum = computeChecksum(data);
        out.writeShort(checksum);
        out.flush();

        int response = in.readUnsignedByte();
        if (response != 0) {
            throw new IOException("Server returned error code: " + response);
        }
    }

    private int computeChecksum(byte[] data) {
        int checksum = 0;
        for (byte b : data) {
            checksum += (b & 0xFF);
        }
        return checksum & 0xFFFF;
    }
}
