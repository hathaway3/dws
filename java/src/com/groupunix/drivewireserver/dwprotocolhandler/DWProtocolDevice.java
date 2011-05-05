package com.groupunix.drivewireserver.dwprotocolhandler;

import java.io.IOException;

import com.groupunix.drivewireserver.dwexceptions.DWCommTimeOutException;



public interface DWProtocolDevice 
{

	public boolean connected();
	public void close();
	public void shutdown();
	public void comWrite(byte[] data, int len, boolean prefix);
	public void comWrite1(int data, boolean prefix);
	public byte[] comRead(int len) throws IOException; 
	public int comRead1(boolean timeout) throws IOException; 
	public int getRate();
	public String getDeviceType();
	public String getDeviceName();

	
}
