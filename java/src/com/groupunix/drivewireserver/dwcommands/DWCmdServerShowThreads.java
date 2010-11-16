package com.groupunix.drivewireserver.dwcommands;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

public class DWCmdServerShowThreads implements DWCommand {

	public String getCommand() 
	{
		return "threads";
	}

	public String getLongHelp() 
	{
		// TODO Auto-generated method stub
		return null;
	}

	
	public String getShortHelp() 
	{
		return "Show server threads";
	}


	public String getUsage() 
	{
		return "dw server show threads";
	}

	public DWCommandResponse parse(String cmdline) 
	{
		String text = new String();
		
		text += "\r\nDriveWire Server Threads:\r\n\n";

		Thread[] threads = getAllThreads();
		
		for (int i = 0;i<threads.length;i++)
		{
			if (threads[i] != null)
			{
				text += String.format("%40s %3d %-8s %-14s",threads[i].getName(),threads[i].getPriority(),threads[i].getThreadGroup().getName(), threads[i].getState().toString()) + "\r\n";

			}
		}
		
		return(new DWCommandResponse(text));
	}

	private ThreadGroup getRootThreadGroup( ) {

	    ThreadGroup tg = Thread.currentThread( ).getThreadGroup( );
	    ThreadGroup ptg;
	    while ( (ptg = tg.getParent( )) != null )
	        tg = ptg;
	    return tg;
	}
	
	private Thread[] getAllThreads( ) {
	    final ThreadGroup root = getRootThreadGroup( );
	    final ThreadMXBean thbean = ManagementFactory.getThreadMXBean( );
	    int nAlloc = thbean.getThreadCount( );
	    int n = 0;
	    Thread[] threads;
	    do {
	        nAlloc *= 2;
	        threads = new Thread[ nAlloc ];
	        n = root.enumerate( threads, true );
	    } while ( n == nAlloc );
            Thread[] copy = new Thread[threads.length];
            System.arraycopy(threads, 0, copy, 0, threads.length);
	    return copy;
	}
}
