package com.groupunix.drivewireui;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Canvas;

public class DisksetWin extends Dialog {

	protected Object result;
	protected Shell shlDisksetEditor;
	private Table table;
	private Combo comboDiskset;
	private DiskDef[] disks;
	private Text textDescription;
	private Button btnSaveChanges;
	private Button btnHdbdosMode;
	private Text text;
	
	
	
	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public DisksetWin(Shell parent, int style) {
		super(parent, style);
		setText("SWT Dialog");
	}

	/**
	 * Open the dialog.
	 * @return the result
	 * @throws DWUIOperationFailedException 
	 * @throws IOException 
	 */
	public Object open() throws IOException, DWUIOperationFailedException {
		
		createContents();
		
		btnSaveChanges = new Button(shlDisksetEditor, SWT.CHECK);
		btnSaveChanges.setBounds(357, 243, 204, 16);
		btnSaveChanges.setText("Save disk changes during use");
		
		btnHdbdosMode = new Button(shlDisksetEditor, SWT.CHECK);
		btnHdbdosMode.setBounds(357, 265, 215, 16);
		btnHdbdosMode.setText("HDBDOS sector -> drive translation");
		
		Button btnOk = new Button(shlDisksetEditor, SWT.NONE);
		btnOk.setBounds(237, 431, 97, 25);
		btnOk.setText("Save Diskset");
		
		textDescription = new Text(shlDisksetEditor, SWT.BORDER);
		textDescription.setBounds(266, 20, 295, 21);
		
		Button btnCancel = new Button(shlDisksetEditor, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shlDisksetEditor.close();
			}
		});
		btnCancel.setBounds(519, 431, 75, 25);
		btnCancel.setText("Close");
		
		
		comboDiskset.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) 
			{
				try {
					loadDiskset(comboDiskset.getText());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (DWUIOperationFailedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
			}
		});
		
		loadDiskSets(comboDiskset);
		comboDiskset.select(0);
		
		text = new Text(shlDisksetEditor, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		text.setBounds(266, 51, 295, 186);
		
		Canvas canvasCover = new Canvas(shlDisksetEditor, SWT.NONE);
		canvasCover.setBounds(10, 51, 250, 250);

		 URL url = new URL("http://www.old-computers.com/museum/photos/tandy_coco1_1.jpg");
	     InputStream stream = url.openStream();
	     ImageLoader loader = new ImageLoader();
	     ImageData[] imageDataArray = loader.load(stream);
	     stream.close();
	     
	     canvasCover.setBackgroundImage(new Image(shlDisksetEditor.getDisplay(), imageDataArray[0]));
		
		shlDisksetEditor.open();
		shlDisksetEditor.layout();
		Display display = getParent().getDisplay();
		while (!shlDisksetEditor.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	
	private void loadDiskSets(Combo cmb) throws IOException, DWUIOperationFailedException 
	{
		ArrayList<String> disksets = UIUtils.loadArrayList("ui diskset show");
		
		Collections.sort(disksets);
		
		for(int i=0; i<disksets.size(); i++)
		{
			cmb.add(disksets.get(i));
		}
	}


	
	
	
	
	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shlDisksetEditor = new Shell(getParent(), getStyle());
		shlDisksetEditor.setSize(613, 494);
		shlDisksetEditor.setText("Diskset Editor");
		shlDisksetEditor.setLayout(null);
		
		table = new Table(shlDisksetEditor, SWT.BORDER | SWT.FULL_SELECTION);
		table.setBounds(10, 321, 584, 104);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		TableColumn tblclmnDrive = new TableColumn(table, SWT.NONE);
		tblclmnDrive.setWidth(40);
		tblclmnDrive.setText("Drive");
		
		TableColumn tblclmnUri = new TableColumn(table, SWT.NONE);
		tblclmnUri.setWidth(281);
		tblclmnUri.setText("URI");
		
		TableColumn tblclmnWp = new TableColumn(table, SWT.NONE);
		tblclmnWp.setWidth(47);
		tblclmnWp.setText("WP");
		
		TableColumn tblclmnSync = new TableColumn(table, SWT.NONE);
		tblclmnSync.setWidth(47);
		tblclmnSync.setText("Sync");
		
		TableColumn tblclmnExpand = new TableColumn(table, SWT.NONE);
		tblclmnExpand.setWidth(50);
		tblclmnExpand.setText("Expand");
		
		TableColumn tblclmnLimit = new TableColumn(table, SWT.NONE);
		tblclmnLimit.setWidth(49);
		tblclmnLimit.setText("Limit");
		
		TableColumn tblclmnOffset = new TableColumn(table, SWT.NONE);
		tblclmnOffset.setWidth(47);
		tblclmnOffset.setText("Offset");
		
		comboDiskset = new Combo(shlDisksetEditor, SWT.READ_ONLY);
		
		comboDiskset.setBounds(10, 20, 250, 23);

	}

	protected void loadDiskset(String disksetname) throws IOException, DWUIOperationFailedException 
	{
		// clear data
		
		disks = new DiskDef[256];
		
		this.textDescription.setText("");
		
		this.btnHdbdosMode.setSelection(false);
		this.btnSaveChanges.setSelection(false);
		
		ArrayList<String> res = UIUtils.loadArrayList("ui diskset show " + disksetname);
		
		try 
		{
		
			for (int i = 0;i<res.size();i++)
			{
				Pattern p_diskitem = Pattern.compile("^(.+)\\((\\d+)\\):\\s(.+)");
				Pattern p_setitem = Pattern.compile("^(.+):\\s(.+)");
				Matcher m = p_diskitem.matcher(res.get(i));
			  
				if (m.find())
				{
					int drive = Integer.parseInt(m.group(2));
					
					if (disks[drive] == null)
					{
						disks[drive] = new DiskDef();
					}

					if (m.group(1).equals("path"))
						disks[drive].setPath(m.group(3));
					
					if (m.group(1).equals("sizelimit"))
						disks[drive].setSizelimit(Integer.parseInt(m.group(3)));
					
					if (m.group(1).equals("offset"))
						disks[drive].setOffset(Integer.parseInt(m.group(3)));
					
					if (m.group(1).equals("writeprotect"))
						disks[drive].setWriteprotect(UIUtils.sTob(m.group(3)));
					
					if (m.group(1).equals("sync"))
						disks[drive].setSync(UIUtils.sTob(m.group(3)));
					
					if (m.group(1).equals("expand"))
						disks[drive].setExpand(UIUtils.sTob(m.group(3)));
					

				}
				else
				{
					m = p_setitem.matcher(res.get(i));
				  
					if (m.find())
					{
						if (m.group(1).equals("Description"))
							this.textDescription.setText(m.group(2));
				
						
						if (m.group(1).equals("SaveChanges"))
							this.btnSaveChanges.setSelection(UIUtils.sTob(m.group(2)));
						
						if (m.group(1).equals("HDBDOSMode"))
							this.btnHdbdosMode.setSelection(UIUtils.sTob(m.group(2)));
						
					}
				  
				}
			}
		}
		catch (NumberFormatException e)
		{
			throw new DWUIOperationFailedException("Error parsing disk set results: " + e.getMessage());
		}
			
		refreshDiskTable();

		

		
		
		
	}

	private void refreshDiskTable() 
	{
		table.removeAll();
		
		for (int i = 0;i<256;i++)
		{
			if (disks[i] != null)
			{
				TableItem item = new TableItem(table, SWT.NONE);
				
				item.setText(0,i + "");
				item.setText(1,disks[i].getPath());
				item.setText(2,UIUtils.bTos(disks[i].isWriteprotect()));
				item.setText(3,UIUtils.bTos(disks[i].isSync()));
				item.setText(4,UIUtils.bTos(disks[i].isExpand()));
				item.setText(5,disks[i].getSizelimit() + "");
				item.setText(6,disks[i].getOffset() + "");
				
				
			}
		}
	}
}
