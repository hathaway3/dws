package com.groupunix.drivewireserver;

public class DWDefs
{

	// DW protocol contants
	public static final byte DW_PROTOCOL_VERSION = 4;
	
	// DW protocol op codes
	
	public static final byte OP_NOP = 0;
	
	public static final byte OP_NAMEOBJ_MOUNT = 1;
	public static final byte OP_NAMEOBJ_CREATE = 2;
	
	
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
	public static final byte OP_SERREAD = 'C';  // 67 43
	public static final byte OP_SERREADM = 'c'; // 99 63
	public static final byte OP_SERWRITE = (byte) ('C'+128); // 195 C3
	public static final byte OP_SERSETSTAT = (byte) ('D'+128); // 196 C4
	public static final byte OP_SERGETSTAT = 'D'; // 68 44
	public static final byte OP_SERINIT = 'E'; // 69 45
	public static final byte OP_SERTERM = (byte) ('E'+128); // 197 C5
	public static final byte OP_RFM = (byte) ('V'+128);
	
	public static final byte OP_230K230K = (byte) 230;
	public static final byte OP_230K115K = (byte) 253;
	
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
	
	// util modes
	public static final int UTILMODE_UNSET = 0;
	public static final int UTILMODE_DWCMD = 1;
	public static final int UTILMODE_URL = 2;
	public static final int UTILMODE_TCPOUT = 3;
	public static final int UTILMODE_VMODEMOUT = 4;
	public static final int UTILMODE_TCPIN = 5;
	public static final int UTILMODE_VMODEMIN = 6;
	public static final int UTILMODE_TCPLISTEN = 7;
	
	
	// result codes
	public static final byte RC_SUCCESS = 0;
	public static final byte RC_SYNTAX_ERROR = 100;
	public static final byte RC_INVALID_DRIVE = 101;
	public static final byte RC_DRIVE_NOT_LOADED = 102;
	public static final byte RC_DRIVE_ALREADY_LOADED = 103;
	public static final byte RC_NO_SUCH_DISKSET = 110;
	
	public static final byte RC_SERVER_FILESYSTEM_EXCEPTION = (byte)200;
	public static final byte RC_SERVER_IO_EXCEPTION = (byte)201;
	public static final byte RC_SERVER_FILE_NOT_FOUND = (byte)202;


	public static final byte RC_INVALID_HANDLER = (byte) 210;


	public static final byte RC_CONFIG_KEY_NOT_SET = (byte)220;

	public static final byte RC_INVALID_PORT = (byte)140;

	public static final byte RC_MIDI_UNAVAILABLE = (byte)150;

	public static final byte RC_MIDI_INVALID_DEVICE = (byte)151;

	public static final byte RC_MIDI_INVALID_DATA = (byte)152;

	public static final byte RC_MIDI_SOUNDBANK_FAILED = (byte)153;

	public static final byte RC_MIDI_SOUNDBANK_NOT_SUPPORTED = (byte)154;

	public static final byte RC_MIDI_INVALID_PROFILE = (byte)155;

	public static final byte RC_MIDI_ERROR = (byte)156;

	public static final int DISK_MAXDRIVES = 256;

	public static final int DISK_MAXSECTORS = 16777215;
	
	public static final int DISK_SECTORSIZE = 256;

	
	
	

}
