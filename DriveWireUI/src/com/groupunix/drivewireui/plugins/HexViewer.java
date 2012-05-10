package com.groupunix.drivewireui.plugins;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import swing2swt.layout.BorderLayout;

import com.groupunix.drivewireserver.dwdisk.filesystem.DWFileSystemDirEntry;
import com.groupunix.drivewireui.GradientHelper;
import com.groupunix.drivewireui.MainWin;




public class HexViewer extends FileViewer
{

	private static final String TYPENAME = "Hex Viewer";
	private static final String TYPEIMAGE = "/menu/hex.png";
	
	private Table tableHex;
	
	private Label lblAddress;
	private Label lblValue;
	
	private Color[] section_hilights;
	private Color[] sector_hilights;

	private int cur_section_hl = 0;

	private Color colorPostamble;
	private Color colorPreamble;
	
	private Canvas canvasMemMap;


	private Vector<MemMapObj> memMap;
	
	private int gwidth = 100;
	
	public HexViewer(Composite parent, int style)
	{
		super(parent, style);
		
		setLayout(new BorderLayout(0, 0));
		
		colorPreamble = new Color(parent.getDisplay(), new RGB(127,255,127));
		colorPostamble = new Color(parent.getDisplay(), new RGB(255,127,127));
		
		section_hilights = new Color[9];
		section_hilights[0] = new Color(parent.getDisplay(), new RGB(200,200,200));
		section_hilights[1] = new Color(parent.getDisplay(), new RGB(220,220,220));
		section_hilights[2] = new Color(parent.getDisplay(), new RGB(240,240,200));
		section_hilights[3] = new Color(parent.getDisplay(), new RGB(200,220,200));
		section_hilights[4] = new Color(parent.getDisplay(), new RGB(220,240,220));
		section_hilights[5] = new Color(parent.getDisplay(), new RGB(220,200,200));
		section_hilights[6] = new Color(parent.getDisplay(), new RGB(240,220,220));
		section_hilights[7] = new Color(parent.getDisplay(), new RGB(200,200,220));
		section_hilights[8] = new Color(parent.getDisplay(), new RGB(220,220,240));
		
		
		sector_hilights = new Color[2];
		sector_hilights[0] = new Color(parent.getDisplay(), new RGB(255,255,255));
		sector_hilights[1] = new Color(parent.getDisplay(), new RGB(235,235,235));

		memMap = new Vector<MemMapObj>();
		
		createContents();
	}

	
	private void createContents()
	{
		tableHex = new Table(this, SWT.NONE);
		tableHex.setLinesVisible(true);
	
		tableHex.setHeaderVisible(true);
		tableHex.setFont(MainWin.logFont);
		
		tableHex.addPaintListener(new PaintListener() {

			@Override
			public void paintControl(PaintEvent e)
			{
				// TODO Auto-generated method stub
				canvasMemMap.redraw();
			} } );
		
		TableColumn tblclmnOffset = new TableColumn(tableHex, SWT.NONE);
		tblclmnOffset.setWidth(40);
		tblclmnOffset.setText("Addr");
		tblclmnOffset.setAlignment(SWT.CENTER);
		

		for (int i = 0;i<16;i++)
		{
			TableColumn tableColumn = new TableColumn(tableHex, SWT.NONE);
			tableColumn.setWidth(28);
			tableColumn.setText("0" + intToHexStr(i));
		}
		
		TableColumn tblclmnabcdef = new TableColumn(tableHex, SWT.NONE);
		tblclmnabcdef.setWidth(130);
		tblclmnabcdef.setText("0123456789abcdef");
		
		final TableCursor tableCursor = new TableCursor(tableHex, SWT.NONE);
		tableCursor.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
		tableCursor.setFont(MainWin.logFont);
		tableCursor.addSelectionListener(new SelectionListener() 
		{

			@Override
			public void widgetSelected(SelectionEvent e)
			{
				if ((tableCursor.getColumn() > 0) && (tableCursor.getColumn() < 17))
				{
					// databyte
					tableHex.setSelection(tableCursor.getRow());
					
					int off = tableHex.getSelectionIndex() * 16;
					int addr = off + tableCursor.getColumn() -1;
					
					
					try
					{
						int val = Integer.parseInt(tableCursor.getRow().getText(tableCursor.getColumn()), 16);
						
						String asc = "";
						if ((val >= 32) && (val<127))
						{
							asc = "'" + (char)val + "'";
						}
						
						lblValue.setText(String.format("val:   %02x h   %02d d   " + asc, val, val) );
					}
					catch (NumberFormatException e1)
					{
						lblValue.setText("val: " + e1.getMessage());
					}
					
					lblAddress.setText(String.format("addr:   %x h   %d d", addr, addr));
					
					
						
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				
			} } );
		
		final Composite composite_3 = new Composite(this, SWT.NO_FOCUS);
		composite_3.setLayoutData(BorderLayout.SOUTH);
		
		GradientHelper.applyVerticalGradientBG(composite_3, MainWin.getDisplay().getSystemColor(SWT.COLOR_TITLE_BACKGROUND_GRADIENT),MainWin.getDisplay().getSystemColor(SWT.COLOR_TITLE_BACKGROUND));
		
		composite_3.addListener(SWT.Resize, new Listener() {

			@Override
			public void handleEvent(Event event)
			{
				GradientHelper.applyVerticalGradientBG(composite_3, MainWin.getDisplay().getSystemColor(SWT.COLOR_TITLE_BACKGROUND_GRADIENT),MainWin.getDisplay().getSystemColor(SWT.COLOR_TITLE_BACKGROUND));
				
				
			} } );
		
		composite_3.setBackgroundMode(SWT.INHERIT_FORCE);
		
		lblAddress = new Label(composite_3, SWT.NONE);
		lblAddress.setBounds(10, 3, 230, 16);
		
		lblValue = new Label(composite_3, SWT.NONE);
		lblValue.setBounds(250, 3, 250, 16);
		
		
		
		ScrolledComposite compositeMemMap = new ScrolledComposite( this, SWT.NONE);
		compositeMemMap.setLayoutData(BorderLayout.WEST);
		
		FillLayout fl_compositeMemMap = new FillLayout();
		fl_compositeMemMap.type = SWT.VERTICAL;
		compositeMemMap.setLayout(fl_compositeMemMap); 
        
		//compositeMemMap.setSize(gwidth, 100);
		compositeMemMap.setExpandVertical(true);
		
		canvasMemMap = new Canvas(compositeMemMap, SWT.DOUBLE_BUFFERED);
		canvasMemMap.setSize(gwidth, compositeMemMap.getBounds().height);
		
		compositeMemMap.setContent(canvasMemMap);
		
		
		
		canvasMemMap.addPaintListener(new PaintListener() 
		{
			int wx;
			int hx;
			
			@Override
			
			public void paintControl(PaintEvent e)
			{
				
				
				e.gc.setTextAntialias(SWT.OFF);
				e.gc.setAntialias(SWT.OFF);
				e.gc.setAdvanced(false);
				
				wx = canvasMemMap.getBounds().width;
				hx = canvasMemMap.getBounds().height;
				
				
				int lo = 0;
				int hi = 0;
					
				for (MemMapObj mmo : memMap)
				{
					if ((lo == 0) || (mmo.getBaseaddr()/256 < lo))
					{
						lo = mmo.getBaseaddr()/256;
					}
					if ((mmo.getBaseaddr() + 256 + mmo.getSize())/256 > hi)
					{
						hi = (mmo.getBaseaddr() + 256 + mmo.getSize())/256;
					}
				}
					
				e.gc.setBackground(MainWin.colorWhite);
				e.gc.fillRectangle(0, 0, wx, hx );
				
				lo = lo * 256;
				hi = hi * 256;
				
				//lo = 0;
				//hi = 65535;
					
				if (hi - lo > 0)
				{
					e.gc.setFont(MainWin.fontGraphLabel);
					
					double scale = ( (double)hx - tableHex.getHeaderHeight()) / (double)(hi - lo);
						
					e.gc.setFont(MainWin.fontGraphLabel);
						
					for (MemMapObj mmo : memMap)
					{
						e.gc.setBackground(mmo.getHicol());
							
						int start = (int)Math.rint(mmo.getBaseaddr() - lo);
						int wend = (int)Math.rint(mmo.getSize());
						int end =  start + wend;
						
						int lty = (int)(start * scale) + tableHex.getHeaderHeight();
						int lby = (int)(end * scale) + tableHex.getHeaderHeight();
						
						e.gc.setForeground(mmo.hicol);
						
						//e.gc.fillRectangle(0, lty, 30, (int) (wend *scale) );
						
						int rty = mmo.getTableItem().getBounds().y + tableHex.getItemHeight()/2;
						int rby = mmo.getEndItem().getBounds().y + tableHex.getItemHeight()/2;
						int sl = 28;							
						
						e.gc.fillPolygon(new int[]{ 0, lty, 28, lty, gwidth, rty, gwidth, rby, sl, lby, 0, lby });
						
						e.gc.setForeground(MainWin.colorGraphBG);
					
						for (int i = 0;i<17;i++)
						{
							int addr = (int)((hi - lo) / 16) * i;
							e.gc.drawString(intToHexStr(addr + lo) , 3, (int)((addr) * scale) + tableHex.getHeaderHeight() - 12 , true);
							e.gc.drawLine(0, (int)((addr) * scale) + tableHex.getHeaderHeight()-1, 28, (int)((addr) * scale) + tableHex.getHeaderHeight()-1);
						}
						
						
					}
					
				}
			
			}
			
		});
		
	}
	
	

	@Override
	public void viewFile(DWFileSystemDirEntry direntry, byte[] bytes)
	{
		int x = 1;
		int y = 0;
		int off = 0;
		
		String txt = "";
		
		tableHex.setRedraw(false);
		
		lblAddress.setText("");
		lblValue.setText("");
		tableHex.removeAll();
		
		TableItem ti = null;
		
		// put bytes in table
		
		for (int i = 0;i<bytes.length ;i++)
		{
			if (x == 1)
			{
				ti = new TableItem(tableHex, SWT.NONE);
				ti.setText(0, String.format("%4s", intToHexStr(off)));
				ti.setBackground(0, this.sector_hilights[ (off/256) % 2 ]);
				off += 16;
				txt = "";
			}
			
			ti.setText(x, byteToHexStr(bytes[i]));
			txt += cleanAsciiStr((char)bytes[i] +"");
			
			x++;
			
			if (x == 17)
			{
				ti.setText(17, txt);
				x = 1;
				y++;
			}
			
		}
		
		
		if (ti != null)
		{
			ti.setText(17, txt);
		}
		
		
		// look for valid pre/postamble chain
		
		int blocksize = 0;
		int loadaddr = 0;
		int flag = 0;
		int pos = 0;
		
		this.memMap.clear();
		
		while ((pos < bytes.length - 4) && (bytes[pos] == 0))
		{
			blocksize = ((bytes[pos+1] & 0xFF) << 8) + (bytes[pos+2] & 0xFF);
			loadaddr =  ((bytes[pos+3] & 0xFF) << 8) + (bytes[pos+4] & 0xFF);
			
			
			Color hc = getNextSectionColor();
			
			try
			{
				memMap.add(new MemMapObj(loadaddr, blocksize, hc, tableHex.getItem( pos / 16 ), tableHex.getItem( (pos + blocksize + 5) / 16 )));
				
				for (int i = pos; i < pos + 5;i++)
				{
					if (i < bytes.length)
						setBGColFor(i, this.colorPreamble);
				}
			
				int i = pos + 5;
				
				File f = new File("piece_" + loadaddr + ".bin");
				
				try
				{
					OutputStream of = new FileOutputStream(f);
					
					
					
					while ((i < pos + blocksize + 5) && (i < bytes.length))
					{
						of.write(bytes[i]);
						setBGColFor(i, hc);
						i++;
					}
					
					
					of.close();
					
				} catch (FileNotFoundException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
			}
			catch (IllegalArgumentException e)
			{
				
			}
			
			pos += blocksize + 5;
		}
		
		if ((pos > 0) && (pos < bytes.length) && ((bytes[pos] & 0xFF) == 0xFF))
		{
			for (int i = pos; i < pos + 5;i++)
			{
				if (i < bytes.length)
					setBGColFor(i, this.colorPostamble);
			}
			
		}
		
		
		
		
		tableHex.setRedraw(true);
		
	}

	@Override
	public String getTypeName()
	{
		return TYPENAME;
	}

	@Override
	public String getTypeIcon()
	{
		return TYPEIMAGE;
	}
	
	protected String filterAscii(byte[] fc)
	{
		
		String res = "";
		
		for (int i = 0;i<fc.length;i++)
		{
			if ((fc[i] < 128) && (fc[i] > 0))
			{
				if (fc[i] == 13)
				{
					res += Text.DELIMITER;
				}
				else
				{
					res += (char)fc[i];
				}
			}
		}
		
		
		return res;
	}
	
	private String cleanAsciiStr(String txt)
	{
		
		txt = txt.replaceAll("[^\\x20-\\x7E]", ".");
	
		return(txt);
	}



	private String byteToHexStr(byte b)
	{
		return String.format("%02x", b);
	}



	private String intToHexStr(int i)
	{
		return String.format("%04x", i);
	}

	
	
	
	

	private Color getNextSectionColor()
	{
		this.cur_section_hl++;
		if (this.cur_section_hl >= this.section_hilights.length)
			this.cur_section_hl = 0;
		
		return(this.section_hilights[this.cur_section_hl]);
	}



	private void setBGColFor(int pos, Color col)
	{
		tableHex.getItem( pos / 16 ).  setBackground(1 + (pos % 16), col);
	}
	
	
	
	
	@Override
	public int getViewable(DWFileSystemDirEntry direntry, byte[] content)
	{
		if (!direntry.isAscii())
			return 2;
		return 1;
	}
	
	
	
	
	

	class MemMapObj {
		
		private int baseaddr = 0;
		private int size = 0;
		private Color hicol = MainWin.colorGreen;
		private TableItem tableItem;
		private TableItem endItem;
		
		public MemMapObj(int base, int sz, Color col, TableItem tableItem, TableItem endItem)
		{
			this.setBaseaddr(base);
			this.setSize(sz);
			this.setHicol(col);
			this.setTableItem(tableItem);
			this.endItem = endItem;
		}

		public void setBaseaddr(int baseaddr)
		{
			this.baseaddr = baseaddr;
		}

		public int getBaseaddr()
		{
			return baseaddr;
		}

		public void setSize(int size)
		{
			this.size = size;
		}

		public int getSize()
		{
			return size;
		}

		public void setHicol(Color hicol)
		{
			this.hicol = hicol;
		}

		public Color getHicol()
		{
			return hicol;
		}

		public void setTableItem(TableItem tableItem)
		{
			this.tableItem = tableItem;
		}

		public TableItem getTableItem()
		{
			return tableItem;
		}
		
		public TableItem getEndItem()
		{
			return endItem;
		}
		
		
		
		
	}
}

