package ch.ethz.mobilecoach.services;

import ch.ethz.mobilecoach.chatlib.engine.ConversationRepository;

public interface ConversationManagementService {
	
	public ConversationRepository getRepository(String interventionId);

}
