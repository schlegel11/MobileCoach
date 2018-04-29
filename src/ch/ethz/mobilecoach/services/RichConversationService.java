package ch.ethz.mobilecoach.services;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalTime;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.bson.types.ObjectId;
import org.mockito.exceptions.verification.NeverWantedButInvoked;

import ch.ethz.mc.MC;
import ch.ethz.mc.conf.Constants;
import ch.ethz.mc.model.Queries;
import ch.ethz.mc.model.persistent.Participant;
import ch.ethz.mc.services.SurveyExecutionManagerService;
import ch.ethz.mc.services.internal.ChatEngineStateStore;
import ch.ethz.mc.services.internal.DatabaseManagerService;
import ch.ethz.mc.services.internal.InDataBaseVariableStore;
import ch.ethz.mc.services.internal.VariablesManagerService;
import ch.ethz.mc.tools.InternalDateTime;
import ch.ethz.mc.tools.InternalTimer;
import ch.ethz.mc.tools.StringHelpers;
import ch.ethz.mobilecoach.app.Option;
import ch.ethz.mobilecoach.app.Post;
import ch.ethz.mobilecoach.chatlib.engine.ChatEngine;
import ch.ethz.mobilecoach.chatlib.engine.ConversationRepository;
import ch.ethz.mobilecoach.chatlib.engine.Evaluator;
import ch.ethz.mobilecoach.chatlib.engine.ExecutionException;
import ch.ethz.mobilecoach.chatlib.engine.HelpersRepository;
import ch.ethz.mobilecoach.chatlib.engine.Input;
import ch.ethz.mobilecoach.chatlib.engine.Logger;
import ch.ethz.mobilecoach.chatlib.engine.conversation.ConversationUI;
import ch.ethz.mobilecoach.chatlib.engine.conversation.UserReplyListener;
import ch.ethz.mobilecoach.chatlib.engine.helpers.IncrementVariableHelper;
import ch.ethz.mobilecoach.chatlib.engine.helpers.MinusVariableHelper;
import ch.ethz.mobilecoach.chatlib.engine.helpers.SumVariablesHelper;
import ch.ethz.mobilecoach.chatlib.engine.media.MediaLibrary;
import ch.ethz.mobilecoach.chatlib.engine.model.AnswerOption;
import ch.ethz.mobilecoach.chatlib.engine.model.Message;
import ch.ethz.mobilecoach.chatlib.engine.timing.TimingCalculatorAdvanced;
import ch.ethz.mobilecoach.chatlib.engine.translation.SimpleTranslator;
import ch.ethz.mobilecoach.chatlib.engine.translation.Translator;
import ch.ethz.mobilecoach.chatlib.engine.translation.VariantSelector;
import ch.ethz.mobilecoach.chatlib.engine.translation.VariantSelectorLegacy;
import ch.ethz.mobilecoach.chatlib.engine.variables.InMemoryVariableStore;
import ch.ethz.mobilecoach.chatlib.engine.variables.VariableException;
import ch.ethz.mobilecoach.chatlib.engine.variables.VariableStore;
import ch.ethz.mobilecoach.model.persistent.ChatEnginePersistentState;
import ch.ethz.mobilecoach.test.helpers.TestHelpersFactory;
import lombok.Getter;
import lombok.Synchronized;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class RichConversationService {

	private MessagingService		messagingService;
	
	private ConversationManagementService		conversationManagementService;

	private VariablesManagerService				variablesManagerService;
	private DatabaseManagerService				dBManagerService;
	private SurveyExecutionManagerService		surveyService;
	private LinkedHashMap<ObjectId, ChatEngine>	chatEngines	= new LinkedHashMap<>();

	private RichConversationService(MessagingService messagingService,
			ConversationManagementService conversationManagementService,
			VariablesManagerService variablesManagerService,
			DatabaseManagerService dBManagerService,
			SurveyExecutionManagerService surveyService) throws Exception {
		this.messagingService = messagingService;
		this.conversationManagementService = conversationManagementService;
		this.surveyService = surveyService;
		this.variablesManagerService = variablesManagerService;
		this.dBManagerService = dBManagerService;
		continueConversation();
	}

	public static RichConversationService start(
			MessagingService messagingService,
			ConversationManagementService conversationManagementService,
			VariablesManagerService variablesManagerService,
			DatabaseManagerService dBManagerService,
			SurveyExecutionManagerService surveyService) throws Exception {
		RichConversationService service = new RichConversationService(
				messagingService, conversationManagementService,
				variablesManagerService, dBManagerService, surveyService);
		return service;
	}

	private void continueConversation() {
		java.util.Iterator<ChatEnginePersistentState> iterator = dBManagerService
				.findModelObjects(ChatEnginePersistentState.class, Queries.ALL)
				.iterator();

		while (iterator.hasNext()) {
			ChatEnginePersistentState ces = iterator.next();
			
			boolean deleteIt = false;

			if (ChatEngineStateStore.containsARecentChatEngineState(ces)) {

				log.debug("Restoring chat engine state: " + new Date(ces.getTimeStamp()) + " " 
						+ ces.getConversationsHash().substring(0, 7) + " "+ ces.getSerializedState());

				ChatEngineStateStore chatEngineStateStore = new ChatEngineStateStore(
						dBManagerService, ces.getParticipantId());
				Participant participant = dBManagerService.getModelObjectById(
						Participant.class, ces.getParticipantId());
				if (participant != null) {
					try {
						ChatEngine engine = prepareChatEngine(participant, chatEngineStateStore, ces.getConversationsHash());
						chatEngineStateStore.restoreState(engine);
						engine.run();
						ces.setStatus("Loaded");
						
					} catch (Exception e) {
						// TODO: should we delete the state if we cannot restore
						// it, maybe after 2 days?
						log.error(
								"Error restoring chat engine: "
										+ StringHelpers.getStackTraceAsLine(e),
								e);
						
						ces.setStatus("Error");
					}
				} else {
					ces.setStatus("Not Found");
				}
			} else {
				// Outdated => delete state
				ces.setStatus("Outdated");
				deleteIt = true;
			}
			
			// update status
			if (!deleteIt){
				dBManagerService.saveModelObject(ces);
			} else {
				dBManagerService.deleteModelObject(ces);
			}
		}

		this.messagingService.startReceiving();
		// now that all the listeners have been set, we can start receiving
	}

	public void sendMessage(String sender, ObjectId recipient, String message) throws ExecutionException {

		final String START_CONVERSATION_PREFIX = "start-conversation:";
		final String CONSIDER_CONVERSATION_PREFIX = "consider-conversation:";
		
		boolean start = message.startsWith(START_CONVERSATION_PREFIX);
		boolean consider = message.startsWith(CONSIDER_CONVERSATION_PREFIX);
		
		if (start || consider) {
			
			String interventionId = null;
			String conversation;
			String restString = message.substring(START_CONVERSATION_PREFIX.length()).trim();
			int slashIndex = restString.indexOf("/");
			if (slashIndex > -1){
				interventionId = restString.substring(0, slashIndex);
				conversation = restString.substring(slashIndex + 1);
			} else {
				conversation = restString;
			}

			ChatEngineStateStore chatEngineStateStore = new ChatEngineStateStore(
					dBManagerService, recipient);
			Participant participant = dBManagerService
					.getModelObjectById(Participant.class, recipient);

			ChatEngine engine = null;
			
			/*
			 * Find or create the chat engine
			 */
			
			try {
				if (chatEngines.containsKey(participant.getId())) {
					// there's already a conversation going on for this
					// participants... re-set it
					engine = chatEngines.get(participant.getId());
					
					ConversationRepository repository = conversationManagementService.getRepository(interventionId);
					
					// re-initialize translator
					Translator translator = prepareTranslator(participant.getLanguage(), repository, engine.getVariableStore());
					
					// get the newest conversation repository and start
					engine.startConversation(conversation, repository, translator);
				} else {
					engine = prepareChatEngine(participant, chatEngineStateStore, interventionId);
					if (start){
						engine.startConversation(conversation);
					} else {
						engine.considerConversation(conversation);
					}
				}
			} catch (Exception e) {
				log.error(e.getMessage() + " " + StringHelpers.getStackTraceAsLine(e), e);
				log.error("Could not start conversation: " + restString);
			}

		} else {
			// stop conversation
			if (chatEngines.containsKey(recipient)) {
				chatEngines.remove(recipient);
			}

			// send a message
			messagingService.sendMessage(sender, recipient, message);
		}
		
		// update channel name
		updateChannelName(recipient);
	}

	private ChatEngine prepareChatEngine(Participant participant,
			ChatEngineStateStore chatEngineStateStore, String interventionId) throws IOException {
		ConversationRepository repository = conversationManagementService.getRepository(interventionId);
		
		if (repository == null){
			throw new IOException("Repository not found: " + interventionId);
		}
		
		final String participantId = participant.getId().toHexString();
		
		Logger logger = new Logger() {

			@Override
			public void logError(String message) {
				log.error(participantId + ": " + message);
			}

			@Override
			public void logDebug(String message) {
				log.debug(participantId + ": " + message);
			}

			@Override
			public void logInfo(String message) {
				log.info(participantId + ": " + message);
			}
		};

		VariableStore variableStore = createVariableStore(participant.getId());
		MediaLibrary mediaLibrary = new InDataBaseMediaLibrary(dBManagerService, surveyService, participant.getId(), participant.getIntervention());

		MattermostConnector mattermostConnector = new MattermostConnector(participant.getId());
		ConversationUI ui = new CommunicationSwitch(mattermostConnector, participant);
		HelpersRepository helpers = new HelpersRepository();
		
		double dialogSpeedup = 1.0;
		
		if (variableStore.containsVariable("$dialog_speedup")){
			try {
				dialogSpeedup = Double.parseDouble(variableStore.get("$dialog_speedup"));
			} catch (Exception e){
				log.error("Error parsing $dialog_speedup", e);
			}
		}
		
		Translator translator = prepareTranslator(participant.getLanguage(), repository, variableStore);
		ChatEngine engine = new ChatEngine(repository, ui, variableStore, mediaLibrary,
				helpers, translator, chatEngineStateStore, new TimingCalculatorAdvanced(dialogSpeedup));
		engine.sendExceptionAsMessage = false;
		
		engine.setLogger(logger);
		chatEngines.put(participant.getId(), engine);

		// add helpers for PathMate intervention
		helpers.addHelper("PM-add-1-to-total_keys",
				new IncrementVariableHelper("$total_keys", 1));
		helpers.addHelper("PM-add-1-to-breathing_collected_keys",
				new IncrementVariableHelper("$breathing_collected_keys", 1));
		helpers.addHelper("PM-add-1-to-steps_collected_keys",
				new IncrementVariableHelper("$steps_collected_keys", 1));
		helpers.addHelper("PM-add-1-to-photo_collected_keys",
				new IncrementVariableHelper("$photo_collected_keys", 1));
		helpers.addHelper("PM-add-1-to-quiz_collected_keys",
				new IncrementVariableHelper("$quiz_collected_keys", 1));
		helpers.addHelper("PM-add-1-to-breathing_keys",
				new IncrementVariableHelper("$breathing_keys", 1));
		helpers.addHelper("PM-add-1-to-steps_keys",
				new IncrementVariableHelper("$steps_keys", 1));
		helpers.addHelper("PM-add-1-to-photo_keys",
				new IncrementVariableHelper("$photo_keys", 1));
		helpers.addHelper("PM-add-1-to-quiz_keys",
				new IncrementVariableHelper("$quiz_keys", 1));
		helpers.addHelper("PM-add-1-to-total_collected_keys",
				new IncrementVariableHelper("$total_collected_keys", 1));
		helpers.addHelper("PM-add-1-to-bonus_keys",
				new IncrementVariableHelper("$bonus_keys", 1));
		helpers.addHelper("PM-get-12mins-walking-steps", 
				new MinusVariableHelper("$12mins_walking_steps", "$12mins_walking_steps_after"));
		
		helpers.addHelper("ASTHMA1-sum", 
				new SumVariablesHelper("$act_sum", new String[]{"$act1", "$act2", "$act3", "$act4", "$act5"}));
		
		new TestHelpersFactory(engine, ui).addHelpers(helpers);

		messagingService.setListener(participant.getId(), mattermostConnector);

		ui.setUserReplyListener(new UserReplyListener() {
			@Override
			public void userReplied(Input input) {

				ObjectId participantId = mattermostConnector.getRecipient();

				if (chatEngines.containsKey(participantId)) {
					// make sure this is handled in another thread, so that we can continue immediately
					ui.delay(new Runnable(){
						@Override
						public void run() {
							
							engine.handleInput(input);
							
						}						
					}, 0L);
					
				} else {

					log.warn(
							"Message received, but no conversation running for participant: "
									+ participantId);
					// TODO (DR): store the message for the MC system to collect
					// it.
					// This is not necessary for the PathMate2 intervention.
				}
			}
		});
		
		final ObjectId participantObjId = participant.getId();

		engine.setOnTerminated(new Runnable() {

			@Override
			public void run() {
				// TODO: clean up the engine
				// - unregister listeners (web socket and timer) to release the
				// engine for garbage collection
				
				// update channel name after a conversation has terminated
				updateChannelName(participantObjId);
			}

		});

		return engine;
	}
	
	private void updateChannelName(ObjectId participantId){
		// update channel name
		try {
			messagingService.setChannelName(participantId);
		} catch (Exception e){
			log.error(e);
		}
	}
	
	public Translator prepareTranslator(Locale language, ConversationRepository repository, VariableStore variables){
		
		// use Variator if available
		
		if (repository.getVariator() != null){
			return new VariantSelector(repository.getVariator(), variables);
		}
		
		
		// Try to use variant selection
			
		Properties properties = getRepositoryProperties(repository);
		
		if (properties != null){
			String variantFile = properties.getProperty("variantFile");
			//String variantMapping = properties.getProperty("variantMapping");
			
			if (variantFile != null){// && variantMapping != null){
				try {
					return new VariantSelectorLegacy(repository.getPath() + "/" + variantFile, null, variables);
				} catch (IOException e) {
					log.error(e);
				}
			}
		}

		// Translation for PathMate

		String gender = null;	
		try {
			gender = variables.get("$participantGender");
		} catch (VariableException ve){
		}
		
		return new SimpleTranslator(language, repository.getPath() + "/translation_en_ch_fr.csv", gender);
	}
	
	public Properties getRepositoryProperties(ConversationRepository repository){
		
		String filePath = repository.getPath() + "/config.properties";
		try (InputStream inputStream = new FileInputStream(filePath)){
			
			Properties prop = new Properties();
			prop.load(inputStream);
			return prop;
			
		} catch (Exception e) {
			log.error(e);
		}
		
		return null;
	}
	

	public VariableStore createVariableStore(ObjectId participantId) {
		VariableStore variableStore;
		// For testing purposes only.
		if (dBManagerService == null || variablesManagerService == null) {
			variableStore = new InMemoryVariableStore();
		} else {
			Participant participant = dBManagerService
					.getModelObjectById(Participant.class, participantId);
			variableStore = new InDataBaseVariableStore(variablesManagerService,
					participantId, participant);
		}
		return variableStore;
	}
	
	private class CommunicationSwitch implements ConversationUI {
		
		private final MattermostConnector mattermostConnector;
		private final Participant participant;
		
		public CommunicationSwitch(MattermostConnector mattermostConnector, Participant participant){
			this.mattermostConnector = mattermostConnector;
			this.participant = participant;
		}

		@Override
		public void showMessage(Message message) {
			if ("EmailOrSMS".equals(message.channel)){
				MC.getInstance().getInterventionExecutionManagerService().sendMessageOutsideOfApp(participant, false, message.text);
			} else if ("EmailOrSMS-Supervisor".equals(message.channel)){
				MC.getInstance().getInterventionExecutionManagerService().sendMessageOutsideOfApp(participant, true, message.text);
			} else {
				mattermostConnector.showMessage(message);
			}
		}

		@Override
		public void showTyping(String sender) {
			mattermostConnector.showTyping(sender);
		}

		@Override
		public void setUserReplyListener(UserReplyListener listener) {
			mattermostConnector.setUserReplyListener(listener);
		}

		@Override
		public void delay(Runnable callback, Long milliseconds) {
			mattermostConnector.delay(callback, milliseconds);
		}

		@Override
		public void cancelDelay() {
			mattermostConnector.cancelDelay();
			
		}

		@Override
		public void setDelayEnabled(boolean enabled) {
			mattermostConnector.setDelayEnabled(enabled);
		}

		@Override
		public long getMillisecondsUntil(LocalTime time) {
			return mattermostConnector.getMillisecondsUntil(time);
		}

		@Override
		public boolean supportsReminders() {
			return mattermostConnector.supportsReminders();
		}

		@Override
		public void showNotification() {
			mattermostConnector.showNotification();
		}
		
	}

	// WebSocket Listener
	// ******************

	private class MattermostConnector
			implements ConversationUI, MessagingService.MessageListener {

		private UserReplyListener	listener;
		private String				sender;
		private boolean				delayEnabled	= true;
		private InternalTimer		timer			= new InternalTimer();	// Timer
																			// thread
																			// for
																			// this
																			// user

		@Getter
		private ObjectId			recipient;

		public MattermostConnector(ObjectId recipient) {
			this.recipient = recipient;
		}

		@Override
		public void showMessage(Message message) {
			if (Message.SENDER_COACH.equals(message.sender)) {

				Post post = new Post();
				post.setMessage(message.text);

				if (message.answerOptions.size() > 0) {
					post.setPostType(Post.POST_TYPE_REQUEST);
					String requestType = message.requestType != null
							? message.requestType
							: Post.REQUEST_TYPE_SELECT_ONE;
					post.setRequestType(requestType);
					for (AnswerOption answerOption : message.answerOptions) {
						Option option = new Option(answerOption.text,
								answerOption.value);
						post.getOptions().add(option);
					}
				} else if (message.objectId != null) {
					post.setPostType(Post.POST_TYPE_REQUEST);
					post.setRequestType(message.objectId);
				} else if (message.requestType != null) {
					post.setPostType(Post.POST_TYPE_REQUEST);
					post.setRequestType(message.requestType);
				}
				
				post.setMediaType(message.mediaType);
				post.setMediaUrl(message.mediaUrl);				
				
				post.getParameters().putAll(message.parameters);
				post.setHidden(message.hidden);
				
				post.setConversation(message.conversation);
				post.setTrackingTag(message.trackingTag);

				messagingService.sendMessage(sender, recipient, post, Boolean.TRUE.equals(message.isReminder));
			}
		}

		@Override
		public void setUserReplyListener(UserReplyListener listener) {
			this.listener = listener;
		}

		@Override
		public void delay(Runnable callback, Long milliseconds) {
			// TODO implement a better delay
			if (!delayEnabled) {
				milliseconds = 1L;
			}

			log.debug("Starting timer with " + milliseconds + " msec.");
			timer.schedule(callback, milliseconds);
		}

		public void receivePost(Post post) {
			if (this.listener != null) {
				this.listener.userReplied(post.getInput());
			} else {
				log.warn("post received but no listener registered.");
			}
		}

		@Override
		public void showTyping(String sender) {
			if (Message.SENDER_COACH.equals(sender)) {
				messagingService.indicateTyping(this.sender, recipient);
			}
		}

		@Override
		public void setDelayEnabled(boolean enabled) {
			delayEnabled = enabled;
		}

		@Override
		public void cancelDelay() {
			try {
				timer.cancelAll();
			} catch (Exception e){
				log.error("Error cancelling timer: " + e.getMessage());
			}
		}

		/**
		 * Note: this assumes that the local time refers to the same day.
		 */
		@Override
		public long getMillisecondsUntil(LocalTime time) {
			return InternalDateTime.getMillisecondsUntil(time);
		}

		@Override
		public boolean supportsReminders() {
			return true;
		}

		@Override
		public void showNotification() {
			Post post = new Post();
			post.setMessage("Please answer to continue! Thanks :)"); // TODO: send translated text
			messagingService.sendMessage(sender, recipient, post, true);
		}

	}

	// for admin UI

	@Synchronized
	public Iterable<ChatEnginePersistentState> getAllConversations() {
		return dBManagerService
				.findModelObjects(ChatEnginePersistentState.class, Queries.ALL);
	}

	public void deleteChatEnginePersistentState(ObjectId stateId) {
		dBManagerService.deleteModelObject(ChatEnginePersistentState.class,
				stateId);
	}

	public void deleteAllChatEnginePersistentStates() {
		for (ObjectId stateId : dBManagerService.findModelObjectIds(
				ChatEnginePersistentState.class, Queries.ALL)) {
			dBManagerService.deleteModelObject(ChatEnginePersistentState.class,
					stateId);
		}
	}

}
