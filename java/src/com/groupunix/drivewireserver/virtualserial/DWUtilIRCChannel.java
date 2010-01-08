package com.groupunix.drivewireserver.virtualserial;

import org.apache.log4j.Logger;
import org.relayirc.chatengine.ChannelEvent;
import org.relayirc.chatengine.ChannelListener;

public class DWUtilIRCChannel implements ChannelListener {

	private int vport;
	private boolean shutup;
	
	private static final Logger logger = Logger.getLogger("DWServer.DWUtilIRCChannel");
		
	
	public DWUtilIRCChannel(int vport) 
	{
		this.vport = vport;
		this.shutup = false;
	}

	public void onAction(ChannelEvent arg0) 
	{
		DWVSerialPorts.write(this.vport, "\r\n" + arg0.getOriginNick() + " " + arg0.getValue() + "\r\n");
	}

	public void onActivation(ChannelEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void onBan(ChannelEvent arg0) 
	{
		DWVSerialPorts.write(this.vport, "\r\n" + arg0.getOriginNick() + " has banned " + arg0.getSubjectNick() + ".\r\n");
		
	}

	public void onConnect(ChannelEvent arg0) 
	{
		logger.debug("channel connect");
		if (shutup == false)
		{
			DWVSerialPorts.write(this.vport, "\r\nConnected to #coco_chat on irc.freenode.net\r\n\nType /QUIT to exit.  Anything else you type will be sent to the channel as a message.\r\n\n");
		}
	}

	public void onDeOp(ChannelEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void onDisconnect(ChannelEvent arg0) 
	{
		// DWVSerialPorts.write(this.vport, "\r\nDisconnected from channel.\r\n");
		
	}

	public void onJoin(ChannelEvent arg0) 
	{
		DWVSerialPorts.write(this.vport, "\r\n" + arg0.getOriginNick() + " has joined the channel.\r\n");
	}

	public void onJoins(ChannelEvent arg0) 
	{
		DWVSerialPorts.write(this.vport, "\r\nUsers in this channel: " + arg0.getValue() + "\r\n");
		
	}

	public void onKick(ChannelEvent arg0) 
	{
		DWVSerialPorts.write(this.vport, "\r\n" + arg0.getOriginNick() + " has kicked " + arg0.getSubjectNick() +" from the channel.\r\n");
		
	}

	public void onMessage(ChannelEvent arg0) 
	{
		DWVSerialPorts.write(this.vport, "\r\n" + arg0.getOriginNick() + ": " + arg0.getValue() + "\r\n");
		
	}

	public void onNick(ChannelEvent arg0) 
	{
		DWVSerialPorts.write(this.vport, "\r\n" + arg0.getOriginNick() + " is now known as " + arg0.getValue() + ".\r\n");
		
	}

	public void onOp(ChannelEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void onPart(ChannelEvent arg0) 
	{
		DWVSerialPorts.write(this.vport, "\r\n" + arg0.getOriginNick() + " has left the channel.\r\n");
		
	}

	public void onQuit(ChannelEvent arg0) 
	{
		DWVSerialPorts.write(this.vport, "\r\n" + arg0.getOriginNick() + " has quit.\r\n");
		
	}


	public void onTopicChange(ChannelEvent arg0) 
	{
		DWVSerialPorts.write(this.vport, "\r\n" + arg0.getOriginNick() + " has set a new topic: " + arg0.getValue()+ "\r\n");
		
	}

	public void shutup() 
	{
		this.shutup = true;
	}

}
