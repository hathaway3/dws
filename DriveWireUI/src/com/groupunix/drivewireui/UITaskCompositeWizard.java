package com.groupunix.drivewireui;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.DriveWireServer;

public class UITaskCompositeWizard extends UITaskComposite
{

	private Pattern unixstyle = Pattern.compile("/dev/.+");
	private Pattern winstyle = Pattern.compile("COM\\d+");
	
	private Font introFont;
	private Font smallFont;
	private Font testStatusFont;
	
	private int tid;
	
	private int state = 0;
	
	private int[] sashform_orig;
	
	private Integer cocomodel = 3;
	private String device = null;
	private boolean usemidi = false;
	private String printertype = "Text";
	private String printerdir = "cocoprints";
	private int fpgarate = 115200;
	
	private Table portlist = null;
	
	private boolean monitorPorts = false;
	private Boolean updatingPorts = false;
	
	private ArrayList<String> manualPorts = new ArrayList<String>();

	private int width = 488;
	private int height = 480;
	private int top = 0;
	
	private Composite master;
	private StyledText testStatusText;
	private Thread testT;
	private Button doit;
	private boolean intesting = false;
	protected String cocodevname;
	
	
	public UITaskCompositeWizard(final Composite master, int style, int tid)
	{
		super(master, style, tid);
		this.tid = tid;
		this.master = master;
		
		HashMap<String,Integer> fontmap = new HashMap<String,Integer>();
		
		fontmap.put("Droid Serif", SWT.NORMAL);
		this.introFont = UIUtils.findFont(master.getDisplay(), fontmap, "Welcome", 56,19);
		this.smallFont = UIUtils.findFont(master.getDisplay(), fontmap, "Welcome", 52,13);
		this.testStatusFont = UIUtils.findFont(master.getDisplay(), fontmap, "Welcome", 61,21);
		
		
		status.setData("survive","please");
		status.setVisible(false);
		
		portlist = new Table(this, SWT.FULL_SELECTION);
		portlist.setVisible(false);
		portlist.setData("survive","please");
		portlist.setHeaderVisible(false);
		portlist.setLinesVisible(false);
		
		TableColumn tcPort = new TableColumn(portlist, SWT.NONE);
		tcPort.setText("Port");
		tcPort.setResizable(true);
		tcPort.setWidth((width - 150)/2);
		
		TableColumn tcStatus = new TableColumn(portlist, SWT.NONE);
		tcStatus.setText("Status");
		tcStatus.setResizable(true);
		tcStatus.setWidth((width - 150)/2);
		
		
		
		
		
		 createContents(master);
	}
	
	
	private void createContents(Composite master)
	{
		// select ui page
		MainWin.selectUIPage();
				
		// wizard grows
		this.sashform_orig = MainWin.getSashformWeights();
		MainWin.setSashformWeights(new int[] { 0, sashform_orig[0] + sashform_orig[1]});
		
		this.addDisposeListener( new DisposeListener() {

			@Override
			public void widgetDisposed(DisposeEvent e)
			{
				monitorPorts = false;
				MainWin.setSashformWeights(sashform_orig);
			}
		
		});
		
		
		this.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		
		this.setBounds((MainWin.scrolledComposite.getBounds().width - width)/2  , top, width, height);
		
		
		
		drawControls();
		
		
	}
	
	
	private void drawControls()
	{
		// clean up
		
		this.setRedraw(false);
		
		this.monitorPorts = false;
		this.status.setVisible(false);
		this.portlist.setVisible(false);
		
		Control[] controls = this.getChildren();
		
		for (Control c : controls)
		{
			// only port table, status lives
			if (c.getData("survive") == null)
				c.dispose();
		}
		
		
		// intro
		if (state == 0)
		{
			drawInitControls();
		}
		// pick a coco
		else if (state == 1)
		{
			drawCoCoSelectControls();
		}
		else if (state == 2)
		{
			if (cocomodel != DWDefs.MODEL_EMULATOR)
				drawSerialSelectControls();
			else
				drawMIDISelectControls();
		}
		else if (state == 3)
		{
			drawMIDISelectControls();
		}
		else if (state == 4)
		{
			drawPrinterSelectControls();
		}
		else if (state == 5)
		{
			switch(cocomodel)
			{
				case DWDefs.MODEL_FPGA:
					drawFPGABaudChooseControls();
					break;
					
				case DWDefs.MODEL_EMULATOR:
				case DWDefs.MODEL_ATARI:
					applyConfig();
					drawWizardSuccessControls();
					break;
					
				default:
					drawCommTestChooseControls();
					break;
			}
		}
		else if (state == 6)
		{
			drawCommTest1Controls();
		}
		else if (state == 7)
		{
			drawCommTest2Controls();
			startCommTest();
		}
		else if (state == 9)
		{
			drawWizardSuccessControls();
		}
		
		this.setRedraw(true);
	}
	
	
	private void drawFPGABaudChooseControls()
	{

		int y = 20;
		
		Label cocoman3 = new Label(this, SWT.NONE);
		cocoman3.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/wizard/cocoman3.png"));
		cocoman3.setBounds(width/2 - 144, y, 288, 160);
		
		y += 180;
		
		StyledText intro = new StyledText(this, SWT.WRAP | SWT.MULTI | SWT.READ_ONLY);
		
		intro.setCursor(new org.eclipse.swt.graphics.Cursor(this.getDisplay(), SWT.CURSOR_ARROW));
		intro.setEditable(false);
		intro.setEnabled(false);
		
		intro.setBounds(0, y, width, 130);
		intro.setForeground(DiskWin.colorDiskBG);
		intro.setFont(this.introFont);
		intro.setText("We're almost finished!");
		intro.append(intro.getLineDelimiter() + intro.getLineDelimiter());
	
		intro.append("An " + this.cocodevname + " running CoCo3FPGA can communicate at several different speeds.  Please select the rate you wish to use with the " + this.cocodevname +" (you must also configure this rate on the " + this.cocodevname + " itself):"); 
		
		intro.setBounds(0, y, width, intro.getTextBounds(0, intro.getCharCount()-1).height) ;
		
		y += 40 + intro.getBounds().height;
		
		final Combo fpgaspeed = new Combo(this, SWT.READ_ONLY);
		fpgaspeed.setItems( new String[]{"115200", "230400", "460800", "921600"});
		fpgaspeed.select(0);
		
		fpgaspeed.setBounds(width/2 - 50 , y, 100, 40);
		
		//fpgaspeed.setBounds(width/2 - fpgaspeed.getBounds().width/2 , y, fpgaspeed.getBounds().width, fpgaspeed.getBounds().height);
		
		fpgaspeed.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e)
			{
				if (fpgaspeed.getSelectionIndex() > -1)
				{
					fpgarate = Integer.parseInt(fpgaspeed.getItem(fpgaspeed.getSelectionIndex()));
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
			
				
			}
			
		});
		
		
		
		final Button doit = new Button(this, SWT.NONE);
		doit.setText("Apply configuration..");
		
		doit.setBounds(0, height - 30, width/2 - 10, 24);
		doit.setEnabled(true);
		
		
		
		
		doit.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e)
			{
				applyConfig();
				
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				
			}
			
		});
		
		
		Button nothanks = new Button(this, SWT.NONE);
		nothanks.setText("Cancel wizard");
		nothanks.setBounds(width/2+9, height - 30, width - (width/2+9), 24);
		
		nothanks.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e)
			{

				state = -1;
				drawControls();
				MainWin.taskman.removeTask(tid);
				
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				
			}
			
		});
		
	}


	private void startCommTest()
	{
		stat = UITaskMaster.TASK_STATUS_ACTIVE;
		
		 Runnable testThread = new Runnable() 
		 {
			 boolean portopen = false;
			 int attempt = 0;
			 boolean wanttodie = false;
			 boolean success = false;
			 
		      public void run() 
		      {
		    	  // get port opened
		    	  while (!portopen && !isDisposed() && !wanttodie && (state == 7))
		    	  {
		    		  attempt++;
		    		  setTestStatusText("Trying to open " + device + "...  (Attempt #" + attempt + " of 10)");
		    		  
		    		  try
					  {
							portopen = DriveWireServer.testSerialPort_Open(device);
							 
							// set params
					    	int rate = 115200;
					    	
					    	if (cocomodel == DWDefs.MODEL_COCO1)
					    		 rate = 38400;
					    	else if (cocomodel == DWDefs.MODEL_COCO2)
					    	  	rate = 57600;
					    	
					    	
					    	  
					    	portopen = DriveWireServer.testSerialPort_setParams(rate);
					  } 
		    		  catch (Exception e1)
		    		  {
		    			  if (attempt < 9)
		    				  setTestStatusText("Failed to open " + device + ": " + e1.getMessage()+ "  (will retry)");
		    			  else
		    			  {
		    				  setTestStatusText("Failed to open " + device + ": " + e1.getMessage());
		    				  wanttodie = true;
		    			  }
		    		  }
		    		  
		    		  if ((!portopen) && (!wanttodie))
		    		  {
		    			try
						{
							Thread.sleep(3000);
						} 
		    			catch (InterruptedException e)
						{
							wanttodie = true;
						}
		    		  }
		    	  }
		    	  
		    	  
		    	  if (portopen && !isDisposed())
		    	  {
		    		  setTestStatusText(device + " open.  Turn on your CoCo now.");
		    	  
		    	 
		    	  
			    	  // watch for bytes..
			    	  try
					  {
			    		  int ticks = 0;
			    		  
			    		  while ((!wanttodie) && !isDisposed() && (state == 7))
			    		  {
			    			  int data = DriveWireServer.testSerialPort_read();
			    			  
			    			  switch(data)
			    			  {
			    			  	case -1:
			    			  		ticks++;
			    			  		appendTestStatusText(".");
			    			  		if (ticks == 33)
						    		{
			    			  			setTestStatusText("No data received after 100 seconds, giving up.");
				    			  		wanttodie = true;  
						    		}
			    			  		break;
			    			  	case 248:
			    			  	case 254:
			    			  	case 255:
			    			  		success = true;
			    			  		wanttodie = true;
			    			  		setTestStatusText("Success! Received the proper value!");
			    			  		break;
			    			  	default:
			    			  		setTestStatusText("Received strange value (" + data + ").");
			    			  		wanttodie = true;
			    			  }
			    		  }
			    		  
			    		 
						
					  } 
			    	  catch (Exception e)
					  {
			    		  setTestStatusText("Error while reading: " + e.getMessage());
					  }
			    	  
			    	  
			    	  
		    	  }
		    	  
		    	  if (portopen)
		    	  {
		    		  DriveWireServer.testSerialPort_close();
		    	  }
		    	  
		    	  // fail or not?
		    	  if (!isDisposed())
		    	  {
		    		  if (success)
		    		  {
		    			  // all good
		    			  getDisplay().asyncExec(new Runnable() {
		    					
		    			@Override
		    					
		    				public void run() 
		    					{
		    						doDisplayTestSuccess();
		    					}
		    			  });
		    		  }
		    	 	  else
		    	 	  {
		    	 		 getDisplay().asyncExec(new Runnable() {
		    					
				    			@Override
				    					
				    				public void run() 
				    					{
				    						doDisplayTestFailed();
				    					}
				    			  });
		    	 		
		    	 	  }
		      	  }
		      
		    	
		      }
		 };
		  
		
		testT = new Thread(testThread);
		testT.start();
		
	}

	protected void doDisplayTestFailed()
	{
		stat = UITaskMaster.TASK_STATUS_FAILED;
			
		StyledText done = new StyledText(this, SWT.WRAP | SWT.MULTI | SWT.READ_ONLY);
		
		done.setCursor(new org.eclipse.swt.graphics.Cursor(this.getDisplay(), SWT.CURSOR_ARROW));
		done.setEditable(false);
		done.setEnabled(false);
		
		int y = height - 300;
		
		done.setBounds(0, y, width, 40);
		done.setForeground(DiskWin.colorDiskBG);
		done.setFont(this.introFont);
		done.setText("We seem to have a problem.  Please check that your cable is tightly connected (and plugged into the correct port).  You also might need to correct one of your previous choices.");
		done.setBounds(0, y, width, done.getTextBounds(0, done.getCharCount() - 1).height);
		
		y += done.getBounds().height + 15;
		
		Button tryagain = new Button(this, SWT.NONE);
		tryagain.setText("Try the test again...");
		tryagain.setBounds(width/2-100,y,200,24);
		
		tryagain.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e)
			{
				drawControls();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				
			}
			
		});
		
		y+=35;
		
		Button chgport = new Button(this, SWT.NONE);
		chgport.setText("Change serial port...");
		chgport.setBounds(width/2-100,y,200,24);
		
		chgport.addSelectionListener(new SelectionListener() {

			

			@Override
			public void widgetSelected(SelectionEvent e)
			{
				intesting = true;
				state = 2;
				drawControls();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				
			}
			
		});
		
		y+=35;
		
		Button chgcoco = new Button(this, SWT.NONE);
		chgcoco.setText("Change CoCo model...");
		chgcoco.setBounds(width/2-100,y,200,24);
		
		chgcoco.addSelectionListener(new SelectionListener() {

			

			@Override
			public void widgetSelected(SelectionEvent e)
			{
				intesting = true;
				state = 1;
				drawControls();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				
			}
			
		});
		
	}


	protected void doDisplayTestSuccess()
	{
		stat = UITaskMaster.TASK_STATUS_COMPLETE;
		
		doit.setText("Apply configuration...");
		
		Label cocoman = new Label(this, SWT.NONE);
		cocoman.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/wizard/cocoman6.png"));
		cocoman.setBounds(width/2 - 171, height - 335 , 342, 231);
		
		StyledText done = new StyledText(this, SWT.WRAP | SWT.MULTI | SWT.READ_ONLY);
		
		done.setCursor(new org.eclipse.swt.graphics.Cursor(this.getDisplay(), SWT.CURSOR_ARROW));
		done.setEditable(false);
		done.setEnabled(false);
		
		done.setBounds(0, height - 90, width, 40);
		done.setForeground(DiskWin.colorDiskBG);
		done.setFont(this.introFont);
		done.setText("The connection appears to be working fine, so let's finish by applying this configuration!");

		
	}


	private void setTestStatusText(final String txt)
	{
		if (!this.isDisposed())
		this.getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() 
			{
				if (!(testStatusText == null) && !testStatusText.isDisposed())
					testStatusText.setText(txt);
			}
			
		});
		
		
	}
	
	
	private void appendTestStatusText(final String txt)
	{
		if (!this.isDisposed())
		this.getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() 
			{
				if (!(testStatusText == null) && !testStatusText.isDisposed())
					testStatusText.append(txt);
			}
			
		});
		
		
	}

	private void drawCommTest2Controls()
	{
		int y = 10;
		
		StyledText intro = new StyledText(this, SWT.WRAP | SWT.MULTI | SWT.READ_ONLY);
		
		intro.setCursor(new org.eclipse.swt.graphics.Cursor(this.getDisplay(), SWT.CURSOR_ARROW));
		intro.setEditable(false);
		intro.setEnabled(false);
		
		intro.setBounds(0, y, width, 130);
		intro.setForeground(DiskWin.colorDiskBG);
		intro.setFont(this.introFont);
		intro.setText("The wizard will now look for a special signal on " + this.device + ". This signal is generated by the CoCo every time it is powered on (or reset).");
		intro.setBounds(0, y, width, intro.getTextBounds(0, intro.getCharCount()-1 ).height);
		
		y += intro.getBounds().height + 25;
		
		
		testStatusText = new StyledText(this, SWT.WRAP | SWT.MULTI | SWT.READ_ONLY);
		
		testStatusText.setCursor(new org.eclipse.swt.graphics.Cursor(this.getDisplay(), SWT.CURSOR_ARROW));
		testStatusText.setEditable(false);
		testStatusText.setEnabled(false);
		
		testStatusText.setBounds(40, y, width-40, 60);
		testStatusText.setForeground(DiskWin.colorDiskBG);
		testStatusText.setFont(this.testStatusFont);
		testStatusText.setText("Waiting for test thread...");
		
		status.setVisible(true);
		status.setBounds(0, y, 32, 32);
		
		
		
		
				
		doit = new Button(this, SWT.NONE);
		doit.setText("Finish without test");
		
		doit.setBounds(0, height - 30, width/2 - 10, 24);
		doit.setEnabled(true);
		
		doit.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e)
			{
				applyConfig();
				
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				
			}
			
		});
		
		
		Button nothanks = new Button(this, SWT.NONE);
		nothanks.setText("Cancel wizard");
		nothanks.setBounds(width/2+9, height - 30, width - (width/2+9), 24);
		
		nothanks.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e)
			{

				state = -1;
				drawControls();
				MainWin.taskman.removeTask(tid);
				
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				
			}
			
		});
		
	}

	
	

	private void drawCommTest1Controls()
	{
		int y = 10;
		
		StyledText intro = new StyledText(this, SWT.WRAP | SWT.MULTI | SWT.READ_ONLY);
		
		intro.setCursor(new org.eclipse.swt.graphics.Cursor(this.getDisplay(),SWT.CURSOR_ARROW));
		intro.setEditable(false);
		intro.setEnabled(false);
		
		intro.setBounds(0, y, width, 130);
		intro.setForeground(DiskWin.colorDiskBG);
		intro.setFont(this.introFont);
		intro.setText("OK! Before we start the test, first please turn off your " + this.cocodevname +".");
		intro.append(intro.getLineDelimiter() + intro.getLineDelimiter());
		intro.append("Next, connect a serial cable from the CoCo's bitbanger port (the port labeled \"SERIAL I/O\") to this computer's " + this.device + " port.");
		intro.append(intro.getLineDelimiter() + intro.getLineDelimiter());
		intro.append("When you have the cable connected, click the button below:");
		
		intro.setBounds(0, y, width, intro.getTextBounds(0, intro.getCharCount()-1 ).height);
		
		y += intro.getBounds().height + 15;
		
		Button begin = new Button(this,SWT.NONE);
		
		begin.setText("Begin the test!");
		begin.setBounds(width/2 - 100,y,200,24);
		
		begin.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e)
			{
				state = 7;
				drawControls();
				
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				
			}
			
		});
		
		
		
		Label cocoman1 = new Label(this, SWT.NONE);
		cocoman1.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/wizard/cocoman7.png"));
		
		cocoman1.setBounds(width/2 - 356/2, height - 310,356,258);
		
		
		final Button doit = new Button(this, SWT.NONE);
		doit.setText("Finish without test");
		
		doit.setBounds(0, height - 30, width/2 - 10, 24);
		doit.setEnabled(true);
		
		
		
		
		doit.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e)
			{
				applyConfig();
				
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				
			}
			
		});
		
		
		Button nothanks = new Button(this, SWT.NONE);
		nothanks.setText("Cancel wizard");
		nothanks.setBounds(width/2+9, height - 30, width - (width/2+9), 24);
		
		nothanks.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e)
			{

				state = -1;
				drawControls();
				MainWin.taskman.removeTask(tid);
				
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				
			}
			
		});
		
	}


	private void drawPrinterSelectControls()
	{
		int y = 10;
		
		StyledText intro = new StyledText(this, SWT.WRAP | SWT.MULTI | SWT.READ_ONLY);
		
		intro.setCursor(new org.eclipse.swt.graphics.Cursor(this.getDisplay(), SWT.CURSOR_ARROW));
		intro.setEditable(false);
		intro.setEnabled(false);
		
		intro.setBounds(0, y, width, 130);
		intro.setForeground(DiskWin.colorDiskBG);
		intro.setFont(this.introFont);
		intro.setText("There are two different types of printer output available.  You can switch at any time, but DriveWire will default to the type you choose here.");
		intro.append(intro.getLineDelimiter() + intro.getLineDelimiter());
		intro.append("The 'text' option will produce files containing plain text.  No interpretation of the contents is performed.  These files can be read by any text editor. ");
		intro.append(intro.getLineDelimiter() + intro.getLineDelimiter());
		intro.append("The 'FX80' option will produce image files containing the output of a simulated Epson FX-80 printer.  FX-80 control codes are interpretted.  These files can be viewed with an image viewer.");
		
		intro.setBounds(0, y, width, intro.getTextBounds(0, intro.getCharCount()-1 ).height);
		
		y += intro.getBounds().height + 15;
		
		final Button text = new Button(this, SWT.RADIO);
		text.setText("Default to text printer");
		text.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		
		text.setBounds(width/2-200,y,200,24);
		
		final Button fx80 = new Button(this, SWT.RADIO);
		fx80.setText("Default to FX-80 printer");
		fx80.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		
		fx80.setBounds(width/2,y,200,24);
		
		text.setSelection(true);
		
		y+=60;
		
		Label cocoman3 = new Label(this, SWT.NONE);
		cocoman3.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/wizard/cocoman5.png"));
		cocoman3.setBounds(0, y, 192, 179);
		
		StyledText more1 = new StyledText(this, SWT.WRAP | SWT.MULTI | SWT.READ_ONLY);
		
		more1.setCursor(new org.eclipse.swt.graphics.Cursor(this.getDisplay(),SWT.CURSOR_ARROW));
		more1.setEditable(false);
		more1.setEnabled(false);
		
		more1.setBounds(210, y, width - 210, 130);
		more1.setForeground(DiskWin.colorDiskBG);
		more1.setFont(this.introFont);
		more1.setText("When the printer is flushed, DriveWire will create a file containing the output.  Please choose where you would like these files to be created:");
		
			
		more1.setBounds(210, y, width-210, more1.getTextBounds(0, more1.getCharCount()-1 ).height);
	
		y +=  more1.getBounds().height + 15;
		
		final Text pdir = new Text(this,SWT.BORDER | SWT.READ_ONLY);
		pdir.setText("cocoprints");
		pdir.setBounds(210, y, width-220, 24);
		
		y+=30;
		
		final Button getdir = new Button(this, SWT.NONE);
		getdir.setText("Choose a directory...");
		getdir.setBounds(210 + ((width-210)/2 - 90 ) ,y,180,24);
		
		getdir.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e)
			{
				
				String res = MainWin.getFile(false,true,pdir.getText(),"Choose a directory for printer output..", "Open");
					
				if (res != null)
				{
					pdir.setText(res);
				}
				
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
			
				
			}
			
		});
		
		
		 
		
		
		
		final Button doit = new Button(this, SWT.NONE);
		doit.setText("Continue...");
		
		doit.setBounds(0, height - 30, width/2 - 10, 24);
		doit.setEnabled(true);
		
		
		doit.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e)
			{
				printerdir = pdir.getText();
				
				if (text.getSelection())
					printertype = "Text";
				else
					printertype = "FX80";
				
				state = 5;
				drawControls();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				
			}
			
		});
		
		
		Button nothanks = new Button(this, SWT.NONE);
		nothanks.setText("Cancel wizard");
		nothanks.setBounds(width/2+9, height - 30, width - (width/2+9), 24);
		
		nothanks.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e)
			{

				state = -1;
				drawControls();
				MainWin.taskman.removeTask(tid);
				
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				
			}
			
		});
	}


	private void drawMIDISelectControls()
	{
		int y = 0;
		
		Label cocoman3 = new Label(this, SWT.NONE);
		cocoman3.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/wizard/cocoman4.png"));
		cocoman3.setBounds(width/2 - 196, y, 391, 277);
		
		y += 295;
		
		StyledText intro = new StyledText(this, SWT.WRAP | SWT.MULTI | SWT.READ_ONLY);
		
		intro.setCursor(new org.eclipse.swt.graphics.Cursor(this.getDisplay(), SWT.CURSOR_ARROW));
		intro.setEditable(false);
		intro.setEnabled(false);
		
		intro.setBounds(0, y, width, 130);
		intro.setForeground(DiskWin.colorDiskBG);
		intro.setFont(this.introFont);
		intro.setText("DriveWire 4 can provide a virtual MIDI device which allows software running on your " + this.cocodevname + " to use any MIDI hardware on the server.");
		intro.append(intro.getLineDelimiter() + intro.getLineDelimiter());
		intro.append("Would you like to enable MIDI support?");
		intro.setBounds(0, y, width, intro.getTextBounds(0, intro.getCharCount()-1 ).height);
		
		y+=intro.getBounds().height + 15;
		
		final Button yes = new Button(this, SWT.RADIO);
		yes.setText("Yes");
		yes.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		
		yes.setBounds(width/2-100,y,100,24);
		
		Button no = new Button(this, SWT.RADIO);
		no.setText("No Thanks");
		no.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			
		no.setBounds(width/2,y,100,24);
		
		yes.setSelection(true);
		
		
		final Button doit = new Button(this, SWT.NONE);
		doit.setText("Continue...");
		
		doit.setBounds(0, height - 30, width/2 - 10, 24);
		doit.setEnabled(true);
		
		
		doit.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e)
			{
				if (yes.getSelection())
				{
					usemidi = true;
				}
				else
				{
					usemidi = false;
				}
				state = 4;
				drawControls();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				
			}
			
		});
		
		
		Button nothanks = new Button(this, SWT.NONE);
		nothanks.setText("Cancel wizard");
		nothanks.setBounds(width/2+9, height - 30, width - (width/2+9), 24);
		
		nothanks.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e)
			{

				state = -1;
				drawControls();
				MainWin.taskman.removeTask(tid);
				
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				
			}
			
		});
	}


	private void drawWizardSuccessControls()
	{
		
		//this.setBounds(this.getBounds().x, this.getBounds().y, this.getBounds().width, 100);
		
		stat = UITaskMaster.TASK_STATUS_COMPLETE;
		
		StyledText intro = new StyledText(this, SWT.WRAP | SWT.MULTI | SWT.READ_ONLY);
		
		intro.setCursor(new org.eclipse.swt.graphics.Cursor(this.getDisplay(), SWT.CURSOR_ARROW));
		intro.setEditable(false);
		intro.setEnabled(false);
		intro.setAlignment(SWT.CENTER);
		intro.setBounds(0, 4, width , 20);
		intro.setForeground(DiskWin.colorDiskBG);
		intro.setFont(this.introFont);
		
		intro.setText("Congratulations, DriveWire is now ready to use with your " + this.cocodevname + "!");
		
		MainWin.setSashformWeights(sashform_orig);
		
		MainWin.taskman.updateTask(tid, UITaskMaster.TASK_STATUS_COMPLETE, "");
		
		
	}


	private void drawCommTestChooseControls()
	{
		
		
		int y = 20;
		
		Label cocoman3 = new Label(this, SWT.NONE);
		cocoman3.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/wizard/cocoman3.png"));
		cocoman3.setBounds(width/2 - 144, y, 288, 160);
		
		y += 180;
		
		StyledText intro = new StyledText(this, SWT.WRAP | SWT.MULTI | SWT.READ_ONLY);
		
		intro.setCursor(new org.eclipse.swt.graphics.Cursor(this.getDisplay(), SWT.CURSOR_ARROW));
		intro.setEditable(false);
		intro.setEnabled(false);
		
		intro.setBounds(0, y, width, 130);
		intro.setForeground(DiskWin.colorDiskBG);
		intro.setFont(this.introFont);
		intro.setText("We're almost finished!  In fact, you can choose to complete the wizard now if you do not wish to test the configuration.");
		
		intro.append(intro.getLineDelimiter() + intro.getLineDelimiter());
	
		intro.append("In order to test the connection between your " + this.cocodevname + " and this computer, "); 
		intro.append("you must have a serial cable connected between " + this.device + " and the CoCo's bitbanger port.  ");
		intro.append("You do not need a DriveWire ROM or any special software on the CoCo to perform the test.");
		
		intro.setBounds(0, y, width, intro.getTextBounds(0, intro.getCharCount()-1).height) ;
		
		y += 20 + intro.getBounds().height;
		
		Button testit = new Button(this, SWT.NONE);
		testit.setText("Test Configuration...");
		testit.setBounds(width/2  - 100, y, 200, 24);
		testit.setEnabled(true);
		
		testit.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e)
			{
				
				state = 6;
				drawControls();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				
			}
			
		});
		
		y += 45;
		
		Link link = new Link(this, SWT.NONE);
		link.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				MainWin.doDisplayAsync(new Runnable() {

					@Override
					public void run() 
					{
						MainWin.openURL(this.getClass(),"http://sourceforge.net/apps/mediawiki/drivewireserver/index.php?title=DriveWire_cables");
					}
					
				});
				
			}
		});
		
		link.setBounds(width/2 - 120,y,240,40);
		
		link.setText("<a>Yikes!  I don't have a DriveWire cable!</a>");
		link.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		link.setFont(this.introFont);
		
		
		
		final Button doit = new Button(this, SWT.NONE);
		doit.setText("Finish without test");
		
		doit.setBounds(0, height - 30, width/2 - 10, 24);
		doit.setEnabled(true);
		
		
		
		
		doit.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e)
			{
				applyConfig();
				
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				
			}
			
		});
		
		
		Button nothanks = new Button(this, SWT.NONE);
		nothanks.setText("Cancel wizard");
		nothanks.setBounds(width/2+9, height - 30, width - (width/2+9), 24);
		
		nothanks.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e)
			{

				state = -1;
				drawControls();
				MainWin.taskman.removeTask(tid);
				
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				
			}
			
		});
		
		
		
	}


	protected void applyConfig()
	{
		// not in display thread, buddy
		Thread t = new Thread(new Runnable() 
		{

			@Override
			public void run()
			{
				boolean fail = false;
				
				try
				{
					// fixup params for submit to server..
					
					int rate = fpgarate;
					
					if (cocomodel == DWDefs.MODEL_COCO1)
						rate = 38400;
					else if ((cocomodel == DWDefs.MODEL_COCO2) || (cocomodel == DWDefs.MODEL_ATARI)) 
						rate = 57600;
					
					if (cocomodel == DWDefs.MODEL_EMULATOR)
						device = "TCP";
					
					UIUtils.simpleConfigServer(cocomodel, rate, cocodevname, device, usemidi, printertype, printerdir);
					
										
				}
				catch (DWUIOperationFailedException e1)
				{
					// TODO
					fail = true;
				} 
				catch (IOException e1)
				{
					fail = true;
				}
				
				if (!fail)
				{
					// back to display thread for cleanup
					getDisplay().asyncExec(new Runnable() {

						@Override
						public void run()
						{
							state = 9;
							drawControls();
						}
						
					});
					
				}
			}
			
		});
		
		
		t.start();
	}

	
	private void startSerialMonitor()
	{
		 Runnable portMonitor = new Runnable() 
		 {
			 ArrayList<String> ports = new ArrayList<String>();
			 
			 public void run() 
		     {
				 if ((monitorPorts) && (!isDisposed()) && (portlist != null) && (manualPorts != null) && !updatingPorts)
				 {
					 // check available serial ports..
					 synchronized(updatingPorts)
					 {
						 updatingPorts = true;
					 }
					 // notice absent friends
					 TableItem[] items = portlist.getItems();
					 
					 for (TableItem i : items)
					 {
						 if (!ports.contains(i.getText(0)) && !manualPorts.contains(i.getText(0)))
						 {
							 i.setText(1, "Failed, uplugged adapter?");
						 }
					 }
					 
					 // auto > manual
					 for (String p: ports)
					 {
						 if (manualPorts.contains(p))
							 manualPorts.remove(p);
					 }
					 
					 // keep the port queries out of the gui thread
					 Runnable updater = new Runnable()
					 {

						 
						 
						@Override
						public void run()
						{
							 ports = DriveWireServer.getAvailableSerialPorts();
							 
							 if (!isDisposed())
								 for (String p : ports)
								 {
									 final String port = p;
									 final String stat = DriveWireServer.getSerialPortStatus(port);
									 
									 if (!isDisposed())
									 	 master.getDisplay().syncExec(
											  new Runnable() {
												  public void run()
												  {
													  updatePortEntry(port, stat);
												  }
											  });
								 }
							 
							 if (!isDisposed())
								 for (String p : manualPorts)
								 {
									 final String port = p;
									 final String stat = DriveWireServer.getSerialPortStatus(port);
									 if (!isDisposed())
									    master.getDisplay().syncExec(
											  new Runnable() {
												  public void run()
												  {
													  updatePortEntry(port, stat);
												  }
											  });
								 }
								 
							 synchronized(updatingPorts)
							 {
								 updatingPorts = false;
							 }
							 
						}
						 
					 };
					
					 Thread t = new Thread(updater);
					 t.start();
					
				 }
				
				 
				 if (!isDisposed() && (state == 2))
					 getDisplay().timerExec(2000, this);
		     }
		 };
		    
		
		 this.getDisplay().timerExec(2000, portMonitor);
	}
	

	private void drawSerialSelectControls()
	{
		
		
		this.monitorPorts = true;
		
		int y = 10;
		
		startSerialMonitor();
		
		
		StyledText intro = new StyledText(this, SWT.WRAP | SWT.MULTI | SWT.READ_ONLY);
		
		intro.setCursor(new org.eclipse.swt.graphics.Cursor(this.getDisplay(), SWT.CURSOR_ARROW));
		intro.setEditable(false);
		intro.setEnabled(false);
		
		intro.setBounds(0, y, width, 130);
		intro.setForeground(DiskWin.colorDiskBG);
		intro.setFont(this.introFont);
		intro.setText("Next, we must choose which serial port to use when communicating with your " + this.cocodevname + ".  If you're not sure which is which, don't worry!  We will test things later and make any changes necessary." + intro.getLineDelimiter() + intro.getLineDelimiter());
		intro.append("If your computer does not have a serial port built in, you may want to acquire an inexpensive USB or Bluetooth serial adapter." + intro.getLineDelimiter() + intro.getLineDelimiter());
		
		intro.append("DriveWire has detected the following ports:");
		
		intro.setBounds(0 , y , width, intro.getTextBounds(0, intro.getCharCount()-1).height);
			
		
		y += intro.getBounds().height;
		
		final Label cocoman2 = new Label(this, SWT.NONE);
		cocoman2.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/wizard/cocoman2.png"));
		cocoman2.setBounds(width - 129, y, 119, 97);
		
		y += 15;
		
		portlist.setBounds(0, y, width - 130, 97);
		
		portlist.setVisible(true);
			
		
	
		
		y += portlist.getBounds().height + 10;

		stat = UITaskMaster.TASK_STATUS_ACTIVE;
		status.setBounds(0, y, 32, 32);
		status.setVisible(true);
		

		StyledText more1 = new StyledText(this, SWT.WRAP | SWT.MULTI | SWT.READ_ONLY);
		
		more1.setCursor(new org.eclipse.swt.graphics.Cursor(this.getDisplay(), SWT.CURSOR_ARROW));
		more1.setEditable(false);
		more1.setEnabled(false);
		
		more1.setBounds(42, y+5, width - 42, 32);
		more1.setForeground(DiskWin.colorDiskBG);
		more1.setFont(this.smallFont);
		more1.setText("The wizard is watching for any changes.  If you will be using an adapter, please plug it into the computer now.");
		more1.setBounds(42, y + ((32 - more1.getTextBounds(0, more1.getCharCount() -1).height)/2) , width - 42 , more1.getTextBounds(0, more1.getCharCount() -1).height);
		
		
		
		
		
		y += Math.max(57, more1.getBounds().height + 25);
		
		StyledText more2 = new StyledText(this, SWT.WRAP | SWT.MULTI | SWT.READ_ONLY);
		
		more2.setCursor(new org.eclipse.swt.graphics.Cursor(this.getDisplay(),SWT.CURSOR_ARROW));
		more2.setEditable(false);
		more2.setEnabled(false);
		
		more2.setBounds(0, y, width, 50);
		more2.setForeground(DiskWin.colorDiskBG);
		more2.setFont(this.introFont);
		
		more2.setText("If DriveWire does not detect your serial port, you can enter the name below:");
		more2.setBounds(0, y, width, more2.getTextBounds(0, more2.getCharCount() -1).height);
		
		y += more2.getBounds().height + 15;
		
		int mo = Math.max(more2.getTextBounds(0, more2.getCharCount() - 1).width, 270);
		
		
		final Text mandev = new Text(this, SWT.BORDER);
		mandev.setBounds(mo - 260, y, 140, 24);
		
		Button manadd = new Button(this, SWT.NONE);
		manadd.setText("Add port");
		manadd.setBounds(mo-100, y, 100, 24);
		
		manadd.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e)
			{
				if (!(mandev.getText() == null) && !mandev.getText().equals("") && validatePortName(mandev.getText()))
				{
					manualPorts.add(mandev.getText());
					mandev.setText("");
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				
			}
			
		});
		
	
		
		y += 80;
		
		final StyledText more3 = new StyledText(this, SWT.WRAP | SWT.MULTI | SWT.READ_ONLY);
		
		more3.setCursor(new org.eclipse.swt.graphics.Cursor(this.getDisplay(), SWT.CURSOR_ARROW));
		more3.setEditable(false);
		more3.setEnabled(false);
		
		more3.setBounds(0, y, width, 50);
		more3.setForeground(DiskWin.colorDiskBG);
		more3.setFont(this.introFont);
		
		more3.setText("Select a serial port with the status 'Available' from the list above to continue.");
		more3.setBounds(0, height - more3.getTextBounds(0, more3.getCharCount() -1).height - 50, width, more3.getTextBounds(0, more3.getCharCount() -1).height);
		
		
		
		
		
		final Button doit = new Button(this, SWT.NONE);
		doit.setText("Choose a port");
		
		doit.setBounds(0, height - 30, width/2 - 10, 24);
		doit.setEnabled(false);
		
		
		portlist.addSelectionListener(new SelectionListener() {

			

			@Override
			public void widgetSelected(SelectionEvent e)
			{
				if (!doit.isDisposed())
				{
					if (portlist.getItem(portlist.getSelectionIndex()).getText(1).equals("Available"))
					{
						device = portlist.getItem(portlist.getSelectionIndex()).getText(0);
						doit.setText("Use " + shortDevName(device) + "...");
						doit.setEnabled(true);
					
					
						
					}
					else
					{
						doit.setEnabled(false);
					
					}
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				
			}
			
		});
		
		
		doit.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e)
			{
				if (intesting)
					state = 6;
				else
					state = 3;
				drawControls();
				
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				
			}
			
		});
		
		
		Button nothanks = new Button(this, SWT.NONE);
		nothanks.setText("Cancel wizard");
		nothanks.setBounds(width/2+9, height - 30, width - (width/2+9), 24);
		
		nothanks.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e)
			{

				state = -1;
				drawControls();
				MainWin.taskman.removeTask(tid);
				
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				
			}
			
		});
		
		
	}

	
	
	

	protected String shortDevName(String dev)
	{
		if ((dev.lastIndexOf('/')>-1) && (dev.lastIndexOf('/')<(dev.length()-2)))
		{
			return dev.substring(dev.lastIndexOf('/')+1);
		}
		
		return dev;
	}


	protected boolean validatePortName(String text)
	{
		
		Matcher m = unixstyle.matcher(text);
		
		if (m.matches())
			return true;
		
		m = winstyle.matcher(text);
		
		if (m.matches())
			return true;
		
		// hmmm
		 MessageBox messageBox = new MessageBox(this.getShell(), SWT.ICON_WARNING | SWT.YES | SWT. NO);
		 messageBox.setMessage("The name '" + text + "' does not look like a typical device name." + System.getProperty("line.separator") 
				 + System.getProperty("line.separator") + "On Windows computers, device names look like 'COM1'." + System.getProperty("line.separator") 
				 + System.getProperty("line.separator") + "On *nix and Mac systems they look like '/dev/ttyS0'." + System.getProperty("line.separator") 
				 + System.getProperty("line.separator") + "Are you sure this is a valid device name?");
		 messageBox.setText("Unexpected device name");
		 int rc = messageBox.open();
		
		 if (rc == SWT.YES)
			 return true;
		 
		 return false;
	}


	private void updatePortEntry(String port, String status)
	{
		
		if  ((this.portlist != null) && (!this.portlist.isDisposed()))
		{

			// humanize a bit
			if (status.equals("NoSuchPortException: null"))
				status = "This port does not exist";
			else if (status.startsWith("PortInUseException: ") && (status.length() > 20))
				status = "In use by " + status.substring(20);
			else if (status.equals("In use by DriveWire"))
				status = "Available";
			
			TableItem[] items = this.portlist.getItems();
			
			for (TableItem ti : items)
			{
				if (ti.getText(0).equals(port))
				{
					ti.setText(1, status);
					return;
				}
			}
			
			TableItem ti = new TableItem(this.portlist, SWT.NONE);
			ti.setText(0, port);
			ti.setText(1, status);
	
		}
			
	}


	private void drawCoCoSelectControls()
	{
		
		int y = 20;
		
		Label cocoman = new Label(this, SWT.NONE);
		cocoman.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/wizard/cocoman1.png"));
		
		cocoman.setBounds(width - 170, y,170,177);
		
		y += 30;
		
		StyledText intro = new StyledText(this, SWT.WRAP | SWT.MULTI | SWT.READ_ONLY);
		
		intro.setCursor(new org.eclipse.swt.graphics.Cursor(this.getDisplay(),SWT.CURSOR_ARROW));
		intro.setEditable(false);
		intro.setEnabled(false);
		
		intro.setBounds(0, y, width - 180, 60);
		intro.setForeground(DiskWin.colorDiskBG);
		intro.setFont(this.introFont);
		intro.setText("This wizard will help you generate a basic configuration. For additional options, please use the configuration editor in the Config menu.");
		intro.append(intro.getLineDelimiter() + intro.getLineDelimiter());
		intro.append("Let's get started!");
		intro.append(intro.getLineDelimiter() + intro.getLineDelimiter());
		intro.append("First, we need to know what type of device you would like to connect.  Please choose your device from the options below:");
		intro.setBounds(0, y, width - 180, intro.getTextBounds(0, intro.getCharCount() - 1).height);
		
		y += 165;
		
		int bwidth = 105;
		int bheight = 81;
		int gap = 24;
		
		int loff = (width/2) - (bwidth + bwidth/2 + gap);
		int toff = y;
		
		int txtoff = toff + bheight + 5;
		
		int bstyle = SWT.TOGGLE;
		
		final Color tcolor = DiskWin.colorDiskGraphBG;
		final Color tacolor = SWTResourceManager.getColor(SWT.COLOR_BLACK);
		
		final Button coco1 = new Button(this, bstyle);
		coco1.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/wizard/coco1.png"));
		coco1.setBounds(loff, toff, bwidth, bheight);
			
		final StyledText coco1txt = new StyledText(this,SWT.WRAP);
		coco1txt.setAlignment(SWT.CENTER);
		coco1txt.setBounds(loff, txtoff, bwidth, 20);
		coco1txt.setCursor(new org.eclipse.swt.graphics.Cursor(this.getDisplay(), SWT.CURSOR_ARROW));
		coco1txt.setEditable(false);
		coco1txt.setEnabled(false);
		coco1txt.setFont(this.introFont);
		coco1txt.setForeground(tcolor);
		coco1txt.setText("CoCo 1");
		
		
		
		
		
		final Button coco2 = new Button(this, bstyle);
		coco2.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/wizard/coco2.png"));
		coco2.setBounds(loff + bwidth + gap, toff, bwidth, bheight);
		
		final StyledText coco2txt = new StyledText(this,SWT.WRAP);
		coco2txt.setAlignment(SWT.CENTER);
		coco2txt.setBounds(loff + bwidth + gap, txtoff, bwidth, 20);
		coco2txt.setCursor(new org.eclipse.swt.graphics.Cursor(this.getDisplay(), SWT.CURSOR_ARROW));
		coco2txt.setEditable(false);
		coco2txt.setEnabled(false);
		coco2txt.setFont(this.introFont);
		coco2txt.setForeground(tcolor);
		coco2txt.setText("CoCo 2");
		
		final Button coco3 = new Button(this, bstyle);
		coco3.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/wizard/coco3.png"));
		coco3.setBounds(loff + bwidth + gap + bwidth + gap, toff, bwidth, bheight);
		coco3.setSelection(true);
		
		final StyledText coco3txt = new StyledText(this,SWT.WRAP);
		coco3txt.setAlignment(SWT.CENTER);
		coco3txt.setBounds(loff + bwidth + gap + bwidth + gap, txtoff, bwidth, 20);
		coco3txt.setCursor(new org.eclipse.swt.graphics.Cursor(this.getDisplay(), SWT.CURSOR_ARROW));
		coco3txt.setEditable(false);
		coco3txt.setEnabled(false);
		coco3txt.setFont(this.introFont);
		coco3txt.setForeground(tacolor);
		coco3txt.setText("CoCo 3");
		
		toff += bheight + 30;
		txtoff += bheight + 30;
		
		
		final Button fpga = new Button(this, bstyle);
		fpga.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/wizard/fpga.png"));
		fpga.setBounds(loff, toff, bwidth, bheight);
		
		final StyledText fpgatxt = new StyledText(this,SWT.WRAP);
		fpgatxt.setAlignment(SWT.CENTER);
		fpgatxt.setBounds(loff, txtoff, bwidth, 20);
		fpgatxt.setCursor(new org.eclipse.swt.graphics.Cursor(this.getDisplay(), SWT.CURSOR_ARROW));
		fpgatxt.setEditable(false);
		fpgatxt.setEnabled(false);
		fpgatxt.setFont(this.introFont);
		fpgatxt.setForeground(tcolor);
		fpgatxt.setText("CoCo3FPGA");
		
		
		
		final Button emulator = new Button(this, bstyle);
		emulator.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/wizard/emulator.png"));
		emulator.setBounds(loff + bwidth + gap, toff, bwidth, bheight);
		
		final StyledText emutxt = new StyledText(this,SWT.WRAP);
		emutxt.setAlignment(SWT.CENTER);
		emutxt.setBounds(loff + bwidth + gap, txtoff, bwidth, 20);
		emutxt.setCursor(new org.eclipse.swt.graphics.Cursor(this.getDisplay(), SWT.CURSOR_ARROW));
		emutxt.setEditable(false);
		emutxt.setEnabled(false);
		emutxt.setFont(this.introFont);
		emutxt.setForeground(tcolor);
		emutxt.setText("Emulator");
		
		
		
		final Button atari = new Button(this, bstyle);
		atari.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/wizard/atari.png"));
		atari.setBounds(loff + bwidth + gap + bwidth + gap, toff, bwidth, bheight);
		
		final StyledText ataritxt = new StyledText(this,SWT.WRAP);
		ataritxt.setAlignment(SWT.CENTER);
		ataritxt.setBounds(loff + bwidth + gap + bwidth + gap, txtoff, bwidth, 20);
		ataritxt.setCursor(new org.eclipse.swt.graphics.Cursor(this.getDisplay(), SWT.CURSOR_ARROW));
		ataritxt.setEditable(false);
		ataritxt.setEnabled(false);
		ataritxt.setFont(this.introFont);
		ataritxt.setForeground(tcolor);
		ataritxt.setText("Atari");
		
		
		
		final Button doit = new Button(this, SWT.NONE);
		doit.setText("Choose CoCo 3..");
		this.cocodevname = "CoCo 3";
		
		doit.setBounds(0, height - 30, width/2 - 10, 24);
		
		doit.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e)
			{
				if (intesting)
					state = 6;
				else
					state = 2;
				drawControls();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				
			}
			
		});
		
		
		
		// make it act like a radio group.. sort of.. bah
		
		coco1.setData("cocomodel", DWDefs.MODEL_COCO1);
		coco1.setData("cocodevname", "CoCo 1");
		
		coco2.setData("cocomodel", DWDefs.MODEL_COCO2);
		coco2.setData("cocodevname", "CoCo 2");
		
		coco3.setData("cocomodel", DWDefs.MODEL_COCO3);
		coco3.setData("cocodevname", "CoCo 3");

		fpga.setData("cocomodel", DWDefs.MODEL_FPGA);
		fpga.setData("cocodevname", "FPGA board");
		
		emulator.setData("cocomodel", DWDefs.MODEL_EMULATOR);
		emulator.setData("cocodevname", "emulator");
		
		atari.setData("cocomodel", DWDefs.MODEL_ATARI);
		atari.setData("cocodevname", "Atari");
		
		
		SelectionListener cocorg = new SelectionListener () {

			
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				synchronized(cocomodel)
				{
					coco1.setSelection(false);
					coco2.setSelection(false);
					coco3.setSelection(false);
					fpga.setSelection(false);
					emulator.setSelection(false);
					atari.setSelection(false);
					
					coco1txt.setForeground(tcolor);
					coco2txt.setForeground(tcolor);
					coco3txt.setForeground(tcolor);
					fpgatxt.setForeground(tcolor);
					emutxt.setForeground(tcolor);
					ataritxt.setForeground(tcolor);
				
					cocomodel = Integer.parseInt(e.widget.getData("cocomodel").toString());
					cocodevname = e.widget.getData("cocodevname").toString();
					doit.setText("Choose " + cocodevname + "..");
					
					if (cocomodel == 1)
					{
						coco1.setSelection(true);
						coco1txt.setForeground(tacolor);
					}
					else if (cocomodel == 2)
					{
						coco2.setSelection(true);
						coco2txt.setForeground(tacolor);
					}
					else if (cocomodel == 3)
					{
						coco3.setSelection(true);
						coco3txt.setForeground(tacolor);
					}
					else if (cocomodel == 4)
					{
						fpga.setSelection(true);
						fpgatxt.setForeground(tacolor);
					}
					else if (cocomodel == 5)
					{
						emulator.setSelection(true);
						emutxt.setForeground(tacolor);
					}
					else if (cocomodel == 6)
					{
						atari.setSelection(true);
						ataritxt.setForeground(tacolor);
					}
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
			
				
			}
			
		};
		
		
		coco1.addMouseTrackListener(new MouseTrackAdapter() 
		{
			@Override
			public void mouseEnter(MouseEvent e) 
			{
				setCursor(new Cursor(getDisplay(), SWT.CURSOR_HAND));
				
			}
			
			@Override
			public void mouseExit(MouseEvent e) 
			{
				setCursor(new Cursor(getDisplay(), SWT.CURSOR_ARROW));
				
			}
		});
		
		coco2.addMouseTrackListener(new MouseTrackAdapter() 
		{
			@Override
			public void mouseEnter(MouseEvent e) 
			{
				setCursor(new Cursor(getDisplay(), SWT.CURSOR_HAND));
				
			}
			
			@Override
			public void mouseExit(MouseEvent e) 
			{
				setCursor(new Cursor(getDisplay(), SWT.CURSOR_ARROW));
				
			}
		});
		
		coco3.addMouseTrackListener(new MouseTrackAdapter() 
		{
			@Override
			public void mouseEnter(MouseEvent e) 
			{
				setCursor(new Cursor(getDisplay(), SWT.CURSOR_HAND));
				
			}
			
			@Override
			public void mouseExit(MouseEvent e) 
			{
				setCursor(new Cursor(getDisplay(), SWT.CURSOR_ARROW));
				
			}
		});
		
		fpga.addMouseTrackListener(new MouseTrackAdapter() 
		{
			@Override
			public void mouseEnter(MouseEvent e) 
			{
				setCursor(new Cursor(getDisplay(), SWT.CURSOR_HAND));
				
			}
			
			@Override
			public void mouseExit(MouseEvent e) 
			{
				setCursor(new Cursor(getDisplay(), SWT.CURSOR_ARROW));
				
			}
		});
		
		emulator.addMouseTrackListener(new MouseTrackAdapter() 
		{
			@Override
			public void mouseEnter(MouseEvent e) 
			{
				setCursor(new Cursor(getDisplay(), SWT.CURSOR_HAND));
				
			}
			
			@Override
			public void mouseExit(MouseEvent e) 
			{
				setCursor(new Cursor(getDisplay(), SWT.CURSOR_ARROW));
				
			}
		});
		
		atari.addMouseTrackListener(new MouseTrackAdapter() 
		{
			@Override
			public void mouseEnter(MouseEvent e) 
			{
				setCursor(new Cursor(getDisplay(), SWT.CURSOR_HAND));
				
			}
			
			@Override
			public void mouseExit(MouseEvent e) 
			{
				setCursor(new Cursor(getDisplay(), SWT.CURSOR_ARROW));
				
			}
		});
		
		
		
		
		coco1.addSelectionListener(cocorg);
		coco2.addSelectionListener(cocorg);
		coco3.addSelectionListener(cocorg);
		fpga.addSelectionListener(cocorg);
		emulator.addSelectionListener(cocorg);
		atari.addSelectionListener(cocorg);
		
		
	
		
		Button nothanks = new Button(this, SWT.NONE);
		nothanks.setText("Cancel wizard");
		nothanks.setBounds(width/2+9, height - 30, width - (width/2+9), 24);
		
		nothanks.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e)
			{
				state = -1;
				drawControls();
				MainWin.taskman.removeTask(tid);
				
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				
			}
			
		});
		
		
	}


	


	private void drawInitControls()
	{
		int y = 70;
		
		Label hello = new Label(this, SWT.NONE);
		hello.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		hello.setImage(org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/wizard/hello_dw4.png"));
		hello.setBounds(width/2 - 144, y, 288, 220);
		
		StyledText intro = new StyledText(this, SWT.WRAP | SWT.MULTI | SWT.READ_ONLY);
		
		intro.setCursor(new org.eclipse.swt.graphics.Cursor(this.getDisplay(), SWT.CURSOR_ARROW));
		intro.setEditable(false);
		intro.setEnabled(false);
		
		y += 244;
		
		intro.setBounds(0, y, width, 130);
		intro.setForeground(DiskWin.colorDiskBG);
		intro.setFont(this.introFont);
		intro.setText("To get started, we need to introduce your CoCo and your DriveWire computer to each other." + intro.getLineDelimiter() + intro.getLineDelimiter());
		intro.append("This wizard can walk you through that process now if you would like.  You can also run this wizard at any time by choosing 'Simple config wizard' from the Config menu above.");
		
		intro.setBounds(0, y, width, intro.getTextBounds(0, intro.getCharCount() -1).height);
		
		y += intro.getBounds().height + 20;
		
		Button doit = new Button(this, SWT.NONE);
		doit.setText("Use setup wizard..");
		
		doit.setBounds(0, height-30, width/2 - 10, 24);
		
		doit.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e)
			{
				state = 1;
				drawControls();
				
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				
			}
			
		});
		
		
		Button nothanks = new Button(this, SWT.NONE);
		nothanks.setText("Leave me alone");
		nothanks.setBounds(width/2+9, height - 30, width - (width/2+9) , 24);
		
		nothanks.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e)
			{
				state = -1;
				drawControls();
				MainWin.taskman.removeTask(tid);
				
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				
			}
			
		});
	}


	public int getHeight()
	{
		if (this.stat != UITaskMaster.TASK_STATUS_COMPLETE)
		{
			return(500);
		}
		else
		{
			return(40);
		}
	}
	
	public void setStatus(int stat)
	{
	}
	
	public void setDetails(String text)
	{
	}
	
	public void setCommand(String text)
	{
	}
	
	
	
	public void setTop(int y)
	{
		this.setBounds(this.getBounds().x,  y, this.getBounds().width, this.getBounds().height);
	}
	
	public void setBottom(int y)
	{
		this.setBounds(this.getBounds().x, this.getBounds().y, this.getBounds().width, y - this.getBounds().y);
	
	}
}
