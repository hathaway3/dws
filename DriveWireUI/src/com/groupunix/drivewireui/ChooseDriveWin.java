package com.groupunix.drivewireui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class ChooseDriveWin extends Dialog
{

	protected int result = -1;
	protected Shell shlChooseDriveFor;
	private Combo comboDrive;
	
	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 * @param i 
	 */
	public ChooseDriveWin(Shell parent, int style, int i)
	{
		super(parent, style);
		result = i;
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public int open()
	{
		createContents();
		
		loadCombo();
		comboDrive.select(result);
		
		int x = getParent().getBounds().x + (getParent().getBounds().width / 2) - (shlChooseDriveFor.getBounds().width / 2);
		int y = getParent().getBounds().y + (getParent().getBounds().height / 2) - (shlChooseDriveFor.getBounds().height / 2);
		
		shlChooseDriveFor.setLocation(x, y);
		
		shlChooseDriveFor.open();
		shlChooseDriveFor.layout();
		Display display = getParent().getDisplay();
		while (!shlChooseDriveFor.isDisposed())
		{
			if (!display.readAndDispatch())
			{
				display.sleep();
			}
		}
		return result;
	}

	private void loadCombo()
	{
		String tmp;
		for (int i = 0;i<256;i++)
		{
			tmp = "(empty)";
			if (MainWin.getDiskDefs()[i].isLoaded())
				tmp = MainWin.getDiskDefs()[i].getFileName();
			comboDrive.add(i + ": " + tmp);
		}
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents()
	{
		shlChooseDriveFor = new Shell(getParent(), getStyle());
		shlChooseDriveFor.setSize(295, 131);
		shlChooseDriveFor.setText("Choose drive for insert...");
		
		comboDrive = new Combo(shlChooseDriveFor, SWT.NONE);
		comboDrive.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.character == (char)13)
				{
					doReturn();
				}
				
			}
		});
		comboDrive.setBounds(55, 22, 217, 23);
		
		Label lblDrive = new Label(shlChooseDriveFor, SWT.NONE);
		lblDrive.setBounds(10, 25, 39, 15);
		lblDrive.setText("Drive:");
		
		Button btnInsert = new Button(shlChooseDriveFor, SWT.NONE);
		btnInsert.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				doReturn();
			}
		});
		btnInsert.setBounds(102, 70, 90, 25);
		btnInsert.setText("Insert");
		
		Button btnCancel = new Button(shlChooseDriveFor, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				result = -1;
				e.display.getActiveShell().close();
			}
		});
		btnCancel.setBounds(207, 70, 65, 25);
		btnCancel.setText("Cancel");

	}

	protected void doReturn()
	{
		if (comboDrive.getSelectionIndex() == -1)
		{
			try
			{
				result = Integer.parseInt(comboDrive.getText());
				
				if ((result > -1) && (result < 256))
				{
					this.shlChooseDriveFor.close();
				}
			}
			catch (NumberFormatException e2)
			{
			}
		}
		else
		{
			result = comboDrive.getSelectionIndex();
			this.shlChooseDriveFor.close();
		}
	}
}
