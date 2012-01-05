package com.groupunix.drivewireui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;

public class ShutdownWin
{

	protected Object result;
	protected Shell shlShuttingDown;
	protected Label lblPleaseWaitA;
	protected Label lblStatus;
	protected ProgressBar progressBar;
	private int x;
	private int y;
	private Shell parent;
	
	public ShutdownWin(Shell parent, int style)
	{
		//super(parent, style);
		this.parent = parent;
	}


	
	public void open()
	{
		createContents();
		this.x = parent.getLocation().x + (parent.getSize().x / 2) - 180;
		this.y = parent.getLocation().y + (parent.getSize().y / 2) - 90;
		shlShuttingDown.setLocation(x , y);
		shlShuttingDown.open();
		shlShuttingDown.layout();
		
	
	}


	/**
	 * @wbp.parser.entryPoint
	 */
	private void createContents()
	{
		shlShuttingDown = new Shell(Display.getCurrent(), SWT.DIALOG_TRIM);
		shlShuttingDown.setSize(369, 181);
		shlShuttingDown.setText("Shutting down...");
		
		lblPleaseWaitA = new Label(shlShuttingDown, SWT.WRAP);
		lblPleaseWaitA.setBounds(10, 21, 343, 36);
		lblPleaseWaitA.setText("Please wait a moment for DriveWire to save the configuration and shutdown cleanly.");
		
		progressBar = new ProgressBar(shlShuttingDown, SWT.SMOOTH);
		progressBar.setBounds(31, 81, 295, 17);
		progressBar.setMinimum(0);
		progressBar.setMaximum(100);
		progressBar.setSelection(0);
		
		
		lblStatus = new Label(shlShuttingDown, SWT.WRAP);
		lblStatus.setBounds(31, 110, 295, 36);

	}

	
	
	public void setStatus(final String txt, final int prog)
	{
		
		
			if ((progressBar != null) && !progressBar.isDisposed())
			{
				this.setProgress(prog);
			}
			
			
			if ((lblStatus != null) && !lblStatus.isDisposed())
			{
				lblStatus.setText(txt);
	
			}
			else
				System.out.println("shutdown: " + txt + " (" + prog  + ")");
			
	}



	public void setProgress(final int i)
	{

		if ((progressBar != null) && !progressBar.isDisposed())
		{
			if (progressBar.getSelection() != i)
			{
				progressBar.setSelection(i);
				
				progressBar.redraw();
				lblPleaseWaitA.redraw();
				lblStatus.redraw();
				
			}
			
		}
		
	}

	
}
