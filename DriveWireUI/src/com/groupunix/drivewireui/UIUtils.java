package com.groupunix.drivewireui;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;

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
			// throw new DWUIOperationFailedException("Null result from server");
		}
		else if (res.get(0).startsWith("FAIL"))
		{
			throw new DWUIOperationFailedException(res.get(0));
		}
		
		
		return(res);
		
	}
	
	public static ArrayList<String> loadArrayList(int instance, String arg) throws IOException, DWUIOperationFailedException
	{
		Connection conn = new Connection(MainWin.getHost(), MainWin.getPort(), MainWin.getInstance());
		
		ArrayList<String> res = new ArrayList<String>();
		
		conn.Connect();
		
		conn.attach(instance);
		
		res = conn.loadArrayList(arg);
			
		conn.close();
			
		if (res.size() < 1)
		{
			// throw new DWUIOperationFailedException("Null result from server");
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
			else if (res.get(0).startsWith("FAIL -36"))
			{
				// don't care
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
				// config item is null..
				//throw new DWUIOperationFailedException("Null result from server");
			}
			else if (res.get(0).startsWith("FAIL -36"))
			{
				// config item is not set
				//values.put(settings.get(i), null);
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


	public static boolean validTCPPort(int port) 
	{
		if ((port > 0) && (port < 65536))
			return true;
		
		return false;
	}


	public static void setInstanceSettings(int instance, HashMap<String, String> values) throws UnknownHostException, IOException, DWUIOperationFailedException
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
				conn.sendCommand("dw config set " + val + " " + values.get(val),instance);
			}
		
			conn.close();
		}
		
	}

	public static void writeDiskDef(int instance, int diskno, DiskDef ddef) throws IOException, DWUIOperationFailedException
	{
		Connection conn = new Connection(MainWin.getHost(), MainWin.getPort(), MainWin.getInstance());
		
		conn.Connect();
		
		conn.sendCommand("dw disk offset " + diskno + " " + ddef.getOffset(), instance);
		conn.sendCommand("dw disk limit " + diskno + " " + ddef.getSizelimit(), instance);
		conn.sendCommand("dw disk sync " + diskno + " " + ddef.isSync(), instance);
		conn.sendCommand("dw disk expand " + diskno + " " + ddef.isExpand(), instance);
		conn.sendCommand("dw disk wp " + diskno + " " + ddef.isWriteprotect(), instance);
		conn.sendCommand("dw disk namedobject " + diskno + " " + ddef.isNamedobject(), instance);
		conn.sendCommand("dw disk ssync " + diskno + " " + ddef.isSyncfromsource(), instance);
		
		conn.close();
	}
	
	public static DiskDef getDiskDef(int instance,int diskno) throws IOException, DWUIOperationFailedException
	{
		
		DiskDef disk = new DiskDef();
		
		
		try 
		{
			ArrayList<String> res = UIUtils.loadArrayList(instance, "ui instance disk show " + diskno);
			
			
			for (int i = 0;i<res.size();i++)
			{
				Pattern p_item = Pattern.compile("^(.+):\\s(.+)");
				Matcher m = p_item.matcher(res.get(i));
			  
				if (m.find())
				{
				
					if (m.group(1).equals("path"))
						disk.setPath(m.group(2));
					
					if (m.group(1).equals("sizelimit"))
						disk.setSizelimit(Integer.parseInt(m.group(2)));
					
					if (m.group(1).equals("offset"))
						disk.setOffset(Integer.parseInt(m.group(2)));
					
					if (m.group(1).equals("writeprotect"))
						disk.setWriteprotect(UIUtils.sTob(m.group(2)));
					
					if (m.group(1).equals("sync"))
						disk.setSync(UIUtils.sTob(m.group(2)));
					
					if (m.group(1).equals("expand"))
						disk.setExpand(UIUtils.sTob(m.group(2)));
					
					if (m.group(1).equals("fswriteable"))
						disk.setFswriteable(UIUtils.sTob(m.group(2)));
					
					if (m.group(1).equals("writeable"))
						disk.setWriteable(UIUtils.sTob(m.group(2)));
					
					if (m.group(1).equals("randomwriteable"))
						disk.setRandomwriteable(UIUtils.sTob(m.group(2)));
					
					if (m.group(1).equals("sectors"))
						disk.setSectors(Integer.parseInt(m.group(2)));
					
					if (m.group(1).equals("dirty"))
						disk.setDirty(Integer.parseInt(m.group(2)));
					
					if (m.group(1).equals("lsn"))
						disk.setLsn(Integer.parseInt(m.group(2)));
					
					if (m.group(1).equals("reads"))
						disk.setReads(Integer.parseInt(m.group(2)));
					
					if (m.group(1).equals("writes"))
						disk.setWrites(Integer.parseInt(m.group(2)));
					
					if (m.group(1).equals("loaded"))
						disk.setLoaded(UIUtils.sTob(m.group(2)));
					
					if (m.group(1).equals("namedobject"))
						disk.setNamedobject(UIUtils.sTob(m.group(2)));
					
					if (m.group(1).equals("syncfromsource"))
						disk.setSyncfromsource(UIUtils.sTob(m.group(2)));
					
				}
				
			}
		}
		catch (NumberFormatException e)
		{
			throw new DWUIOperationFailedException("Error parsing disk set results: " + e.getMessage());
		} 
		
		
		return(disk);
		
	}

	public static HierarchicalConfiguration getServerConfig() throws UnknownHostException, IOException, ConfigurationException 
	{
		Connection conn = new Connection(MainWin.getHost(), MainWin.getPort(), MainWin.getInstance());
		
		conn.Connect();
		
		StringReader sr = conn.loadReader("ui server config write");
		conn.close();
		
		XMLConfiguration res = new XMLConfiguration();
		res.load(sr);
		
		// TODO Auto-generated method stub
		return (HierarchicalConfiguration) res.clone();
	}

	public static int getDWConfigSerial() throws IOException, DWUIOperationFailedException 
	{
		int ser = -1;
		
		ArrayList<String> res = UIUtils.loadArrayList("ui server config serial");
		if (res.size() != 1)
		{
			throw new DWUIOperationFailedException("invalid response size: " + res.size());
		}
		
		try
		{
			ser = Integer.parseInt(res.get(0));
			
			if (ser != MainWin.dwconfigserial)
			{
				MainWin.dwconfig = UIUtils.getServerConfig();
				MainWin.dwconfigserial = ser;
			}
			
		}
		catch (NumberFormatException e)
		{
			throw new DWUIOperationFailedException("invalid response: '" + res.get(0) + "'");
		} 
		catch (ConfigurationException e) 
		{
			throw new DWUIOperationFailedException("returned config did not work!: " + e.getMessage());
		}
		
		return(ser);
	}

	public static DiskStatus getDiskStatus(int instance, int drive) throws IOException, DWUIOperationFailedException 
	{
		DiskStatus res = new DiskStatus();
		
		ArrayList<String> dstat = UIUtils.loadArrayList(instance, "ui instance disk status " + drive);
	
		Pattern p_item = Pattern.compile("^(.+):\\s(.+)");
		
						
		for (int i = 0;i<dstat.size();i++)
		{
			Matcher m = p_item.matcher(dstat.get(i));
		  
			if (m.find())
			{
				if (m.group(1).equals("serial"))
					res.setSerial(Integer.parseInt(m.group(2)));
				
				if (m.group(1).equals("loaded"))
					res.setLoaded(UIUtils.sTob(m.group(2)));
				
				if (m.group(1).equals("sectors"))
					res.setSectors(Integer.parseInt(m.group(2)));
				
				if (m.group(1).equals("dirty"))
					res.setDirty(Integer.parseInt(m.group(2)));
				
				if (m.group(1).equals("lsn"))
					res.setLsn(Integer.parseInt(m.group(2)));
				
				if (m.group(1).equals("reads"))
					res.setReads(Integer.parseInt(m.group(2)));
				
				if (m.group(1).equals("writes"))
					res.setWrites(Integer.parseInt(m.group(2)));
				
			}
		}
		
		return(res);
	}

}
