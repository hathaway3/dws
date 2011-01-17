package com.groupunix.drivewireserver.virtualserial;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.DriveWireServer;
import com.groupunix.drivewireserver.dwexceptions.DWConnectionNotValidException;
import com.groupunix.drivewireserver.dwexceptions.DWPortNotOpenException;
import com.groupunix.drivewireserver.dwexceptions.DWPortNotValidException;

public class DWVSerialPorts {

	private static final Logger logger = Logger.getLogger("DWServer.DWVSerialPorts");
	
	// should move multiread toggle and max ports to config file
	public static final int MULTIREAD_LIMIT = 3;
	public static final int TERM_PORT = 0;
	public static final int MODE_TERM = 3;
	public static final int MAX_COCO_PORTS = 15;
	public static final int MAX_PORTS = MAX_COCO_PORTS;
	public static final int MIDI_PORT = 14;
	
	
	private int handlerno;
	private boolean bytelog = false;
	
	
	private DWVSerialPort[] vserialPorts = new DWVSerialPort[MAX_PORTS];
	private DWVPortListenerPool listenerpool = new DWVPortListenerPool();
	
	private int[] dataWait = new int[MAX_PORTS];
	
	// midi stuff
	private MidiDevice midiDevice;
	private Synthesizer midiSynth;
	private String soundbankfilename = null;
	private boolean midiVoicelock = false;
	private  HierarchicalConfiguration midiProfConf = null;
	private int[] GMInstrumentCache;
	
	
	public DWVSerialPorts(int handlerno)
	{
		this.handlerno = handlerno;
		bytelog = DriveWireServer.getHandler(this.handlerno).config.getBoolean("LogVPortBytes", false);
		
		
		if (DriveWireServer.getHandler(this.handlerno).config.getBoolean("UseMIDI", true))
		{
			// initialize MIDI device to internal synth
			logger.debug("initialize internal midi synth");
			
			clearGMInstrumentCache();
		
			try 
			{
				midiSynth = MidiSystem.getSynthesizer();
				setMIDIDevice(midiSynth);
			
				if (DriveWireServer.getHandler(this.handlerno).config.containsKey("MIDISynthDefaultSoundbank"))
				{
					loadSoundbank(DriveWireServer.getHandler(this.handlerno).config.getString("MIDISynthDefaultSoundbank"));
				}
			
			} 
			catch (MidiUnavailableException e) 
			{
				logger.warn("MIDI is not available");
			}
		
			if (DriveWireServer.getHandler(this.handlerno).config.containsKey("MIDISynthDefaultProfile"))
			{
				if (!setMidiProfile(DriveWireServer.getHandler(this.handlerno).config.getString("MIDISynthDefaultProfile")))
				{
					logger.warn("Invalid MIDI profile specified in config file.");
				}
			}
		
		}
		
	}









	public void openPort(int port)
	{
		if (vserialPorts[port] == null)
		{
			resetPort(port);
		}
		
 		vserialPorts[port].open();
	}


	public String prettyPort(int port) 
	{
		if (port == TERM_PORT)
		{
			return("Term");
		}
		else if (port == MIDI_PORT)
		{
			return("/MIDI");
		}
		else if (port < MAX_COCO_PORTS)
		{
			return("/N" + port);
		}
		else
		{
			return("NA:" + port);
		}
	}


	public void closePort(int port) throws DWPortNotValidException
	{
		if (port < vserialPorts.length)
		{
			if (vserialPorts[port] != null)
			{
				vserialPorts[port].close();	
				//vserialPorts[port] = null;
			}
		}
		else
		{
			throw new DWPortNotValidException("Valid port range is 0 - " + (vserialPorts.length - 1));
		}
	}
	

	public byte[] serRead() 
	{
		byte[] response = new byte[2];
		
		// redesigned to avoid bandwidth hogging
		
		// first look for termed ports
		for (int i = 0;i<MAX_COCO_PORTS;i++)
		{
			if (vserialPorts[i] != null)
			{
				if (vserialPorts[i].isTerm())
				{
					response[0] = (byte) 16;  // port status
					response[1] = (byte) i;   // 000 portnumber
					
					logger.debug("sending terminated status to coco for port " + i);
					
					vserialPorts[i] = new DWVSerialPort(this.handlerno, i);
					
					return(response);
				}
			}
		}
		
		
		
		// first data pass, increment data waiters
		
		for (int i = 0;i<MAX_COCO_PORTS;i++)
		{
			if (vserialPorts[i] != null)
			{
				if (vserialPorts[i].bytesWaiting() > 0)
				{
					// increment wait count
					dataWait[i]++;
				}
			}
		}
		
		// second pass, look for oldest waiting ports
		
		int oldest1 = 0;
		int oldest1port = -1;
		int oldestM = 0;
		int oldestMport = -1;
		
		for (int i = 0;i<MAX_COCO_PORTS;i++)
		{
			if (vserialPorts[i] != null)
			{
				if (vserialPorts[i].bytesWaiting() < MULTIREAD_LIMIT)
				{
					if (dataWait[i] > oldest1)
					{
						oldest1 = dataWait[i];
						oldest1port = i;
					}
				}
				else
				{
					if (dataWait[i] > oldestM)
					{
						oldestM = dataWait[i];
						oldestMport = i;
					}
				}
			}
		}
		
		if (oldest1port > -1)
		{
			// if we have a small byte waiter, send serread for it
			
			dataWait[oldest1port] = 0;
			response[0] = (byte) (oldest1port + 1);     // add one
			response[1] = vserialPorts[oldest1port].read1();  // send data byte
		}
		else if (oldestMport > -1)
		{
			// send serream for oldest bulk
			
			dataWait[oldestMport] = 0;
			response[0] = (byte) (oldestMport + 16 + 1);     // add one and 16 for serreadm
			response[1] = (byte) vserialPorts[oldestMport].bytesWaiting(); //send data size
			// logger.debug("SERREADM RESPONSE: " + Integer.toBinaryString(response[0]) + " " + Integer.toBinaryString(response[1]));

		}
		else
		{
			// no waiting ports
			
			response[0] = (byte) 0;
			response[1] = (byte) 0;
		}
		
		// logger.debug("SERREAD RESPONSE: " + Integer.toBinaryString(response[0]) + " " + Integer.toBinaryString(response[1]));
		
		return(response);
	}


	public void serWriteM(int port, String str) throws DWPortNotOpenException, DWPortNotValidException
	{
		for (int i = 0;i<str.length();i++)
		{
			serWrite(port, str.charAt(i));
		}
	}
	

	public void serWrite(int port, int databyte) throws DWPortNotOpenException, DWPortNotValidException 
	{
		if (bytelog)
		{
			
			logger.debug("write to port " + port + ": " + databyte + " (" + (char)databyte + ")" );
		}
		
		if ((port < MAX_COCO_PORTS) && (port >= 0))
		{
			if (vserialPorts[port] != null)
			{
				if (vserialPorts[port].isOpen())
				{
					// normal write
					vserialPorts[port].write(databyte);
				}
				else
				{
					throw new DWPortNotOpenException("Port " + port + " is not open");
				}
			}
			else
			{
				// should port not initialized be different than port not open?
				throw new DWPortNotOpenException("Port " + port + " is not open");
			}
		}
		else
		{
			throw new DWPortNotValidException(port + " is not a valid port number");
		}
		
	}



	public byte[] serReadM(int port, int len) throws DWPortNotOpenException, DWPortNotValidException 
	{
		

		if ((port < MAX_COCO_PORTS) && (port >= 0))
		{
			if (vserialPorts[port].isOpen())
			{
				byte[] data = new byte[len];
				data = vserialPorts[port].readM(len);
				return(data);
			}
			else
			{
				throw new DWPortNotOpenException("Port " + port + " is not open");
			}
		}
		else
		{
			throw new DWPortNotValidException(port + " is not a valid port number");
		}
		
	}
	
	
	public OutputStream getPortInput(int vport) 
	{
		return (vserialPorts[vport].getPortInput());
	}

	public InputStream getPortOutput(int vport) 
	{
		return (vserialPorts[vport].getPortOutput());
	}
	
	public void setPortOutput(int vport, OutputStream output)
	{
		if (isNull(vport))
		{
			logger.debug("attempt to set output on null port " + vport);
		}
		else
		{
			vserialPorts[vport].setPortOutput(output);
		}
	}


	public void markConnected(int vport) 
	{
		if (vserialPorts[vport] == null)
		{
			logger.warn("mark connected on null port " + vport);
		}
		else
		{
			vserialPorts[vport].setConnected(true);
		}
	}


	public void markDisconnected(int vport) 
	{
		vserialPorts[vport].setConnected(false);
	}


	public boolean isConnected(int port)
	{
		if (vserialPorts[port] != null)
		{
			return(vserialPorts[port].isConnected());
		}
		return(false);
	}

	

	public void setUtilMode(int port, int mode)
	{
		vserialPorts[port].setUtilMode(mode);
	}
	
		
	
	
	public void write1(int port, byte data)
	{

		try 
		{
			getPortInput(port).write(data);
		} 
		catch (IOException e) 
		{
			logger.error("IO error writing to port " + prettyPort(port));
		}
	}

	public void write(int port, String str)
	{
		
			vserialPorts[port].writeM(str);
		
	}

	
	
	public void setPD_INT(int port, byte pD_INT) 
	{
		vserialPorts[port].setPD_INT(pD_INT);
	}



	public byte getPD_INT(int port) 
	{
		return(vserialPorts[port].getPD_INT());
	}



	public void setPD_QUT(int port, byte pD_QUT) 
	{
		vserialPorts[port].setPD_QUT(pD_QUT);
	}



	public byte getPD_QUT(int port) 
	{
		return(vserialPorts[port].getPD_QUT());
	}






	public void sendUtilityFailResponse(int vport, int code, String txt) 
	{
		logger.debug("API FAIL: port " + vport + " code " + code + ": " + txt);
		vserialPorts[vport].sendUtilityFailResponse(code, txt);
	}


	public void sendUtilityOKResponse(int vport, String txt) 
	{
		logger.debug("API OK: port " + vport + ": command successful");
		vserialPorts[vport].sendUtilityOKResponse("command successful");
		vserialPorts[vport].writeToCoco(txt);
	}

	
	public void sendUtilityOKResponse(int vport, byte[] responseBytes) 
	{
		logger.debug("API OK: port " + vport + ": command successful (byte mode)");
		vserialPorts[vport].sendUtilityOKResponse("command successful");
		vserialPorts[vport].writeToCoco(responseBytes);
		
	}

	public int bytesWaiting(int vport) throws DWPortNotValidException 
	{
		validateport(vport);
		return(vserialPorts[vport].bytesWaiting());
	}

	

	public void setDD(byte vport, byte[] devdescr) throws DWPortNotValidException
	{
		validateport(vport);
		vserialPorts[vport].setDD(devdescr);
	}


	public void resetAllPorts() 
	{
		logger.debug("Resetting all virtual serial ports - part 1, close all sockets");
		
		
		for (int i = 0;i<MAX_COCO_PORTS;i++)
		{
			this.listenerpool.closePortConnectionSockets(i);
			this.listenerpool.closePortServerSockets(i);
		}
		
		logger.debug("Resetting all virtual serial ports - part 2, init all ports");
		
		//vserialPorts = new DWVSerialPort[MAX_PORTS];
		for (int i = 0;i<MAX_COCO_PORTS;i++)
		{
			// dont reset term
			if (i != TERM_PORT)
				resetPort(i);
		}
		
		// if term is null, init
		if (this.vserialPorts[TERM_PORT] == null)
			resetPort(TERM_PORT);
		
	}

	public void resetPort(int i)
	{
		vserialPorts[i] = new DWVSerialPort(this.handlerno, i);
	}
	
	public boolean isOpen(int vport) 
	{
		if (vserialPorts[vport] != null)
			return(vserialPorts[vport].isOpen());
		
		return(false);
	}


	public int getOpen(int i) throws DWPortNotValidException 
	{
		validateport(i);
		return(vserialPorts[i].getOpen());
	}


	public byte[] getDD(int i) throws DWPortNotValidException
	{
		validateport(i);
		return(vserialPorts[i].getDD());

	}


	
	//public static void setSocket(int vport, Socket skt) 
	//{
	//	vserialPorts[vport].setSocket(skt);
	//}


	public void writeToCoco(int vport, byte databyte) throws DWPortNotValidException 
	{
		validateport(vport);
		vserialPorts[vport].writeToCoco(databyte);
	}
	
	public void writeToCoco(int vport, String str) throws DWPortNotValidException 
	{
		validateport(vport);
		vserialPorts[vport].writeToCoco(str);
	}



	public boolean hasOutput(int vport)
	{
		if (vserialPorts[vport] != null)
		{
			return(vserialPorts[vport].hasOutput());
		}

		return false;
	}
	
	public boolean isNull(int vport)
	{
		if (vserialPorts[vport] == null)
			return(true);
		
		return(false);
	}


	public boolean isValid(int vport)
	{
	  if ((vport >= 0) && (vport < MAX_PORTS))
		  return(true);
	  
	  return(false);
		
	}
	
	private void validateport(int vport) throws DWPortNotValidException 
	{
		if (!isValid(vport) || isNull(vport) )
		{
			throw(new DWPortNotValidException("Invalid port #" + vport));
		}
	}	


	public void sendConnectionAnnouncement(int vport, int conno, int localport, String hostaddr) throws DWPortNotValidException
	{
		validateport(vport);
		vserialPorts[vport].sendConnectionAnnouncement(conno, localport, hostaddr);
	}


	public void setConn(int vport, int conno) throws DWPortNotValidException
	{
		validateport(vport);
		vserialPorts[vport].setConn(conno);
		
	}
	





	public int getConn(int vport) throws DWPortNotValidException
	{
		validateport(vport);
		return(vserialPorts[vport].getConn());
	}


	public String getHostIP(int vport) throws DWPortNotValidException, DWConnectionNotValidException
	{
		validateport(vport);
		return(this.listenerpool.getConn( vserialPorts[vport].getConn() ).getInetAddress().getHostAddress());
		
	}


	public int getHostPort(int vport) throws DWPortNotValidException, DWConnectionNotValidException
	{
		validateport(vport);
		return(this.listenerpool.getConn(vserialPorts[vport].getConn()).getPort());
	}


	public void shutdown()
	{
		logger.debug("shutting down");
		
		for (int i = 0;i<MAX_PORTS;i++)
		{
			this.listenerpool.closePortConnectionSockets(i);
			this.listenerpool.closePortServerSockets(i);
			if (this.vserialPorts[i] != null)
			{
				this.vserialPorts[i].shutdown();
			}
		}
	}


	
	
	
	public MidiDevice.Info getMidiDeviceInfo()
	{
		if (this.midiDevice != null)
			return(this.midiDevice.getDeviceInfo());
		
		return(null);
	}
	
	
	public void setMIDIDevice(MidiDevice device) 
	{
		if (this.midiDevice != null)
		{
			if (this.midiDevice.isOpen())
			{
				logger.info("midi: closing " + this.midiDevice.getDeviceInfo().getName());
				this.midiDevice.close();
			}
		}
		
		this.midiDevice = device;
		try 
		{
			this.midiDevice.open();
			logger.info("midi: opened " + this.midiDevice.getDeviceInfo().getName());
		} 
		catch (MidiUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}


	public void sendMIDIMsg(ShortMessage mmsg, int timestamp) 
	{
		try 
		{
			this.midiDevice.getReceiver().send(mmsg, timestamp);
		} 
		catch (MidiUnavailableException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IllegalStateException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}


	public Receiver getMidiReceiver() throws MidiUnavailableException 
	{
		return(this.midiDevice.getReceiver());
	}
	

	public Synthesizer getMidiSynth() 
	{
		return(midiSynth);
	}
	
	
	public boolean isSoundbankSupported(Soundbank soundbank) 
	{
		return(midiSynth.isSoundbankSupported(soundbank));
	}
		
		
	public boolean setMidiSoundbank(Soundbank soundbank, String fname) 
	{
		
		if (midiSynth.loadAllInstruments(soundbank))
		{
			logger.debug("loaded soundbank file '" + fname + "'");
			this.soundbankfilename = fname;
			return(true);
		}
		
		return(false);
	}
	
	public String getMidiSoundbankFilename()
	{
		return(this.soundbankfilename);
	}
	
	public boolean getMidiVoicelock()
	{
		return(this.midiVoicelock);
	}

	public void setMidiVoicelock(boolean lock)
	{
		this.midiVoicelock = lock;
		logger.debug("MIDI: synth voicelock = " + lock);
	}

	private void loadSoundbank(String filename) 
	{
		Soundbank soundbank = null;
		
		File file = new File(filename);
		try 
		{
			soundbank = MidiSystem.getSoundbank(file);
		} 
		catch (InvalidMidiDataException e) 
		{
			logger.warn("Error loading soundbank: " + e.getMessage());
			return;
		} 
		catch (IOException e) 
		{
			logger.warn("Error loading soundbank: " + e.getMessage());
			return;
		}
		
		if (isSoundbankSupported(soundbank))
		{				
			if (!setMidiSoundbank(soundbank, filename))
			{
				logger.warn("Failed to set soundbank '" + filename + "'");
				return;
			}
			
		}
		else
		{
			logger.warn("Unsupported soundbank '" + filename + "'");	
			return;
		}	
	}




	public String getMidiProfileName() 
	{
		return(this.midiProfConf.getString("name","none"));
	}

	public HierarchicalConfiguration getMidiProfile() 
	{
		return(this.midiProfConf);
	}

	
	
	@SuppressWarnings("unchecked")
	public boolean setMidiProfile(String profile)
	{
		
		List<HierarchicalConfiguration> profiles = DriveWireServer.serverconfig.configurationsAt("midisynthprofile");
    	
		for(Iterator<HierarchicalConfiguration> it = profiles.iterator(); it.hasNext();)
		{
		    HierarchicalConfiguration mprof = it.next();
		    
		    if (mprof.getString("name").equalsIgnoreCase(profile))
		    {
		    	
		    	this.midiProfConf = (HierarchicalConfiguration) mprof.clone();
		    	doMidiTranslateCurrentVoices();
		    	
		    	logger.debug("MIDI: set profile to '" + profile + "'");
		    	return(true);
		    }
		    
		}
		
		return(false);
	}
	
	
	
	private void doMidiTranslateCurrentVoices() 
	{
		// translate current GM voices to current profile
		
		MidiChannel[] chans = this.midiSynth.getChannels();
		
		for (int i = 0;i < chans.length;i++)
		{
			if (chans[i] != null)
			{
				chans[i].programChange(getGMInstrument(this.GMInstrumentCache[i]));
			}
				
		}
	}




	@SuppressWarnings("unchecked")
	public int getGMInstrument(int voice)
	{
		if (this.midiProfConf == null)
		{
			return(voice);
		}
		
		int xvoice = voice;
		
		List<HierarchicalConfiguration> mappings = this.midiProfConf.configurationsAt("mapping");
		
		for(Iterator<HierarchicalConfiguration> it = mappings.iterator(); it.hasNext();)
		{
			HierarchicalConfiguration sub = it.next();
			
			if (sub.getInt("[@dev]") == voice)
			{
				xvoice = sub.getInt("[@gm]");
				logger.debug("MIDI: profile '" + this.midiProfConf.getString("name") + "' translates device inst " + voice + " to GM instr " + xvoice);
				return(xvoice);
			}
			
		}
		
		// no translation match
		return(voice);
	}


	public boolean setMIDIInstr(int channel, int instr) 
	{
		MidiChannel[] chans = this.midiSynth.getChannels();
		
		if (channel < chans.length)
		{
			if (chans[channel] != null)
			{
				chans[channel].programChange(instr);
				logger.debug("MIDI: set instrument " + instr + " on channel " + channel);
				return(true);
			}
		}
		
		return(false);
		
	}

	
	public void clearGMInstrumentCache() 
	{
		this.GMInstrumentCache = new int[16];
		
		for (int i = 0;i<16;i++)
		{
			this.GMInstrumentCache[i] = 0;
		}
	}

	
	public void setGMInstrumentCache(int chan,int instr)
	{
		if ((chan >= 0) && (chan < this.GMInstrumentCache.length))
		{
			this.GMInstrumentCache[chan] = instr;
		}
		else
		{
			logger.debug("MIDI: channel out of range on program change: " + chan);
		}
	}

	public int getGMInstrumentCache(int chan)
	{
		return(this.GMInstrumentCache[chan]);
	}







	public DWVPortListenerPool getListenerPool() 
	{
		return(this.listenerpool);
	}









	public int getUtilMode(int i) throws DWPortNotValidException 
	{
		validateport(i);
		return(this.vserialPorts[i].getUtilMode());
	}










}
