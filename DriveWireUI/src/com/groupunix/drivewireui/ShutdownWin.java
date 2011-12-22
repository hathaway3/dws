package com.groupunix.drivewireui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;

public class ShutdownWin
{

	protected Object result;
	protected Shell shlShuttingDown;

	protected Label lblStatus;
	protected ProgressBar progressBar;
	private int x;
	private int y;

	public ShutdownWin(Shell parent, int style)
	{
		//super(parent, style);
		this.x = parent.getLocation().x + (parent.getSize().x / 2) - 180;
		this.y = parent.getLocation().y + (parent.getSize().y / 2) - 90;
	}


	
	public void open()
	{
		createContents();
		shlShuttingDown.setLocation(x , y);
		shlShuttingDown.open();
		shlShuttingDown.layout();
		
	
	}


	private void createContents()
	{
		shlShuttingDown = new Shell(MainWin.getDisplay(), SWT.DIALOG_TRIM);
		shlShuttingDown.setSize(369, 181);
		shlShuttingDown.setText("Shutting down...");
		
		Label lblPleaseWaitA = new Label(shlShuttingDown, SWT.WRAP);
		lblPleaseWaitA.setBounds(10, 21, 343, 36);
		lblPleaseWaitA.setText("Please wait a moment for DriveWire to save the configuration and shutdown cleanly.");
		
		progressBar = new ProgressBar(shlShuttingDown, SWT.NONE);
		progressBar.setBounds(31, 81, 295, 17);
		progressBar.setMinimum(0);
		progressBar.setMaximum(100);
		progressBar.setSelection(0);
		
		lblStatus = new Label(shlShuttingDown, SWT.WRAP);
		lblStatus.setBounds(31, 110, 295, 36);

	}

	public void setStatus(final String txt, final int prog)
	{
		
		
		if ((lblStatus != null) && !lblStatus.isDisposed())
		{
			lblStatus.setText(txt);
			lblStatus.redraw();
			lblStatus.update();
		}
		else
			System.out.println("shutdown: " + txt + " (" + prog  + ")");
		
		if ((progressBar != null) && !progressBar.isDisposed())
		{
			progressBar.setSelection(prog);
			progressBar.redraw();
			progressBar.update();
		}
		
		shlShuttingDown.redraw();
		shlShuttingDown.update();
	
	}



	public void setProgress(int i)
	{
		if ((progressBar != null) && !progressBar.isDisposed())
		{
			if (progressBar.getSelection() != i)
			{
				progressBar.setSelection(i);
				progressBar.redraw();
				progressBar.update();
			}
		}
	}
}
