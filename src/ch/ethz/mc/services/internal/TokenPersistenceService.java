package ch.ethz.mc.services.internal;

import java.util.Date;

import org.bson.types.ObjectId;

import ch.ethz.mc.model.persistent.AppToken;
import ch.ethz.mc.model.persistent.OneTimeToken;

public class TokenPersistenceService {
	
	private DatabaseManagerService dbService;

	public TokenPersistenceService(DatabaseManagerService dbService) {
		this.dbService = dbService;
	}
	
	public AppToken findAppTokenByToken(String token) {
		return dbService.findOneModelObject(AppToken.class, "{token:#}", token);
	}
	
	public AppToken createTokenForParticipant(ObjectId participantId) {
		// TODO guarantee uniqueness and consistent write (probably have to generate tokenId containing participantId to avoid clashes/wrong lookups)
		AppToken token = AppToken.create(participantId);
		dbService.saveModelObject(token);
		return token;
	}
	
	public String getOrCreateRecentOneTimeToken(ObjectId participantId) {
		Iterable<OneTimeToken> tokens = dbService.findModelObjects(OneTimeToken.class, "{participantId:#}", participantId);
		OneTimeToken newest = null;
		for (OneTimeToken token : tokens) {
			if (newest == null) {
				newest = token;
			} else if (newest.getCreatedAt().before(token.getCreatedAt())) {
				newest = token;
			}
		}
		if (newest == null || new Date().getTime()-newest.getCreatedAt().getTime()>24*3600*1000) {
			
			newest = createOneTimeTokenForParticipant(participantId);
		}
		return newest.getToken();
	}
	
	public OneTimeToken createOneTimeTokenForParticipant(ObjectId participantId) {
		OneTimeToken oneTimeToken = OneTimeToken.create(participantId);
		dbService.saveModelObject(oneTimeToken);
		return oneTimeToken;
	}
	
	public ObjectId consumeOneTimeToken(String token) {
		OneTimeToken oneTimeToken = dbService.findOneModelObject(OneTimeToken.class, "{token:#}", token);
		if (oneTimeToken == null) {
			return null;
		}
		ObjectId participantId = oneTimeToken.getParticipantId();
		dbService.deleteModelObject(oneTimeToken);
		return participantId;
	}

}
