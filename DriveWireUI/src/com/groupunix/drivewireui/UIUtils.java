package com.groupunix.drivewireui;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class UIUtils {

	public static ArrayList<String> loadArrayList(String arg) 
	{
		ArrayList<String> res = new ArrayList<String>();
		
		try 
		{
			Socket sock = new Socket(MainWin.getConnection().getHost(), MainWin.getConnection().getPort());
			
			sock.getOutputStream().write(("ui list " + arg + "\n").getBytes());
			
			String line = readLine(sock);
			
			// eat welcome
			while ((!sock.isClosed()) && (!line.equals(Character.toString((char) 0))))
			{
				line = readLine(sock);
			}
			
			// data
			line = readLine(sock);
			
			while ((!sock.isClosed()) && (!line.equals(Character.toString((char) 0))))
			{
				res.add(line);

				line = readLine(sock);
				
			}
			
			sock.close();
		} 
		catch (UnknownHostException e) 
		{
			MainWin.addToDisplay(e.getMessage());
		} 
		catch (IOException e) 
		{
			MainWin.addToDisplay(e.getMessage());
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
	
	
}
