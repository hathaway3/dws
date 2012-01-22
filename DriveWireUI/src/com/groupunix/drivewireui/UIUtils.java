package com.groupunix.drivewireui;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public class UIUtils {


	
	public static List<String> loadList(String arg) throws IOException, DWUIOperationFailedException
	{
		return(loadList(-1,arg));
	}
	
	
	public static List<String> loadList(int instance, String arg) throws IOException, DWUIOperationFailedException
	{
		//TODO - ignoring instance?
		Connection conn = new Connection(MainWin.getHost(), MainWin.getPort(), MainWin.getInstance());
		
		List<String> res = new ArrayList<String>();
		
		conn.Connect();
		
		res = conn.loadList(instance, arg);
			
		conn.close();
		
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
			List<String> res = conn.loadList(-1, "ui server config show " + settings.get(i));
			values.put(settings.get(i), res.get(0));
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
			int tid = MainWin.taskman.addTask("Server settings dump");
			
			MainWin.taskman.updateTask(tid, UITaskMaster.TASK_STATUS_ACTIVE, "Connecting to server...");
			
			Connection conn = new Connection(MainWin.getHost(), MainWin.getPort(), MainWin.getInstance());
		
			conn.Connect();

			Collection<String> c = values.keySet();
			Iterator<String> itr = c.iterator();
		
			while(itr.hasNext())
			{
				String val = itr.next();
				conn.sendCommand(tid, "ui server config set " + val + " " + values.get(val),0);
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
		
		
		for (int i = 0;i<settings.size();i++)
		{
			List<String> res = conn.loadList(instance, "ui instance config show " + settings.get(i));
			values.put(settings.get(i), res.get(0));
		
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
			int tid = MainWin.taskman.addTask("Instance settings dump");
			
			MainWin.taskman.updateTask(tid, UITaskMaster.TASK_STATUS_ACTIVE, "Connecting to server...");
			
			Connection conn = new Connection(MainWin.getHost(), MainWin.getPort(), MainWin.getInstance());
		
			conn.Connect();
			
			Collection<String> c = values.keySet();
			Iterator<String> itr = c.iterator();
		
			while(itr.hasNext())
			{
				String val = itr.next();
				conn.sendCommand(tid,"dw config set " + val + " " + values.get(val),instance);
			}
		
			conn.close();
		}
		
	}

	
	
	public static DiskDef getDiskDef(int instance,int diskno) throws IOException, DWUIOperationFailedException
	{
		
		DiskDef disk = new DiskDef(diskno);
		
		
		try 
		{
			List<String> res = UIUtils.loadList(instance, "ui instance disk show " + diskno);
			
			
			for (int i = 0;i<res.size();i++)
			{
				Pattern p_item = Pattern.compile("^(.+):\\s(.+)");
				Matcher m = p_item.matcher(res.get(i));
				
				if (m.find())
				{
					if (m.group(1).startsWith("*"))
					{
						if (m.group(1).equals("*loaded"))
							disk.setLoaded(Boolean.parseBoolean(m.group(2)));
					}
					else
					{
						disk.setParam(m.group(1), m.group(2));
					}
					
				
				}
				
			}
		}
		catch (NumberFormatException e)
		{
			throw new DWUIOperationFailedException("Error parsing disk set results: " + e.getMessage());
		} 
		
		
		return(disk);
		
	}

	public static HierarchicalConfiguration getServerConfig() throws UnknownHostException, IOException, ConfigurationException, DWUIOperationFailedException 
	{
		Connection conn = new Connection(MainWin.getHost(), MainWin.getPort(), MainWin.getInstance());
		
		conn.Connect();
		
		StringReader sr = conn.loadReader(-1,"ui server config write");
		conn.close();

		XMLConfiguration res = new XMLConfiguration();
		res.load(sr);
		return (HierarchicalConfiguration) res.clone();
	}
	
	

	public static MIDIStatus getServerMidiStatus() throws IOException, DWUIOperationFailedException 
	{
		List<String> res = UIUtils.loadList(MainWin.getInstance(), "ui instance midi");
		
		MIDIStatus st = new MIDIStatus();
		
		for (String l : res)
		{
			String[] kv = l.trim().split("\\|");
			
			// ignore anything odd
			if (kv.length > 1)
			{
				String key = kv[0];
				
				if (key.equals("cprofile"))
				{
					st.setCurrentProfile(kv[1]);
				}
				else if (key.equals("cdevice"))
				{
					st.setCurrentDevice(kv[1]);
				}
				else if (key.equals("enabled"))
				{
					st.setEnabled(Boolean.parseBoolean(kv[1]));
				}
				else if (key.equals("profile"))
				{
					if (kv.length == 3)
						st.addProfile(kv[1], kv[2]);
				}
				else if (key.equals("device"))
				{
					if (kv.length == 7)
						st.addDevice(Integer.parseInt(kv[1]), kv[2], kv[3], kv[4], kv[5], kv[6]);
				}

				
			}
		}
		
		return(st);
		
	}
	
	
	public static DiskDef[] getServerDisks() throws IOException, DWUIOperationFailedException 
	{
		DiskDef[] disks = new DiskDef[256];
		
		List<String> res = UIUtils.loadList(MainWin.getInstance(), "ui instance disk show");
		
		// get info for loaded drives
		for (String l : res)
		{
			String[] parts = l.trim().split("\\|");
			
			if (parts.length == 2)
			{
				int drive = Integer.parseInt(parts[0]);
				disks[drive] = getDiskDef(MainWin.getInstance(), drive);
			}
		}
		
		// create blank defs for the rest
		
		for (int i = 0;i<256;i++)
		{
			if (disks[i] == null)
				disks[i] = new DiskDef(i);
		}
		
		return(disks);
	}

	
	

	
	
	
	
	
	

	public static DWServerFile[] getFileArray(String uicmd) throws IOException, DWUIOperationFailedException 
	{

		DWServerFile[] res = null;
			
		List<String> roots = UIUtils.loadList(uicmd);
				
		res = new DWServerFile[roots.size()];
				
		for (int i = 0;i<roots.size();i++)
		{
			res[i] = new DWServerFile(".");
			res[i].setVals(roots.get(i));
					
		}
			
		return(res);
		
	}

	public static void fileCopy(String infile, String outfile) throws IOException 
	{
		File f1 = new File(infile);
		File f2 = new File(outfile);
		InputStream in = new FileInputStream(f1);
		  
		OutputStream out = new FileOutputStream(f2);

		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0)
		{
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
		
	}


	
	


	public static String getFilenameFromURI(String string)
	{
		String res = string;
		
		if ((res.indexOf('/') > -1) && (res.indexOf('/') < (res.length()-1)))
					res = res.substring(res.lastIndexOf('/')+1);
			
		return(res);
	}
	
	public static String shortenLocalURI(String df) 
	{
		if ((df != null ) && df.startsWith("file:///"))
		{
			if (df.charAt(9) == ':')
			{
				return df.substring(8);
			}
			else
			{
				return df.substring(7);
			}
		}
		return(df);
	}
	
	public static String getLocationFromURI(String string)
	{
		String res = string;
		
		if ((res.indexOf('/') > -1) && (res.indexOf('/') < (res.length()-1)))
					res = res.substring(0,res.lastIndexOf('/'));
		
		
		return(shortenLocalURI(res));
	}
	
	
	public static void loadFonts()
	{
		
		MainWin.debug("load fonts");
		
		class OnlyExt implements FilenameFilter 
		{ 
			String ext; 
			public OnlyExt(String ext) 
			{ 
				this.ext = "." + ext; 
			} 
			
				
			public boolean accept(File dir, String name) 
			{ 
				return name.endsWith(ext); 
			}	 
		}
		
		
		File fontdir = new File("fonts");
		
		if (fontdir.exists() && fontdir.isDirectory())
		{
			String[] files = fontdir.list(new OnlyExt("ttf"));
			
			for(int i = 0;i<files.length;i++)
			{
				if (!MainWin.getDisplay().loadFont("fonts/" + files[i])) 
				{
		                MainWin.debug("Failed to load font from " + files[i]);
				}
				else
				{
					
					
					MainWin.debug("Loaded font from " + files[i]);
				}
			}
			
			
			
		}
		else
		{
			MainWin.debug("No font dir");
		}
		
	}
	
	
	public static Font findSizedFont(String fname, String txt, int maxw, int maxh, int style) 
	{
        
        int size = 8;
        int width = 0;
        int height = 0;
        
        Image test = new Image(null, maxw*2,maxh*2);
        GC gc = new GC(test);
        Font font = null;
        
        while ((width < maxw) && (height < maxh))
        {
        	if (font!=null)
        		font.dispose();
        	
        	font = new Font(MainWin.getDisplay(), fname, size, style);
        
        	gc.setFont(font);
        	width = gc.textExtent(txt).x;
        	height = gc.textExtent(txt).y;
        	
        	size++;
        }
        
        gc.dispose();
        
        MainWin.debug("chose font " + fname + " @ " + size + " = " + width + " x " + height);
    	
        
        return font;
	}

	public static Font findFont(Display display, HashMap<String,Integer> candidates , String text, int maxw, int maxh)
	{
		FontData[] fd = MainWin.getDisplay().getFontList(null, true);
		
		
		for (FontData f : fd)
		{
			for (Entry<String,Integer> e : candidates.entrySet())
			{
				if (f.getName().equals(e.getKey()) && (f.getStyle() == e.getValue()))
				{
					return(findSizedFont(f.getName(), text, maxw, maxh, f.getStyle()));
				}
			}
			
		}
		
		MainWin.debug("Failed to find a font");
		return Display.getCurrent().getSystemFont();
		
	}

	
	
	public static String listFonts()
	{
		String res = "";
		
		 FontData[] fd = Display.getDefault().getFontList(null, true);
		 
		 for (FontData f : fd)
			{
				res += f.getName() + " gh:" + f.getHeight() + " st: " + f.getStyle() + " ht: " + f.height + "\n";
		
			}
		
		return res;
	}

	
	
	
	
	
	public static void simpleConfigServer(int rate, String devname, String device, boolean usemidi, String printertype, String printerdir) throws IOException, DWUIOperationFailedException 
	{
		// configure device
		
		ArrayList<String> cmds = new ArrayList<String>();
		
		cmds.add("dw config set DeviceType serial");
		cmds.add("dw config set SerialDevice " + device);
		cmds.add("dw config set SerialRate " + rate);
		cmds.add("dw config set [@name] "+ devname + " on " + device);
		cmds.add("dw config set [@desc] Autocreated " +  new java.sql.Timestamp(Calendar.getInstance().getTime().getTime()).toString() );
		
		cmds.add("dw config set UseMIDI " + usemidi);
		
		
		for (int i = 0;i<=MainWin.getInstanceConfig().getMaxIndex("Printer");i++)
		{
			if (MainWin.getInstanceConfig().getString("Printer("+i+")[@name]").equals(printertype))
				cmds.add("dw config set CurrentPrinter " + printertype);
			
			if (MainWin.getInstanceConfig().getString("Printer("+i+")[@name]").equals("Text"))
				cmds.add("dw config set Printer("+i+").OutputDir " + printerdir);
			
			if (MainWin.getInstanceConfig().getString("Printer("+i+")[@name]").equals("FX80"))
				cmds.add("dw config set Printer("+i+").OutputDir " + printerdir);
		}
	
		
		int tid = MainWin.taskman.addTask("Configure server for " + devname + " on " + device);
		String res = "";
		
			
		try
		{
			for (String cmd : cmds)
			{	
				res += "Sending command: " + cmd; 
				MainWin.taskman.updateTask(tid, UITaskMaster.TASK_STATUS_ACTIVE, res);
			
				UIUtils.loadList(MainWin.getInstance(), cmd);
				res += "\tOK\n";
			}
			
			res += "\nRestarting device handler...";
			MainWin.taskman.updateTask(tid, UITaskMaster.TASK_STATUS_ACTIVE, res);
			UIUtils.loadList(MainWin.getInstance(), "ui instance reset protodev");
			res += "\tOK\n";
			MainWin.taskman.updateTask(tid,UITaskMaster.TASK_STATUS_COMPLETE, res);
			
			
		} 
		catch (IOException e)
		{
			res += "\tFAIL\n\n" + e.getMessage();
			
			MainWin.taskman.updateTask(tid,UITaskMaster.TASK_STATUS_FAILED, res);
			throw  new IOException(e);
			
		} 
		catch (DWUIOperationFailedException e)
		{
			res += "\tFAIL\n\n" + e.getMessage();
			
			MainWin.taskman.updateTask(tid,UITaskMaster.TASK_STATUS_FAILED, res);
			
			throw new DWUIOperationFailedException(e.getMessage());
		}
		
		
		
	}


	public static String dumpMIDIStatus(MIDIStatus midiStatus)
	{
		String res = "";
		
		res += "curdev: " + midiStatus.getCurrentDevice() + "\n";
		res += "curprof: " + midiStatus.getCurrentProfile() + "\n";
		for (String s : midiStatus.getProfiles())
		{
			res += "profile: " + s + "\n";
		}
		
		return res;
	}

	
	public static void saveLogItemsToFile()
	{
		Runnable doit = new Runnable() {

			@Override
			public void run()
			{
				String fn = MainWin.getFile(true, false, "", "Save log to...", "Save");
				
				if (fn != null)
				{
					try
					{
						FileWriter fstream = new FileWriter(fn);
					
						BufferedWriter out = new BufferedWriter(fstream);
						
						synchronized(MainWin.logItems)
						{
							for (LogItem li : MainWin.logItems)
							{
								out.write(li.toString() + System.getProperty("line.separator"));
							}
						}
						
						out.close();
					  
					}
					catch (Exception e)
					{
						MainWin.showError("Error saving log items", e.getClass().getSimpleName() + ": " + e.getMessage(), UIUtils.getStackTrace(e), false);
					}
					  
				}
			}
		
		};
		
		Thread t = new Thread(doit);
		t.start();
	}


	public static boolean hasArg(String[] args, String arg)
	{
		for (String a:args)
		{
			if (a.endsWith("-" + arg))
				return true;
		}
		
		return false;
	}
	
	
	
}
