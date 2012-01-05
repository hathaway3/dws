package com.groupunix.drivewireui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;

public class UITaskMaster
{
	public static final int TASK_STATUS_ACTIVE = 0;
	public static final int TASK_STATUS_COMPLETE = 1;
	public static final int TASK_STATUS_FAILED = 2;
	static Font taskFont;
	static Font versionFont;
	
	private Composite master;
	private List<UITask> tasks = new ArrayList<UITask>();
	
	public UITaskMaster(Composite master)
	{
		this.master = master;
		
		HashMap<String,Integer> fontmap = new HashMap<String,Integer>();
		
		fontmap.put("Droid Sans Mono", SWT.NORMAL);
		
		UITaskMaster.taskFont = UIUtils.findFont(master.getDisplay(), fontmap, "4.0.0", 36,17);
		
		fontmap.clear();
		fontmap.put("Roboto Cn", SWT.NORMAL);
		fontmap.put("Roboto", 3);
		fontmap.put("Roboto", 0);
		UITaskMaster.versionFont = UIUtils.findFont(master.getDisplay(), fontmap, "4.0.0", 24,15);
		
		
	}
	
	
	public int addTask(final String cmd)
	{
		
		master.getDisplay().syncExec(new Runnable() {
			  public void run()
			  {
				  
				  UITaskComposite tc;
				  UITask task;
				  
				  if (tasks.size() > MainWin.config.getInt("DWOpsHistorySize", 20))
				  {
					  tasks.remove(0);
				  }
				  
				  if (cmd.equals("/splash"))
				  {
					  tc = new UITaskCompositeSplash(master, SWT.DOUBLE_BUFFERED);
					
				  }
				  else if (cmd.equals("/wizard"))
				  {
					  tc = new UITaskCompositeWizard(master, SWT.DOUBLE_BUFFERED, tasks.size());
				  }
				  else
				  {
					  master.setRedraw(false);
					  tc =  new UITaskComposite(master, SWT.DOUBLE_BUFFERED);
					  // get some initial dimensions, maybe not needed..
					  tc.setBounds(0,tasks.size() * 40, master.getClientArea().width, 40);
					  
				  }
					
				  tc.setCommand(cmd);
				  
				  task = new UITask(tc);
				  
				  tasks.add(task);
				  master.setRedraw(true);
				  
				  
				  if (MainWin.tabFolderOutput.getSelectionIndex() != 0)
				  {
					  MainWin.tabFolderOutput.getItems()[0].setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/menu/active.png"));
				  }
			  }
		});
		
		
		
		return(tasks.size()-1);
		
	}
	
	protected void resizeTasks()
	{
		int y = 0;
		
		for (UITask t : this.tasks)
		{
			t.setTop(y);
			y += t.getHeight();

			t.setBottom(y);
		}
		
		master.setBounds(0,0, master.getBounds().width, y);
		
		MainWin.scrolledComposite.setOrigin(0, y);
		
	}

	public UITask getTask(int tid)
	{
		if (tid < this.tasks.size())
			return(this.tasks.get(tid));
		else
		{
			MainWin.debug("invalid task id : " + tid);
			return(null);
		}
	}
	
	public void updateTask(final int tid, final int status, final String txt)
	{
		master.getDisplay().syncExec(new Runnable() {
			  public void run()
			  {
				  if (tid > -0)
					  master.setRedraw(false);
				  tasks.get(tid).setText(txt);
				  tasks.get(tid).setStatus(status);
				  
				  resizeTasks(); 
				  master.setRedraw(true);
				 
				  if (MainWin.tabFolderOutput.getSelectionIndex() != 0)
				  {
					  MainWin.tabFolderOutput.getItems()[0].setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/menu/active.png"));
				  }
			  }
		});
	}

	public int getNumTasks()
	{
		return this.tasks.size();
	}


	public static Font taskFont()
	{
		// TODO Auto-generated method stub
		return taskFont;
	}


	public void removeTask(int tid)
	{
		MainWin.shell.setRedraw(false);
		
		if ((tid < this.tasks.size()) && (this.tasks.get(tid) != null))
		{
			if (this.tasks.get(tid).getTaskcomp() != null)
			{
				this.tasks.get(tid).getTaskcomp().dispose();
			}
			
			this.tasks.remove(tid);
			
			this.resizeTasks();
		}
		
		MainWin.shell.setRedraw(true);
		
	}


	public void rotateWaiters()
	{
		for (UITask t : this.tasks)
		{
			if (t.getStatus() == UITaskMaster.TASK_STATUS_ACTIVE)
			{
				t.rotateActive();
			}
		}
	}
	
	
	public Composite getMaster()
	{
		return this.master;
	}
	
}
