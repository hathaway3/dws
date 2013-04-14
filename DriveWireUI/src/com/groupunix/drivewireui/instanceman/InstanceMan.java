package com.groupunix.drivewireui.instanceman;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import com.groupunix.drivewireui.GradientHelper;
import com.groupunix.drivewireui.MainWin;

public class InstanceMan extends Shell {

	
	public InstanceMan(Display display) {
		super(display, SWT.CLOSE | SWT.RESIZE | SWT.TITLE);
		setImage(SWTResourceManager.getImage(InstanceMan.class, "/menu/database-gear.png"));
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.verticalSpacing = 0;
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		gridLayout.horizontalSpacing = 0;
		setLayout(gridLayout);
		
		final Composite composite_3 = new Composite(this, SWT.NONE);
		composite_3.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		composite_3.setLayout(new GridLayout(1, false));
		
		GradientHelper.applyVerticalGradientBG(composite_3, MainWin.getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT),MainWin.getDisplay().getSystemColor(SWT.COLOR_GRAY));
		
		composite_3.addListener(SWT.Resize, new Listener() {

			@Override
			public void handleEvent(Event event)
			{
				GradientHelper.applyVerticalGradientBG(composite_3 , MainWin.getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT),MainWin.getDisplay().getSystemColor(SWT.COLOR_GRAY));
				
				
			} } );
		
		composite_3.setBackgroundMode(SWT.INHERIT_FORCE);
		
		
		
		ToolBar toolBar_1 = new ToolBar(composite_3, SWT.FLAT | SWT.RIGHT);
			
		ToolItem tltmNewInstance = new ToolItem(toolBar_1, SWT.NONE);
		tltmNewInstance.setImage(SWTResourceManager.getImage(InstanceMan.class, "/menu/database-add.png"));
		tltmNewInstance.setText("New Instance..");
		
		ExpandBar expandBar = new ExpandBar(this, SWT.NONE);
		expandBar.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		ExpandItem xpndtmInstance = new ExpandItem(expandBar, SWT.NONE);
		xpndtmInstance.setExpanded(true);
		xpndtmInstance.setImage(SWTResourceManager.getImage(InstanceMan.class, "/status/completed_16.png"));
		xpndtmInstance.setText("Instance 0");
		
		Composite composite = new Composite(expandBar, SWT.NONE);
		composite.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		composite.setBackgroundMode(SWT.INHERIT_FORCE);
		
		xpndtmInstance.setControl(composite);
		xpndtmInstance.setHeight(87);
		composite.setLayout(new GridLayout(2, false));
		
		ToolBar toolBar = new ToolBar(composite, SWT.FLAT | SWT.RIGHT);
		toolBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		
		ToolItem tltmStop = new ToolItem(toolBar, SWT.NONE);
		tltmStop.setImage(SWTResourceManager.getImage(InstanceMan.class, "/instman/media-playback-stop-blue.png"));
		tltmStop.setText("Stop");
		
		ToolItem toolItem = new ToolItem(toolBar, SWT.SEPARATOR);
		toolItem.setText(" ");
		
		ToolItem tltmRestart = new ToolItem(toolBar, SWT.NONE);
		tltmRestart.setImage(SWTResourceManager.getImage(InstanceMan.class, "/instman/media-repeat-blue.png"));
		tltmRestart.setText("Restart");
		
		ToolItem toolItem_1 = new ToolItem(toolBar, SWT.SEPARATOR);
		
		ToolItem tltmAutostart = new ToolItem(toolBar, SWT.CHECK);
		tltmAutostart.setImage(SWTResourceManager.getImage(InstanceMan.class, "/menu/active.png"));
		tltmAutostart.setText("AutoStart");
		
		ToolItem toolItem_2 = new ToolItem(toolBar, SWT.SEPARATOR);
		
		ToolItem tltmConnect = new ToolItem(toolBar, SWT.NONE);
		tltmConnect.setImage(SWTResourceManager.getImage(InstanceMan.class, "/menu/database-go.png"));
		tltmConnect.setText("Connect ");
		
		Composite compositeStatus = new Composite(composite, SWT.BORDER);
		compositeStatus.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		compositeStatus.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		compositeStatus.setLayout(new GridLayout(3, false));
	
		
		Label lblDevImage = new Label(compositeStatus, SWT.NONE);
		
		GridData gd_lblDevImage = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 2);
		gd_lblDevImage.horizontalIndent = 3;
		lblDevImage.setLayoutData(gd_lblDevImage);
		lblDevImage.setImage(SWTResourceManager.getImage(InstanceMan.class, "/instman/network-connect-3.png"));
		
		Label label = new Label(compositeStatus, SWT.NONE);
		label.setText(" ");
		
		Label lblTcpServer = new Label(compositeStatus, SWT.NONE);
		
	
		lblTcpServer.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		lblTcpServer.setText("TCP Server, listening on port 65504");
		new Label(compositeStatus, SWT.NONE);
		
		Label lblClientConnectedFrom = new Label(compositeStatus, SWT.NONE);
	

		lblClientConnectedFrom.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		lblClientConnectedFrom.setText("Client connected from 192.168.128.50");
		
		ExpandItem xpndtmInstance_1 = new ExpandItem(expandBar, SWT.NONE);
		xpndtmInstance_1.setExpanded(true);
		xpndtmInstance_1.setImage(SWTResourceManager.getImage(InstanceMan.class, "/menu/application-exit-5.png"));
		xpndtmInstance_1.setText("Instance 1");
		
		Composite composite_1 = new Composite(expandBar, SWT.NONE);
		composite_1.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		xpndtmInstance_1.setControl(composite_1);
		xpndtmInstance_1.setHeight(87);
		composite_1.setLayout(new GridLayout(1, false));
		
		ToolBar toolBar_2 = new ToolBar(composite_1, SWT.FLAT | SWT.RIGHT);
		toolBar_2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		ToolItem tltmStart = new ToolItem(toolBar_2, SWT.NONE);
		tltmStart.setImage(SWTResourceManager.getImage(InstanceMan.class, "/instman/media-playback-play-blue.png"));
		tltmStart.setText("Start");
		
		ToolItem toolItem_3 = new ToolItem(toolBar_2, SWT.SEPARATOR);
		
		ToolItem tltmDelete = new ToolItem(toolBar_2, SWT.NONE);
		tltmDelete.setImage(SWTResourceManager.getImage(InstanceMan.class, "/menu/database-delete.png"));
		tltmDelete.setText("Delete");
		
		ToolItem tltmSep1 = new ToolItem(toolBar_2, SWT.SEPARATOR);
		
		ToolItem tltmAutostart_1 = new ToolItem(toolBar_2, SWT.NONE);
		tltmAutostart_1.setImage(SWTResourceManager.getImage(InstanceMan.class, "/menu/inactive.png"));
		tltmAutostart_1.setText("AutoStart");
		
		ToolItem toolItem_4 = new ToolItem(toolBar_2, SWT.SEPARATOR);
		
		ToolItem tltmConnect_1 = new ToolItem(toolBar_2, SWT.NONE);
		tltmConnect_1.setImage(SWTResourceManager.getImage(InstanceMan.class, "/menu/database-go.png"));
		tltmConnect_1.setText("Connect");
		
		Composite composite_2 = new Composite(composite_1, SWT.BORDER);
		composite_2.setLayout(new GridLayout(3, false));
		composite_2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		
		Label label_1 = new Label(composite_2, SWT.NONE);
		label_1.setImage(SWTResourceManager.getImage(InstanceMan.class, "/instman/network-disconnect-3.png"));
		GridData gd_label_1 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 2);
		gd_label_1.horizontalIndent = 3;
		label_1.setLayoutData(gd_label_1);
		
		Label label_2 = new Label(composite_2, SWT.NONE);
		label_2.setText(" ");
		
		Label lblTcpClientHost = new Label(composite_2, SWT.NONE);
		lblTcpClientHost.setText("TCP Client, host 192.168.128.50 port 65504");
		new Label(composite_2, SWT.NONE);
		
		Label lblNoConnectionRetrying = new Label(composite_2, SWT.NONE);
		lblNoConnectionRetrying.setText("No connection, retrying...");
		
		pack();
		createContents();
	}

	/**
	 * Create contents of the shell.
	 */
	protected void createContents() {
		setText("Instance Manager");
		setSize(380, 331);

	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
