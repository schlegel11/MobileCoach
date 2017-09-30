package ch.ethz.mobilecoach.services;

import org.bson.types.ObjectId;

import ch.ethz.mc.services.internal.DatabaseManagerService;
import ch.ethz.mobilecoach.chatlib.engine.media.MediaLibrary;
import ch.ethz.mobilecoach.model.persistent.MediaLibraryEntry;

public class InDataBaseMediaLibrary implements MediaLibrary {
	
	private final DatabaseManagerService databaseManagerService;

	private final ObjectId participantId;

	public InDataBaseMediaLibrary(final DatabaseManagerService databaseManagerService, ObjectId participantId){
		this.databaseManagerService = databaseManagerService;
		this.participantId = participantId;
	}

	@Override
	public void addMedia(String type, String url, String title) {
		MediaLibraryEntry e = MediaLibraryEntry.create(participantId, type, url, title);
		
		//TODO: make sure media are not added more than once
		//databaseManagerService.findOneModelObject(MediaLibraryEntry.class, query, parameters)
		
		databaseManagerService.saveModelObject(e);		
	}
	
}