package com.groupunix.drivewireserver.virtualserial;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.VFS;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.jasypt.util.password.BasicPasswordEncryptor;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DWUILogAppender;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwcommands.DWCmdConfig;
import com.groupunix.drivewireserver.dwcommands.DWCmdDisk;
import com.groupunix.drivewireserver.dwcommands.DWCmdLog;
import com.groupunix.drivewireserver.dwcommands.DWCmdMidi;
import com.groupunix.drivewireserver.dwcommands.DWCmdNet;
import com.groupunix.drivewireserver.dwcommands.DWCmdPort;
import com.groupunix.drivewireserver.dwcommands.DWCmdServer;
import com.groupunix.drivewireserver.dwcommands.DWCommandList;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;
import com.groupunix.drivewireserver.dwexceptions.DWPortNotValidException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWUtils;

public class DWUtilUIThread implements Runnable 
{

	private static final Logger logger = Logger.getLogger("DWServer.DWUtilUIThread");
	
	private static PatternLayout logLayout = new PatternLayout("%d{dd MMM yyyy HH:mm:ss} %-5p [%-14t] %26.26C: %m%n");
	
	
	private int vport = -1;
	private String strargs = null;
	private int handlerno;
	
	
	public DWUtilUIThread(int handlerno, int vport, String args)
	{
		this.vport = vport;
		this.strargs = args;
		this.handlerno = handlerno;
		
		logger.debug("init ui util thread");	
	}
	
	
	
	public void run() 
	{
		
		Thread.currentThread().setName("uiutil-" + Thread.currentThread().getId());
		Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
		
		logger.debug("run for handler #" + handlerno);
		
		if (this.strargs.equalsIgnoreCase("ui logview"))
		{
			logger.debug("adding log watch appender");
			DWUILogAppender logAppender = new DWUILogAppender(logLayout, DriveWireServer.getHandler(this.handlerno).getVPorts().getPortInput(this.vport));
			Logger.getRootLogger().addAppender(logAppender);
			
			while(DriveWireServer.getHandler(handlerno).getVPorts().isOpen(this.vport))
			{
				// wait for window to be closed... ?
				
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			logger.debug("removing log watch appender");
			Logger.getRootLogger().removeAppender(logAppender);
		}
		else if (this.strargs.equalsIgnoreCase("ui list disksets"))
		{
			List<HierarchicalConfiguration> disksets = DriveWireServer.serverconfig.configurationsAt("diskset");
	    	
			int tmp = 0;
			
			try 
		    {
				DriveWireServer.getHandler(this.handlerno).getVPorts().getPortInput(this.vport).write(("\n" + (char) 0 + "\n").getBytes());
				
			
				for(Iterator<HierarchicalConfiguration> it = disksets.iterator(); it.hasNext();)
				{
					HierarchicalConfiguration dset = (HierarchicalConfiguration) it.next();
			    
			    	DriveWireServer.getHandler(this.handlerno).getVPorts().getPortInput(this.vport).write((dset.getString("Name","unnamed-" + tmp) + "\n").getBytes());
			    	tmp++;
				} 
			    
				DriveWireServer.getHandler(this.handlerno).getVPorts().getPortInput(this.vport).write(((char) 0 + "\n").getBytes());
				
			    
		    }
			catch (IOException e) 
		    {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		
		}
		else if (this.strargs.equalsIgnoreCase("ui list synthprofiles"))
		{
			
			try 
		    {
				DriveWireServer.getHandler(this.handlerno).getVPorts().getPortInput(this.vport).write(("\n" + (char) 0 + "\n").getBytes());
				
				
				List<HierarchicalConfiguration> profiles = DriveWireServer.serverconfig.configurationsAt("midisynthprofile");
		    	
				for(Iterator<HierarchicalConfiguration> it = profiles.iterator(); it.hasNext();)
				{
					
				    HierarchicalConfiguration mprof = (HierarchicalConfiguration) it.next();
				    DriveWireServer.getHandler(this.handlerno).getVPorts().getPortInput(this.vport).write((mprof.getString("name") + ((char) 0) + mprof.getString("desc") + "\n").getBytes());
			    	
				    
				}
			
				DriveWireServer.getHandler(this.handlerno).getVPorts().getPortInput(this.vport).write(((char) 0 + "\n").getBytes());
				
			    
		    }
			catch (IOException e) 
		    {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		
		}
		else if (this.strargs.equalsIgnoreCase("ui list midioutdevs"))
		{
			
			try 
		    {
				DriveWireServer.getHandler(this.handlerno).getVPorts().getPortInput(this.vport).write(("\n" + (char) 0 + "\n").getBytes());
				
				MidiDevice device;
				MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
			
				for (int i = 0; i < infos.length; i++) 
				{
					try 
					{
						device = MidiSystem.getMidiDevice(infos[i]);
						String line = i + "" + ((char) 0) + device.getDeviceInfo().getName()+ " (" + device.getClass().getSimpleName() + ")\n";
						DriveWireServer.getHandler(this.handlerno).getVPorts().getPortInput(this.vport).write(line.getBytes());
					} 
					catch (MidiUnavailableException e) 
					{
						logger.warn("MIDI unavailable during UI device listing");
					}
			    
				}
				
				
				DriveWireServer.getHandler(this.handlerno).getVPorts().getPortInput(this.vport).write(((char) 0 + "\n").getBytes());
				
			    
		    }
			catch (IOException e) 
		    {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		
		}
		else if (this.strargs.equalsIgnoreCase("ui list disks"))
		{
			
			try 
		    {
				String path = DriveWireServer.getHandler(this.handlerno).config.getString("DiskDir","");
				
				DriveWireServer.getHandler(this.handlerno).getVPorts().getPortInput(this.vport).write(("\n" + (char) 0 + "\n").getBytes());
			
				FileSystemManager fsManager;
				
				fsManager = VFS.getManager();
				
				FileObject dirobj = fsManager.resolveFile(path);
				
				FileObject[] children = dirobj.getChildren();
		    	
		    	for (int i=0; i<children.length; i++) 
		    	{
		    		if (children[i].getType() == FileType.FILE)
		    			DriveWireServer.getHandler(this.handlerno).getVPorts().getPortInput(this.vport).write((children[i].getName() + "\n").getBytes());
		    	}
				
				DriveWireServer.getHandler(this.handlerno).getVPorts().getPortInput(this.vport).write(((char) 0 + "\n").getBytes());
				
			    
		    }
			catch (IOException e) 
		    {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		
		}
		
		
		
		/*
		if (DriveWireServer.getHandler(handlerno).getVPorts().isOpen(this.vport))
		{
		
			try 
			{
				DriveWireServer.getHandler(handlerno).getVPorts().closePort(this.vport);
			}	 
			catch (DWPortNotValidException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		*/
		
		logger.debug("exiting");
		
	}

	
	
	
}
