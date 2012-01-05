package com.groupunix.drivewireui;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LogItem
{
	private final static SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss.SS");
	
	private String message;
	private long timestamp;
	private String thread;
	private String level;
	private String source;
	
	public void setMessage(String message)
	{
		this.message = message;
	}
	public String getMessage()
	{
		return message;
	}
	public void setTimestamp(long timestamp)
	{
		this.timestamp = timestamp;
	}
	public long getTimestamp()
	{
		return timestamp;
	}
	public void setThread(String thread)
	{
		this.thread = thread;
	}
	public String getThread()
	{
		return thread;
	}
	public void setLevel(String level)
	{
		this.level = level;
	}
	public String getLevel()
	{
		return level;
	}
	public void setSource(String source)
	{
		this.source = source;
	}
	public String getSource()
	{
		return source;
	}
	
	
	public LogItem clone()
	{
		LogItem res = new LogItem();
		
		res.setLevel(new String(level));
		res.setMessage(new String(message));
		res.setSource(new String(source));
		res.setThread(new String(thread));
		res.setTimestamp(timestamp);
	
		return res;
	}
	public String getShortTimestamp()
	{
		return format.format(new Date(this.timestamp));
	}
	
	public String getShortSource()
	{
		if ((this.source.lastIndexOf(".") > -1) && (this.source.lastIndexOf(".") < this.source.length()-2))
			return(this.source.substring(this.source.lastIndexOf(".")+1));
		return(this.source);
	}

}
