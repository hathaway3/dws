package com.groupunix.drivewireserver.virtualserial;

import java.io.PipedInputStream;

import org.relayirc.chatengine.Channel;
import org.relayirc.chatengine.Server;
import org.relayirc.chatengine.ServerAdapter;
import org.relayirc.chatengine.ServerEvent;

class DWUtilIRCServer extends ServerAdapter {

   private Server _server;
  
   @SuppressWarnings("unused")
private PipedInputStream input;
   private int vport;
   private DWUtilIRCChannel ircChannel;
   
   public DWUtilIRCServer(int vport, String nick) 
   {
      _server = new Server("irc.freenode.net",6667,"n/a","n/a");
      _server.addServerListener(this);
      _server.connect(nick,nick + "_",nick,nick);
      
      this.vport = vport;
   }
  
   public void onConnect(ServerEvent event) 
   {
      _server.sendJoin("#coco_chat");
   }
   
   public void onChannelJoin(ServerEvent event) 
   {
      Channel chan = (Channel)event.getChannel();
      if (chan.getName().equalsIgnoreCase("#coco_chat"))
      {
    	  ircChannel = new DWUtilIRCChannel(this.vport);
    	  chan.addChannelListener(ircChannel);
      }
   }
   
   public void stop() 
   {
	   if (ircChannel != null)
	   {
		   ircChannel.shutup();
	   }
      _server.disconnect();  
     
   }
   
   public void onDisconnect(ServerEvent event) 
   {
	   
   }
   
   public void sendMsg(String msg)
   {
	   _server.sendPrivateMessage("#coco_chat", msg);
   }
   
}
