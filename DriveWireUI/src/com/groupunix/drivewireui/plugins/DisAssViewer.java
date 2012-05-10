package com.groupunix.drivewireui.plugins;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import mc6809.dasm.DASMItem;
import mc6809.dasm.Dasm;
import mc6809.dasm.DasmRegs;
import mc6809.dasm.IDasmRegs;

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
import util.AddressBinder;

import com.groupunix.drivewireserver.dwdisk.filesystem.DWFileSystemDirEntry;
import com.groupunix.drivewireui.GradientHelper;
import com.groupunix.drivewireui.MainWin;




public class DisAssViewer extends FileViewer
{

	private static final String TYPENAME = "6809 Disassembly";
	private static final String TYPEIMAGE = "/menu/hex.png";
	
	private Table tableDis;
	
	
	public DisAssViewer(Composite parent, int style)
	{
		super(parent, style);
		
		setLayout(new BorderLayout(0, 0));
		
	
		
		createContents();
	}

	
	private void createContents()
	{
		tableDis = new Table(this, SWT.NONE);
		tableDis.setLinesVisible(true);
	
		tableDis.setHeaderVisible(true);
		tableDis.setFont(MainWin.logFont);
	
		
		TableColumn tblclmnOffset = new TableColumn(tableDis, SWT.NONE);
		tblclmnOffset.setWidth(49);
		tblclmnOffset.setText("Addr");
		tblclmnOffset.setAlignment(SWT.CENTER);
		
		TableColumn tblclmnInstruction = new TableColumn(tableDis, SWT.NONE);
		tblclmnInstruction.setWidth(76);
		tblclmnInstruction.setText("Instruction");
		
		TableColumn tblclmnArgument = new TableColumn(tableDis, SWT.NONE);
		tblclmnArgument.setWidth(71);
		tblclmnArgument.setText("Argument");
		
		TableColumn tblclmnComment = new TableColumn(tableDis, SWT.NONE);
		tblclmnComment.setWidth(85);
		tblclmnComment.setText("Comment");
		
		TableColumn tblclmnData = new TableColumn(tableDis, SWT.NONE);
		tblclmnData.setWidth(62);
		tblclmnData.setText("Data");
		
		TableColumn tblclmnAscii = new TableColumn(tableDis, SWT.NONE);
		tblclmnAscii.setWidth(100);
		tblclmnAscii.setText("ASCII");
		
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
	
		
		
	
		
		
	
		
	}
	
	

	@Override
	public void viewFile(DWFileSystemDirEntry direntry, byte[] bytes)
	{
		
		tableDis.setRedraw(false);
		
		tableDis.removeAll();
		
		AddressBinder ab = new AddressBinder();
		
		IDasmRegs regs = new DasmRegs();
		
		HashMap<String,String> map = new HashMap<String,String>();
		
		List<Integer> addrs = new ArrayList<Integer>();
		
		
		try
		{
			
			Dasm d = new Dasm(ab,  false, bytes, 0);
			
			Vector<DASMItem> dasmres = d.disassemble(0, bytes.length, regs, bytes, map, addrs);
			
			for (DASMItem e : dasmres)
			{
				TableItem ti = new TableItem(tableDis, SWT.NONE);
				
				ti.setText(0, e.getAddr() + "");
				if (e.getInstr() != null)
					ti.setText(1, e.getInstr().getName());
				ti.setText(3, e.getNotes());
			}
			
		} 
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		
		
		tableDis.setRedraw(true);
		
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

	
	
	
	

	
	
	@Override
	public int getViewable(DWFileSystemDirEntry direntry, byte[] content)
	{
		if (!direntry.isAscii())
			return 3;
		return 1;
	}
	
	
	
	
	
}

