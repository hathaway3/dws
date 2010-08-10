package com.groupunix.drivewireserver.dwcommands;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.dwprotocolhandler.DWUtils;


public class DWCommandList {

	private List<DWCommand> commands = new ArrayList<DWCommand>();
	
	public void addcommand(DWCommand dwCommand) 
	{
		commands.add(dwCommand);
	}

	
	
	public DWCommandResponse parse(String cmdline) 
	{
		String[] args = cmdline.split(" ");
		
		if (cmdline.length() == 0)
		{
			// implied 'help'
			return(new DWCommandResponse(genHelp()));
		}
		
		int matches = numCommandMatches(args[0]);
		
		if (matches == 0)
		{
			return(new DWCommandResponse(false,DWDefs.RC_SYNTAX_ERROR,"Unknown command '" + args[0] + "'"));
		}
		else if (matches > 1)
		{
			return(new DWCommandResponse(false,DWDefs.RC_SYNTAX_ERROR,"Ambiguous command, '" + args[0] + "' matches " + getTextMatches(args[0])));
		}
		else
		{
			return(getCommandMatch(args[0]).parse(DWUtils.dropFirstToken(cmdline)));
		}
		 
	}

	
	
	private String genHelp() 
	{
		String txt = new String();
		
		txt = "\r\nCommands available:\r\n\n";
		
		for (Iterator<DWCommand> it = this.commands.iterator(); it.hasNext(); )
		{
			DWCommand cmd = it.next();
			txt = txt + String.format("  %-31s - %-31s\r\n", cmd.getUsage(), cmd.getShortHelp() );
		}
		
		return(txt);
	}



	private String getTextMatches(String arg) 
	{
		String matchtxt = new String();
		
		for (Iterator<DWCommand> it = this.commands.iterator(); it.hasNext(); )
		{
			DWCommand cmd = it.next();
			
			if (cmd.getCommand().startsWith(arg.toLowerCase()))
			{
				if (matchtxt.length() == 0)
				{
					matchtxt = matchtxt + cmd.getCommand();
				}
				else
				{
					matchtxt = matchtxt + " or " + cmd.getCommand();
				}
			}
		}
		
		return matchtxt;
	}

	private int numCommandMatches(String arg) 
	{
		int matches = 0;
		
		for (Iterator<DWCommand> it = this.commands.iterator(); it.hasNext(); )
		{
			if (it.next().getCommand().startsWith(arg.toLowerCase()))
			{
				matches++;
			}
		}
		
		return matches;
	}

	
	private DWCommand getCommandMatch(String arg) 
	{
		DWCommand cmd;
		
		for (Iterator<DWCommand> it = this.commands.iterator(); it.hasNext(); )
		{
			cmd = it.next();
			
			if (cmd.getCommand().startsWith(arg.toLowerCase()))
			{
				return(cmd);
			}
		}
		
		return null;
	}

	
	
}
