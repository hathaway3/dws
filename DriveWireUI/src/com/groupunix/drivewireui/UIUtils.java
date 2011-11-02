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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.configuration.AbstractHierarchicalFileConfiguration;
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
		
		conn.sendCommand("dw disk set " + diskno + " offset " + ddef.getOffset(), instance);
		conn.sendCommand("dw disk set " + diskno + " sizelimit " + ddef.getSizelimit(), instance);
		conn.sendCommand("dw disk set " + diskno + " syncto " + ddef.isSync(), instance);
		conn.sendCommand("dw disk set " + diskno + " expand " + ddef.isExpand(), instance);
		conn.sendCommand("dw disk set " + diskno + " writeprotect " + ddef.isWriteprotect(), instance);
		conn.sendCommand("dw disk set " + diskno + " namedobject " + ddef.isNamedobject(), instance);
		conn.sendCommand("dw disk set " + diskno + " syncfrom " + ddef.isSyncfromsource(), instance);
		conn.sendCommand("dw disk set " + diskno + " padpartial " + ddef.isPadPartial(), instance);
		
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
					
					if (m.group(1).equals("syncto"))
						disk.setSync(UIUtils.sTob(m.group(2)));
					
					if (m.group(1).equals("expand"))
						disk.setExpand(UIUtils.sTob(m.group(2)));
					
					if (m.group(1).equals("fswriteable"))
						disk.setFswriteable(UIUtils.sTob(m.group(2)));
					
					if (m.group(1).equals("writeable"))
						disk.setWriteable(UIUtils.sTob(m.group(2)));
					
					if (m.group(1).equals("randomwriteable"))
						disk.setRandomwriteable(UIUtils.sTob(m.group(2)));
					
					if (m.group(1).equals("_sectors"))
						disk.setSectors(Integer.parseInt(m.group(2)));
					
					if (m.group(1).equals("_dirty"))
						disk.setDirty(Integer.parseInt(m.group(2)));
					
					if (m.group(1).equals("_lsn"))
						disk.setLsn(Integer.parseInt(m.group(2)));
					
					if (m.group(1).equals("_reads"))
						disk.setReads(Integer.parseInt(m.group(2)));
					
					if (m.group(1).equals("_writes"))
						disk.setWrites(Integer.parseInt(m.group(2)));
					
					if (m.group(1).equals("loaded"))
						disk.setLoaded(UIUtils.sTob(m.group(2)));
					
					if (m.group(1).equals("namedobject"))
						disk.setNamedobject(UIUtils.sTob(m.group(2)));
					
					if (m.group(1).equals("syncfrom"))
						disk.setSyncfromsource(UIUtils.sTob(m.group(2)));
					
					if (m.group(1).equals("padpartial"))
						disk.setPadPartial(UIUtils.sTob(m.group(2)));
					
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

	
	
	public static int getNextFreeDisk(String dsname) 
	{
		
		
		HierarchicalConfiguration dset = getDiskset(dsname);
		
		if (dset != null)
		{
			for (int i = 0;i < MainWin.getInstanceConfig().getInt("DiskMaxDrives",255);i++)
			{
				if (getDisksetDisk(dsname, i) == null)
				{
					return(i);
				}
			}
		}
		
		return -1;
	}

	
	public static HierarchicalConfiguration getDiskset(String dsname)
	{
		HierarchicalConfiguration res = null;
		 
		@SuppressWarnings("unchecked")
		List<HierarchicalConfiguration> disksets = MainWin.dwconfig.configurationsAt("diskset");
		
		for(Iterator<HierarchicalConfiguration> it = disksets.iterator(); it.hasNext();)
		{
			HierarchicalConfiguration dset = it.next();
			if (dset.getString("Name").equals(dsname))
			{
				res = dset;
			}
		}
		
		return res;
	}
	
	public static HierarchicalConfiguration getDisksetDisk(String dsname, int diskno)
	{
		HierarchicalConfiguration res = null;
		 
		HierarchicalConfiguration dset = getDiskset(dsname);
		
		if (dset != null)
		{
			@SuppressWarnings("unchecked")
			List<HierarchicalConfiguration> disks = dset.configurationsAt("disk");
		
			for(Iterator<HierarchicalConfiguration> it = disks.iterator(); it.hasNext();)
			{
				HierarchicalConfiguration disk = it.next();
				if (disk.getInt("drive") == diskno)
				{
					res = disk;
				}
			}
		
		}
		
		return res;
	}

	public static void createDiskset(String dsname, String cp) throws IOException, DWUIOperationFailedException 
	{
		Connection conn = new Connection(MainWin.getHost(), MainWin.getPort(), MainWin.getInstance());
		
		conn.Connect();
		
		// conn.sendCommand("dw disk dset create " + dsname, MainWin.getInstance());
		MainWin.addToDisplay("dw disk dset create " + dsname);
		
		if (cp != null)
		{
			HierarchicalConfiguration  cpdset = UIUtils.getDiskset(cp);
			
			if (cpdset != null)
			{
				// copy diskset settings
				
				for(@SuppressWarnings("unchecked")
				Iterator<String> itk = cpdset.getKeys(); itk.hasNext();)
				{
					String option = itk.next();
					
					if (!option.equals("Name") && !option.startsWith("disk."))
					{
						// conn.sendCommand("dw disk dset set " + dsname + " " + option + " " + cpdset.getString(option)  , MainWin.getInstance());
						MainWin.addToDisplay("dw disk dset set " + dsname + " " + option + " " + cpdset.getString(option));
					}
				}
					
				
				// copy disks and settings
				
				@SuppressWarnings("unchecked")
				List<HierarchicalConfiguration> disks = cpdset.configurationsAt("disk");
			
				for(Iterator<HierarchicalConfiguration> itd = disks.iterator(); itd.hasNext();)
				{
					HierarchicalConfiguration disk = itd.next();
					
					if (disk.containsKey("drive") && disk.containsKey("path"))
					{
						//conn.sendCommand("dw disk dset adddisk " + dsname + " " + disk.getInt("drive") + " " + disk.getString("path") , MainWin.getInstance());
						MainWin.addToDisplay("dw disk dset adddisk " + dsname + " " + disk.getInt("drive") + " " + disk.getString("path"));
						
						for(@SuppressWarnings("unchecked")
						Iterator<String> itk = disk.getKeys(); itk.hasNext();)
						{
							String option = itk.next();
							
							if (!option.equals("drive") && !option.equals("path"))
							{
								//conn.sendCommand("dw disk dset setdisk " + dsname + " " + disk.getInt("drive") + " " + option + " " + disk.getString(option)  , MainWin.getInstance());
								MainWin.addToDisplay("dw disk dset setdisk " + dsname + " " + disk.getInt("drive") + " " + option + " " + disk.getString(option) );
							}
						}
					}
					
				}
			}
			
		}
		
		conn.close();	
		
	}

	public static DWServerFile[] getFileArray(String uicmd) throws IOException, DWUIOperationFailedException 
	{

		DWServerFile[] res = null;
			
		ArrayList<String> roots = UIUtils.loadArrayList(uicmd);
				
		res = new DWServerFile[roots.size()];
				
		for (int i = 0;i<roots.size();i++)
		{
			res[i] = new DWServerFile(".");
			res[i].setVals(roots.get(i));
					
		}
				

			
			
		return(res);
		
	}
	
	
}
