package com.groupunix.drivewireui;


public class UITask
{
	private int status = 0;
	private UITaskComposite taskcomp;
	private String text = "";

	
	public UITask(UITaskComposite tc)
	{
		this.taskcomp = tc;
	}
	
	public void setStatus(int status)
	{
		this.status = status;
		this.taskcomp.setStatus(status);
	}

	public int getStatus()
	{
		return status;
	}

	public void setText(String text)
	{
		this.text = text;
		this.taskcomp.setDetails(text);
	}

	public String getText()
	{
		return text;
	}

	public void setTaskcomp(UITaskComposite taskcomp)
	{
		this.taskcomp = taskcomp;
	}

	public UITaskComposite getTaskcomp()
	{
		return taskcomp;
	}
	
	public int getHeight()
	{
		return this.taskcomp.getHeight();
	}

	public void setTop(int y)
	{
		this.taskcomp.setTop(y);
	}

	public void setBottom(int y)
	{
		this.taskcomp.setBottom(y);
	}

	public void rotateActive()
	{
		if ((this.taskcomp != null) && (!this.taskcomp.isDisposed()))
				this.taskcomp.rotateActive();
	}
	
}
