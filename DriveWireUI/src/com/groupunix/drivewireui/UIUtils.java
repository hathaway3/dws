package com.groupunix.drivewireui;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

public class UIUtils {


	
	public static ArrayList<String> loadArrayList(String arg) throws IOException, DWUIOperationFailedException
	{
		Connection conn = new Connection(MainWin.getHost(), MainWin.getPort(), MainWin.getInstance());
		
		ArrayList<String> res = new ArrayList<String>();
		
		conn.Connect();
			
		res = conn.loadArrayList(arg);
			
		conn.close();
			
		if (res.size() < 1)
		{
			throw new DWUIOperationFailedException("Null result from server");
		}
		else if (res.get(0).startsWith("FAIL"))
		{
			throw new DWUIOperationFailedException(res.get(0));
		}
		
		
		return(res);
		
	}
	
	
	public static String getStackTrace(Throwable aThrowable) {
	    final Writer result = new StringWriter();
	    final PrintWriter printWriter = new PrintWriter(result);
	    aThrowable.printStackTrace(printWriter);
	    return result.toString();
	  }


	public static HashMap<String, String> getServerSettings(ArrayList<String> settings) throws DWUIOperationFailedException, IOException 
	{
		// create hashmap containing the requested settings
		
		HashMap<String,String> values = new HashMap<String,String>();
			
		Connection conn = new Connection(MainWin.getHost(), MainWin.getPort(), MainWin.getInstance());
		
		conn.Connect();
		
		
		
		
		for (int i = 0;i<settings.size();i++)
		{
			ArrayList<String> res = conn.loadArrayList("ui server config show " + settings.get(i));
			
			if (res.size() < 1)
			{
				throw new DWUIOperationFailedException("Null result from server");
			}
			else if (res.get(0).startsWith("FAIL"))
			{
				throw new DWUIOperationFailedException(res.get(0));
			}
			else
			{
				values.put(settings.get(i), res.get(0));
			}
		}
		
		conn.close();
		
		
		return(values);
	}



	public static boolean sTob(String val) 
	{
		if ((val != null) && (val.equalsIgnoreCase("true")))
			return true;
		
		return false;
	}


	public static void setServerSettings(HashMap<String, String> values) throws IOException, DWUIOperationFailedException 
	{
		if (values.size() > 0)
		{
			Connection conn = new Connection(MainWin.getHost(), MainWin.getPort(), MainWin.getInstance());
		
			conn.Connect();

			Collection<String> c = values.keySet();
			Iterator<String> itr = c.iterator();
		
			while(itr.hasNext())
			{
				String val = itr.next();
				conn.sendCommand("ui server config set " + val + " " + values.get(val),0);
			}
		
			conn.close();
		}
	}


	public static String bTos(boolean selection) 
	{
		if (selection)
			return("true");
		
		return "false";
	}

	public static boolean validateNum(String data) 
	{
		try 
		{
			Integer.parseInt(data);

		}
		catch (NumberFormatException e)
		{
			return false;
		}
		
		return true;
	}

	

	public static boolean validateNum(String data, int min) 
	{
		try 
		{
			int val = Integer.parseInt(data);
			if (val < min)
				return false;
		}
		catch (NumberFormatException e)
		{
			return false;
		}
		
		return true;
	}

	public static boolean validateNum(String data, int min, int max) 
	{
		try 
		{
			int val = Integer.parseInt(data);
			if ((val < min) || (val > max))
				return false;
			
		}
		catch (NumberFormatException e)
		{
			return false;
		}
		
		return true;
	}


	public static HashMap<String, String> getInstanceSettings(int instance, ArrayList<String> settings) throws DWUIOperationFailedException, IOException 
	{
		// create hashmap containing the requested settings
		
		HashMap<String,String> values = new HashMap<String,String>();
			
		Connection conn = new Connection(MainWin.getHost(), MainWin.getPort(), instance);
		
		conn.Connect();
		
		// connect to instance
		conn.attach(instance);
		
		for (int i = 0;i<settings.size();i++)
		{
			ArrayList<String> res = conn.loadArrayList("ui instance config show " + settings.get(i));
			
			if (res.size() < 1)
			{
				throw new DWUIOperationFailedException("Null result from server");
			}
			else if (res.get(0).startsWith("FAIL -36"))
			{
				// config item is not set
				values.put(settings.get(i), null);
			}
			else if (res.get(0).startsWith("FAIL"))
			{
				
				throw new DWUIOperationFailedException(res.get(0));
			}
			else
			{
				values.put(settings.get(i), res.get(0));
			}
		}
		
		conn.close();
		
		
		return(values);
	}
}
