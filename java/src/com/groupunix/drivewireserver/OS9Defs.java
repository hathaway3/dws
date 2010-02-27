package com.groupunix.drivewireserver;

public class OS9Defs
{
	// Constants from OS9
	
	// Mode byte
	public static final byte MODE_R = (byte) 1;
	public static final byte MODE_W = (byte) 2;
	public static final byte MODE_E = (byte) 4;
	public static final byte MODE_PR = (byte) 8;
	public static final byte MODE_PW = (byte) 16;
	public static final byte MODE_PE = (byte) 32;
	public static final byte MODE_SHARE = (byte) 64;
	public static final byte MODE_DIR = (byte) 128;
	
	
	// Status codes for Get/SetStat
	public static final byte SS_Opt = 0;
	public static final byte SS_Ready = 1;
	public static final byte SS_Size = 2;
	public static final byte SS_Reset = 3;
	public static final byte SS_WTrk = 4;
	public static final byte SS_Pos = 5;
	public static final byte SS_EOF = 6;
	public static final byte SS_Link = 7;
	public static final byte SS_ULink = 8;
	public static final byte SS_Feed = 9;
	public static final byte SS_Frz = 10;
	public static final byte SS_SPT = 11;
	public static final byte SS_SQD = 12;
	public static final byte SS_DCmd = 13;
	public static final byte SS_DevNm = 14;
	public static final byte SS_FD = 15;
	public static final byte SS_Ticks = 16;
	public static final byte SS_Lock = 17;
	
	
}
