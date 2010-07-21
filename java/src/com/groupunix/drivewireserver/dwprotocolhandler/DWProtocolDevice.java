package com.groupunix.drivewireserver.dwprotocolhandler;

import com.groupunix.drivewireserver.dwexceptions.DWCommTimeOutException;



public interface DWProtocolDevice 
{

	public boolean connected();
	public void close();
	public void shutdown();
	public void comWrite(byte[] data, int len);
	public void comWrite1(int data);
	public byte[] comRead(int len) throws DWCommTimeOutException; 
	public int comRead1(boolean timeout) throws DWCommTimeOutException; 
	public int getRate();
}
