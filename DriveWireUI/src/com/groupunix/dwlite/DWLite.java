package com.groupunix.dwlite;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import net.miginfocom.swing.MigLayout;

import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwcommands.DWCmd;
import com.groupunix.drivewireserver.dwcommands.DWCommandList;
import com.groupunix.drivewireserver.dwcommands.DWCommandResponse;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotLoadedException;
import com.groupunix.drivewireserver.dwexceptions.DWDriveNotValidException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;
import com.groupunix.drivewireui.MainWin;
import java.awt.GridLayout;
import java.awt.Dimension;

public class DWLite 
{

	
	private JFrame frmDwlite;

	protected static long servermagic;
	private JTextField textFieldCLIInput;
	private JTextArea textAreaLog;
	private JTextArea textAreaCLIOutput;
	private int handlerno = 0;
	private JCheckBoxMenuItem chckbxmntmHdbdosTranslation;
	private JLabel lblDisk0;
	private JLabel lblDisk1;
	private JLabel lblDisk2;
	private JLabel lblDisk3;
	private JLabel lblDiskX;
	private final JSpinner spinnerDriveX = new JSpinner();

	private String lastDir = ".";
	private JLabel lblDisk0Path;
	private JLabel lblDisk1Path;
	private JPanel panelDrives;
	private JLabel lblDisk3Path;
	private JLabel lblDisk2Path;
	private JLabel lblDiskXPath;

	protected boolean showDriveX = false;
	private JButton btnEjectX;

	private JCheckBoxMenuItem chckbxmntmShowDriveX;
	private JButton button_X;

	private JScrollPane sp;

	/**
	 * Launch the application.
	 */
	public static void main(final String[] args) 
	{
		EventQueue.invokeLater(new Runnable() 
		{
			public void run() {
				try {
					
					
					try 
					{
					    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) 
					    {
					       if ("Windows".equals(info.getName())) {
					            UIManager.setLookAndFeel(info.getClassName());
					            break;
					       }
					    	
					    	
					    }
					} 
					catch (Exception e) 
					{
					  
					    try 
					    {
					        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
					    } 
					    catch (Exception ex) 
					    {
					        // whatever
					    }
					}
					
					DWLite window = new DWLite();
					
					Thread diskUpdate = new Thread(new DiskViewUpdater(window));
					diskUpdate.setDaemon(true);
					diskUpdate.start();
					
					
					window.frmDwlite.setVisible(true);
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
				}
			}
		});
	}
	
	
	
	

	
	
	
	
	
	
	
	
	

	/**
	 * Create the application.
	 * @param args 
	 */
	public DWLite() 
	{
		
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() 
	{
		frmDwlite = new JFrame();
		frmDwlite.setIconImage(Toolkit.getDefaultToolkit().getImage(DWLite.class.getResource("/dw/dw4square.jpg")));
		
		frmDwlite.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		frmDwlite.addWindowListener( new WindowAdapter()
		{
		    public void windowClosing(WindowEvent e)
		    {
		    	MainWin.stopDWServer();
		    }
		});
		
		
		
		frmDwlite.setTitle("DW4Lite");
		frmDwlite.setBounds(100, 100, 382, 403);
		frmDwlite.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmDwlite.getContentPane().setLayout(new MigLayout("", "[366px,grow,fill]", "[312px,grow,fill]"));
		
		sp = new JScrollPane();
		sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
		frmDwlite.getContentPane().add(sp, "cell 0 0,grow");
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		
		sp.setViewportView(tabbedPane);
		
		//frmDwlite.getContentPane().add(tabbedPane, "cell 0 1,grow");
		
		panelDrives = new JPanel();
		tabbedPane.addTab(" Drives ", new ImageIcon(DWLite.class.getResource("/fs/unknown.png")), panelDrives, null);
		panelDrives.setLayout(new MigLayout("", "[][grow,fill][]", "[20px:20px][20px:20px][20px:20px][20px:20px][20px:20px][20px:20px][20px:20px][20px:20px][20px:20px][20px:20px][]"));
		
		JButton btnDrive = new JButton("");
		btnDrive.setMinimumSize(new Dimension(24, 9));
		btnDrive.setMaximumSize(new Dimension(35, 35));
		btnDrive.setToolTipText("Load Drive 0");
		btnDrive.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				  doDiskInsert(0);
			}
		});
		panelDrives.add(btnDrive, "cell 0 0 1 2");
		btnDrive.setIcon(new ImageIcon(DWLite.class.getResource("/lite/disk0.png")));
		
		lblDisk0 = new JLabel("Not loaded");
		lblDisk0.setFont(new Font("Tahoma", Font.BOLD, 11));
		panelDrives.add(lblDisk0, "cell 1 0,alignx left,aligny bottom");
		
		JButton button = new JButton("");
		button.setMaximumSize(new Dimension(35, 35));
		button.setToolTipText("Eject Drive 0");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) 
			{
				doCommand("dw disk eject 0");
			}
		});
		panelDrives.add(button, "cell 2 0 1 2,alignx right,aligny center");
		button.setIcon(new ImageIcon(DWLite.class.getResource("/lite/eject.png")));
		
		lblDisk0Path = new JLabel(" ");
		panelDrives.add(lblDisk0Path, "cell 1 1,alignx left,aligny top");
		
		JButton btnDrive_1 = new JButton("");
		btnDrive_1.setMaximumSize(new Dimension(35, 35));
		btnDrive_1.setToolTipText("Load Drive 1");
		panelDrives.add(btnDrive_1, "cell 0 2 1 2");
		btnDrive_1.setIcon(new ImageIcon(DWLite.class.getResource("/lite/disk1.png")));
		btnDrive_1.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				  doDiskInsert(1);
			}
		});
		
		
		lblDisk1 = new JLabel("Not loaded");
		lblDisk1.setFont(new Font("Tahoma", Font.BOLD, 11));
		panelDrives.add(lblDisk1, "cell 1 2,aligny bottom");
		
		JButton button_1 = new JButton("");
		button_1.setMaximumSize(new Dimension(35, 35));
		button_1.setToolTipText("Eject Drive 1");
		panelDrives.add(button_1, "cell 2 2 1 2,alignx right");
		button_1.setIcon(new ImageIcon(DWLite.class.getResource("/lite/eject.png")));
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) 
			{
				doCommand("dw disk eject 1");
			}
		});
		
		lblDisk1Path = new JLabel(" ");
		panelDrives.add(lblDisk1Path, "cell 1 3,growx,aligny top");
		
		JButton btnDrive_2 = new JButton("");
		btnDrive_2.setMaximumSize(new Dimension(35, 35));
		btnDrive_2.setToolTipText("Load Drive 2");
		panelDrives.add(btnDrive_2, "cell 0 4 1 2");
		btnDrive_2.setIcon(new ImageIcon(DWLite.class.getResource("/lite/disk2.png")));
		btnDrive_2.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				  doDiskInsert(2);
			}
		});
		
		lblDisk2 = new JLabel("Not loaded");
		lblDisk2.setFont(new Font("Tahoma", Font.BOLD, 11));
		panelDrives.add(lblDisk2, "cell 1 4,aligny bottom");
		
		JButton button_2 = new JButton("");
		button_2.setMaximumSize(new Dimension(35, 35));
		button_2.setToolTipText("Eject Drive 2");
		panelDrives.add(button_2, "cell 2 4 1 2");
		button_2.setIcon(new ImageIcon(DWLite.class.getResource("/lite/eject.png")));
		button_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) 
			{
				doCommand("dw disk eject 2");
			}
		});
		
		lblDisk2Path = new JLabel(" ");
		panelDrives.add(lblDisk2Path, "cell 1 5,growx,aligny top");
		
		JButton btnDrive_3 = new JButton("");
		btnDrive_3.setMaximumSize(new Dimension(35, 35));
		btnDrive_3.setToolTipText("Load Drive 3");
		panelDrives.add(btnDrive_3, "cell 0 6 1 2");
		btnDrive_3.setIcon(new ImageIcon(DWLite.class.getResource("/lite/disk3.png")));
		btnDrive_3.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				  doDiskInsert(3);
			}
		});
		
		lblDisk3 = new JLabel("Not loaded");
		lblDisk3.setFont(new Font("Tahoma", Font.BOLD, 11));
		panelDrives.add(lblDisk3, "cell 1 6,aligny bottom");
		
		JButton button_3 = new JButton("");
		button_3.setMaximumSize(new Dimension(35, 35));
		button_3.setToolTipText("Eject Drive 3");
		panelDrives.add(button_3, "cell 2 6 1 2");
		button_3.setIcon(new ImageIcon(DWLite.class.getResource("/lite/eject.png")));
		
		lblDisk3Path = new JLabel(" ");
		panelDrives.add(lblDisk3Path, "cell 1 7,growx,aligny top");
		
	
		
		lblDiskX = new JLabel("Not loaded");
		lblDiskX.setFont(new Font("Tahoma", Font.BOLD, 11));
		panelDrives.add(lblDiskX, "cell 1 8,aligny bottom");
		spinnerDriveX.setMaximumSize(new Dimension(40, 20));
		spinnerDriveX.setToolTipText("Choose which drive 4 through 255 to display in Drive X slot");
		
		
		spinnerDriveX.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) 
			{
				updateDriveDisplay();
			}
		});
		
		lblDiskXPath = new JLabel(" ");
		panelDrives.add(lblDiskXPath, "cell 1 9,growx,aligny top");
		spinnerDriveX.setModel(new SpinnerNumberModel(4, 4, 255, 1));
		panelDrives.add(spinnerDriveX, "cell 0 10,growx");
		
		button_X = new JButton("");
		button_X.setMaximumSize(new Dimension(35, 35));
		button_X.setToolTipText(" Load Drive X (choose number below)");
		button_X.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) 
			{
				doDiskInsert((Integer) spinnerDriveX.getValue());
			}
		});
		button_X.setIcon(new ImageIcon(DWLite.class.getResource("/lite/diskX.png")));
		panelDrives.add(button_X, "cell 0 8 1 2");
		
		btnEjectX = new JButton("");
		btnEjectX.setMaximumSize(new Dimension(35, 35));
		btnEjectX.setToolTipText("Eject Drive X");
		btnEjectX.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
			{
				doCommand("dw disk eject " + (Integer)spinnerDriveX.getValue());
			}
		});
		btnEjectX.setIcon(new ImageIcon(DWLite.class.getResource("/lite/eject.png")));
		panelDrives.add(btnEjectX, "cell 2 8 1 2");
		
		
		
		button_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) 
			{
				doCommand("dw disk eject 3");
			}
		});
		
		
		JPanel panelCLI = new JPanel();
		tabbedPane.addTab("CLI", new ImageIcon(DWLite.class.getResource("/menu/preferences-system-network-2.png")), panelCLI, null);
		panelCLI.setLayout(new MigLayout("", "[grow,left]", "[grow][]"));
		
		textFieldCLIInput = new JTextField();
		textFieldCLIInput.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				doCommand(textFieldCLIInput.getText());
				textFieldCLIInput.setText("");
			}
		});
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		panelCLI.add(scrollPane, "cell 0 0,grow");
		
		textAreaCLIOutput = new JTextArea();
		textAreaCLIOutput.setFont(new Font("Monospaced", Font.PLAIN, 11));
		textAreaCLIOutput.setForeground(Color.WHITE);
		textAreaCLIOutput.setBackground(Color.DARK_GRAY);
		scrollPane.setViewportView(textAreaCLIOutput);
		panelCLI.add(textFieldCLIInput, "cell 0 1,growx");
		textFieldCLIInput.setColumns(10);
		
		JPanel panelLog = new JPanel();
		tabbedPane.addTab(" Log ", new ImageIcon(DWLite.class.getResource("/menu/accessories-text-editor-3.png")), panelLog, null);
		panelLog.setLayout(new MigLayout("", "[4px,grow,fill]", "[grow]"));
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		panelLog.add(scrollPane_1, "cell 0 0,grow");
		
		textAreaLog = new JTextArea();
		scrollPane_1.setViewportView(textAreaLog);
		textAreaLog.setFont(new Font("Monospaced", Font.PLAIN, 11));
		textAreaLog.setEditable(false);
		
		JMenuBar menuBar = new JMenuBar();
		frmDwlite.setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.setIcon(new ImageIcon(DWLite.class.getResource("/menu/application-exit-5.png")));
		mntmExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) 
			{
				WindowEvent windowClosing = new WindowEvent(frmDwlite, WindowEvent.WINDOW_CLOSING);
				frmDwlite.dispatchEvent(windowClosing);
			
				
			}
		});
		mnFile.add(mntmExit);
		
		JMenu mnOptions = new JMenu("Options");
		mnOptions.addMenuListener(new MenuListener() {
			public void menuCanceled(MenuEvent arg0) {
			}
			public void menuDeselected(MenuEvent arg0) {
			}
			public void menuSelected(MenuEvent arg0) 
			{
				if ((DriveWireServer.getHandler(handlerno) != null) && (DriveWireServer.getHandler(handlerno).getConfig() != null))
				{
					chckbxmntmHdbdosTranslation.setEnabled(true);
					chckbxmntmHdbdosTranslation.setSelected(DriveWireServer.getHandler(handlerno).getConfig().getBoolean("HDBDOSMode", false));
				}
				else
					chckbxmntmHdbdosTranslation.setEnabled(false);
				
				chckbxmntmShowDriveX.setSelected(showDriveX);
					
				
			}
		});
		menuBar.add(mnOptions);
		
		chckbxmntmHdbdosTranslation = new JCheckBoxMenuItem("HDBDOS Translation");
		
		chckbxmntmHdbdosTranslation.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (DriveWireServer.getHandler(handlerno).getConfig().getBoolean("HDBDOSMode", false))
					doCommand("dw config set HDBDOSMode false");
				else
					doCommand("dw config set HDBDOSMode true");
				
			}
		});
		mnOptions.add(chckbxmntmHdbdosTranslation);
		
		chckbxmntmShowDriveX = new JCheckBoxMenuItem("Show Drive X ");
		chckbxmntmShowDriveX.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (showDriveX )
					showDriveX = false;
				else
					showDriveX = true;
				updateDriveDisplay();
				
			}
		});
		mnOptions.add(chckbxmntmShowDriveX);
		
		JMenu mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);
		
		JMenuItem mntmAbout = new JMenuItem("About..");
		mntmAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
			{
				AboutDWLite a = new AboutDWLite();
				a.setVisible(true);
			}
		});
		mntmAbout.setIcon(new ImageIcon(DWLite.class.getResource("/menu/help-about-3.png")));
		mnHelp.add(mntmAbout);
		
		
		DriveWireServer.getLogger().addAppender(new DWLiteLogAppender(textAreaLog));
		
		
		this.btnEjectX.setVisible(false);
		this.lblDiskX.setVisible(false);
		this.lblDiskXPath.setVisible(false);
		this.spinnerDriveX.setVisible(false);
		this.button_X.setVisible(false);
		
		frmDwlite.pack();
		
	}
	


	protected void doDiskInsert(int diskno) 
	{
		JFileChooser c = new JFileChooser(lastDir);
	     
	      int rVal = c.showOpenDialog(frmDwlite);
	      
	      if (rVal == JFileChooser.APPROVE_OPTION) 
	      {
	    	  lastDir  = c.getCurrentDirectory().toString();
	    	  doCommand("dw disk insert " + diskno + " " + c.getSelectedFile().getAbsolutePath());
	      }
	}





	protected void doCommand(String cmd) 
	{
		DWCommandList commands = new DWCommandList(DriveWireServer.getHandler(0));
		commands.addcommand(new DWCmd(DriveWireServer.getHandler(0)));	
		
		DWCommandResponse response = commands.parse(cmd);
		
		this.textAreaCLIOutput.append(response.getResponseText());
		
		if (!response.getResponseText().endsWith(System.getProperty("line.separator")))
			this.textAreaCLIOutput.append(System.getProperty("line.separator"));
		
		updateDriveDisplay();
	}




	public void updateDriveDisplay() 
	{
		DWProtocolHandler dw = (DWProtocolHandler) DriveWireServer.getHandler(handlerno);
		
		if (dw != null)
		{
			try 
			{
				if (dw.getDiskDrives().isLoaded(0))
				{
					this.lblDisk0.setText(prettyFile(dw.getDiskDrives().getDisk(0).getFilePath()));
					this.lblDisk0Path.setText(prettyPath(dw.getDiskDrives().getDisk(0).getFilePath()));
				}
				else
				{
					this.lblDisk0.setText("Not loaded");
					this.lblDisk0Path.setText("");
				}
				
				if (dw.getDiskDrives().isLoaded(1))
				{
					this.lblDisk1.setText(prettyFile(dw.getDiskDrives().getDisk(1).getFilePath()));
					this.lblDisk1Path.setText(prettyPath(dw.getDiskDrives().getDisk(1).getFilePath()));
				}
				else
				{
					this.lblDisk1.setText("Not loaded");
					this.lblDisk1Path.setText("");
				}
				
				if (dw.getDiskDrives().isLoaded(2))
				{
					this.lblDisk2.setText(prettyFile(dw.getDiskDrives().getDisk(2).getFilePath()));
					this.lblDisk2Path.setText(prettyPath(dw.getDiskDrives().getDisk(2).getFilePath()));
				}
				else
				{
					this.lblDisk2.setText("Not loaded");
					this.lblDisk2Path.setText("");
				}
				
				if (dw.getDiskDrives().isLoaded(3))
				{
					this.lblDisk3.setText(prettyFile(dw.getDiskDrives().getDisk(3).getFilePath()));
					this.lblDisk3Path.setText(prettyPath(dw.getDiskDrives().getDisk(3).getFilePath()));
				}
				else
				{
					this.lblDisk3.setText("Not loaded");
					this.lblDisk3Path.setText("");
				}
				
				setDriveXVisible(showDriveX);
				
				if (showDriveX)
				{
					if (dw.getDiskDrives().isLoaded((Integer)spinnerDriveX.getValue()))
					{
						this.lblDiskX.setText(prettyFile(dw.getDiskDrives().getDisk((Integer)spinnerDriveX.getValue()).getFilePath()));
						this.lblDiskXPath.setText(prettyPath(dw.getDiskDrives().getDisk((Integer)spinnerDriveX.getValue()).getFilePath()));
					}
					else
					{
						this.lblDiskX.setText("Not loaded");
						this.lblDiskXPath.setText("");
					}
					
				}
			
			} 
			catch (DWDriveNotLoadedException e) 
			{
					e.printStackTrace();
				
			} 
			catch (DWDriveNotValidException e) 
			{
					e.printStackTrace();
				
			}
		}
			
	}


	private void setDriveXVisible(boolean vis) 
	{
		this.btnEjectX.setVisible(vis);
		this.lblDiskX.setVisible(vis);
		this.lblDiskXPath.setVisible(vis);
		this.spinnerDriveX.setVisible(vis);
		this.button_X.setVisible(vis);
		
	}















	private String prettyPath(String path) 
	{
		String res = path;
		
		if (path.startsWith("file:///"))
			res = path.substring(8);
		
		if (res.indexOf("/") > -1)
			res = res.substring(0, res.lastIndexOf("/"));
		
		
		
		return res;
	}

	private String prettyFile(String path) 
	{
		String res = path;
		
		res = res.substring(res.lastIndexOf("/")+1);
		
		
		return res;
	}












	protected JButton getBtnEjectX() {
		return btnEjectX;
	}
	protected JSpinner getSpinnerDriveX() {
		return spinnerDriveX;
	}
	protected JButton getButton_4() {
		return button_X;
	}
}
