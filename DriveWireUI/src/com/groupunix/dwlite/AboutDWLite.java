package com.groupunix.dwlite;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Toolkit;
import javax.swing.JLabel;
import net.miginfocom.swing.MigLayout;
import javax.swing.ImageIcon;
import java.awt.Font;

public class AboutDWLite extends JDialog {

	private final JPanel contentPanel = new JPanel();

	private final String[] images = { "donutcc", "ambush", "donut", "escape",  "gatecrasher", "gladiator", "intruders", "neutroid", "neutroid2", "rupert", "stellar", "stellar2" };

	private JLabel lblImage;
	
	public AboutDWLite() 
	{
		setTitle("About DW4Lite");
		setIconImage(Toolkit.getDefaultToolkit().getImage(AboutDWLite.class.getResource("/menu/help-about-3.png")));
		setBounds(100, 100, 450, 367);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new MigLayout("", "[46px,grow,center]", "[][][14px,grow,center][][]"));
		{
			JLabel lblDrivewireLite = new JLabel("DriveWire 4 Lite UI - Inspired by greatness");
			lblDrivewireLite.setFont(new Font("Tw Cen MT", Font.PLAIN, 14));
			contentPanel.add(lblDrivewireLite, "cell 0 0,alignx center");
		}
		{
			JLabel label = new JLabel(" ");
			contentPanel.add(label, "cell 0 1");
		}
		{
			lblImage = new JLabel("");
			
			contentPanel.add(lblImage, "cell 0 2,alignx center,aligny center");
		}
		{
			JLabel label = new JLabel(" ");
			contentPanel.add(label, "cell 0 3");
		}
		{
			JPanel buttonPane = new JPanel();
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						dispose();
					}
				});
				buttonPane.setLayout(new MigLayout("", "[47px,grow,center]", "[23px]"));
				okButton.setActionCommand("OK");
				buttonPane.add(okButton, "cell 0 0,alignx center,aligny top");
				getRootPane().setDefaultButton(okButton);
			}
		}
		
		
		Thread imgSwitcher = new Thread(new Runnable(){

			@Override
			public void run() 
			{
				int imgno = 0;
				boolean stayalive = true;
				
				while(stayalive)
				{
					if (lblImage != null)
					{
						lblImage.setIcon(new ImageIcon(AboutDWLite.class.getResource("/lite/" + images[imgno]+ ".gif")));
					
					imgno++;
					
					if (imgno >= images.length)
						imgno = 0;
					
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						stayalive = false;
					}
					}
			
				}		
				
				}}
				);
		
		imgSwitcher.setDaemon(true);
		imgSwitcher.start();
	}

}
