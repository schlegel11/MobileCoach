package ch.ethz.mobilecoach.services;

import java.util.HashMap;

import org.bson.types.ObjectId;

import ch.ethz.mc.model.persistent.ScreeningSurvey;
import ch.ethz.mc.services.SurveyAdministrationManagerService;
import ch.ethz.mc.services.SurveyExecutionManagerService;
import ch.ethz.mc.services.internal.DatabaseManagerService;
import ch.ethz.mobilecoach.chatlib.engine.media.Media;
import ch.ethz.mobilecoach.chatlib.engine.media.MediaLibrary;
import ch.ethz.mobilecoach.model.persistent.MediaLibraryEntry;
import lombok.val;

public class InDataBaseMediaLibrary implements MediaLibrary {
	
	private final DatabaseManagerService databaseManagerService;
	private final SurveyExecutionManagerService surveyService;
	private final HashMap<String, Media> mediaMap = new HashMap<>();

	private final ObjectId participantId;
	private final ObjectId interventionId;

	public InDataBaseMediaLibrary(
			final DatabaseManagerService databaseManagerService,
			final SurveyExecutionManagerService surveyService,
			ObjectId participantId,
			ObjectId interventionId){
		this.databaseManagerService = databaseManagerService;
		this.surveyService = surveyService;
		this.participantId = participantId;
		this.interventionId = interventionId;
	}

	@Override
	public void addToPersonalLibrary(String type, String url, String title) {
		MediaLibraryEntry e = MediaLibraryEntry.create(participantId, type, url, title);
		
		//TODO: make sure media are not added more than once
		//databaseManagerService.findOneModelObject(MediaLibraryEntry.class, query, parameters)
		
		databaseManagerService.saveModelObject(e);		
	}

	@Override
	public Media get(String id) {
		Media result = mediaMap.get(id);
		
		// for surveys: get a personal url
		if (result != null && "survey".equals(result.type)){
			final val screeningSurveys = databaseManagerService.findModelObjects(ScreeningSurvey.class, "{intervention: #}", interventionId).iterator();
			
			// find a survey that matches 'id'
			while (screeningSurveys.hasNext()){
				ScreeningSurvey survey = screeningSurveys.next();
				if (survey.getName().anyLocaleValueEquals(id)){
					val url = surveyService.intermediateSurveyParticipantShortURLEnsure(participantId, survey.getId());
					result = new Media(result.type, url.calculateURL(), result.title);
					break;
				}
			}
		}
		
		return result;
	}

	@Override
	public void add(String id, Media media) {
		mediaMap.put(id, media);
	}
	
}