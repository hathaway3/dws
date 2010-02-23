package com.groupunix.drivewireserver.dwprotocolhandler;

import org.apache.log4j.Logger;

public class DWRFMPath
{
	private static final Logger logger = Logger.getLogger("DWServer.DWRFMPath");
		
	private int pathno;
	private String pathstr;
	
	public DWRFMPath(int pathno)
	{
		this.setPathno(pathno);
		logger.debug("new path " + pathno);
		
	}

	public void setPathno(int pathno)
	{
		this.pathno = pathno;
	}

	public int getPathno()
	{
		return pathno;
	}

	public void setPathstr(String pathstr)
	{
		this.pathstr = pathstr;
	}

	public String getPathstr()
	{
		return pathstr;
	}

	public void close()
	{
		logger.debug("closing path " + this.pathno + " to " + this.pathstr);
	}
	
	
	
}

