package ch.ethz.mobilecoach.services;

public interface MessagingService {

	public abstract void sendMessage(String sender, String recipient, String message);
	
	
	public interface MessageListener {
		public void receiveMessage(String message);  // TODO (DR): receive not just the message string
	}
			
	public void setListener(String recipient, MessageListener listener);
	
}