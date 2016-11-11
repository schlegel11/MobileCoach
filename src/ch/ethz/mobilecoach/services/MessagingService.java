package ch.ethz.mobilecoach.services;

import ch.ethz.mobilecoach.app.Post;

public interface MessagingService {

	public void sendMessage(String sender, String recipient, String message);
	
	public void sendMessage(String sender, String recipient, Post post);
	
	
	public interface MessageListener {
		public void receiveMessage(String message);  // TODO (DR): receive not just the message string
	}
			
	public void setListener(String recipient, MessageListener listener);
	
}