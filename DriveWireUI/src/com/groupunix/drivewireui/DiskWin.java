package com.groupunix.drivewireui;




import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.swtdesigner.SWTResourceManager;



public class DiskWin {

	
	
	public static final int DGRAPH_WIDTH = 410;
	public static final int DGRAPH_HEIGHT = 52;
	
	DiskDef currentDisk;
	
	protected Shell shlDwDrive;

	private Display display;
	
	private Composite compositeDisk;
	
	public static Color colorWhite;
	public static Color colorRed;
	public static Color colorGreen;
	public static Color colorBlack;
	public static Color colorDiskBG;
	public static Color colorDiskFG;
	public static Color colorDiskGraphBG;
	public static Color colorDiskGraphLSN;
	public static Color colorDiskClean;
	public static Color colorDiskDirty;
	public static Color colorShadow;
	
	protected static Font fontDiskNumber;
	protected static Font fontDiskGraph;
	
	private Boolean driveactivity = false;
	
	private Combo comboDiskPath;
	protected Vector<DiskStatusItem> diskStatusItems;
	private Label diskStatusLED;
	
	private Canvas diskGraph;
	private List<String> displayUpdateItems = Arrays.asList("_path", "syncto", "syncfrom", "writeprotect", "expand", "sizelimt", "offset", "_last_modified", "*insert", "*eject");
	private int initx;
	private int inity;
	
	protected static Image background;
	
	
	public DiskWin(DiskDef cdisk, int x, int y)
	{
		this.currentDisk = cdisk;

		this.initx = x;
		this.inity = y;
		
		colorWhite = new Color(display, 255,255,255);
		colorRed = new Color(display, 255,0,0);
		colorGreen = new Color(display, 0,255,0);
		colorBlack = new Color(display, 0,0,0);
		colorDiskBG = new Color(display, 0x49,0x48,0x48);
		colorDiskFG = new Color(display, 0xb5,0xb5,0xb5);
		colorDiskGraphBG = new Color(display, 0x89,0x89,0x89);
		colorDiskGraphLSN = new Color(display, 0x2d, 0x2d, 0x2d);
		colorDiskDirty = new Color(display, 255,0,0);
		colorDiskClean = new Color(display, 150,0,0);
		colorShadow = new Color(display, 0x31,0x31,0x31);
		
		fontDiskNumber = UIUtils.findSizedFont("Liberation Sans","255",90,50,SWT.BOLD);
		
		
	
		fontDiskGraph = UIUtils.findSizedFont("Liberation Sans Narrow","Aaron",29,17,SWT.NORMAL);
			
		background = org.eclipse.wb.swt.SWTResourceManager.getImage(DiskWin.class, "/disk/driveinfo_bg.png");
		
			
	}



	
	/**
	 * @wbp.parser.entryPoint
	 */
	
	public void open(final Display display) 
	{
		
		this.display = display;
		createContents();
		
		createDiskStatusItems();
		
		shlDwDrive.setLocation(this.initx, this.inity);
		
		shlDwDrive.open();
		shlDwDrive.layout();

		
		// drive light
		
		 Runnable drivelightoff = new Runnable() 
		 	{
		      public void run() 
		      {
		    	 if (!shlDwDrive.isDisposed())
		    	 {
			    	  synchronized(driveactivity)
			    	  {
			    		  if (driveactivity.booleanValue() == true)
			    		  {
			    			  getDiskStatusLED().setImage(MainWin.diskBigLEDdark);
			    			  
			    		  }
			    		  
			    		  driveactivity = false;
			    	  }
				  	  	
			    	  display.timerExec(1000, this);
			      }
		      }
		    };
		    
	    display.timerExec(1000, drivelightoff);
		
		
		while (!shlDwDrive.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		
	}

	private void createDiskStatusItems()
	{
		
		this.diskStatusLED = new Label(this.getCompositeDisk(), SWT.NONE);
		getDiskStatusLED().setBackground(DiskWin.colorDiskBG);
		getDiskStatusLED().setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/disk/diskdrive-leddark-big.png"));
		getDiskStatusLED().setLocation(25, 220);
		getDiskStatusLED().setSize(41, 30);
		
		this.diskStatusItems = new Vector<DiskStatusItem>();
		
		this.diskStatusItems.add(new DiskStatusItemSyncTo(this));
		this.diskStatusItems.add(new DiskStatusItemSyncFrom(this));
		this.diskStatusItems.add(new DiskStatusItemReload(this));
		this.diskStatusItems.add(new DiskStatusItemExport(this));
		this.diskStatusItems.add(new DiskStatusItemEject(this));
		this.diskStatusItems.add(new DiskStatusItemWriteProtect(this));
		this.diskStatusItems.add(new DiskStatusItemOffset(this));
		this.diskStatusItems.add(new DiskStatusItemExpand(this));
		this.diskStatusItems.add(new DiskStatusItemLimit(this));
		this.diskStatusItems.add(new DiskStatusItemParams(this));
		
		for (DiskStatusItem dsi : this.diskStatusItems)
		{
			dsi.createCanvas(this.getCompositeDisk());
			
		}
		
		this.diskGraph = new Canvas(this.getCompositeDisk(), SWT.NONE);
		getDiskGraph().setLocation(20, 284);
		getDiskGraph().setSize(410, 52);
		getDiskGraph().setBackground(DiskWin.colorDiskBG);
		
		
		getDiskGraph().addPaintListener(new PaintListener()
		{
		   public void paintControl(final PaintEvent event)
		   {
			 
			   
			   if (currentDisk.isLoaded())
			   {
				   if (diskGraph.getData("buff") == null)
				   {	
					   
					   diskGraph.setData("buff", currentDisk.getDiskGraph());
				   }
				   
				   event.gc.drawImage((Image) diskGraph.getData("buff"),0,0);
	
				   
				   if (currentDisk.isGraphchanged())
				   display.asyncExec(new Runnable() {
		    			
						@Override
						public void run() 
						{
							if (!getDiskGraph().isDisposed())
							{
								getDiskGraph().setData("buff", currentDisk.getDiskGraph());
								if (!currentDisk.isGraphchanged())
									getDiskGraph().redraw();
							}
						}
		    			
		    		});
				   
				   
				   
				   
			   }
			   else
			   {
				  event.gc.setBackground(DiskWin.colorDiskBG);
				  event.gc.fillRectangle(0,0, DiskWin.DGRAPH_WIDTH, 52);
			   }
		   
		   }
		 });
	}
	
	
	
	



	
	protected void createContents() 
	{
		shlDwDrive = new Shell();
		//shlDwDrive.setMinimumSize(new Point(465, 430));
		

		//shlDwDrive.setSize(448, 391);
		shlDwDrive.setImage(SWTResourceManager.getImage(DiskWin.class, "/dw/dw4square.jpg"));
		shlDwDrive.addShellListener(new ShellAdapter() {
			@Override
			public void shellClosed(ShellEvent e) 
			{
				MainWin.config.setProperty("DiskWin_"+ currentDisk.getDriveNo() +"_x",  shlDwDrive.getLocation().x);
				MainWin.config.setProperty("DiskWin_"+ currentDisk.getDriveNo() +"_y",  shlDwDrive.getLocation().y);
				MainWin.config.setProperty("DiskWin_"+ currentDisk.getDriveNo() +"_open",false);
			}
		});
		
		
		updateTitlebar();
		
		shlDwDrive.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		shlDwDrive.setMinimumSize(background.getImageData().width, background.getImageData().height);
		
		setCompositeDisk(new Composite(shlDwDrive, SWT.NONE));
			
		
			
		Label spacer = new Label(getCompositeDisk(), SWT.NONE);
		
		spacer.setBounds(background.getImageData().width, background.getImageData().height,0,0);
			
			
			getCompositeDisk().addPaintListener(new PaintListener() {
				public void paintControl(PaintEvent e) 
				{
				
					// background
					
					e.gc.drawImage(DiskWin.background, 0, 0);
					
					// disk #
					e.gc.setTextAntialias(SWT.ON);
					e.gc.setAntialias(SWT.ON);
					e.gc.setFont(DiskWin.fontDiskNumber); 
					
					if (currentDisk.isLoaded())
					{
						e.gc.setForeground(DiskWin.colorShadow); 
						e.gc.drawText(currentDisk.getDriveNo()+"",75,138);
					}
										
					e.gc.setForeground(DiskWin.colorDiskFG); 
					e.gc.drawText(currentDisk.getDriveNo()+"",72,135,true);
					
					
					
					if (diskStatusItems != null)
					{
						for (DiskStatusItem dsi : diskStatusItems)
						{
							
							dsi.setDisk(currentDisk);
							if (dsi.getCurrentImage() == null)
								dsi.setVisible(false);
							else
								dsi.setVisible(true);
							
							dsi.redraw();
						}
					}
					
					// disk status
					if (currentDisk.isLoaded())
					{
						comboDiskPath.setText(UIUtils.shortenLocalURI(currentDisk.getPath()));
						e.gc.drawImage(org.eclipse.wb.swt.SWTResourceManager.getImage(DiskWin.class, "/disk/disk_lever_inserted.png"), 320, 152);
					}
					else
					{
						comboDiskPath.setText("");
						e.gc.drawImage(org.eclipse.wb.swt.SWTResourceManager.getImage(DiskWin.class, "/disk/disk_lever_ejected.png"), 320, 152);
						
						if (diskStatusItems != null)
						{
							for (DiskStatusItem dsi : diskStatusItems)
							{
		
								dsi.setDisk(null);
							}
						}
					}
					
					
					
				}
			});
			getCompositeDisk().setBackground(colorDiskBG);
			getCompositeDisk().setLayout(null);
			
			comboDiskPath = new Combo(getCompositeDisk(), SWT.NONE);
			comboDiskPath.setBounds(25, 25, 365, 23);
			
			Button btnDiskFile = new Button(getCompositeDisk(), SWT.FLAT | SWT.CENTER);
			btnDiskFile.setBounds(396, 24, 30, 26);
			btnDiskFile.setBackground(DiskWin.colorDiskBG);
			btnDiskFile.setText("...");
			
			
			shlDwDrive.pack();
	}

	
	
	


	
	

	
	void updateTitlebar()
	{
		final String title;
		
		if (currentDisk.isLoaded())
			title = "DW4 / Drive " + this.currentDisk.getDriveNo() + " / " + this.currentDisk.getFileName();
		else
			title = "DW4 / Drive " + this.currentDisk.getDriveNo() + " / " + "No Disk";
		
		display.asyncExec(
				  new Runnable() {
					  public void run()
					  {
						  shlDwDrive.setText(title);
					  }
				  });
	}








	


	public Label getDiskStatusLED()
	{
		return diskStatusLED;
	}





	public Canvas getDiskGraph()
	{
		return diskGraph;
	}




	public void setCompositeDisk(Composite compositeDisk)
	{
		this.compositeDisk = compositeDisk;
	}




	public Composite getCompositeDisk()
	{
		return compositeDisk;
	}




	public void refreshdisplay()
	{
		this.compositeDisk.redraw();
		this.updateTitlebar();
		this.diskGraph.redraw();
	}




	public void submitEvent(String key, String val)
	{
		// special cases for update display
		if (this.displayUpdateItems.contains(key))
		{
			this.refreshdisplay();
		}
		
		// special cases for drive light..
		if (key.equals("_reads") && !val.equals("0"))
		{
			this.diskStatusLED.setImage(MainWin.diskBigLEDgreen);
			this.driveactivity = true;
			this.diskGraph.redraw();
		}
		else if (key.equals("_writes") && !val.equals("0"))
		{
			this.diskStatusLED.setImage(MainWin.diskBigLEDred);
			this.driveactivity = true;
			this.diskGraph.redraw();
		}
	
		
	}


	

	public int getLocX()
	{
		return(this.shlDwDrive.getLocation().x);
	}

	public int getLocY()
	{
		return(this.shlDwDrive.getLocation().y);
	}




	public void close()
	{
		display.syncExec(
				  new Runnable() {
					  public void run()
					  {
						  shlDwDrive.close();
					  }
				  });
	}
	
	
	
	/*
	public static void updateDiskPathDropdown()
	{
		// update disk path dropdown
		//MainWin.textDiskURI.removeAll();
		List<String> fh = DiskWin.getDiskHistory();
			
		if (fh != null)
		{
			for (int i = fh.size() - 1;i > -1;i--)
			{
				//MainWin.textDiskURI.add(fh.get(i));
			}
		
		}

	}
	
	public  void displayCurrentDisk(boolean update) 
	{
		if ((currentDisk > -1) && (diskGraph != null))
		{
			diskGraph.redraw(0, 0, DiskWin.DGRAPH_WIDTH, DiskWin.DGRAPH_HEIGHT, true);
		}
		
		compositeDisk.redraw();
		
	}

	
	*/
	





	
	
	
}