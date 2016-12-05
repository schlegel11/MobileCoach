package ch.ethz.mobilecoach.services;

import org.bson.types.ObjectId;

import ch.ethz.mobilecoach.app.Post;

public interface MessagingService {

	public void sendMessage(String sender, ObjectId recipient, String message);
	
	public void sendMessage(String sender, ObjectId recipient, Post post);
	
	
	public interface MessageListener {
		public void receivePost(Post post);
	}
			
	public void setListener(ObjectId recipient, MessageListener listener);
	
}