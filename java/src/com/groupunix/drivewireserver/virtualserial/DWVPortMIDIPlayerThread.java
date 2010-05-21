package com.groupunix.drivewireserver.virtualserial;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Transmitter;

import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.DriveWireServer;

public class DWVPortMIDIPlayerThread implements Runnable {

	private static final Logger logger = Logger.getLogger("DWServer.DWVPortMIDIPlayerThread");
		
	private int vport = -1;
	private int handlerno;
	private	DWVSerialCircularBuffer inputBuffer = new DWVSerialCircularBuffer(1024, true);
	private static Sequencer	sm_sequencer = null;
	private static Synthesizer	sm_synthesizer = null;

	
	
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
		
		int dbyte;
		
		FileWriter fstream;
		  
		
	/*	try {
			fstream = new FileWriter("test.mid");
			BufferedWriter out = new BufferedWriter(fstream);
		  
			dbyte = this.inputBuffer.getInputStream().read();
			
			while (dbyte > -1)
			{
				logger.debug("midibyte: " + dbyte + " avail: " + this.inputBuffer.getInputStream().available());
				out.write(dbyte);
				dbyte = this.inputBuffer.getInputStream().read();
			}
			
			out.close();
			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
			
		logger.debug("close");
		
		*/
		
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
			
		logger.debug("run1");
		
		try
		{
			sm_sequencer = MidiSystem.getSequencer();
		}
		catch (MidiUnavailableException e)
		{
			e.printStackTrace();
		}

		logger.debug("run2");
		
		if (sm_sequencer == null)
		{
			logger.error("can't get a Sequencer");

		}


				/*
				 *	The Sequencer is still a dead object.
				 *	We have to open() it to become live.
				 *	This is necessary to allocate some ressources in
				 *	the native part.
				 */
		try
		{
			sm_sequencer.open();
		}
		catch (MidiUnavailableException e)
		{
			e.printStackTrace();
		}

		logger.debug("run3");
		
		try
		{
			sm_sequencer.setSequence(sequence);
		}
		catch (InvalidMidiDataException e)
		{
			e.printStackTrace();
		}

				/*
				 *	Now, we set up the destinations the Sequence should be
				 *	played on. Here, we try to use the default
				 *	synthesizer. With some Java Sound implementations
				 *	(Sun jdk1.3/1.4 and others derived from this codebase),
				 *	the default sequencer and the default synthesizer
				 *	are combined in one object. We test for this
				 *	condition, and if it's true, nothing more has to
				 *	be done. With other implementations (namely Tritonus),
				 *	sequencers and synthesizers are always seperate
				 *	objects. In this case, we have to set up a link
				 *	between the two objects manually.
				 *
				 *	By the way, you should never rely on sequencers
				 *	being synthesizers, too; this is a highly non-
				 *	portable programming style. You should be able to
				 *	rely on the other case working. Alas, it is only
				 *	partly true for the Sun jdk1.3/1.4.
				 */
				
		if (! (sm_sequencer instanceof Synthesizer))
		{
					/*
					 *	We try to get the default synthesizer, open()
					 *	it and chain it to the sequencer with a
					 *	Transmitter-Receiver pair.
					 */
			try
			{
				sm_synthesizer = MidiSystem.getSynthesizer();
				sm_synthesizer.open();
				Receiver	synthReceiver = sm_synthesizer.getReceiver();
				Transmitter	seqTransmitter = sm_sequencer.getTransmitter();
				seqTransmitter.setReceiver(synthReceiver);
			}
			catch (MidiUnavailableException e)
			{
				e.printStackTrace();
			}
		}

				/*
				 *	Now, we can start over.
				 */
		sm_sequencer.start();
				
		
		DriveWireServer.getHandler(handlerno).getVPorts().closePort(vport);
		logger.debug("exit");
	}

}
