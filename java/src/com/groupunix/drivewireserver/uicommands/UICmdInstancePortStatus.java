package com.groupunix.drivewireserver.uicommands;

import com.groupunix.drivewireserver.DWUIClientThread;
import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;
import com.groupunix.drivewireserver.dwexceptions.DWConnectionNotValidException;
import com.groupunix.drivewireserver.dwexceptions.DWPortNotValidException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;
import com.groupunix.drivewireserver.dwprotocolhandler.DWUtils;
import com.groupunix.drivewireserver.virtualserial.DWVSerialPorts;

public class UICmdInstancePortStatus extends DWCommand {

	private DWUIClientThread dwuithread;

	public UICmdInstancePortStatus(DWUIClientThread dwuiClientThread) 
	{
		this.dwuithread = dwuiClientThread;
	}


	@Override
	public String getCommand() 
	{
		return "portstatus";
	}


	@Override
	public String getShortHelp() {
		return "show port status";
	}

	@Override
	public String getUsage() {
		return "ui instance portstatus";
	}

	@Override
	public DWCommandResponse parse(String cmdline) 
	{
		String res = "";
		
		if (this.dwuithread.getInstance() > -1)
		{
			DWProtocolHandler dwProto = (DWProtocolHandler)DriveWireServer.getHandler(this.dwuithread.getInstance());
			
			if (!(dwProto == null) && !(dwProto.getVPorts() == null) )
			{
				dwProto.getVPorts();
				
				for (int p = 0;p < DWVSerialPorts.MAX_PORTS;p++)
				{
					if (!dwProto.getVPorts().isNull(p))
					{
						try
						{
							res += dwProto.getVPorts().prettyPort(p) + "|";
							
							if (dwProto.getVPorts().isOpen(p))
							{
								res += "open|";
								
								res += dwProto.getVPorts().getOpen(p) + "|";
								
								res += dwProto.getVPorts().getUtilMode(p) + "|";
								
								res += DWUtils.prettyUtilMode(dwProto.getVPorts().getUtilMode(p)) + "|";
								
								res += dwProto.getVPorts().bytesWaiting(p)  + "|";
								
								res += dwProto.getVPorts().getConn(p) + "|";
								
								if (dwProto.getVPorts().getConn(p) > -1)
								{
									try
									{
										res += dwProto.getVPorts().getHostIP(p) + "|";
										res += dwProto.getVPorts().getHostPort(p) + "|";
										
									} 
									catch (DWConnectionNotValidException e)
									{
										res += "||";
									}
								}
								else
									res += "||";
								
								
								
								res += new String(dwProto.getVPorts().getDD(p)) + "|";
								
								
							}
							else
							{
								res += "closed|";
							}
						}
						catch (DWPortNotValidException e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						res += "\r\n";
					}
				}
			}
		}
		
		return(new DWCommandResponse(res));
	}

	public boolean validate(String cmdline) 
	{
		return(true);
	}
}
