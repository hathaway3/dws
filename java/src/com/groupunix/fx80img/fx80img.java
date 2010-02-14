package com.groupunix.fx80img;

// emulate an epson fx80.  VERY INCOMPLETE

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.imageio.ImageIO;

public class fx80img {

	public static final double DEF_XSIZE = 300 * 8.5;
	public static final double DEF_YSIZE = 300 * 11;
	
	private static double line_height = DEF_YSIZE / 66;
	private static double char_width = DEF_XSIZE / 132;
	
	private static characterset charset = new characterset();
	private static Graphics rGraphic;
	
	public static void print(String printText, File printFile) throws NumberFormatException, IOException 
	{
	
		// load settings?
		
		// load characters
		
		loadCharacter("default.chars");

		System.out.println("DEF_XSIZE: " + DEF_XSIZE);
		System.out.println("DEF_YSIZE: " + DEF_YSIZE);
		System.out.println("Line height:" + line_height);
		System.out.println("Character width: " + char_width);
		
		
		// init img
		
        BufferedImage rImage = new BufferedImage((int)DEF_XSIZE, (int)DEF_YSIZE, BufferedImage.TYPE_USHORT_GRAY );
        rGraphic = rImage.getGraphics();

       
        rGraphic.setColor(Color.WHITE);	
        rGraphic.fillRect(0, 0, (int) DEF_XSIZE, (int) DEF_YSIZE);
        
      

		
		// process file
		
        // testing
       
	    double xpos = 0;
	    double ypos = line_height;
	    
	    for (int i = 0;i<printText.length();i++)
	    {
	    	char c = printText.charAt(i);
	    		
	    	if ((int) c == 13)
	    	{
	    		xpos = 0;
//	    	}
//	    	else if ((int) c == 10) 
//	    	{
	    		ypos += line_height;
	    	}
	    	else if ((int) c == 9)
	    	{
	    		xpos += (8 * char_width);
	    	}
	    	else
	    	{
	    		drawCharacter(c,xpos,ypos);
	    		// drawCharacter(c,xpos+2,ypos,2);
	    		xpos += char_width;
	    	}
	    
	    	
	    }
       
	    
		// output img

        try {
       //     ImageIO.write(rImage, "JPEG", new File("fx80img.jpg"));
        	
        	
        	
            ImageIO.write(rImage, "PNG", printFile);	
        } 
        catch (IOException ex) 
        {
            System.out.println("Cannot save result image.");
        }

        
	}

	private static void drawCharacter(int ch, double xpos, double ypos) 
	{
		// draw one character.. just testing
		
		for (int i = 0; i<12;i++)
		{
			int lbits = charset.getCharacterCol(ch, i);
		
			drawCharCol(lbits,xpos, ypos);
			xpos += (char_width/12);
		}
		
	}

	private static void drawCharCol(int lbits, double xpos, double ypos) 
	{
		// draw one column
		
		for (int i = 0; i < 9; i++)
		{
			if ((lbits & (int) Math.pow(2,i)) == Math.pow(2,i))
			{
				
				
				int r = (int) (char_width / 5);
				int x = ((int) xpos) - (r/2);
				int y = ((int) ypos) - (r/2);
				
				int[] pdx = new int[4];
				int[] pdy = new int[4];
				
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
				
				rGraphic.setColor(Color.DARK_GRAY);
				rGraphic.fillPolygon(pdx, pdy, 4);
				
				rGraphic.setColor(Color.LIGHT_GRAY);
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

	private static void loadCharacter(String fname) throws NumberFormatException, IOException 
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

}
