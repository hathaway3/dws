package com.groupunix.drivewireui;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;


public class Connection 
{
	private int port;
	private String host;
	
	private Socket sock;
	private Thread inputT;
	
	
	public Connection(String host, int port)
	{
		setHost(host);
		setPort(port);
		
	}
	
	
	public void Connect()
	{
		try 
		{
			this.sock = new Socket(this.host, this.port);
			inputT = new Thread(new ConnectionInputThread(this.sock.getInputStream()));
			inputT.start();
			
			MainWin.sendCommand("dw");
			
		} 
		catch (UnknownHostException e) 
		{
			MainWin.addToDisplay(e.getMessage());
		} 
		catch (IOException e) 
		{
			MainWin.addToDisplay(e.getMessage());
		}
		
		
	}
	
	
	public void setPort(int port) {
		this.port = port;
	}
	public int getPort() {
		return port;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public String getHost() {
		return host;
	}


	public void close() 
	{
		try 
		{
			this.sock.close();
			MainWin.addToDisplay("Connection to DriveWire server closed.\r\n");
		} 
		catch (IOException e) 
		{
			MainWin.addToDisplay(e.getMessage());
		}
		
	}


	public boolean connected() 
	{
		if (this.sock.isClosed())
		{
			return false;
		}
		
		return true;
	}


	public void sendCommand(String cmd) 
	{
		
		if (this.sock != null)
		{
			if (!this.sock.isClosed())
			{
				
				try 
				{
					sock.getOutputStream().write(cmd.getBytes());
					sock.getOutputStream().write(10);
					
				} 
				catch (IOException e) 
				{
					addToDisplay(e.getMessage());
					
				}
				
			}
			else
			{
				addToDisplay("Cannot send command, because we are not connected to a DriveWire server (socket closed).");
			}
		}
		else
		{
			addToDisplay("Cannot send command, because we are not connected to a DriveWire server (null socket).");
		}
	}


	
	private void addToDisplay(String txt)
	{
		MainWin.addToDisplay(txt);
	}
	
	
	
	
}
