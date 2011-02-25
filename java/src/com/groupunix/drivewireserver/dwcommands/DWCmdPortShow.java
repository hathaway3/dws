package com.groupunix.drivewireserver.dwcommands;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwexceptions.DWPortNotValidException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;
import com.groupunix.drivewireserver.dwprotocolhandler.DWUtils;
import com.groupunix.drivewireserver.virtualserial.DWVSerialPorts;

public class DWCmdPortShow implements DWCommand {

	private DWProtocolHandler dwProto;

	public DWCmdPortShow(DWProtocolHandler dwProto)
	{
		this.dwProto = dwProto;
	}
	
	public String getCommand() 
	{
		return "show";
	}

	public String getLongHelp() 
	{
		return null;
	}

	
	public String getShortHelp() 
	{
		return "Show port status";
	}


	public String getUsage() 
	{
		return "dw port show";
	}

	public DWCommandResponse parse(String cmdline) 
	{
		return(doPortShow());
	}

	
	private DWCommandResponse doPortShow()
	{
		String text = new String();
		
		text += "\r\nCurrent port status:\r\n\n";
		
		for (int i = 0;i<DWVSerialPorts.MAX_PORTS;i++)
		{
			text += String.format("%6s", dwProto.getVPorts().prettyPort(i));
			
			try 
			{
				
			
				if (dwProto.getVPorts().isOpen(i))
				{
					text += String.format(" %-8s", "open(" + dwProto.getVPorts().getOpen(i) + ")");
				
					text += String.format(" %-10s", "PD.INT=" + dwProto.getVPorts().getPD_INT(i));
					text += String.format(" %-10s", "PD.QUT=" + dwProto.getVPorts().getPD_QUT(i));
					text += String.format(" %-15s", "buffer: " + dwProto.getVPorts().bytesWaiting(i));
				
				}
				else
				{
					text += String.format(" %-46s", "closed");
				}
			
			
				if (dwProto.getVPorts().getUtilMode(i) != DWDefs.UTILMODE_UNSET)
					text += " " + DWUtils.prettyUtilMode(dwProto.getVPorts().getUtilMode(i));
				
				//text += " " + DWProtocolHandler.byteArrayToHexString(DWVSerialPorts.getDD(i));	
			}
			catch (DWPortNotValidException e)
			{
				text += " Error: " + e.getMessage();
			} 

			
			text += "\r\n";
		}
		
		return(new DWCommandResponse(text));
		
	}
	
	public boolean validate(String cmdline) 
	{
		return(true);
	}
	
}
