package ch.ethz.mobilecoach.services;

import org.bson.types.ObjectId;

import ch.ethz.mobilecoach.app.Post;

public interface MessagingService {

	public void sendMessage(String sender, ObjectId recipient, String message);
	
	public void sendMessage(String sender, ObjectId recipient, Post post, boolean pushOnly);
	
	public void indicateTyping(String sender, ObjectId recipient);
	
	public void startReceiving(); // this should be called after all the listeners have been set
	
	public interface MessageListener {
		public void receivePost(Post post);
	}
			
	public void setListener(ObjectId recipient, MessageListener listener);
	
	public void setChannelName(ObjectId recipient);
	
}