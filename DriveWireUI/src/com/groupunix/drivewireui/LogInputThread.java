package com.groupunix.drivewireui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;


public class LogInputThread implements Runnable 
{
	private String host =  new String();
	private int port = -1;
	private Socket sock = null;
	private boolean wanttodie = false;
	private PrintWriter out;
	private BufferedReader in;
	
	public LogInputThread()
	{
		
	}
	
	@Override
	public void run() 
	{
		char[] cbuf = new char[256];
		
		while (!wanttodie)
		{
			// change/establish connection
			if (!(MainWin.getHost() == null) && !this.host.equals(MainWin.getHost()) || !(this.port == MainWin.getPort()) || (this.sock == null))		
			{
				if (!(sock == null))
				{
					try 
					{
						sock.close();
					} 
					catch (IOException e) 
					{
						MainWin.addToDisplay("Log viewer: " + e.getMessage());
					}
				}
				
				this.host = MainWin.getHost();
				this.port = MainWin.getPort();
								
				try 
				{
					sock = new Socket(host, port);
					
					this.out = new PrintWriter(sock.getOutputStream(), true);
				    this.in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
				    
				    out.println("ui logview");
				} 
				catch (UnknownHostException e) 
				{
					MainWin.addToDisplay("Log viewer: " + e.getMessage());
					sock = null;
				} 
				catch (IOException e) 
				{
					MainWin.addToDisplay("Log viewer: " + e.getMessage());
					sock = null;
				}	
			}
			
			
			if ((sock != null) && !sock.isInputShutdown())
			{
				try 
				{
					int thisread = in.read(cbuf,0,256);
					
					if (thisread < 0)
					{
						MainWin.addToServerDisplay("Closed");
						try 
						{
							sock.close();
						} 
						catch (IOException e1) 
						{
							MainWin.addToDisplay("Log viewer: " + e1.getMessage());
						}
						
						sock = null;
					}
					else if (thisread > 0)
					{
						MainWin.addToServerDisplay(String.valueOf(cbuf).substring(0, thisread));
					}
					
					
				
				} 
				catch (IOException e) 
				{
					MainWin.addToDisplay("Log viewer: " + e.getMessage());
					
					try 
					{
						sock.close();
					} 
					catch (IOException e1) 
					{
						MainWin.addToDisplay("Log viewer: " + e.getMessage());
					}
					
					sock = null;
					
				}
				catch (NullPointerException e)
				{
					try 
					{
						sock.close();
					} 
					catch (IOException e1) 
					{
					}
					sock = null;
				}
			}
		}
	}
	
	public void die()
	{
		this.wanttodie = true;
	}

}
