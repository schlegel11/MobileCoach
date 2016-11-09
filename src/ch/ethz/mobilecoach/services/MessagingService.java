package ch.ethz.mobilecoach.services;

public interface MessagingService {

	public abstract void sendMessage(String sender, String recipient, String message);

}