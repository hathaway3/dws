package com.groupunix.drivewireserver.dwprotocolhandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

public class DWSerialReader implements SerialPortEventListener
{
	private final ArrayBlockingQueue<Byte> queue;
	private final InputStream in;
	
	public DWSerialReader(InputStream in, ArrayBlockingQueue<Byte> q)
	{
		this.queue = q;
		this.in = in;
	}

	@Override
	public void serialEvent(SerialPortEvent arg0)
	{
		 int data;
         
         try
         {
             while ( ( data = in.read()) > -1 )
             {
                 queue.add((byte) data);
             }
             
         }
         catch ( IOException e )
         {
             e.printStackTrace();
         }     
	}

}
