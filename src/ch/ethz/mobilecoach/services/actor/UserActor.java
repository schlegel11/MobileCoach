package ch.ethz.mobilecoach.services.actor;

import akka.actor.UntypedActor;
import akka.actor.Actor;
import ch.ethz.mobilecoach.chatlib.engine.ChatEngine;
import ch.ethz.mobilecoach.chatlib.engine.model.Message;

public class UserActor extends UntypedActor
{
	//private enum 
	
	private ChatEngine chatEngine;
	//private boolean queueing
	
	public UserActor(ChatEngine chatEngine){
		this.chatEngine = chatEngine;
		
		
		
	}
	
    public void onReceive(Object message)
    {
        if( message instanceof Message)
        {
            
        }
    }
}