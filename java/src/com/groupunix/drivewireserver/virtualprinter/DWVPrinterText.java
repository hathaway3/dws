package com.groupunix.drivewireserver.virtualprinter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.groupunix.drivewireserver.dwexceptions.DWPrinterFileError;
import com.groupunix.drivewireserver.dwexceptions.DWPrinterNotDefinedException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWUtils;
import com.groupunix.drivewireserver.virtualserial.DWVSerialCircularBuffer;

public class DWVPrinterText implements DWVPrinterDriver 
{

	private DWVSerialCircularBuffer printBuffer = new DWVSerialCircularBuffer(-1, true);
	private DWVPrinter vprinter;
	
	public DWVPrinterText(DWVPrinter dwvPrinter) 
	{
		this.vprinter = dwvPrinter;
		this.printBuffer.clear();
	}

	
	@Override
	public void addByte(byte data) throws IOException 
	{
		this.printBuffer.getOutputStream().write(data);
		
	}

	@Override
	public String getDriverName() 
	{
		return("TEXT");
	}

	@Override
	public void flush() throws IOException, DWPrinterNotDefinedException, DWPrinterFileError 
	{
		
		File file = getPrinterFile();
		
		FileOutputStream fos = new FileOutputStream(file);
		
		while (this.printBuffer.getAvailable() > 0)
		{
			byte[] buf = new byte[256];
			int read = this.printBuffer.getInputStream().read(buf);
			fos.write(buf, 0, read);
		}
		
		fos.flush();
		fos.close();
		
	}


	public String getFileExtension() 
	{
		return(".txt");
	}


	public String getFilePrefix() 
	{
		return("dw_text_");
	}

	
	public File getPrinterFile() throws IOException, DWPrinterNotDefinedException, DWPrinterFileError 
	{
		if (vprinter.getConfig().containsKey("PrinterFile"))
		{
			if (DWUtils.FileExistsOrCreate(vprinter.getConfig().getString("PrinterFile")))
			{
				return(new File(vprinter.getConfig().getString("PrinterFile")));
			}
			else
			{
				throw new DWPrinterFileError("Cannot find or create the output file '" + vprinter.getConfig().getString("PrinterFile") + "'");
			}
			
		} 
		else if (vprinter.getConfig().containsKey("PrinterDir"))
		{
			if (DWUtils.DirExistsOrCreate(vprinter.getConfig().getString("PrinterDir")))
			{
				return(File.createTempFile(getFilePrefix(),getFileExtension(), new File(vprinter.getConfig().getString("PrinterDir"))));
					
			}
			else
			{
				throw new DWPrinterFileError("Cannot find or create the output directory '" + vprinter.getConfig().getString("PrinterDir") + "'");
				
			}
		
		}
		else
		{
			throw new DWPrinterFileError("No PrinterFile or PrinterDir defined in config");
		}
	}

	
	
}
