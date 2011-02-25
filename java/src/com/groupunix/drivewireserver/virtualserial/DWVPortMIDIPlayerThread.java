package com.groupunix.drivewireserver.virtualserial;

import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Transmitter;

import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.dwexceptions.DWPortNotValidException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocolHandler;

public class DWVPortMIDIPlayerThread implements Runnable {

	private static final Logger logger = Logger.getLogger("DWServer.DWVPortMIDIPlayerThread");
		
	private int vport = -1;
	private	DWVSerialCircularBuffer inputBuffer = new DWVSerialCircularBuffer(1024, true);
	private static Sequencer	sm_sequencer = null;

	private DWProtocolHandler dwProto;
	
	public DWVPortMIDIPlayerThread(DWProtocolHandler dwProto, int vport, DWVSerialCircularBuffer inputBuffer) 
	{
		this.vport = vport;
		this.inputBuffer = inputBuffer;
		this.dwProto = dwProto;
	}

	public void run() 
	{
		
		Thread.currentThread().setName("midiplay-" + Thread.currentThread().getId());
		Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
			
		logger.debug("run");
	

		Sequence sequence = null;
		
		
		try
		{
			sequence = MidiSystem.getSequence(this.inputBuffer.getInputStream());
		}
		catch (InvalidMidiDataException e)
		{
			logger.warn(e.getMessage());
		} 
		catch (IOException e) 
		{
			logger.warn(e.getMessage());
		}
			
		
		try
		{
			sm_sequencer = MidiSystem.getSequencer(false);
	
		}
		catch (MidiUnavailableException e)
		{
			logger.warn(e.getMessage());
		}

		
		if (sm_sequencer == null)
		{
			logger.error("can't get a Sequencer");

		}


		try
		{
			sm_sequencer.open();
		}
		catch (MidiUnavailableException e)
		{
			logger.warn(e.getMessage());
		}
		
		try
		{
			sm_sequencer.setSequence(sequence);
		}
		catch (InvalidMidiDataException e)
		{
			logger.warn(e.getMessage());
		}


				
		try
		{

		
			Transmitter	seqTransmitter = sm_sequencer.getTransmitter();
			seqTransmitter.setReceiver(dwProto.getVPorts().getMidiReceiver());
		}
		catch (MidiUnavailableException e)
		{
			logger.warn(e.getMessage());
		}

		sm_sequencer.start();
				
		
		try 
		{
			dwProto.getVPorts().closePort(vport);
		} 
		catch (DWPortNotValidException e) 
		{
			logger.warn(e.getMessage());
		}
		
		logger.debug("exit");
	}

}
