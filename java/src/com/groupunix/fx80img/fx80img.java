package com.groupunix.fx80img;

// emulate an epson fx80.  VERY INCOMPLETE

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.DriveWireServer;

public class fx80img implements Runnable {

	private static final Logger logger = Logger.getLogger("fx80img");
	
	// 300 dpi
	public static final double DEF_XSIZE = 300 * 8.5;
	public static final double DEF_YSIZE = 300 * 11;
	
	private static final double SZ_PICA = DEF_XSIZE / 80;
	private static final double SZ_ELITE = DEF_XSIZE / 96;
	private static final double SZ_COMPRESSED = DEF_XSIZE / 132;

	
	private boolean m_expanded = false;
	@SuppressWarnings("unused")
	private boolean m_pica = true;
	private boolean m_elite = false;
	private boolean m_compressed = false;
	private boolean m_doublestrike = false;
	private boolean m_emphasized = false;
	
	private boolean m_escape = false;
	
	private double line_height;
	private double char_width;
	
	private characterset charset = new characterset();
	private Graphics2D rGraphic;
	
	private String printText;
	private File printDir;
	private File printFile;
	
	private double xpos;
	private double ypos;
	
	private BufferedImage rImage;
	private int handlerno;
	
	
	public fx80img(int handlerno, String text, File dir)
	{
		this.printDir = dir;
		this.printText = text;
		this.handlerno = handlerno;
		this.line_height = DEF_YSIZE / DriveWireServer.getHandler(this.handlerno).config.getInt("PrinterLines", 66);
		// this.char_width = DEF_XSIZE / DriveWireServer.config.getInt("PrinterColumns", 80);
		
		
	}
	
	public void print(String printText, File printDir) throws NumberFormatException, IOException 
	{
	
		// load characters
		
		loadCharacter(DriveWireServer.getHandler(this.handlerno).config.getString("PrinterCharacterFile","default.chars"));

		// init img

				
        rImage = new BufferedImage((int)DEF_XSIZE, (int)DEF_YSIZE, BufferedImage.TYPE_BYTE_INDEXED);
        //.TYPE_USHORT_GRAY );
        rGraphic = (Graphics2D) rImage.getGraphics();

        
        rGraphic.setColor(Color.WHITE);	
        rGraphic.fillRect(0, 0, (int) DEF_XSIZE, (int) DEF_YSIZE);
        
      
        this.char_width = getCPI();
        
		
		// process file
		
	    xpos = 0;
	    ypos = line_height;
	    
	    for (int i = 0;i<printText.length();i++)
	    {
	    	char c = printText.charAt(i);
	    	
	    	int cc = (int) c;
	    	
	    	// control codes
	    	if (m_escape)
	    	{
	    		switch (cc)
	    		{
	    			case 64:
	    			{
	    				reset_printer();
	    				break;
	    				
	    			}
	    			case 'E':
	    			{
	    				m_emphasized = true;
	    				break;
	    			}
	    			case 'F':
	    			{
	    				m_emphasized = false;
	    			}
	    			case 'G':
	    			{
	    				m_doublestrike = true;
	    				break;
	    			}
	    			case 'H':
	    			{
	    				m_doublestrike = false;
	    				break;
	    			}
	    			
	    			case 'M':
	    			{
	    				m_elite = true;
	    				break;
	    			}
	    			case 'P':
	    			{
	    				m_elite = false;
	    				break;
	    			}
	    		}
	    		
	    		this.char_width = getCPI();
	    		//logger.debug("character width: " + this.char_width);
	    		this.m_escape = false;
	    	}
	    	
	    	else if ( (cc<32) || (cc == 127) || ((cc > 127) && (cc < 160)) || (cc == 255))
	    	{
	    		
	    		switch (cc)
	    		{
	    			case 9:
	    			{
	    				xpos += (8 * char_width);
	    				break;
	    			}
	    			case 13:
	    			{
	    				xpos = 0;
	    		    	newline();
	    		    	break;
	    			}
	    			case 14:
	    			{
	    				m_expanded = true;
	    				break;
	    			}
	    			case 15:
	    			{
	    				m_compressed = true;
	    				break;
	    			}
	    			case 18:
	    			{
	    				m_compressed = false;
	    				break;
	    			}
	    			case 20:
	    			{
	    				m_expanded = false;
	    				break;
	    			}
	    			case 27:
	    			{
	    				m_escape = true;
	    				break;
	    			}
	    		}
	    		
	    		// apply
	    		this.char_width = getCPI();
	    		//logger.debug("character width: " + this.char_width);
	    	}
	     	else
	    	{
	    		drawCharacter(c,xpos,ypos);
	    		// drawCharacter(c,xpos+2,ypos,2);
	    		
	    		if (m_expanded)
	    		{
	    			xpos += (char_width * 2);
	    		}
	    		else
	    		{
	    			xpos += char_width;
	    		}
	    		
	    		if (xpos >= DEF_XSIZE)
	    		{
	    			// line wrap
	    			xpos = 0;
	    			newline();
	    		}
	    		
	    	}
	    
	    	
	    }
       
	    
		// output img

        try 
        {
        	printFile = File.createTempFile("dw_print_",getFileExt(DriveWireServer.getHandler(this.handlerno).config.getString("PrinterImageFormat","PNG")),printDir);
    		
            ImageIO.write(rImage, DriveWireServer.getHandler(this.handlerno).config.getString("PrinterImageFormat","PNG"), printFile);
            logger.info("wrote last print page image to: " + printFile.getAbsolutePath());
            
        } 
        catch (IOException ex) 
        {
            logger.warn("Cannot save print image: " + ex.getMessage());
        }

        
	}

	private void reset_printer()
	{
		this.m_expanded = false;
		this.m_pica = true;
		this.m_elite = false;
		this.m_compressed = false;
		this.m_doublestrike = false;
		this.m_emphasized = false;
	}

	private double getCPI()
	{
		double sz;
		
		if (m_elite)
		{
			
			sz = SZ_ELITE;
		}
		else if (m_compressed)
		{
			 sz = SZ_COMPRESSED;
		}
		else
		{
			sz = SZ_PICA;
		}
		
		return(sz);
	}

	
	private void newline()
	{
		ypos += line_height;
		
		if (ypos >= DEF_YSIZE)
		{
			// new page
			ypos = line_height;
			
			try 
		    {
				printFile = File.createTempFile("dw_print_",getFileExt(DriveWireServer.getHandler(this.handlerno).config.getString("PrinterImageFormat","PNG")),printDir);
				ImageIO.write(rImage, DriveWireServer.getHandler(this.handlerno).config.getString("PrinterImageFormat","PNG"), printFile);
				
				logger.info("wrote print page image to: " + printFile.getAbsolutePath());
				
				rImage = new BufferedImage((int)DEF_XSIZE, (int)DEF_YSIZE, BufferedImage.TYPE_USHORT_GRAY );
				rGraphic = (Graphics2D) rImage.getGraphics();
			       
				rGraphic.setColor(Color.WHITE);	
			    rGraphic.fillRect(0, 0, (int) DEF_XSIZE, (int) DEF_YSIZE);
		    } 
		    catch (IOException ex) 
		    {
		    	 logger.warn("Cannot save print image: " + ex.getMessage());
		    }
			
			
		}
	}

	private String getFileExt(String set) 
	{
		if (set.equalsIgnoreCase("JPEG") || set.equalsIgnoreCase("JPG") )
			return(".jpg");

		if (set.equalsIgnoreCase("GIF"))
			return(".gif");

		if (set.equalsIgnoreCase("BMP"))
			return(".bmp");

		if (set.equalsIgnoreCase("WBMP"))
			return(".bmp");

		
		return ".png";
	}

	private void drawCharacter(int ch, double xpos, double ypos) 
	{
		// draw one character.. just testing
		
		for (int i = 0; i<12;i++)
		{
			int lbits = charset.getCharacterCol(ch, i);
		
			drawCharCol(lbits,xpos, ypos);
			
			if (m_doublestrike)
			{
				drawCharCol(lbits,xpos, ypos + (DEF_YSIZE / 66 / 24));
			}
			
			if (m_expanded)
			{
				xpos += (this.char_width / 6);
				drawCharCol(lbits,xpos, ypos);
				
				if (m_doublestrike)
				{
					drawCharCol(lbits,xpos, ypos + (DEF_YSIZE / 66 / 24));
				}
				
			}
			else if (m_emphasized)
			{
				xpos += (this.char_width / 12);
				drawCharCol(lbits,xpos, ypos);
				
				if (m_doublestrike)
				{
					drawCharCol(lbits,xpos, ypos + (DEF_YSIZE / 66 / 24));
				}
			}
			else
			{
				xpos += (this.char_width / 12);
			}
			
		}
		
	}

	private void drawCharCol(int lbits, double xpos, double ypos) 
	{
		// draw one column
		
		for (int i = 0; i < 9; i++)
		{
			if ((lbits & (int) Math.pow(2,i)) == Math.pow(2,i))
			{
				
				
				int r = (int) (char_width / 5);
				@SuppressWarnings("unused")
				int x = ((int) xpos) - (r/2);
				@SuppressWarnings("unused")
				int y = ((int) ypos) - (r/2);
				
				int[] pdx = new int[4];
				int[] pdy = new int[4];
				
				int[] sdx = new int[8];
				int[] sdy = new int[8];
				
				int ix = (int) xpos;
				int iy = (int) ypos;
				
				pdx[0] = ix - 2;
				pdy[0] = iy;
				pdx[1] = ix;
				pdy[1] = iy - 2;
				pdx[2] = ix + 2;
				pdy[2] = iy;
				pdx[3] = ix;
				pdy[3] = iy + 2;
				
				sdx[0] = ix - 2;
				sdy[0] = iy - 1;
				
				sdx[1] = ix - 1;
				sdy[1] = iy - 2;
				
				sdx[2] = ix + 1;
				sdy[2] = iy - 2;
				
				sdx[3] = ix + 2;
				sdy[3] = iy - 1;
				
				sdx[4] = ix + 2;
				sdy[4] = iy + 1;
				
				sdx[5] = ix + 1;
				sdy[5] = iy + 2;
				
				sdx[6] = ix - 1;
				sdy[6] = iy + 2;
			
				sdx[7] = ix - 2;
				sdy[7] = iy + 1;
				
				
				rGraphic.setColor(Color.GRAY);
				rGraphic.drawPolygon(sdx, sdy, 8);
				
				rGraphic.setColor(Color.BLACK);
				rGraphic.fillPolygon(pdx, pdy, 4);
				
				rGraphic.setColor(Color.DARK_GRAY);
				rGraphic.drawPolygon(pdx, pdy, 4);
				
				//rGraphic.fillOval(x, y, 2, 2);
				
				rGraphic.setColor(Color.BLACK);
				
				r = (int) (char_width / 6);
				x = ((int) xpos) - (r/2);
				y = ((int) ypos) - (r/2);
				
				//rGraphic.fillOval(x,y, r, r);
			}
			
			ypos -= (line_height/10);
		}
		
		
	}

	private void loadCharacter(String fname) throws NumberFormatException, IOException 
	{
	    int curline = 0;
	    int curchar = -1;
	    int curpos = -1;
	    int[] charbits = new int[12];
	    int prop = 0;
	    
		//try
		{
		    FileInputStream fstream = new FileInputStream(fname);
		    DataInputStream in = new DataInputStream(fstream);
		
		    BufferedReader br = new BufferedReader(new InputStreamReader(in));
			
		    String strLine;
			
		    
		    while ((strLine = br.readLine()) != null)   
		    {
		    	curline++;
		    	
			    if ((strLine.startsWith("#")) || (strLine.length() == 0))
			    {
			    	// comment
			    	// System.out.println(strLine);
			    }
			    else
			    {
			    	// process input
			    	if (strLine.startsWith("c"))
			    	{
			    		if (curchar > -1)
			    		{
			    			// finish current char
			    			charset.setCharacter(curchar, charbits, prop);
			    			
			    			
			    			curpos = -1;
			    			curchar = -1;
			    			charbits = new int[12];
			    		}
		    		
			    		//start new char
			    		int tmpint = Integer.parseInt(strLine.substring(1));
			    		
			    		if ((tmpint < 0) || (tmpint > 255))
			    		{
			    			System.err.println("Error at line " + curline + ": invalid character number, must be 0-255 ");
			    		}
			    		else
			    		{
			    			curpos = 0;
			    			prop = 12;
			    			curchar = tmpint;
			    		}
			    	}
			    	else if (strLine.startsWith("p"))
			    	{
			    		// set prop val
			    		int tmpint  = Integer.parseInt(strLine.substring(1));
			    		
			    		if ((tmpint < 1) || (tmpint > 12))
			    		{
			    			System.err.println("Error at line " + curline + ": invalid proportional length, must be 1-12 ");	
			    		}
			    		else
			    		{
			    			prop = tmpint;
			    		}
			    		
			    	}
			    	else
			    	{
			    		// parse bits line
			    		if (strLine.length() == 9)
			    		{
			    			// boolean bits
			    			int tmpval = 0;
			    			
			    			for (int i = 0;i<9;i++)
			    			{
			    				char c = strLine.charAt(i);
			    				if (c == '1')
			    				{
			    					tmpval += Math.pow(2,i);
			    								    				}
			    				else if (c != '0')
			    				{
			    					System.err.println("Error at line " + curline + " (in character " + curchar + "): boolean values must contain only 0 or 1");
				    			}
			    			}
			    			
			    			charbits[curpos] = tmpval;
			    			curpos++;
			    		}
			    		else
			    		{
			    			// decimal value
			    			int tmpval = Integer.parseInt(strLine);
			    			if ((tmpval < 0) || (tmpval > 511))
			    			{
			    				tmpval = 0;
			    				System.err.println("Error at line " + curline + " (in character " + curchar + "): decimal values must be 0-511");
			    			}
			    			charbits[curpos] = tmpval;
			    			curpos++;
			    		}
			    	}
			    	

			    }
			    
		    	
			}
			
		    in.close();
			
		    // finish last char
		    charset.setCharacter(curchar, charbits, prop);
		
		}
		/*catch (Exception e)
		{
			System.err.println("Error reading characters at line " + curline +" (in character " + curchar + "): " + e.getMessage());
		}*/	
	}

	public void run()
	{
		
		Thread.currentThread().setName("fx80img-" + Thread.currentThread().getId());
		Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
		logger.debug("run");
		
		try
		{
			print(this.printText, this.printDir);
		} 
		catch (NumberFormatException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		logger.debug("exiting");
		
	}

}
