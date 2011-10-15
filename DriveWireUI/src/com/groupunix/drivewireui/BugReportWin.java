package com.groupunix.drivewireui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class BugReportWin extends Dialog {

	protected Object result;
	protected Shell shlBugReport;

	private Button btnClose;
	
	private String title;
	private String summary;
	private String detail;
	private Text textAdditionalInfo;
	private Text textEmail;
	private Button btnErrMsg;
	private Button btnServerConf;
	private Button btnUIConf;
	private Button btnErrDetails;
	
	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public BugReportWin(Shell parent, int style, String title, String summary, String detail) {
		super(parent, style);
		this.title = title;
		this.summary = summary;
		this.detail = detail;
		
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		createContents();

		shlBugReport.open();
		shlBugReport.layout();
		Display display = getParent().getDisplay();
		while (!shlBugReport.isDisposed()) {
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
		shlBugReport = new Shell(getParent(), getStyle());
		shlBugReport.setSize(598, 504);
		shlBugReport.setText("Bug Report for '" + title + "'");
		
		
		FontData f = new FontData(MainWin.config.getString("DialogFont",MainWin.default_DialogFont), MainWin.config.getInt("DialogFontSize", MainWin.default_DialogFontSize), MainWin.config.getInt("DialogFontStyle", MainWin.default_DialogFontStyle) );
		
		
		btnClose = new Button(shlBugReport, SWT.NONE);
		btnClose.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
			
				shlBugReport.close();
			
			}
		});
		btnClose.setBounds(493, 440, 75, 25);
		btnClose.setText("Cancel");
		
		Label lblIfYouBelieve = new Label(shlBugReport, SWT.WRAP);
		lblIfYouBelieve.setBounds(25, 25, 543, 114);
		lblIfYouBelieve.setText("Please submit as much information as you can about this problem.  Internet access is required to submit a bug report.\r\n\r\nFor those concerned with privacy, you should know that all information in this bug report is sent in plain text over the internet.  While it will never intentionally be made public, the author offers absolutely no promise of confidentiality.   On the other hand, it's just some DriveWire configuration data and will normally not contain anything sensitive at all.\r\n\r\n");
		
		btnErrMsg = new Button(shlBugReport, SWT.CHECK);
		btnErrMsg.setSelection(true);
		btnErrMsg.setBounds(25, 176, 382, 16);
		btnErrMsg.setText("The error message itself (if any)");
		
		Label lblDataToInclude = new Label(shlBugReport, SWT.NONE);
		lblDataToInclude.setBounds(25, 145, 226, 25);
		lblDataToInclude.setText("Data to include in this bug report:");
		
		btnErrDetails = new Button(shlBugReport, SWT.CHECK);
		btnErrDetails.setSelection(true);
		btnErrDetails.setText("The extended error details (as seen in the lower pane of the error dialog)");
		btnErrDetails.setBounds(25, 198, 529, 16);
		
		btnUIConf = new Button(shlBugReport, SWT.CHECK);
		btnUIConf.setSelection(true);
		btnUIConf.setBounds(25, 220, 543, 16);
		btnUIConf.setText("The DriveWire UI configuration (the contents of drivewireUI.xml on the client)");
		
		btnServerConf = new Button(shlBugReport, SWT.CHECK);
		btnServerConf.setSelection(true);
		btnServerConf.setText("The DriveWire server configuration (the contents of config.xml on the server)");
		btnServerConf.setBounds(25, 241, 543, 16);
		
		textAdditionalInfo = new Text(shlBugReport, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
		textAdditionalInfo.setBounds(25, 298, 543, 60);
		
		Label lblAdditionalInformationbe = new Label(shlBugReport, SWT.NONE);
		lblAdditionalInformationbe.setBounds(25, 277, 226, 15);
		lblAdditionalInformationbe.setText("Additional information (be verbose!):");
		
		Button btnNewButton = new Button(shlBugReport, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (doSubmit())
				{
					shlBugReport.close();
				}
				
			}
		});
		btnNewButton.setBounds(223, 440, 141, 25);
		btnNewButton.setText("Submit Bug Report");
		
		textEmail = new Text(shlBugReport, SWT.BORDER);
		textEmail.setBounds(25, 390, 288, 21);
		
		Label lblEmailAddressoptional = new Label(shlBugReport, SWT.NONE);
		lblEmailAddressoptional.setBounds(25, 369, 543, 15);
		lblEmailAddressoptional.setText("Email address (optional, used only to communicate regarding this report):");
		
		
		Control[] controls = shlBugReport.getChildren();
		
		for (int i = 0;i<controls.length;i++)
		{
			controls[i].setFont(new Font(shlBugReport.getDisplay(), f));
		}
		
	}

	protected boolean doSubmit() 
	{
		boolean res = false;
		
		String err = new String();
		
		// submit this thing..
		String surl = new String();
		
		surl = "br=0";
		
		try 
		{
			// Error details
			
			if (this.getBtnErrMsg().getSelection())
			{
				surl += "&" + encv("bugsum", this.summary);
			}
			
			if (this.getBtnErrDetails().getSelection())
			{
				surl += "&" + encv("bugdet", this.detail);
			}
			
			// DWUI
			
			if (this.getBtnUIConf().getSelection())
			{
				// DWUI version
				surl += "&" + encv("uiv",MainWin.DWUIVersion);
			
				// DWUI config
				StringWriter sw = new StringWriter();
				try 
				{
					MainWin.config.save(sw);
					surl += "&" + encv("uiconf", sw.getBuffer().toString());
				} 
				catch (ConfigurationException e) 
				{
					surl += "&" + encv("uiconf", e.getMessage());
				}
				
				
				
			}
			
			
			// Server
			if (this.getBtnServerConf().getSelection())
			{
				// Try to get server version..
				Connection conn = new Connection(MainWin.getHost(),MainWin.getPort(), MainWin.getInstance());
			
				ArrayList<String> vres = new ArrayList<String>();
		
				try 
				{
					conn.Connect();
					vres = conn.loadArrayList("ui server show version");	
					conn.close();
				
					surl += "&" + encv("dwv",vres.get(0));
				
				} 
				catch (UnknownHostException e) 
				{
					surl += "&" + encv("dwv",e.getMessage());
				} 
				catch (IOException e) 
				{
					surl += "&" + encv("dwv",e.getMessage());
				}
				
				// use local server config
				XMLConfiguration tmpc = new XMLConfiguration(MainWin.dwconfig);
				StringWriter sw = new StringWriter();
				try 
				{
					tmpc.save(sw);
					surl += "&" + encv("dwconf", sw.getBuffer().toString());
				} 
				catch (ConfigurationException e) 
				{
					surl += "&" + encv("dwconf", e.getMessage());
				}
				
				
			}
			
			// user
			if (!this.getTextAdditionalInfo().getText().equals(""))
			{
				surl += "&" + encv("usrinf",this.getTextAdditionalInfo().getText());
			}
			if (!this.getTextEmail().getText().equals(""))
			{
				surl += "&" + encv("usraddr",this.getTextEmail().getText());
			}
			
		} 
		catch (UnsupportedEncodingException e) 
		{
			err += " " + e.getMessage();
		}
		
		
		if (!surl.equals("br=0"))
		{
			// 	finally.. submit this thing
		
			URL url;
			try 
			{
				url = new URL("http://aaronwolfe.com:80/dw4/br.pl");
				URLConnection conn = url.openConnection();
				conn.setDoOutput(true);
				OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
				wr.write(surl);
				wr.flush();
				
				BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				String line;
				while ((line = rd.readLine()) != null) 
				{
					MainWin.addToDisplay(line);  
				}
				wr.close();
				rd.close();

				res = true;
			} 
			catch (MalformedURLException e) 
			{
				err += " " + e.getMessage();
			} 
			catch (IOException e) 
			{	
				err += " " + e.getMessage();
			}
	    	
		}
		else
		{
			err += " No data to send!";
		}
		
		if (!res)
		{
			MainWin.showError("This just isn't your day...", "We had an error sending the bug report.  Seek alternate routes, consult an exorcist, abort, retry, fail, etc...", "Possible clues: " + err);
		}
		
		
		return res;
	}

	private String encv(String key, String val) throws UnsupportedEncodingException 
	{
		String res = new String();
		
		res = URLEncoder.encode(key, "UTF-8") + "=" + URLEncoder.encode(val,"UTF-8");
		
		return res;
	}
	protected Button getBtnErrMsg() {
		return btnErrMsg;
	}
	protected Text getTextAdditionalInfo() {
		return textAdditionalInfo;
	}
	protected Text getTextEmail() {
		return textEmail;
	}
	protected Button getBtnServerConf() {
		return btnServerConf;
	}
	protected Button getBtnUIConf() {
		return btnUIConf;
	}
	protected Button getBtnErrDetails() {
		return btnErrDetails;
	}
}
