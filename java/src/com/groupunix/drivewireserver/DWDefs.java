package com.groupunix.drivewireserver;

public class DWDefs
{

	// DW protocol contants
	public static final byte DW_PROTOCOL_VERSION = 4;
	
	// DW protocol op codes
	
	public static final byte OP_NOP = 0;
	public static final byte OP_GETSTAT = 'G';
	public static final byte OP_SETSTAT = 'S';
	public static final byte OP_READ = 'R';
	public static final byte OP_READEX = (byte) ('R'+128);
	public static final byte OP_WRITE = 'W';
	public static final byte OP_REREAD = 'r';
	public static final byte OP_REREADEX = (byte) ('r'+128);
	public static final byte OP_REWRITE = 'w';
	public static final byte OP_INIT = 'I';
	public static final byte OP_TERM = 'T';
	public static final byte OP_TIME = '#';
	public static final byte OP_RESET3 = (byte) 248;  // my coco gives 248 on reset..?
	public static final byte OP_RESET2 = (byte) 254;
	public static final byte OP_RESET1 = (byte) 255;
	public static final byte OP_DWINIT = (byte) 'Z';
	public static final byte OP_PRINT = 'P';
	public static final byte OP_PRINTFLUSH = 'F';
	public static final byte OP_SERREAD = 'C';
	public static final byte OP_SERREADM = 'c';
	public static final byte OP_SERWRITE = (byte) ('C'+128);
	public static final byte OP_SERSETSTAT = (byte) ('D'+128);
	public static final byte OP_SERGETSTAT = 'D';
	public static final byte OP_SERINIT = 'E';
	public static final byte OP_SERTERM = (byte) ('E'+128);
	public static final byte OP_RFM = (byte) ('V'+128);
	
	// response codes
	public static final byte DWERROR_WP = (byte) 0xF2;
	public static final byte DWERROR_CRC = (byte) 0xF3;
	public static final byte DWERROR_READ = (byte) 0xF4;
	public static final byte DWERROR_WRITE = (byte) 0xF5;
	public static final byte DWERROR_NOTREADY = (byte) 0xF6;
	public static final byte DWOK = (byte) 0;
	
	// input buffer
	public static final int INPUT_WAIT = 250;
	
	// fast writes
	public static final byte OP_FASTWRITE_BASE = (byte) 128;
	

}
