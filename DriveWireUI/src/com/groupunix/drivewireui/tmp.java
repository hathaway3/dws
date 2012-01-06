package com.groupunix.drivewireui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class tmp
{
	public static void main(String[] arg)
	{
		
		try
		{
			RandomAccessFile f = new RandomAccessFile(new File("coco3.rom"),"r");
			
			
			dumpblock(f,0x2000);
			dumpblock(f,0x2a00);
			dumpblock(f,0x4500);
			dumpblock(f,0x5c00);
			dumpblock(f,0x6000);
			dumpblock(f,0x7000);
			dumpblock(f,0x7700);
			
			
			
			
			
			f.close();
			
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

	private static void dumpblock(RandomAccessFile f, int pos) throws IOException
	{
		byte[] buf = new byte[512];
		
		f.seek(pos);
		
		f.readFully(buf, 0, 512);
		
		System.out.print("{ ");
		
		for (int i = 0; i < 512;i++)
		{
			if (i % 20 == 0)
				System.out.println();
			
			System.out.print((0xff & buf[i]) + ", "); 
		}
		
		System.out.println(" }");
		
	}
}
