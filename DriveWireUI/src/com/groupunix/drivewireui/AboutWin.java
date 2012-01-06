package com.groupunix.drivewireui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.swtdesigner.SWTResourceManager;

public class AboutWin extends Dialog {

	protected Object result;
	protected Shell shell;

	private int width = 582;
	private int height = 472;

	private int xborder = 32;
	private int yborder = 32;
	private int yfudge = 24;
	private int xfudge = 6;
	
	private Image cocotext;
	private Image cocotext2;
	private Canvas coco;
	private Image cocoimg;
	private GC cocogc;
	
	private int[][] text = new int[16][32];
	private int[][] dtext = new int[16][32];
	
	private int curx = 0;
	private int cury = 3;
	
	private int cursorcolor = 0;
	private Color[] curscols = new Color[8];
	private int curpos = 1;
	private int dpos = -256;
	private boolean scrolltext = false;
	private Color scrollColor;
	private Color blendColor;
	private Font scrollFont;
	private Font thanksFont;
	private HashMap<String,Integer> fontmap;
	private boolean ssmode = false;
	private boolean precrash = true;
	
	ArrayList<String >folks = new ArrayList<String>(Arrays.asList( "Special thanks to:", "", "Cloud-9", "#coco_chat", "Malted Media", 
			"The Glenside Color Computer Club", "Darren Atkinson", "Boisy Pitre", 
			"John Linville", "RandomRodder", "lorddragon", "lostwizard", "beretta",  "Gary Becker", "Jim Hathaway",
			"Gene Heskett", "Wayne Campbell", "Stephen Fischer", "Christopher Hawks", "And apologies to any I forgot!"));
	
	
	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public AboutWin(Shell parent, int style) {
		super(parent, style);
		setText("About DriveWire User Interface");
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		cocotext = org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/dw/cocotext.png");
		cocotext2 = org.eclipse.wb.swt.SWTResourceManager.getImage(MainWin.class, "/dw/cocotext2.png");
		
		
		createContents();
		shell.open();
		shell.layout();
		Display display = getParent().getDisplay();
		
		shell.setLocation(getParent().getLocation().x + (getParent().getSize().x/2 -  shell.getSize().x/2) ,getParent().getLocation().y + (getParent().getSize().y/2 - shell.getSize().y/2 ));
		
		
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
		shell = new Shell(getParent(), getStyle());
		shell.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
		shell.setImage(SWTResourceManager.getImage(AboutWin.class, "/com/sun/java/swing/plaf/windows/icons/Inform.gif"));
		shell.setSize(width, height);
		shell.setText("About DriveWire...");
		
		curscols[0] = new Color(shell.getDisplay(), 0, 255, 0);
		curscols[1] = new Color(shell.getDisplay(), 255, 255, 0);
		curscols[2] = new Color(shell.getDisplay(), 0, 0, 255);
		curscols[3] = new Color(shell.getDisplay(), 255, 0, 0);
		curscols[4] = new Color(shell.getDisplay(), 255, 255, 255);
		curscols[5] = new Color(shell.getDisplay(), 0, 255, 255);
		curscols[6] = new Color(shell.getDisplay(), 255, 0, 255);
		curscols[7] = new Color(shell.getDisplay(), 255, 128, 0);
		
		
		
		scrollColor = new Color(shell.getDisplay(), 240,240,240);
		blendColor = new Color(shell.getDisplay(), 50,50,50);
		
		fontmap = new HashMap<String,Integer>();
		fontmap.put("Roboto Cn", SWT.NORMAL);
		fontmap.put("Roboto", 3);
		fontmap.put("Roboto", 0);
		thanksFont = UIUtils.findFont(shell.getDisplay(), fontmap, "Special thanks to..", 250, 45);
		
		
		coco = new Canvas(shell, SWT.DOUBLE_BUFFERED);
		
		coco.addKeyListener(new KeyListener() 
		{

			@Override
			public void keyPressed(KeyEvent e)
			{
			
				if (e.character == 's')
				{
					ssmode = true;
				}
			}

			@Override
			public void keyReleased(KeyEvent e)
			{
				// TODO Auto-generated method stub
				
			}
			
		} 
		);
		
		coco.setBounds(0,0,width,height);
		coco.setBackground(MainWin.colorBlack);
		cocoimg = new Image(null, width-(xborder*2)-xfudge, height-(yborder*2)-yfudge);
		this.cocogc = new GC(cocoimg);
		// ?
		this.cocogc.setAdvanced(false);
		
		
		coco.addPaintListener(new PaintListener() 
		{
			
			private int namewid = 100;
			private int namehi = 100;
			@Override
			
			public void paintControl(PaintEvent e)
			{
				
				
				genCocoimg();
				e.gc.drawImage(cocoimg, xborder, yborder);
				
				
				
				if (!ssmode)
				{
					if (precrash)
					{
						e.gc.setBackground(curscols[cursorcolor]);
						e.gc.fillRectangle(Math.min(31,curx) * 16 + xborder, cury * 24 + yborder, 15, 24);
					}
				
				
					if (scrolltext)
					{
						if (dpos > -255)
						{
							int x = (shell.getBounds().width/2 - namewid/2);
							
							int h = Math.max(0, Math.min(128, dpos + 64));
							
							e.gc.setAlpha(h);
							e.gc.setBackground(MainWin.colorBlack);
							e.gc.setForeground(blendColor);
							e.gc.fillGradientRectangle(32, 416 - namehi - 20, 512, namehi + 20, true);
							
							
							if (dpos > 0)
							{
								e.gc.setFont(scrollFont);
								
								//e.gc.setTextAntialias(SWT.ON);
								
								e.gc.setAlpha(Math.min(255, dpos));
								
								//e.gc.setForeground(MainWin.colorBlack);
								//e.gc.drawString(folks.get(curpos), x + 5, 240, true);
								
								e.gc.setForeground(scrollColor);
								e.gc.drawString(folks.get(curpos), x, 416 - namehi - 10, true);
								
								if (curpos != folks.size()-1)
								{
									e.gc.setAlpha(Math.max(0, Math.min(255, dpos - 200)));
									e.gc.setFont(thanksFont);
									e.gc.drawString("Special thanks to...", x, 416 - namehi - 50, true);
									
								}
								
							}	
							
							
							dpos = dpos - 5;
						}
						else
						{
							curpos++;
							
							if (curpos==folks.size())
								curpos = 2;
						
							dpos = 500;
							
							if (scrollFont != null)
								scrollFont.dispose();
							
							scrollFont = UIUtils.findFont(shell.getDisplay(), fontmap, folks.get(curpos), 512, 90);
							e.gc.setFont(scrollFont);
							namewid = e.gc.stringExtent(folks.get(curpos)).x;
							namehi = e.gc.stringExtent(folks.get(curpos)).y;
							lockup1();
						}
						
					
					}
				}
				
				
			}
			
		});

		
		Runnable cursor = new Runnable()
		{
		

			Random r = new Random();
			
			@Override
			public void run()
			{
				if (!shell.isDisposed())
				{
				
					if (ssmode)
						lockup1();
					else
					{
						cursorcolor++;
						if (cursorcolor==8)
							cursorcolor = 0;
					}
					
					
					if (!coco.isDisposed())
						coco.redraw();
					
					if (ssmode)
					{
						shell.getDisplay().timerExec(r.nextInt(10000), this);
					}
					else
						shell.getDisplay().timerExec(50, this);
				}
				
			}
			
		};
		
		shell.getDisplay().timerExec(100, cursor);
		
		
		
		
		
		
		Runnable scroller = new Runnable() 
		{

			int curname = -1;
			int curline = 3;
			boolean wanttodie = false;
			
			@Override
			public void run()
			{
				while (!shell.isDisposed() && !wanttodie && !ssmode)
				{
					if (curline == 3)
					{
						String[] c = { "DriveWire " + MainWin.DWUIVersion + " (" + MainWin.DWUIVersionDate + ")", 
							"by mobster #3", " " };
					
						for (int i = 0; i < 3;i++)
						{
							for (int j = 0;j<32;j++)
							{
								if (j >= c[i].length())
									text[i][j] = 32;
								else
									text[i][j] = c[i].toUpperCase().charAt(j);
							}
							
						}
						
						for (int i = 3; i < 16;i++)
						{
							for (int j = 0;j<32;j++)
							{
								text[i][j] = 32;
							}
							
						}
						
					
					}
						
					// type it
					
					if (ssmode)
						return;
					
					String  f = getNextName();
					
					for (int j = 0;j<32;j++)
					{
						if (ssmode)
							return;
						
						if (j >= f.length())
							text[curline][j] = 32;
						else
						{
							curx = j+1;
							cury = curline;
							text[curline][j] = f.toUpperCase().charAt(j);
							
							
							if (curname < 21)
							try
							{
								Thread.sleep(new Random().nextInt(250)+50);
							} 
							catch (InterruptedException e)
							{
								wanttodie = true;
							}
							
							if ((curname == 7) && (j == 7))
							{
								lockup1();
								
								try
								{
									Thread.sleep(1200);
								} 
								catch (InterruptedException e)
								{
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								
								Random r = new Random();
								
								int t = r.nextInt(100);
								for (int i = 0;i < t;i++)
								{
									// favor the weird stuff
									text[r.nextInt(16)][r.nextInt(32)] = r.nextInt(192) + 64;
								}
								
								curx = 32;
								cury = 24;
								
								
								try
								{
									Thread.sleep(3000);
								} 
								catch (InterruptedException e)
								{
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								
								
								scrolltext = true;
								precrash = false;
								return;
							}
							
						}
					}
					
					if (!shell.isDisposed() && !wanttodie && !ssmode)
					{
						curline++;
						if (curline == 16)
						{
							curline = 15;
							
							int k = 5;
							if ((curname > 21) || (curname < 12))
									k=3;
							
							for (int i = k;i<15;i++)
							{
								for (int j = 0;j<32;j++)
								{
									text[i][j] = text[i+1][j];
								}
							}
							
							for (int i = 0;i<32;i++)
								text[15][i] = 32;
									 
						
						}
					}
					
					curx = 0;
					cury = curline; 
					
					
					
					if (!wanttodie && !ssmode)
					try
					{
						Thread.sleep(1200);
					} catch (InterruptedException e)
					{
						wanttodie = true;
					}
					
				}

			}

			
			private String getNextName()
			{
				curname++;
				if (curname == folks.size())
					curname = 0;
				
				return(folks.get(curname));
			}
			
			
		};
		
		Thread scrT = new Thread(scroller);
		scrT.start();
	
		
	}
	
	

	protected void genCocoimg()
	{
		//long start = System.currentTimeMillis();
		
		if (!shell.isDisposed())
		
		for (int y = 0;y<16;y++)
		{
			for (int x = 0;x<32;x++)
			{
				if (text[y][x] != dtext[y][x])
				{
					if (text[y][x] < 128)
					{
						Point p = getCharPoint(text[y][x]);
						cocogc.drawImage(cocotext, p.x, p.y , 16, 24, x*16, y*24, 16, 24);
						
					}
					else
					{
						genCoCoChar(text[y][x], x*16, y*24);
						Point p = getGfxPoint(text[y][x]);
						cocogc.drawImage(cocotext2, p.x, p.y , 16, 24, x*16, y*24, 16, 24);
					}
					dtext[y][x] = text[y][x];
				}
			}
			
		}
	
		//MainWin.debug("genimg took " + (System.currentTimeMillis() - start));
	}

	
	
	private void genCoCoChar(int chr, int x, int y)
	{
		cocogc.setBackground(MainWin.colorBlack);
		cocogc.fillRectangle(x, y, 16, 24);
		
		cocogc.setBackground(curscols[(chr-128) / 16]);
		
		if ((chr & 1) == 1)
			cocogc.fillRectangle(x+8, y+12, 8, 12);
		
		if ((chr & 2) == 2)
			cocogc.fillRectangle(x, y+12, 8, 12);
		
		if ((chr & 4) == 4)
			cocogc.fillRectangle(x+8, y, 8, 12);
		
		if ((chr & 8) == 8)
			cocogc.fillRectangle(x, y, 8, 12);
		
		
	}

	
	
	
	
	private void lockup1()
	{
		Random r = new Random();
		// make a likely pattern..
		
		int patlen = r.nextInt(48)+1;
		
		int[] pat = new int[patlen];
		
		for (int i = 0;i<patlen;i++)
		{
			int type = r.nextInt(10);
			
			if (type<2)
			{
				// inverse @s are popular
				pat[i] = 96;
			}
			else if (type <3)
			{
				// regular letters
				pat[i] = 65 + r.nextInt(26);
			}
			else if (type<6)
			{
				// some weird gfx char
				pat[i] = 128 + r.nextInt(128);
			}
			else if (type<8)
			{
				// some other inverse char
				pat[i] = 96 + r.nextInt(32);
			}
			else if (type<9)
			{
				// !
				pat[i] = 33;
			}
			else
			{
				// wildcard
				pat[i] = r.nextInt(256);
			}
		}
		
		int ptr = 0;
		
		for (int i = 0;i<16;i++)
		{
			for (int j = 0;j<32;j++)
			{
				text[i][j] = pat[ptr];
				ptr++;
				if (ptr == patlen)
					ptr = 0;
			}
		}
		
		// random mess on top?
		
		if (r.nextInt(10)>5)
		{
			int t = r.nextInt(100);
			for (int i = 0;i < t;i++)
			{
				// favor the weird stuff
				text[r.nextInt(16)][r.nextInt(32)] = r.nextInt(192) + 64;
			}
		}
	
	}
	
	
	

	protected Point getCharPoint(int val)
	{
		Point res = new Point(0,0);
		
		if (val > 63)
			res.y = 24;
		res.x = (val % 64) * 16;
		
		return res;
	}

	protected Point getGfxPoint(int val)
	{
		val = val - 128;
	
		
		
		Point res = new Point(0,0);
		
		res.y = (val / 32) * 24;
		res.x = (val % 32) * 16;
		
		return res;
	}
	

	
	
}
