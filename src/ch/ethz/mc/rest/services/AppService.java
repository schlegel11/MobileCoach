package ch.ethz.mc.rest.services;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.bson.types.ObjectId;

import ch.ethz.mc.conf.Constants;
import ch.ethz.mc.model.persistent.AppToken;
import ch.ethz.mc.model.persistent.OneTimeToken;
import ch.ethz.mc.model.persistent.Participant;
import ch.ethz.mc.model.persistent.ScreeningSurvey;
import ch.ethz.mc.services.RESTManagerService;
import ch.ethz.mc.services.SurveyExecutionManagerService;
import ch.ethz.mc.tools.StringHelpers;
import ch.ethz.mobilecoach.model.persistent.MattermostUserConfiguration;
import ch.ethz.mobilecoach.model.persistent.MediaLibraryEntry;
import ch.ethz.mobilecoach.services.MattermostManagementService;
import ch.ethz.mobilecoach.services.MattermostManagementService.UserConfigurationForAuthentication;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

/**
 * Service to read/write variables using REST
 *
 * @author Filipe Barata
 */
@Path("/app/v01")
@Log4j2
public class AppService {

	private RESTManagerService restManagerService;
	private MattermostManagementService mattMgmtService;
	private SurveyExecutionManagerService surveyService;

	public AppService(RESTManagerService restManagerService, MattermostManagementService mattMgmtService, SurveyExecutionManagerService surveyService) {
		this.restManagerService = restManagerService;
		this.mattMgmtService = mattMgmtService;
		this.surveyService = surveyService;	}

	@GET
	@Path("/getconfig")
	@Produces("application/json")
	public Result getConfig(@Context final HttpServletRequest request,
			@HeaderParam("Authentication") final String token) throws BadRequestException {

		if (token == null) {
			throw new WebApplicationException(Response.status(400).entity("Missing header 'Authentication'.").build());
		}

		try {
			ObjectId userId = null;
			String mctoken = null;
			if (OneTimeToken.isOneTimeToken(token)){
				// handle OneTimeToken: invalidate and create AppToken
				userId = restManagerService.consumeOneTimeToken(token);
				if (userId != null) mctoken = restManagerService.createAppTokenForParticipant(userId);
			} else if (AppToken.isAppToken(token)){
				// handle AppToken: use the supplied token
				userId = restManagerService.findParticipantIdForAppToken(token);
				mctoken = token;
			}
			
			if (userId == null) {
				throw new WebApplicationException(Response.status(403).entity("Invalid Token supplied").build());
			}
	
			MattermostUserConfiguration userConfiguration = fetchUserConfiguration(userId);
			
			Participant p = restManagerService.getParticipant(userId);
			String participantShortId = p != null ? p.getShortId() : "___";
	
			return new Result(new MobileCoachAuthentication(userId.toHexString(), participantShortId,  mctoken),
					new UserConfigurationForAuthentication(userConfiguration),
					getVariables(userId));
		} catch (Exception e){
			log.error("getconfig error: " + e.getMessage() + StringHelpers.getStackTraceAsLine(e));
			throw e;
		}
	}
	
	
	@GET
	@Path("/create-participant")
	@Produces("application/json")
	public Result createParticipant(@Context final HttpServletRequest request, 
			@HeaderParam("Survey") final String surveyId,
			@HeaderParam("Locale") final String locale) throws BadRequestException {
		
		// validate locale
		Locale localeObj = null;
		for (Locale l: Constants.getInterventionLocales()){
			if (l.toLanguageTag().equals(locale)){
				localeObj = l;
			}
		}
		if (localeObj == null) {
			throw new WebApplicationException(Response.status(400).entity("Invalid Locale supplied").build());
		}
		
		// validate screening survey
		ScreeningSurvey survey = null;
		for (ScreeningSurvey s: surveyService.getActiveNonItermediateScreeningSurveys()){
			if (s.getId().toHexString().equals(surveyId)){
				survey = s;
				if (!s.isActive()){
					throw new WebApplicationException(Response.status(403).entity("Survey is inactive").build());
				}
			}
		}
		if (survey == null) {
			throw new WebApplicationException(Response.status(400).entity("Invalid Survey Id supplied").build());
		}
		
		// create participant
		Participant user = surveyService.createParticipantForApp(survey, localeObj);
		ObjectId userId = user.getId();
		
		String mctoken = restManagerService.createAppTokenForParticipant(userId);
		MattermostUserConfiguration userConfiguration = fetchUserConfiguration(userId);

		return new Result(new MobileCoachAuthentication(userId.toHexString(), user.getShortId(), mctoken),
				new UserConfigurationForAuthentication(userConfiguration),
				getVariables(userId));
	}
	
	
	@GET
	@Path("/media-library")
	@Produces("application/json")
	public List<LibraryEntry> mediaLibrary(@Context final HttpServletRequest request,
			@HeaderParam("Authentication") final String token) throws BadRequestException {

		ObjectId userId = restManagerService.checkTokenAndGetParticipantId(token);
		
		List<LibraryEntry> result = new LinkedList<LibraryEntry>();
		for (MediaLibraryEntry e: restManagerService.getMediaLibrary(userId)){
			result.add(new LibraryEntry(e.getType(), e.getUrl(), e.getTitle()));
		}
		return result;
	}
	

	private MattermostUserConfiguration fetchUserConfiguration(final ObjectId participantId) {
		MattermostUserConfiguration userConfig;
		if (mattMgmtService.existsUserForParticipant(participantId)) {
			userConfig = mattMgmtService.getUserConfiguration(participantId);
		} else {
			userConfig = mattMgmtService.createParticipantUser(participantId);
		}
		return userConfig;
	}
	
	private Map<String, String> getVariables(final ObjectId participantId) {
		return restManagerService.getExternallyReadableVariableValues(participantId);
	}

	@AllArgsConstructor
	private static class Result {

		@Getter
		private final MobileCoachAuthentication mobilecoach;

		@Getter
		private final UserConfigurationForAuthentication mattermost;
		
		@Getter
		private final Map<String, String> variables;

	}

	@AllArgsConstructor
	private static class MobileCoachAuthentication {
		@Getter
		private final String participant_id;
		@Getter
		private final String participant_short_id;
		@Getter
		private final String token;

	}
	
	@AllArgsConstructor
	private static class LibraryEntry {

		@Getter
		private final String type;

		@Getter
		private final String url;
		
		@Getter
		private final String title;

	}
}