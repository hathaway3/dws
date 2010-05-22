package com.groupunix.drivewireserver.virtualserial;

import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Transmitter;

import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.DriveWireServer;

public class DWVPortMIDIPlayerThread implements Runnable {

	private static final Logger logger = Logger.getLogger("DWServer.DWVPortMIDIPlayerThread");
		
	private int vport = -1;
	private int handlerno;
	private	DWVSerialCircularBuffer inputBuffer = new DWVSerialCircularBuffer(1024, true);
	private static Sequencer	sm_sequencer = null;

	
	
	public DWVPortMIDIPlayerThread(int handlerno, int vport, DWVSerialCircularBuffer inputBuffer) 
	{
		this.vport = vport;
		this.handlerno = handlerno;
		this.inputBuffer = inputBuffer;
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
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
		
		try
		{
			sm_sequencer = MidiSystem.getSequencer(false);
	
		}
		catch (MidiUnavailableException e)
		{
			e.printStackTrace();
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
			e.printStackTrace();
		}
		
		try
		{
			sm_sequencer.setSequence(sequence);
		}
		catch (InvalidMidiDataException e)
		{
			e.printStackTrace();
		}


				
		try
		{

		
			Transmitter	seqTransmitter = sm_sequencer.getTransmitter();
			seqTransmitter.setReceiver(DriveWireServer.getHandler(handlerno).getVPorts().getMidiReceiver());
		}
		catch (MidiUnavailableException e)
		{
			e.printStackTrace();
		}

		sm_sequencer.start();
				
		
		DriveWireServer.getHandler(handlerno).getVPorts().closePort(vport);
		logger.debug("exit");
	}

}
