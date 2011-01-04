package com.groupunix.drivewireui;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;


public class Connection 
{
	
	private int port;
	private String host;
	private int instance;
	
	private Socket sock;
	
	
	public Connection(String host, int port, int instance)
	{
		setHost(host);
		setPort(port);
		setInstance(instance);
	}
	
	
	public void Connect() throws UnknownHostException, IOException
	{
		this.sock = new Socket(this.host, this.port);
		this.sock.setSoTimeout(MainWin.config.getInt("TCPTimeout",MainWin.default_TCPTimeout));
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


	public void close() throws IOException 
	{
		this.sock.close();
	}


	public boolean connected() 
	{
		if (this.sock.isClosed())
		{
			return false;
		}
		
		return true;
	}


	public void sendCommand(String cmd, int instance) throws IOException, DWUIOperationFailedException 
	{
		// attach to instance
		attach(instance);
		
		// send command
		ArrayList<String> resp = loadArrayList(cmd);	
			
		if ((resp.size() > 0) && (resp.get(0).startsWith("FAIL")))
		{
			MainWin.showError("Error in command", "The command failed with the following message:" , resp.get(0) );
		}
		else
		{
			addToDisplay("");
			for (int i = 0;i<resp.size();i++)
			{
				addToDisplay(resp.get(i));
			}
		}
		
		
				
	}


	
	private void addToDisplay(String txt)
	{
		MainWin.addToDisplay(txt);
	}


	public void setInstance(int instance) {
		this.instance = instance;
	}


	public int getInstance() {
		return instance;
	}
	
	
	
	
	public ArrayList<String> loadArrayList(String arg) throws IOException 
	{
		if (MainWin.config.getBoolean("ShowCommandsSent",false))
		{
			addToDisplay(">>> " + arg);
		}
		
		ArrayList<String> res = new ArrayList<String>();
		
		sock.getOutputStream().write((arg + "\n").getBytes());
			
		String line = readLine(sock);
			
		// eat welcome
		while ((!sock.isClosed()) && (!line.equals(">>")))
		{
			line = readLine(sock);
		}
			
		// data
		line = readLine(sock);
		
		while ((!sock.isClosed()) && (!line.endsWith("<<")))
		{
			res.add(line);
			
			line = readLine(sock);
		}
		
		if  ((line.length() > 2) && (line.endsWith("<<")))
		{
			res.add(line.substring(0, line.length() - 2));
		}
		
		
		return res;
	}

	private static String readLine(Socket sock) throws IOException 
	{
		String line = new String();
		
		int data = sock.getInputStream().read();
		
		while ((!sock.isClosed()) && (data != -1) && (data != 10))
		{
			line += Character.toString((char) data);
			data = sock.getInputStream().read();
		}
		
		return line;
	}


	public void attach(int inst) throws IOException, DWUIOperationFailedException 
	{
	// attach to instance
		
		ArrayList<String> resp = loadArrayList("ui instance attach " + instance);
		
		if (resp.size() > 0)
		{
			if (resp.get(0).startsWith("FAIL"))
			{
				throw new DWUIOperationFailedException("Error attaching to instance: " + resp.get(0));
			}
			
			
		}
		else
		{
			throw new DWUIOperationFailedException("Unknown error attaching to instance");
		}
		
	}
	
	
	
}
