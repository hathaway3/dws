package com.groupunix.drivewireui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.HierarchicalConfiguration.Node;
import org.apache.commons.configuration.XMLConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Text;

public class ConfigEditor extends Shell
{

	private Tree tree;
	private Label lblItemTitle;
	private Composite scrolledComposite;
	private Composite composite_1;
	private Label textDescription;
	private Button btnToggle;
	private Button btnApply;
	
	private HierarchicalConfiguration itemDefs;
	private ToolBar toolBar;
	private ToolItem tltmMidi;
	private ToolItem tltmLogging;
	private ToolItem tltmDevice;
	private ToolItem tltmPrinting;
	private ToolItem tltmNetworking;
	private ToolItem tltmAll;
	private ToolItem tltmCheckAdvanced;

	
	private Node selected;
	private Label lblIntText;
	private Spinner spinnerInt;
	private TreeColumn trclmnNewColumn;
	
	private HashMap<Node,Object> changes = new HashMap<Node,Object>();
	private ModifyListener spinnerModifyListener;
	private Combo comboList;
	private Label lblList;
	private ModifyListener comboModifyListener;
	private Text textString;
	private Label lblString;
	private Button btnFileDir;
	private ToolItem tltmNetworking_1;
	
	
	
	
	class ConfigItem implements Comparable<ConfigItem>
	{
		private Node node;
		
		public ConfigItem(Node node)
		{
			this.node = node;
		}
		
		public Node getNode()
		{
			return this.node;
		}
		
		@Override
		public int compareTo(ConfigItem o)
		{
			if (o.getNode().hasChildren() && !this.getNode().hasChildren())
				return(-1);
			
			if (!o.getNode().hasChildren() && this.getNode().hasChildren())
				return(1);
			
			return(-1 * o.getNode().getName().compareTo(this.getNode().getName()));

		}
		
	}
	

	/**
	 * Create the shell.
	 * @param display
	 */
	public ConfigEditor(Display display)
	{
		super(display, SWT.SHELL_TRIM);
		addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				setRedraw(false);
				toolBar.setLayoutData(new RowData(getSize().x - 26, -1));
				scrolledComposite.setLayoutData(new RowData(getSize().x - 26, 150));
				composite_1.setLayoutData(new RowData(getSize().x - 26, 36));
				tree.setLayoutData(new RowData(-1, getSize().y-280));
				setRedraw(true);
			}
		});
		
		
		setLayout(new RowLayout(SWT.HORIZONTAL));
		
		toolBar = new ToolBar(this, SWT.FLAT | SWT.RIGHT);
		toolBar.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				tree.removeAll();
				loadConfig(null, MainWin.dwconfig.getRootNode().getChildren());
			}
		});
		toolBar.setLayoutData(new RowData(664, SWT.DEFAULT));
		
		tltmAll= new ToolItem(toolBar, SWT.RADIO);
		tltmAll.setImage(SWTResourceManager.getImage(ConfigEditor.class, "/menu/view-list-tree-4.png"));
		tltmAll.setSelection(true);
		tltmAll.setText("All");
		
		tltmDevice = new ToolItem(toolBar, SWT.RADIO);
		tltmDevice.setImage(SWTResourceManager.getImage(ConfigEditor.class, "/menu/connect.png"));
		tltmDevice.setText("Device");
		
		tltmPrinting = new ToolItem(toolBar, SWT.RADIO);
		tltmPrinting.setImage(SWTResourceManager.getImage(ConfigEditor.class, "/menu/document-print.png"));
		tltmPrinting.setText("Printing");
		
		tltmMidi = new ToolItem(toolBar, SWT.RADIO);
		tltmMidi.setImage(SWTResourceManager.getImage(ConfigEditor.class, "/menu/music.png"));
		tltmMidi.setText("MIDI");
		
		tltmNetworking_1 = new ToolItem(toolBar, SWT.RADIO);
		tltmNetworking_1.setImage(SWTResourceManager.getImage(ConfigEditor.class, "/menu/preferences-system-network-2.png"));
		tltmNetworking_1.setText("Networking");
		
		tltmLogging = new ToolItem(toolBar, SWT.RADIO);
		tltmLogging.setImage(SWTResourceManager.getImage(ConfigEditor.class, "/menu/documentation.png"));
		tltmLogging.setText("Logging");
		
		ToolItem toolItem = new ToolItem(toolBar, SWT.SEPARATOR);
		
		tltmCheckAdvanced = new ToolItem(toolBar, SWT.CHECK);
		tltmCheckAdvanced.setImage(SWTResourceManager.getImage(ConfigEditor.class, "/menu/cog-edit.png"));
		tltmCheckAdvanced.setText("Advanced");
		

		
		tree = new Tree(this, SWT.BORDER | SWT.FULL_SELECTION);
		tree.setLayoutData(new RowData(663, SWT.DEFAULT));
		tree.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				if (tree.getSelection()[0].getData("param") != null)
				{
					selected = (Node) tree.getSelection()[0].getData("param");
					displayParam(selected);
				}
			}
		});
		tree.setHeaderVisible(true);
		
		TreeColumn trclmnItem = new TreeColumn(tree, SWT.LEFT);
		trclmnItem.setWidth(232);
		trclmnItem.setText("Item");
		trclmnItem.addSelectionListener(new SortTreeListener());
		
		TreeColumn trclmnValue = new TreeColumn(tree, SWT.NONE);
		trclmnValue.setWidth(289);
		trclmnValue.setText("Current Value");
		
		trclmnNewColumn = new TreeColumn(tree, SWT.NONE);
		trclmnNewColumn.setWidth(121);
		trclmnNewColumn.setText("New Value");
		
		
		
		scrolledComposite = new Composite(this, SWT.NONE);
		
		
		lblItemTitle = new Label(scrolledComposite, SWT.NONE);
		lblItemTitle.setBounds(10, 10, 239, 24);
		lblItemTitle.setText("");
		lblItemTitle.setFont(SWTResourceManager.getBoldFont(display.getSystemFont()));
		
		
		textDescription = new Label(scrolledComposite, SWT.WRAP);
		textDescription.setBounds(10, 40, 647, 67);
		textDescription.setText("");
		textDescription.setVisible(true);
		
		btnToggle = new Button(scrolledComposite, SWT.CHECK);
		btnToggle.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateBoolean(selected,btnToggle.getSelection());
			}
		});
		btnToggle.setBounds(10,113, 300, 24);
		
		composite_1 = new Composite(this, SWT.NONE);
		FillLayout fl_composite_1 = new FillLayout(SWT.HORIZONTAL);
		fl_composite_1.spacing = 10;
		fl_composite_1.marginWidth = 5;
		fl_composite_1.marginHeight = 5;
		composite_1.setLayout(fl_composite_1);
		
		Button btnHelp = new Button(composite_1, SWT.NONE);
		btnHelp.setText("Help");
		
		Label label = new Label(composite_1, SWT.NONE);
		label.setText(" ");
		
		Button btnBackup = new Button(composite_1, SWT.NONE);
		btnBackup.setText("Backup");
		
		Button btnRestore = new Button(composite_1, SWT.NONE);
		btnRestore.setText("Restore");
		
		Label lblNewLabel = new Label(composite_1, SWT.NONE);
		lblNewLabel.setText(" ");
		
		Button btnOk = new Button(composite_1, SWT.NONE);
		btnOk.setText("Ok");
		
		Button btnCancel = new Button(composite_1, SWT.NONE);
		btnCancel.setText("Cancel");
		
		btnApply = new Button(composite_1, SWT.NONE);
		btnApply.setText("Apply");
		btnApply.setEnabled(false);
		
		scrolledComposite.setLayoutData(new RowData(667, 200));
		
		spinnerInt = new Spinner(scrolledComposite, SWT.BORDER);
		
		spinnerInt.setBounds(10, 114, 70, 22);
		
		
		this.spinnerModifyListener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateInt(selected,spinnerInt.getSelection());
			}
		};
		
		
		lblIntText = new Label(scrolledComposite, SWT.NONE);
		lblIntText.setBounds(86, 116, 398, 18);
		
		comboList = new Combo(scrolledComposite, SWT.READ_ONLY);
		comboList.setBounds(10, 114, 128, 23);
		
		this.comboModifyListener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateString(selected, comboList.getText());
			}
		};
		
		lblList = new Label(scrolledComposite, SWT.NONE);
		lblList.setBounds(150, 116, 357, 18);
		
		textString = new Text(scrolledComposite, SWT.BORDER);
		textString.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateString(selected, textString.getText());
			}
		});
		textString.setBounds(20, 116, 383, 21);
		
		btnFileDir = new Button(scrolledComposite, SWT.NONE);
		btnFileDir.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				String res;
				
				if (getAttributeVal(selected.getAttributes(),"type").equals("directory")) 
					res = MainWin.getFile(false, true, textString.getText(), "Choose " + selected.getName(), "Set directory for " +  selected.getName());
				else
					res = MainWin.getFile(false, false, textString.getText(), "Choose " + selected.getName(), "Choose file for " + selected.getName());
				
				if (res != null)
					textString.setText(res);
			}
		});
		btnFileDir.setBounds(409, 113, 98, 25);
		btnFileDir.setText("Choose...");
		
		lblString = new Label(scrolledComposite, SWT.NONE);
		lblString.setBounds(409, 116, 248, 18);
		composite_1.setLayoutData(new RowData(this.getSize().x, 100));
		
		createContents();
	}

	
	
	
	




	protected void updateBoolean(Node node, boolean selection)
	{
		if (node.getValue().equals(selection+""))
		{
			if (changes.containsKey(node))
				changes.remove(node);
			tree.getSelection()[0].setText(2,"");
		}
		else
		{
			changes.put(node, selection);
			tree.getSelection()[0].setText(2,selection+"");
		}
		
		updateApply();
	}


	protected void updateInt(Node node, int selection)
	{
		String cmp;
		
		if (node.getValue() != null)
		{
			cmp = node.getValue().toString();
		}
		else
		{
			cmp = "";
		}
		
		if (cmp.equals(selection+""))
		{
			if (changes.containsKey(node))
				changes.remove(node);
			tree.getSelection()[0].setText(2,"");
		}
		else
		{
			changes.put(node, selection);
			tree.getSelection()[0].setText(2,selection+"");
		}
		
		updateApply();
	}

	
	protected void updateString(Node node, String selection)
	{
		String cmp;
		
		if (node.getValue() != null)
		{
			cmp = node.getValue().toString();
		}
		else
		{
			cmp = "";
		}
		
		
		if (cmp.equals(selection))
		{
			if (changes.containsKey(node))
				changes.remove(node);
			tree.getSelection()[0].setText(2,"");
		}
		else
		{
			changes.put(node, selection);
			tree.getSelection()[0].setText(2,selection);
		}
			
		
		
		updateApply();
	}




	private void updateApply()
	{
		if (this.changes.size() > 0)
			this.btnApply.setEnabled(true);
		else
			this.btnApply.setEnabled(false);
	}









	private String getKeyPath(Node node)
	{
		String res = node.getName();
		
		if (node.getParent() != null)
		{
			res = getKeyPath(node.getParent()) + "." + res;
		}
		
		
		return res;
	}





	protected void displayParam(Node node)
	{
		
		if (this.getAttributeVal(node.getAttributes(), "type") == null)
		{
			// unknown item
			
			
			setDisplayFor(null,"none");
		}
		else
		{
			String type = this.getAttributeVal(node.getAttributes(), "type").toString();
			
			setDisplayFor(node, type);
			
		}
			
		
		
	}

	
	
	
	
	
	
	private void setDisplayFor(Node node, String type)
	{
		this.setRedraw(false);
		// all off
		this.lblItemTitle.setVisible(false);
		this.btnToggle.setVisible(false);
		this.textDescription.setVisible(false);
		this.spinnerInt.setVisible(false);
		this.lblIntText.setVisible(false);
		this.comboList.setVisible(false);
		this.lblList.setVisible(false);
		this.lblString.setVisible(false);
		this.btnFileDir.setVisible(false);
		this.textString.setVisible(false);
		
		if (type.equals("boolean"))
		{
			this.lblItemTitle.setVisible(true);
			this.lblItemTitle.setText(node.getName());
			this.btnToggle.setVisible(true);
			
			if (changes.containsKey(node))
				this.btnToggle.setSelection(Boolean.parseBoolean(changes.get(node).toString()));
			else
				this.btnToggle.setSelection(Boolean.parseBoolean(node.getValue().toString()));
			this.textDescription.setVisible(true);
			this.btnToggle.setText("Enable " + node.getName());
			showHelpFor(node);
			
		} 
		else if (type.equals("int"))
		{
			this.lblItemTitle.setVisible(true);
			this.lblItemTitle.setText(node.getName());
			
			this.textDescription.setVisible(true);
			showHelpFor(node);
			
			this.spinnerInt.removeModifyListener(this.spinnerModifyListener);
			
			
			if (this.hasAttribute(node.getAttributes(), "min"))
				this.spinnerInt.setMinimum(Integer.parseInt(this.getAttributeVal(node.getAttributes(), "min").toString()));
			
			if (this.hasAttribute(node.getAttributes(), "max"))
				this.spinnerInt.setMaximum(Integer.parseInt(this.getAttributeVal(node.getAttributes(), "max").toString()));
			
			if (changes.containsKey(node))
				this.spinnerInt.setSelection(Integer.parseInt(changes.get(node).toString()));
			else if (node.getValue() != null)
				this.spinnerInt.setSelection(Integer.parseInt(node.getValue().toString()));
			
			
			this.spinnerInt.addModifyListener(this.spinnerModifyListener);
			
			this.spinnerInt.setVisible(true);
			this.lblIntText.setVisible(true);
			this.lblIntText.setText("Choose value for " + node.getName());
			
			
		}
		else if (type.equals("list"))
		{
			this.lblItemTitle.setVisible(true);
			this.lblItemTitle.setText(node.getName());
			this.comboList.setVisible(true);
			
			this.comboList.removeModifyListener(this.comboModifyListener);
			
			this.comboList.removeAll();
			
			if (this.hasAttribute(node.getAttributes(), "list"))
			{
				for (String s : this.getAttributeVals(node.getAttributes(), "list"))
				{
					this.comboList.add(s);
				}
				
			}
			
			
			if (changes.containsKey(node))
				this.comboList.select(this.comboList.indexOf(changes.get(node).toString()));
			else if ((node.getValue() != null) && (this.comboList.indexOf(node.getValue().toString()) > -1))
				this.comboList.select(this.comboList.indexOf(node.getValue().toString()));
			
			this.comboList.addModifyListener(this.comboModifyListener);
			
			this.lblList.setVisible(true);
			this.lblList.setText("Select value for " + node.getName());
			
			this.textDescription.setVisible(true);
			showHelpFor(node);
		}
		else if (type.equals("section"))
		{
			this.lblItemTitle.setVisible(true);
			this.lblItemTitle.setText(node.getName());
			this.comboList.setVisible(true);
			
			this.comboList.removeModifyListener(this.comboModifyListener);
			
			this.comboList.removeAll();
			
			if (this.hasAttribute(node.getAttributes(), "section"))
			{
				String key = this.getAttributeVal(node.getAttributes(), "section").toString();
				
				for (int i = 0;i <= MainWin.dwconfig.getMaxIndex(key);i++)
				{
					if (MainWin.dwconfig.containsKey(key + "(" + i + ")[@name]"))
						this.comboList.add(MainWin.dwconfig.getString(key + "(" + i + ")[@name]"));
				}
				
				
			}
			
			
			if (changes.containsKey(node))
				this.comboList.select(this.comboList.indexOf(changes.get(node).toString()));
			else
				this.comboList.select(this.comboList.indexOf(node.getValue().toString()));
			
			this.comboList.addModifyListener(this.comboModifyListener);
			
			this.lblList.setVisible(true);
			this.lblList.setText("Select value for " + node.getName());
			
			this.textDescription.setVisible(true);
			showHelpFor(node);
		}
		else if (type.equals("string"))
		{
			this.lblItemTitle.setVisible(true);
			this.lblItemTitle.setText(node.getName());
			
			this.textDescription.setVisible(true);
			showHelpFor(node);
			
			if (changes.containsKey(node))
				this.textString.setText(changes.get(node).toString());
			else if (node.getValue() != null)
				this.textString.setText(node.getValue().toString());
			else
				this.textString.setText("");
			
			this.textString.setVisible(true);
			this.lblString.setVisible(true);
			this.lblString.setText("Enter value for " + node.getName());
			
		}
		else if (type.equals("file") || type.equals("directory")) 
		{
			this.lblItemTitle.setVisible(true);
			this.lblItemTitle.setText(node.getName());
			
			this.textDescription.setVisible(true);
			showHelpFor(node);
			
			if (changes.containsKey(node))
				this.textString.setText(changes.get(node).toString());
			else if (node.getValue() != null)
				this.textString.setText(node.getValue().toString());
			else
				this.textString.setText("");
			
			this.textString.setVisible(true);
			this.btnFileDir.setVisible(true);
		}
		
		this.setRedraw(true);
	}









	private void showHelpFor(final Node node)
	{
		textDescription.setText("Loading help for " + node.getName() + "...");
		
		Thread t = new Thread(
				  new Runnable() {
					  public void run()
					  {
						  String txt = "";
						  
						  try
							{
								List<String> help = UIUtils.loadList(MainWin.getInstance(),"ui server show help " + getKeyPath(node));
								
								for (String t:help)
								{
									txt += t;
								}
								
							}
							catch (IOException e)
							{
								txt = e.getMessage();
							} 
							catch (DWUIOperationFailedException e)
							{
								txt = "No help found for " + node.getName(); 
							}
							
							final String ftxt = txt;
							final String fname = node.getName();
							
							if (!isDisposed())
							getDisplay().asyncExec( new Runnable() {
								public void run()
								{
									if (!textDescription.isDisposed() && selected.getName().equals(fname))
										textDescription.setText(ftxt);
								}});
					  }
				  });
		
		t.start();
	}	









	/**
	 * Create contents of the shell.
	 */
	protected void createContents()
	{
		setText("Configuration Editor");
		setSize(684, 534);
		
		
		loadConfig(null, MainWin.dwconfig.getRootNode().getChildren());
		
	}

	private void loadConfig(TreeItem ti, List<Node> nodes)
	{
		setDisplayFor(null,"none");
		this.setRedraw(false);
		loadAllConfig(ti,nodes);
		filterConfig(null);
		this.setRedraw(true);
		
		if (this.selected != null)
			tryToSelect(selected);
		
	}
	
	private void tryToSelect(Node node)
	{
		
		for (TreeItem t:tree.getItems())
		{
			if (t.getData("param").equals(node))
			{
				tree.select(t);
				this.displayParam(node);
				return;
			}
		}
		
		// our item isn't in the list..
		
		setDisplayFor(null,"none");
	}









	private void filterConfig(TreeItem ti)
	{
		TreeItem[] items;
		
		if (ti == null)
			items = tree.getItems();
		else
			items = ti.getItems();
		
		for (TreeItem item : items)
		{
			if (item.getItemCount() > 0)
			{
				filterConfig(item);
			}
			
			if (item.getItemCount() == 0)
			{
				if (!filterItem((Node) item.getData("param")))
				{
					item.dispose();
				}
			}
		}
		
	}
	
	
	private void loadAllConfig(TreeItem ti, List<Node> nodes)
	{
	
		List<ConfigItem> items = new ArrayList<ConfigItem>();
		
		for (Node t : nodes)
		{
			items.add(new ConfigItem(t));
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
			
			if (getAttributeVal(item.getNode().getAttributes(),"name") == null)
				tmp.setText(0,item.getNode().getName());
			else
				tmp.setText(0, item.getNode().getName() + ": " + getAttributeVal(item.getNode().getAttributes(),"name").toString());
			
			tmp.setData("param", item.getNode());
			
			if (item.getNode().getValue() == null)
			{
				if (getAttributeVal(item.getNode().getAttributes(),"desc") != null)
					tmp.setText(1, getAttributeVal(item.getNode().getAttributes(),"desc").toString());
				else if (hasAttribute(item.getNode().getAttributes(),"dev") && hasAttribute(item.getNode().getAttributes(),"gm"))
				{
					tmp.setText(1, "In (native): " + getAttributeVal(item.getNode().getAttributes(),"dev").toString() + " Out (GM): " + getAttributeVal(item.getNode().getAttributes(),"gm").toString());
				}
			}
			else
			{
				tmp.setText(1,item.getNode().getValue().toString());
			}
		
			if (changes.containsKey(item.getNode()))
				tmp.setText(2, changes.get(item.getNode()).toString());
			
			if (item.getNode().hasChildren())
			{
				loadAllConfig(tmp, item.getNode().getChildren());
				
				if (item.getNode().getName().equals("instance"))
					tmp.setExpanded(true);
			}
				
		}
		
	}
	
	
	
	
	
	private boolean filterItem(Node t)
	{
		List<Node> attributes = t.getAttributes();
		
		// advanced/unknown
		//if (!this.tltmCheckAdvanced.getSelection() && !hasAttribute(attributes,"category"))
		//	return(false);
		
		if (!this.tltmCheckAdvanced.getSelection() && matchAttributeVal(attributes,"category","advanced"))
			return(false);
		
		// category
		if (this.tltmAll.getSelection())
			return(true);
		//else if (!hasAttribute(attributes,"category"))
		//	return(false);
		
		if (this.tltmMidi.getSelection() && !matchCategory(t,"midi"))
			return false;
		
		if (this.tltmDevice.getSelection() && !matchCategory(t,"device"))
			return false;
		
		if (this.tltmPrinting.getSelection() && !matchCategory(t,"printing"))
			return false;
		
		if (this.tltmLogging.getSelection() && !matchCategory(t,"logging"))
			return false;
		
		//if (this.tltmNetworking.getSelection() && !matchAttributeVal(attributes,"category","networking"))
		//	return false;
		
		
		
		return true;
	}





	private boolean matchCategory(Node node, String category)
	{
	
		
		if (matchAttributeVal(node.getAttributes(), "category", category))
			return true;
		
		if (node.getParent() != null)
		{
			return(matchCategory(node.getParent(), category));
		}
				
		return false;
	}





	private boolean matchAttributeVal(List<Node> attributes, String key, String val)
	{
		for (Node n : attributes)
		{
			if (n.getName().equals(key) && n.getValue().equals(val))
				return(true);
		}
		
		return false;
	}





	private Object getAttributeVal(List<Node> attributes, String key)
	{
		for (Node n : attributes)
		{
			if (n.getName().equals(key))
			{
				return(n.getValue());
			}
		}
		
		return null;
	}

	private List<String> getAttributeVals(List<Node> attributes, String key)
	{
		List<String> res = new ArrayList<String>();
		
		for (Node n : attributes)
		{
			if (n.getName().equals(key))
			{
				res.add(n.getValue().toString());
			}
		}
		
		return res;
	}



	private boolean hasAttribute(List<Node> attributes, String key)
	{
		for (Node n : attributes)
		{
			if (n.getName().equals(key))
				return(true);
		}
		
		return false;
	}





	@Override
	protected void checkSubclass()
	{
		// Disable the check that prevents subclassing of SWT components
	}
	protected Label getLblIntText() {
		return lblIntText;
	}
	protected Spinner getSpinnerInt() {
		return spinnerInt;
	}
	protected Combo getComboList() {
		return comboList;
	}
	protected Label getLblList() {
		return lblList;
	}
	protected Label getLblString() {
		return lblString;
	}
	protected Button getBtnFileDir() {
		return btnFileDir;
	}
	protected Text getTextString() {
		return textString;
	}
}
