package ch.ethz.mc.services.internal;

import org.bson.types.ObjectId;

import ch.ethz.mc.model.persistent.AppToken;

public class OneTimeTokenPersistenceService {
	
	private DatabaseManagerService dbService;

	public OneTimeTokenPersistenceService(DatabaseManagerService dbService) {
		this.dbService = dbService;
	}
	
	public AppToken findAppTokenByToken(String token) {
		return dbService.findOneModelObject(AppToken.class, "{token:#}", token);
	}
	
	public AppToken createTokenForParticipant(ObjectId participantId) {
		// TODO guarantee uniqueness and consostent write (probably have to generate tokenId containing participantId to avoid clashes/wrong lookups)
		AppToken token = AppToken.create(participantId);
		dbService.saveModelObject(token);
		return token;
	}

}
