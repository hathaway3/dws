package com.groupunix.drivewireui;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import swing2swt.layout.BorderLayout;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.SWT;
import com.swtdesigner.SWTResourceManager;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;

public class LogViewerWin extends Dialog {

	protected Object result;
	protected Shell shell;
	private Text text;
	protected Display display;
	
	private Socket sock;

	private String host;
	private int port;
	private Thread inputT;
	
	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public LogViewerWin(Shell parent, int style, String host, int port) {
		super(parent, style);
		this.host = host;
		this.port = port;
		
		setText("Log Viewer");
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		createContents();
		shell.open();
		shell.layout();
		display = getParent().getDisplay();
		
		// start log thread
		Connect();
		
		
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shell = new Shell(getParent(), SWT.SHELL_TRIM);
		shell.addShellListener(new ShellAdapter() {
			@Override
			public void shellClosed(ShellEvent e) 
			{
				try 
				{
					sock.close();
				} 
				catch (IOException e1) 
				{
					MainWin.addToDisplay(e1.getMessage());
				}
			}
		});
		shell.setSize(642, 477);
		shell.setText(getText());
		shell.setLayout(new BorderLayout(0, 0));
		
		text = new Text(shell, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL);
		text.setFont(SWTResourceManager.getFont("Lucida Console", 9, SWT.NORMAL));
		text.setEditable(false);
		text.setLayoutData(BorderLayout.CENTER);

	}

	public void addToDisplay(final String line) 
	{
		display.asyncExec(
				  new Runnable() {
					  public void run()
					  {
						  text.append(line + System.getProperty("line.separator"));
						  
					  }
				  });
	}

	
	public void Connect()
	{
		try 
		{
			this.sock = new Socket(this.host, this.port);
			inputT = new Thread(new LogInputThread(this.sock.getInputStream()));
			inputT.start();
			
			sock.getOutputStream().write("ui logview\n".getBytes());
		} 
		catch (UnknownHostException e) 
		{
			MainWin.addToDisplay(e.getMessage());
		} 
		catch (IOException e) 
		{
			MainWin.addToDisplay(e.getMessage());
		}
		
		
	}
	
	
}
