package com.groupunix.dwlite;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Toolkit;
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
		frmDwlite.setBounds(100, 100, 480, 364);
		frmDwlite.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmDwlite.getContentPane().setLayout(new MigLayout("", "[70px:70px,grow]", "[grow]"));
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		frmDwlite.getContentPane().add(tabbedPane, "cell 0 0,grow");
		
		JPanel panel = new JPanel();
		tabbedPane.addTab(" Drives ", new ImageIcon(DWLite.class.getResource("/fs/unknown.png")), panel, null);
		panel.setLayout(new MigLayout("", "[][][grow,fill][]", "[][][][][][]"));
		
		JButton btnDrive = new JButton("");
		btnDrive.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				  doDiskInsert(0);
			}
		});
		panel.add(btnDrive, "cell 0 0");
		btnDrive.setIcon(new ImageIcon(DWLite.class.getResource("/lite/disk0.png")));
		
		lblDisk0 = new JLabel("Not loaded");
		panel.add(lblDisk0, "cell 1 0 2 1");
		
		JButton button = new JButton("");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) 
			{
				doCommand("dw disk eject 0");
			}
		});
		panel.add(button, "cell 3 0");
		button.setIcon(new ImageIcon(DWLite.class.getResource("/lite/eject.png")));
		
		JButton btnDrive_1 = new JButton("");
		panel.add(btnDrive_1, "cell 0 1");
		btnDrive_1.setIcon(new ImageIcon(DWLite.class.getResource("/lite/disk1.png")));
		btnDrive_1.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				  doDiskInsert(1);
			}
		});
		
		
		lblDisk1 = new JLabel("Not loaded");
		panel.add(lblDisk1, "cell 1 1 2 1");
		
		JButton button_1 = new JButton("");
		panel.add(button_1, "cell 3 1");
		button_1.setIcon(new ImageIcon(DWLite.class.getResource("/lite/eject.png")));
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) 
			{
				doCommand("dw disk eject 1");
			}
		});
		
		JButton btnDrive_2 = new JButton("");
		panel.add(btnDrive_2, "cell 0 2");
		btnDrive_2.setIcon(new ImageIcon(DWLite.class.getResource("/lite/disk2.png")));
		btnDrive_2.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				  doDiskInsert(2);
			}
		});
		
		lblDisk2 = new JLabel("Not loaded");
		panel.add(lblDisk2, "cell 1 2 2 1");
		
		JButton button_2 = new JButton("");
		panel.add(button_2, "cell 3 2");
		button_2.setIcon(new ImageIcon(DWLite.class.getResource("/lite/eject.png")));
		button_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) 
			{
				doCommand("dw disk eject 2");
			}
		});
		
		JButton btnDrive_3 = new JButton("");
		panel.add(btnDrive_3, "cell 0 3");
		btnDrive_3.setIcon(new ImageIcon(DWLite.class.getResource("/lite/disk3.png")));
		btnDrive_3.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				  doDiskInsert(3);
			}
		});
		
		lblDisk3 = new JLabel("Not loaded");
		panel.add(lblDisk3, "cell 1 3 2 1");
		
		JButton button_3 = new JButton("");
		panel.add(button_3, "cell 3 3");
		button_3.setIcon(new ImageIcon(DWLite.class.getResource("/lite/eject.png")));
		
	
		
		lblDiskX = new JLabel("Not loaded");
		panel.add(lblDiskX, "cell 2 4");
		
		
		spinnerDriveX.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) 
			{
				updateDriveDisplay();
			}
		});
		spinnerDriveX.setModel(new SpinnerNumberModel(4, 4, 255, 1));
		panel.add(spinnerDriveX, "cell 0 5,growx");
		
		JButton button_4 = new JButton("");
		button_4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) 
			{
				doDiskInsert((Integer) spinnerDriveX.getValue());
			}
		});
		button_4.setIcon(new ImageIcon(DWLite.class.getResource("/lite/diskX.png")));
		panel.add(button_4, "cell 0 4");
		
		JButton btnEjectX = new JButton("");
		btnEjectX.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
			{
				doCommand("dw disk eject " + (Integer)spinnerDriveX.getValue());
			}
		});
		btnEjectX.setIcon(new ImageIcon(DWLite.class.getResource("/lite/eject.png")));
		panel.add(btnEjectX, "cell 3 4");
		
		
		
		button_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) 
			{
				doCommand("dw disk eject 3");
			}
		});
		
		
		JPanel panel_2 = new JPanel();
		tabbedPane.addTab("CLI", new ImageIcon(DWLite.class.getResource("/menu/preferences-system-network-2.png")), panel_2, null);
		panel_2.setLayout(new MigLayout("", "[grow,left]", "[grow][]"));
		
		textFieldCLIInput = new JTextField();
		textFieldCLIInput.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				doCommand(textFieldCLIInput.getText());
				textFieldCLIInput.setText("");
			}
		});
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		panel_2.add(scrollPane, "cell 0 0,grow");
		
		textAreaCLIOutput = new JTextArea();
		textAreaCLIOutput.setFont(new Font("Monospaced", Font.PLAIN, 11));
		textAreaCLIOutput.setForeground(Color.WHITE);
		textAreaCLIOutput.setBackground(Color.DARK_GRAY);
		scrollPane.setViewportView(textAreaCLIOutput);
		panel_2.add(textFieldCLIInput, "cell 0 1,growx");
		textFieldCLIInput.setColumns(10);
		
		JPanel panel_1 = new JPanel();
		tabbedPane.addTab(" Log ", new ImageIcon(DWLite.class.getResource("/menu/accessories-text-editor-3.png")), panel_1, null);
		panel_1.setLayout(new MigLayout("", "[4px,grow,fill]", "[grow]"));
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		panel_1.add(scrollPane_1, "cell 0 0,grow");
		
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
					this.lblDisk0.setText(prettyPath(dw.getDiskDrives().getDisk(0).getFilePath()));
				else
					this.lblDisk0.setText("Not loaded");
				
				if (dw.getDiskDrives().isLoaded(1))
					this.lblDisk1.setText(prettyPath(dw.getDiskDrives().getDisk(1).getFilePath()));
				else
					this.lblDisk1.setText("Not loaded");
				
				if (dw.getDiskDrives().isLoaded(2))
					this.lblDisk2.setText(prettyPath(dw.getDiskDrives().getDisk(2).getFilePath()));
				else
					this.lblDisk2.setText("Not loaded");
				
				if (dw.getDiskDrives().isLoaded(3))
					this.lblDisk3.setText(prettyPath(dw.getDiskDrives().getDisk(3).getFilePath()));
				else
					this.lblDisk3.setText("Not loaded");
				
				if (dw.getDiskDrives().isLoaded((Integer)spinnerDriveX.getValue()))
					this.lblDiskX.setText(prettyPath(dw.getDiskDrives().getDisk((Integer)spinnerDriveX.getValue()).getFilePath()));
				else
					this.lblDiskX.setText("Not loaded");
				
				
			
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


	private String prettyPath(String path) 
	{
		String res = path;
		
		if (path.startsWith("file://"))
			res = path.substring(7);
		
		return res;
	}















	protected JTextArea getTextAreaLog() {
		return textAreaLog;
	}
	protected JTextArea getTextAreaCLIOutput() {
		return textAreaCLIOutput;
	}
	protected JTextField getTextFieldCLIInput() {
		return textFieldCLIInput;
	}
}
