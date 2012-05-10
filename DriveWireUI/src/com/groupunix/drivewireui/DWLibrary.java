package com.groupunix.drivewireui;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.HierarchicalConfiguration.Node;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.VFS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import swing2swt.layout.BorderLayout;

import com.groupunix.drivewireserver.dwdisk.DWDisk;
import com.groupunix.drivewireserver.dwdisk.DWDiskDrives;
import com.groupunix.drivewireserver.dwdisk.filesystem.DWDECBFileSystem;
import com.groupunix.drivewireserver.dwdisk.filesystem.DWDECBFileSystemDirEntry;
import com.groupunix.drivewireserver.dwdisk.filesystem.DWRBFFileSystem;
import com.groupunix.drivewireserver.dwexceptions.DWDiskInvalidSectorNumber;
import com.groupunix.drivewireserver.dwexceptions.DWImageFormatException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWUtils;
import com.groupunix.drivewireui.library.DECBFileLibraryItem;
import com.groupunix.drivewireui.library.LibraryItem;
import com.groupunix.drivewireui.library.PathLibraryItem;
import com.groupunix.drivewireui.library.RBFFileLibraryItem;
import com.groupunix.drivewireui.library.URLLibraryItem;
import com.groupunix.drivewireui.plugins.ASCIIViewer;
import com.groupunix.drivewireui.plugins.BASICViewer;
import com.groupunix.drivewireui.plugins.CoCoMaxImageViewer;
import com.groupunix.drivewireui.plugins.DWBrowser;
import com.groupunix.drivewireui.plugins.DisAssViewer;
import com.groupunix.drivewireui.plugins.HSCREENImageViewer;
import com.groupunix.drivewireui.plugins.NIBImageViewer;

import com.groupunix.drivewireui.plugins.FileViewer;
import com.groupunix.drivewireui.plugins.HexViewer;
import com.groupunix.drivewireui.plugins.PMODEImageViewer;
import com.groupunix.drivewireui.plugins.PathInfoViewer;
import com.swtdesigner.SWTResourceManager;

public class DWLibrary extends Composite
{
	private static final String DEFAULT_URL = "http://cococoding.com/dw4";
	
	public static final int TYPE_UNKNOWN = -1;
	public static final int TYPE_FOLDER_MOUNTED = 0;
	public static final int TYPE_FOLDER_LOCAL_PATHS = 1;
	public static final int TYPE_FOLDER_LOCAL_DEVICES = 2;
	public static final int TYPE_FOLDER_CLOUD = 3;
	public static final int TYPE_FOLDER_SERVER = 4;
	
	public static final int TYPE_FOLDER = 5;
	public static final int TYPE_URL = 6;
	public static final int TYPE_PATH = 7;
	public static final int TYPE_DISK = 8;
	public static final int TYPE_DECB_FILE = 9;
	public static final int TYPE_RBF_FILE = 10;
	public static final int TYPE_RBF_DIR = 11;
	public static final int TYPE_DEVICE = 12;
	
	public static final int FSTYPE_UNKNOWN = 0;
	public static final int FSTYPE_RBF = 1;
	public static final int FSTYPE_DECB = 2;
	
	
	public static final int FILETYPE_UNKNOWN = 0;
	public static final int FILETYPE_ASCII = 1;
	public static final int FILETYPE_BASIC_ASCII = 2;
	public static final int FILETYPE_BASIC_TOKENS = 16;
	public static final int FILETYPE_BINARY = 3;
	public static final int FILETYPE_PMODE = 4;
	public static final int FILETYPE_CCMAX = 5;
	public static final int FILETYPE_CCMAX3 = 6;
	public static final int FILETYPE_BASIC_DATA = 7;
	public static final int FILETYPE_SOURCE_ASM = 8;
	public static final int FILETYPE_BIN = 9;
	public static final int FILETYPE_DOC = 10;
	public static final int FILETYPE_FONT = 11;
	public static final int FILETYPE_ARCHIVE = 12;
	public static final int FILETYPE_SOUND = 13;
	public static final int FILETYPE_PRINTDRIVER = 14;
	public static final int FILETYPE_MIDI = 15;
	public static final int FILETYPE_SOURCE_FORTH = 17;
	public static final int FILETYPE_SOURCE_BASIC09 = 18;
	public static final int FILETYPE_SOURCE_C = 19;
	public static final int FILETYPE_SOURCE_C_HEADER = 20;
	public static final int FILETYPE_OS9_MODULE = 21;
	public static final int FILETYPE_OS9_MODULE_6809 = 22;
	public static final int FILETYPE_OS9_MODULE_BASIC09 = 23;
	public static final int FILETYPE_OS9_MODULE_PASCAL = 24;
	public static final int FILETYPE_OS9_MODULE_PRGRM = 25;
	public static final int FILETYPE_OS9_MODULE_SBRTN = 26;
	public static final int FILETYPE_OS9_MODULE_MULTI = 27;
	public static final int FILETYPE_OS9_MODULE_DATA = 28;
	public static final int FILETYPE_OS9_MODULE_USERDEF = 29;
	public static final int FILETYPE_OS9_MODULE_SYSTM = 30;
	public static final int FILETYPE_OS9_MODULE_FLMGR = 31;
	public static final int FILETYPE_OS9_MODULE_DRIVR = 32;
	public static final int FILETYPE_OS9_MODULE_DEVIC = 33;

	
	
	
	
	
	private Tree tree;


	HierarchicalConfiguration locallib;
	HierarchicalConfiguration cloudlib;

	ArrayList<FileViewer> viewers;
	Composite compositeViewers;
	Composite compositeFileViewer;
	DWBrowser compositeWebViewer;
	PathInfoViewer compositePathViewer;
	ScrolledComposite compositeFileView;
	private ToolBar toolBarFileViewers;
	StackLayout stackViewersLayout;
	CTabItem ourtab;

	

	public DWLibrary(Composite parent, int style, CTabItem mytab)
	{
		super(parent, style);
		this.ourtab = mytab;
		
		setLayout(new BorderLayout(0, 0));
		
		loadlibs();
		
		//waitcursor = new Cursor(getDisplay(), SWT.CURSOR_WAIT);
		//normalcursor = new Cursor(getDisplay(), SWT.CURSOR_ARROW);
		
		SashForm sashForm = new SashForm(this, SWT.NONE);
		sashForm.setLayoutData(BorderLayout.CENTER);
		
		Composite compositeTree = new Composite(sashForm, SWT.NONE);
		compositeTree.setLayout(new BorderLayout(0, 0));
		
		//Composite treeHeader = new Composite(compositeTree, SWT.NONE);
		//treeHeader.setLayoutData(BorderLayout.NORTH);
		
		this.tree = new Tree(compositeTree, SWT.VIRTUAL | SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		tree.setLayoutData(BorderLayout.CENTER);
		
		
		compositeViewers = new Composite(sashForm, SWT.NONE);
		
		stackViewersLayout = new StackLayout();
		compositeViewers.setLayout(stackViewersLayout);
		
		compositeFileViewer = new Composite(compositeViewers, SWT.BORDER);
		compositeFileViewer.setLayout(new BorderLayout(0, 0));
		
		compositeWebViewer = new DWBrowser(compositeViewers, DWLibrary.DEFAULT_URL, ourtab);
		compositeWebViewer.setLayout(new BorderLayout(0, 0));
		
		compositePathViewer = new PathInfoViewer(compositeViewers, SWT.BORDER);
				
		compositeFileView = new ScrolledComposite(compositeFileViewer, SWT.NONE);
		compositeFileView.setBackground(org.eclipse.wb.swt.SWTResourceManager.getColor(SWT.COLOR_INFO_BACKGROUND));
		compositeFileView.setExpandVertical(true);
		compositeFileView.setExpandHorizontal(true);
		compositeFileView.setLayoutData(BorderLayout.CENTER);
		
		//Composite compositeToolbar = new Composite(compositeFileViewer, SWT.NONE);
		//compositeToolbar.setLayoutData(BorderLayout.NORTH);
		
		//FillLayout fl_compositeToolbar = new FillLayout(SWT.VERTICAL);
		//compositeToolbar.setLayout(fl_compositeToolbar);
		
		//toolBarFileViewers = new ToolBar(compositeToolbar, SWT.RIGHT); //  | SWT.FLAT
		//toolBarFileViewers.setEnabled(true);
		
		loadviewers(compositeFileView);
		
		
		stackViewersLayout.topControl = compositeWebViewer;
		
		//toolBar.setSize(SWT.DEFAULT, 24);
		
		
		tree.setHeaderVisible(false);
		tree.addSelectionListener(new SelectionAdapter() 
		{
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				LibraryItem libitem = (LibraryItem) e.item.getData();
				
				if (libitem.getType() == DWLibrary.TYPE_DECB_FILE)
				{
					DECBFileLibraryItem decbitem = (DECBFileLibraryItem) e.item.getData();
					
				
					FileViewer bestv = null;
							
					bestv = getBestViewer(decbitem.getEntry(), decbitem.getData());
						
					if (bestv != null)
					{
						
						compositeFileView.setContent(bestv);
						bestv.viewFile(decbitem.getEntry(), decbitem.getData());
						compositeFileView.layout();
						
						/*
						for (ToolItem ti: toolBarFileViewers.getItems())
						{
							if (ti.getToolTipText().equals(bestv.getTypeName()))
									ti.setSelection(true);
							else
								ti.setSelection(false);
						}
						*/
							
						ourtab.setText(decbitem.getEntry().getFileName().trim() + "." + decbitem.getEntry().getFileExt() + " ");
						ourtab.setImage(SWTResourceManager.getImage(MainWin.class, bestv.getTypeIcon()));
						stackViewersLayout.topControl = compositeFileViewer;
						compositeViewers.layout();
					}
					else
					{
						// no viewer? bah
					}
					
				}
				else if (libitem.getType() == DWLibrary.TYPE_RBF_FILE)
				{
					RBFFileLibraryItem rbfitem = (RBFFileLibraryItem) e.item.getData();
					
					compositeFileView.setContent(viewers.get(1));
					
					try
					{
						viewers.get(1).viewFile(rbfitem.getEntry(), rbfitem.getRBFFS().getFileContentsFromDescriptor(rbfitem.getEntry().getFD()));
						ourtab.setText(rbfitem.getEntry().getFileName() + " ");
						
						stackViewersLayout.topControl = compositeFileViewer;
						compositeViewers.layout();
					} 
					catch (IOException e1)
					{
					} 
					catch (DWDiskInvalidSectorNumber e1)
					{
					}
							
					
				}
				
				else if (libitem.getType() == DWLibrary.TYPE_URL)
				{
					compositeWebViewer.openURL( ((URLLibraryItem) libitem).getUrl()  );
					
					stackViewersLayout.topControl = compositeWebViewer;
					compositeViewers.layout();
				}
				else if (libitem.getType() == DWLibrary.TYPE_PATH)
				{
					PathLibraryItem pitem = (PathLibraryItem)  e.item.getData();
					
					if ((pitem.getFSType() == DWLibrary.FSTYPE_DECB) || (pitem.getFSType() == DWLibrary.FSTYPE_RBF)) 
					{
						compositePathViewer.displayPath(pitem, ourtab);
						stackViewersLayout.topControl = compositePathViewer;
						compositeViewers.layout();
					}
				}
						
			}

			
		});
		
		this.tree.setFont(MainWin.logFont);
		
		TreeColumn trclmnItem = new TreeColumn(tree, SWT.LEFT);
		trclmnItem.setWidth(350);
		trclmnItem.setText("Item");
		trclmnItem.addSelectionListener(new SortTreeListener());
		
		final Menu treemenu = new Menu (tree);
		treemenu.addMenuListener(new MenuAdapter() 
		{
			@Override
			public void menuShown(MenuEvent e) 
			{
				
				while (treemenu.getItemCount() > 0)
				{
					treemenu.getItem(0).dispose();
				}
				
				if (tree.getSelection()[0].getData()  != null)
				{
					LibraryItem libitem = (LibraryItem) tree.getSelection()[0].getData();
					
					
					MenuItem mi;
					
					switch(libitem.getType() )
					{
						case DWLibrary.TYPE_FOLDER:
						case DWLibrary.TYPE_FOLDER_LOCAL_PATHS:
								
							mi = new MenuItem(treemenu, SWT.PUSH);
							mi.setText("Add New Path...");
							mi.setImage(SWTResourceManager.getImage(DWBrowser.class, "/menu/database-add.png"));
							
							mi = new MenuItem(treemenu, SWT.PUSH);
							mi.setText("Add New URL...");
							mi.setImage(SWTResourceManager.getImage(DWBrowser.class, "/menu/database-add.png"));
							
							mi = new MenuItem(treemenu, SWT.PUSH);
							mi.setText("Add New Folder...");
							mi.setImage(SWTResourceManager.getImage(DWBrowser.class, "/menu/database-add.png"));
							
							if (libitem.getType() == DWLibrary.TYPE_FOLDER) 
							{
								mi = new MenuItem(treemenu, SWT.SEPARATOR);
								
								mi = new MenuItem(treemenu, SWT.PUSH);
								mi.setText("Delete Folder");
								mi.setImage(SWTResourceManager.getImage(DWBrowser.class, "/menu/database-add.png"));
							}
							
							break;
							
						
						case DWLibrary.TYPE_URL:
							
							mi = new MenuItem(treemenu, SWT.PUSH);
							mi.setText("Delete URL");
							mi.setImage(SWTResourceManager.getImage(DWBrowser.class, "/menu/database-add.png"));
							
							break;
							
						case DWLibrary.TYPE_PATH:
							
							PathLibraryItem pitem = (PathLibraryItem) tree.getSelection()[0].getData();
							
							if (pitem.isValidDisk())
							{
								

								mi = new MenuItem(treemenu, SWT.PUSH);
								mi.setText("Insert Disk");
								mi.setImage(SWTResourceManager.getImage(DWBrowser.class, "/menu/disk-insert.png"));
								
									
								if (pitem.getNode() != null)
								{
									mi = new MenuItem(treemenu, SWT.SEPARATOR);
									
									mi = new MenuItem(treemenu, SWT.PUSH);
									mi.setText("Remove Disk Link");
									mi.setImage(SWTResourceManager.getImage(DWBrowser.class, "/menu/disk-delete.png"));
									
								}
								
							}
							else
							{
								// directory
								mi = new MenuItem(treemenu, SWT.PUSH);
								mi.setText("Refresh");
								mi.setImage(SWTResourceManager.getImage(DWBrowser.class, "/menu/database-refresh.png"));
							
								// mi = new MenuItem(treemenu, SWT.SEPARATOR);
								
								mi = new MenuItem(treemenu, SWT.PUSH);
								mi.setText("Search..");
								mi.setImage(SWTResourceManager.getImage(DWBrowser.class, "/menu/system-search-4.png"));
							
								
								if (pitem.getNode() != null)
								{
									mi = new MenuItem(treemenu, SWT.SEPARATOR);
									
									mi = new MenuItem(treemenu, SWT.PUSH);
									mi.setText("Remove Path Link");
									mi.setImage(SWTResourceManager.getImage(DWBrowser.class, "/menu/database-delete.png"));
									
								}
								
								
								
							}
							
							break;
							
					}
					
					
				}
			}
		});
		
		tree.setMenu(treemenu);
		
		tree.setData(MainWin.libraryroot);
		
		tree.addListener(SWT.SetData, new Listener() 
		{
		    public void handleEvent(Event event) 
		    {
		    	
		    	TreeItem item = (TreeItem)event.item;
		    	TreeItem parentItem = item.getParentItem();
		    	
		    	LibraryItem lit;
		    	
		    	if (parentItem == null) 
		    	{
		    	
		    		lit = ((LibraryItem[])tree.getData())[event.index];
		    	} 
		    	else 
		    	{
		    		
		    		lit = ((LibraryItem) parentItem.getData()).getChildren().get(event.index);
		    	}

		    	item.setText(0,lit.getTitle());
		    	
	    		item.setImage(lit.getIcon());
	    		item.setData(lit);
	    		
		    	item.setItemCount(lit.getChildren().size());

		    	
		    }
		    
		});
		
		tree.setItemCount(MainWin.libraryroot.length);
		sashForm.setWeights(new int[] {350, 700});
		
		
	}


	

	protected String shortURL(String url)
	{
		String res = url;
	
		if (res.indexOf("//") > -1)
		{
			res = res.substring(res.indexOf("//")+2);
		}
		
		if (res.indexOf("/") > -1)
		{
			res = res.substring(0, res.indexOf("/"));
		}
		
		return res;
	}




	public FileViewer getBestViewer(DWDECBFileSystemDirEntry direntry, byte[] contents)
	{
		int bestvote = -1;
		FileViewer bestv = null;
		
		for (FileViewer fv : viewers)
		{
			
			int vote = fv.getViewable(direntry, contents);
			
			if (vote > 0)
			{
				//
			
				if (vote > bestvote)
				{
					bestv = fv;
					bestvote = vote;
				}
				
				if (toolBarFileViewers != null)
				{
					for (ToolItem ti: toolBarFileViewers.getItems())
					{
						if ((ti != null) && (ti.getToolTipText() != null) && (ti.getToolTipText().equals(fv.getTypeName())))
								ti.setEnabled(true);
					}
				}
				
			}
			else if (toolBarFileViewers != null)
			{
				
				for (ToolItem ti: toolBarFileViewers.getItems())
				{
					if ((ti != null) && (ti.getToolTipText() != null) && (ti.getToolTipText().equals(fv.getTypeName())))
							ti.setEnabled(false);
				}
			}
		} 
		
		
		return bestv;
	}




	private void loadviewers(Composite parent)
	{
		this.viewers = new ArrayList<FileViewer>();
		
		viewers.add(new HexViewer(parent, SWT.NONE));
		viewers.add(new ASCIIViewer(parent, SWT.NONE));
		viewers.add(new BASICViewer(parent, SWT.NONE));
		//viewers.add(new HSCREENImageViewer(parent, SWT.NONE));
		viewers.add(new NIBImageViewer(parent, SWT.NONE));
		viewers.add(new PMODEImageViewer(parent, SWT.NONE));
		
	}



	private void loadlibs()
	{
		if (! MainWin.config.containsKey("Library.Local.updated") )
		{
			MainWin.config.addProperty("Library.Local.autocreated", System.currentTimeMillis());
			MainWin.config.addProperty("Library.Local.updated", 0);
		}
		
		locallib = MainWin.config.configurationAt("Library.Local");
		
		if (! MainWin.config.containsKey("Library.Cloud.updated") )
		{
			MainWin.config.addProperty("Library.Cloud.autocreated", System.currentTimeMillis());
			MainWin.config.addProperty("Library.Cloud.updated", 0);
		}
		
		cloudlib = MainWin.config.configurationAt("Library.Cloud");
		
	}





	/*

	@SuppressWarnings("unchecked")
	private void buildTree(TreeItem ti, List<Node> nodes)
	{
		
		
		List<ConfigItem> items = new ArrayList<ConfigItem>();
		
		HashMap<String,Integer> count = new HashMap<String,Integer>();
		
		for (Node t : nodes)
		{
			// recognized types..
			
			if (t.getName().equals("Directory") || t.getName().equals("URL") || t.getName().equals("Folder"))
			{
				if (count.containsKey(t.getName()))
					count.put(t.getName() , count.get(t.getName()) + 1);
				else
					count.put(t.getName(), 0);
				
				items.add(new ConfigItem(t,count.get(t.getName())));
			}
		}
			
		
		Collections.sort(items);
		

		for (ConfigItem item : items)
		{
			TreeItem tmp;
			if (ti == null)
			{
				tmp = new TreeItem(tree,SWT.NONE);
			}
			else
			{
				tmp = new TreeItem(ti, SWT.NONE);
			}
			
			
			if (UIUtils.getAttributeVal(item.getNode().getAttributes(),"name") == null)
				tmp.setText(0,item.getNode().getName());
			else
				tmp.setText(0, UIUtils.getAttributeVal(item.getNode().getAttributes(),"name").toString());
			
			if (item.getNode().getName().equals("URL"))
			{
				tmp.setImage(0, SWTResourceManager.getImage(DWBrowser.class, "/menu/world-link.png"));
				tmp.setData("browseable","yes");
				tmp.setData("url", item.getNode().getValue());
			}
			else if (item.getNode().getName().equals("Directory"))
				tmp.setImage(0, SWTResourceManager.getImage(DWBrowser.class, "/menu/folder-yellow.png"));
			else if (item.getNode().getName().equals("Folder"))
			{
				tmp.setImage(0, SWTResourceManager.getImage(DWBrowser.class, "/menu/folder.png"));
				tmp.setForeground(0, new Color(MainWin.getDisplay(), 128,128,128));
				tmp.setForeground(1, new Color(MainWin.getDisplay(), 128,128,128));
			}
			
			
			tmp.setData("param", item.getNode());
			tmp.setData("index", item.getIndex());
			
			if (item.getNode().getValue() == null)
			{
				if (UIUtils.getAttributeVal(item.getNode().getAttributes(),"desc") != null)
					tmp.setText(1, UIUtils.getAttributeVal(item.getNode().getAttributes(),"desc").toString());
				
			}
			else
			{
				tmp.setText(1,item.getNode().getValue().toString());
			}
		
			if (UIUtils.getAttributeVal(item.getNode().getAttributes(),"platform") != null)
				tmp.setText(2, UIUtils.getAttributeVals(item.getNode().getAttributes(),"platform").toString());
			
			if (UIUtils.getAttributeVal(item.getNode().getAttributes(),"category") != null)
				tmp.setText(3, UIUtils.getAttributeVals(item.getNode().getAttributes(),"category").toString());
			
			
			if (item.getNode().hasChildren())
			{
				buildTree(tmp, item.getNode().getChildren());
			}
			
			if (item.getNode().getName().equals("Directory"))
			{
				boolean r = false;
				
				if (UIUtils.getAttributeVal(item.getNode().getAttributes(),"recursive") != null)
					if (Integer.parseInt((String) UIUtils.getAttributeVal(item.getNode().getAttributes(),"recursive")) == 1)
						r = true;
				
				buildFSTree(tmp, item.getNode().getValue().toString(), r);
			}
				
		}
		
	
	}
	
	*/
	
	
	private void buildFSTree(TreeItem ti, String path, boolean recurse)
	{
		try
		{
			FileObject fo = VFS.getManager().resolveFile(path);
			buildFSTree(ti, fo, recurse);
		} 
		catch (FileSystemException e)
		{
			ti.setImage(0, SWTResourceManager.getImage(DWBrowser.class, "/menu/folder-important.png") );
			ti.setText(1, e.getMessage());
		}
		
	}
	
	private void buildFSTree(TreeItem ti, FileObject fo, boolean recurse)
	{
	
		try
		{
			
			
			if (fo.exists() && fo.isReadable())
			{
				if (fo.getType() == FileType.FOLDER)
				{
					FileObject[] files = fo.getChildren();
					
					for (int i = 0; i < files.length; i++)
					{
						
						if (files[i].getType() == FileType.FOLDER)
						{
							TreeItem fi = new TreeItem(ti,SWT.NONE);
							fi.setText(0, files[i].getName().getBaseName());
							
							fi.setImage(0, SWTResourceManager.getImage(DWBrowser.class, "/menu/folder-grey.png"));
							
							if (recurse)
								buildFSTree(fi, files[i], true );
							
							//tmp.setForeground(0, new Color(MainWin.getDisplay(), 128,128,128));
							//tmp.setForeground(1, new Color(MainWin.getDisplay(), 128,128,128));
							
						}
						else
						{
								try
								{
									
									DWDisk disk = DWDiskDrives.DiskFromFile(files[i]);
									
									TreeItem fi = new TreeItem(ti,SWT.NONE);
									fi.setText(0, files[i].getName().getBaseName());
									
									DWRBFFileSystem rbffs = new DWRBFFileSystem(disk);
									
									if (rbffs.isValidFS())
									{
										fi.setImage(0, SWTResourceManager.getImage(DWBrowser.class, "/fs/rbf.png"));
										// TODO
									}
									else
									{
										DWDECBFileSystem decbfs = new DWDECBFileSystem(disk);
									
										if (decbfs.isValidFS())
										{
											//buildDECBTree(fi, disk);
											fi.setImage(0, SWTResourceManager.getImage(DWBrowser.class, "/fs/decb.png"));
										}
										else
										{
											fi.setImage(0, SWTResourceManager.getImage(DWBrowser.class, "/fs/unknown.png"));
										}
									}
									
									fi.setText(1, String.format("%13s", NumberFormat.getIntegerInstance().format(disk.getFileObject().getContent().getSize())) + " bytes   " + String.format("%10s", NumberFormat.getIntegerInstance().format(disk.getDiskSectors())) + " sectors");
									
									fi.setText(2, DWUtils.prettyFormat(disk.getDiskFormat()) );
									
									if (!disk.getParam("_filesystem").toString().equals("unknown"))
										fi.setText(3, disk.getParam("_filesystem").toString());
									
									
								} 
								catch (DWImageFormatException e)
								{
									
								} 
								catch (IOException e)
								{
									TreeItem fi = new TreeItem(ti,SWT.NONE);
									fi.setText(0, files[i].getName().getBaseName());
									fi.setText(1, e.getMessage());
									fi.setImage(0, SWTResourceManager.getImage(DWBrowser.class, "/menu/failed_16.png"));
								}
							
								
							
							
						}
					}
				}
			}
			else
			{
				ti.setImage(0, SWTResourceManager.getImage(DWBrowser.class, "/menu/folder-important.png") );
				ti.setText(1,"Unreadable or nonexistent path");
			}
			
		}
		catch (FileSystemException e)
		{
			ti.setImage(0, SWTResourceManager.getImage(DWBrowser.class, "/menu/folder-important.png") );
			ti.setText(1, e.getMessage());
			
		}
		
		
	}




	public static String prettyFSType(int fsType)
	{
		String res = "Unknown";
		
		if (fsType == DWLibrary.FSTYPE_DECB)
			res = "DECB";
		else if (fsType == DWLibrary.FSTYPE_RBF)
			res = "OS9/RBF";
		return res;
	}

	
	/*
	private void buildDECBTree(TreeItem fi, DWDisk disk)
	{
		TreeItem ti = null;
		
		try
		{
			FileViewer bv;
			DWDECBFileSystem decbfs = new DWDECBFileSystem(disk);
			
			for (DWDECBFileSystemDirEntry entry : decbfs.getDirectory())
			{
				
				if (entry.isUsed() && !entry.isKilled())
				{
					
					
					ti = new TreeItem(fi, SWT.NONE);
					ti.setText(0, String.format("%-8s", entry.getFileName()) + "." + entry.getFileExt());
					int sectors = decbfs.getFileSectors( entry.getFileName().trim() + "." + entry.getFileExt()).size();
					int bytes = (sectors - 1) * 256 + entry.getBytesInLastSector();
					ti.setText(1, String.format("%13s", NumberFormat.getIntegerInstance().format(bytes)) + " bytes   " + String.format("%10s", NumberFormat.getIntegerInstance().format(sectors)) + " sectors");
					
					ti.setText(2, entry.getPrettyFileFlag());
					ti.setText(3, entry.getPrettyFileType());
					
					// TODO - use plugins
					
					bv = getBestViewer(decbfs, entry.getFileName().trim() + "." + entry.getFileExt() );
					
					if (bv == null)
					{
						ti.setImage(0, SWTResourceManager.getImage(DWBrowser.class, "/filetypes/blank.png"));
					}
					else
					{
						String is = bv.getTypeIcon();
						ti.setImage(0, SWTResourceManager.getImage(DWBrowser.class,  is));
						ti.setData("viewable", 1);
						ti.setData("viewfile", entry.getFileName().trim() + "." + entry.getFileExt());
						ti.setData("diskimage", disk);
					}
						
				}
			}
		}
		catch (IOException e)
		{
			if (ti != null)
			{
				ti.setImage(0, SWTResourceManager.getImage(DWBrowser.class, "/status/failed.png"));
				ti.setText(1, e.getMessage());
			}
		} 
		catch (DWFileSystemFileNotFoundException e)
		{
			if (ti != null)
			{
				ti.setImage(0, SWTResourceManager.getImage(DWBrowser.class, "/status/failed.png"));
				ti.setText(1, e.getMessage());
			}
		} 
		catch (DWFileSystemInvalidFATException e)
		{
			if (ti != null)
			{
				ti.setImage(0, SWTResourceManager.getImage(DWBrowser.class, "/status/failed.png"));
				ti.setText(1, e.getMessage());
			}
		} 
		catch (DWDiskInvalidSectorNumber e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DWFileSystemInvalidDirectoryException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	*/
	

}


