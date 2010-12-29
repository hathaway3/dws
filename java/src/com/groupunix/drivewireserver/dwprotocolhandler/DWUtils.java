package com.groupunix.drivewireserver.dwprotocolhandler;

import gnu.io.CommPortIdentifier;

import java.util.ArrayList;

import com.groupunix.drivewireserver.DWDefs;
import com.groupunix.drivewireserver.virtualserial.DWVSerialPorts;

public class DWUtils
{

	
	public static int int4(byte[] data) 
	{
		 return( (data[0] & 0xFF) << 32) + ((data[1] & 0xFF) << 16) + ((data[2] & 0xFF) << 8) + (data[3] & 0xFF);
	}
	
	public static int int3(byte[] data) 
	{
		 return((data[0] & 0xFF) << 16) + ((data[1] & 0xFF) << 8) + (data[2] & 0xFF);
	}

	
	public static int int2(byte[] data) 
	{
		 return((data[0] & 0xFF) << 8) + (data[1] & 0xFF);
	}
	
	
	
	public static String byteArrayToHexString(byte in[]) {

	    byte ch = 0x00;

	    int i = 0; 

	    if (in == null || in.length <= 0)

	        return null;

	        

	    String pseudo[] = {"0", "1", "2",
	"3", "4", "5", "6", "7", "8",
	"9", "A", "B", "C", "D", "E",
	"F"};

	    StringBuffer out = new StringBuffer(in.length * 2);

	    

	    while (i < in.length) {

	        ch = (byte) (in[i] & 0xF0);

	        ch = (byte) (ch >>> 4);
	     // shift the bits down

	        ch = (byte) (ch & 0x0F);    
	// must do this is high order bit is on!

	        out.append(pseudo[ (int) ch]); // convert the

	        ch = (byte) (in[i] & 0x0F); // Strip off

	        out.append(pseudo[ (int) ch]); // convert the

	        i++;

	    }

	    String rslt = new String(out);

	    return rslt;

	}    

	
	
	
	public static String prettySS(byte statcode)
	{
		String result = "unknown";
		
		switch (statcode)
		{
			case 0x00:
				result = "SS.Opt";
				break;

			case 0x02:
				result = "SS.Size";
				break;

			case 0x03:
				result = "SS.Reset";
				break;

			case 0x04:
				result = "SS.WTrk";
				break;

			case 0x05:
				result = "SS.Pos";
				break;

			case 0x06:
				result = "SS.EOF";
				break;

			case 0x0A:
				result = "SS.Frz";
				break;

			case 0x0B:
				result = "SS.SPT";
				break;

			case 0x0C:
				result = "SS.SQD";
				break;

			case 0x0D:
				result = "SS.DCmd";
				break;

			case 0x0E:
				result = "SS.DevNm";
				break;

			case 0x0F:
				result = "SS.FD";
				break;

			case 0x10:
				result = "SS.Ticks";
				break;

			case 0x11:
				result = "SS.Lock";
				break;

			case 0x12:
				result = "SS.VarSect";
				break;

			case 0x14:
				result = "SS.BlkRd";
				break;

			case 0x15:
				result = "SS.BlkWr";
				break;

			case 0x16:
				result = "SS.Reten";
				break;

			case 0x17:
				result = "SS.WFM";
				break;

			case 0x18: 
				result = "SS.RFM";
				break;
			
			case 0x1A:
				result = "SS.SSig";
				break;
				
			case 0x1B:
				result = "SS.Relea";
				break;

			case 0x1C:
				result = "SS.Attr";
				break;

			case 0x1E:
				result = "SS.RsBit";
				break;

			case 0x20:
				result = "SS.FDInf";
				break;

			case 0x26:
				result = "SS.DSize";
				break;

			case 0x27:
				result = "SS.KySns";
				break;

			// added for SCF/Ns	
			case 0x28:
				result = "SS.ComSt";
				break;
				
			case 0x29:
				result = "SS.Open";
				break;	
				
			case 0x2A:
				result = "SS.Close";
				break;
				
			case 0x30:
				result = "SS.HngUp";
				break;
				
			case (byte) 255:
				result =  "None";
				break;
			
			default:
				result = "Unknown: " + statcode;
		}
		
		return(result);
	}

	
	
	
	public static String prettyOP(byte opcode) 
	{
		String res = "Unknown";

		if ((opcode >= DWDefs.OP_FASTWRITE_BASE) && (opcode <= (DWDefs.OP_FASTWRITE_BASE + DWVSerialPorts.MAX_COCO_PORTS - 1)))
		{
			res = "OP_FASTWRITE_" + (opcode - DWDefs.OP_FASTWRITE_BASE);
		}
		else
		{
		switch(opcode)
		{
		case DWDefs.OP_NOP:
			res = "OP_NOP";
			break;

		case DWDefs.OP_INIT:
			res = "OP_INIT";
			break;

		case DWDefs.OP_READ:
			res = "OP_READ";
			break;

		case DWDefs.OP_READEX:
			res = "OP_READEX";
			break;

		case DWDefs.OP_WRITE:
			res = "OP_WRITE";
			break;

		case DWDefs.OP_REREAD:
			res = "OP_REREAD";
			break;

		case DWDefs.OP_REREADEX:
			res = "OP_REREADEX";
			break;

		case DWDefs.OP_REWRITE:
			res = "OP_REWRITE";
			break;

		case DWDefs.OP_TERM:
			res = "OP_TERM";
			break;

		case DWDefs.OP_RESET1:
		case DWDefs.OP_RESET2:
		case DWDefs.OP_RESET3:
			res =  "OP_RESET";
			break;

		case DWDefs.OP_GETSTAT:
			res = "OP_GETSTAT";
			break;

		case DWDefs.OP_SETSTAT:
			res = "OP_SETSTAT";
			break;

		case DWDefs.OP_TIME:
			res = "OP_TIME";
			break;

		case DWDefs.OP_PRINT:
			res = "OP_PRINT";
			break;

		case DWDefs.OP_PRINTFLUSH:
			res = "OP_PRINTFLUSH";
			break;

		case DWDefs.OP_SERREADM:
			res = "OP_SERREADM";
			break;

		case DWDefs.OP_SERREAD:
			res = "OP_SERREAD";      
			break;

		case DWDefs.OP_SERWRITE:
			res = "OP_SERWRITE";
			break;

		case DWDefs.OP_SERSETSTAT:
			res = "OP_SERSETSTAT";
            break;
            
		case DWDefs.OP_SERGETSTAT:
			res =  "OP_SERGETSTAT";
            break;
		
		case DWDefs.OP_SERINIT:
			res =  "OP_SERINIT";
            break;
            
		case DWDefs.OP_SERTERM:
			res =  "OP_SERTERM";
            break;
            
		case DWDefs.OP_DWINIT:
			res =  "OP_DWINIT";
            break;
            
		default:
				res = "Unknown: " + opcode;
		}
		}
		
		return(res);
	}
	
	
	
	@SuppressWarnings("unchecked")
	public static ArrayList<String> getPortNames()
	{
		ArrayList<String> ports = new ArrayList();
		
		java.util.Enumeration<CommPortIdentifier> portEnum = CommPortIdentifier.getPortIdentifiers();
		while ( portEnum.hasMoreElements() ) 
		{
			CommPortIdentifier portIdentifier = portEnum.nextElement();
		    if (portIdentifier.getPortType() == 1)
		    {
		    	ports.add(portIdentifier.getName());
		    }
		        
		}        
				
		return(ports);
	}

	public static String midimsgToText(int statusbyte, int data1, int data2) 
	{
		// make midi messages into something humans can read.. 
		String action = new String();
		String chan = new String();
		String d1 = new String();
		String d2 = new String();
		
		if ((statusbyte >= 128) && (statusbyte <= 143))
		{
			action = "Note off";
			chan = "Chan: " + (statusbyte - 128);
			d1 = "Pitch: " + prettyMidiPitch(data1);
			d2 = "Vel: " + data2;
		}
		else if ((statusbyte >= 144) && (statusbyte <= 159))
		{
			action = "Note on";
			chan = "Chan: " + (statusbyte - 144);
			d1 = "Pitch: " + prettyMidiPitch(data1);
			d2 = "Vel: " + data2;
		}
		else if ((statusbyte >= 160) && (statusbyte <= 175))
		{
			action = "Key press";
			chan = "Chan: " + (statusbyte - 160);
			d1 = "Key: " + data1;
			d2 = "Pressure: " + data2;
		}
		else if ((statusbyte >= 176) && (statusbyte <= 191))
		{
			action = "Ctr change";
			chan = "Chan: " + (statusbyte - 176);
			d1 = "Controller: " + data1;
			d2 = "Value: " + data2;
		}
		else if ((statusbyte >= 192) && (statusbyte <= 207))
		{
			action = "Prg change";
			chan = "Chan: " + (statusbyte - 192);
			d1 = "Preset: " + data1;
		}
		else if ((statusbyte >= 208) && (statusbyte <= 223))
		{
			action = "Chan press";
			chan = "Chan: " + (statusbyte - 208);
			d1 = "Pressure: " + data1;
		}
		else if ((statusbyte >= 224) && (statusbyte <= 239))
		{
			action = "Pitch bend";
			chan = "Chan: " + (statusbyte - 224);
			d1 = "LSB: " + data1;
			d2 = "MSB: " + data2;
		}
		else if (statusbyte == 248)
		{
			action = "Timing tick";
		}
		else 
		{
			action = "Unknown: " + statusbyte;
			d1 = "Data1: " + data1;
			d2 = "Data2: " + data2;
		}
		
		
		
		return(String.format("%-10s %-10s %-20s %-20s", action, chan, d1, d2));
	}
	
	private static String prettyMidiPitch(int pitch) 
	{
		String[] notes = new String[] {"C","C#","D","D#","E","F","F#","G","G#","A","A#","B"};
		
		return(String.format("%-3d %-2s %d",pitch,notes[pitch % 12],(pitch / 12) - 1));
	}
	
	
	public static String dropFirstToken(String txt)
	{
		// drop first token in string

		String rest = new String();
		
		String[] tokens = txt.split(" ");
		
		for (int x = 1; x < tokens.length; x++)
		{
			if (rest.length() > 0)
			{
				rest = rest + " " + tokens[x];
			}
			else
			{
				rest = tokens[x];
			}
			
		}
		
		return(rest);
	}
	
	
	public static String convertStarToBang(String txt)
	{
	
		txt = txt.replaceAll("\\*", "!");
		
		return txt;
	}

	public static boolean isStringFalse(String tf) 
	{
		if (tf.equalsIgnoreCase("false"))
			return true;
		
		if (tf.equalsIgnoreCase("off"))
			return true;
		
		return false;
	}
	
	public static boolean isStringTrue(String tf) 
	{
		if (tf.equalsIgnoreCase("true"))
			return true;
		
		if (tf.equalsIgnoreCase("on"))
			return true;
		
		return false;
	}
	
}
