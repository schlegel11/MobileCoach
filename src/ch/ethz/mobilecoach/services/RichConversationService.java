package ch.ethz.mobilecoach.services;

import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Timer;
import java.util.TimerTask;

import org.bson.types.ObjectId;

import ch.ethz.mc.conf.Constants;
import ch.ethz.mc.model.Queries;
import ch.ethz.mc.model.persistent.Participant;
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
import ch.ethz.mobilecoach.chatlib.engine.ExecutionException;
import ch.ethz.mobilecoach.chatlib.engine.HelpersRepository;
import ch.ethz.mobilecoach.chatlib.engine.Input;
import ch.ethz.mobilecoach.chatlib.engine.Logger;
import ch.ethz.mobilecoach.chatlib.engine.Translator;
import ch.ethz.mobilecoach.chatlib.engine.conversation.ConversationUI;
import ch.ethz.mobilecoach.chatlib.engine.conversation.UserReplyListener;
import ch.ethz.mobilecoach.chatlib.engine.helpers.IncrementVariableHelper;
import ch.ethz.mobilecoach.chatlib.engine.model.AnswerOption;
import ch.ethz.mobilecoach.chatlib.engine.model.Message;
import ch.ethz.mobilecoach.chatlib.engine.serialization.RestoreException;
import ch.ethz.mobilecoach.chatlib.engine.variables.InMemoryVariableStore;
import ch.ethz.mobilecoach.chatlib.engine.variables.VariableStore;
import ch.ethz.mobilecoach.model.persistent.ChatEnginePersistentState;
import ch.ethz.mobilecoach.test.helpers.TestHelpersFactory;
import lombok.Getter;
import lombok.Synchronized;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class RichConversationService {

	private MessagingService					messagingService;
	private ConversationManagementService		conversationManagementService;

	private VariablesManagerService				variablesManagerService;
	private DatabaseManagerService				dBManagerService;
	private LinkedHashMap<ObjectId, ChatEngine>	chatEngines	= new LinkedHashMap<>();

	private RichConversationService(MessagingService mattermostMessagingService,
			ConversationManagementService conversationManagementService,
			VariablesManagerService variablesManagerService,
			DatabaseManagerService dBManagerService) throws Exception {
		this.messagingService = mattermostMessagingService;
		this.conversationManagementService = conversationManagementService;

		this.variablesManagerService = variablesManagerService;
		this.dBManagerService = dBManagerService;
		continueConversation();
	}

	public static RichConversationService start(
			MessagingService messagingService,
			ConversationManagementService conversationManagementService,
			VariablesManagerService variablesManagerService,
			DatabaseManagerService dBManagerService) throws Exception {
		RichConversationService service = new RichConversationService(
				messagingService, conversationManagementService,
				variablesManagerService, dBManagerService);
		return service;
	}

	private void continueConversation() {
		java.util.Iterator<ChatEnginePersistentState> iterator = dBManagerService
				.findModelObjects(ChatEnginePersistentState.class, Queries.ALL)
				.iterator();

		while (iterator.hasNext()) {
			ChatEnginePersistentState ces = iterator.next();

			if (ChatEngineStateStore.containsAValidChatEngineState(ces)) {

				log.debug("Restoring chat engine state: "
						+ ces.getSerializedState());

				ChatEngineStateStore chatEngineStateStore = new ChatEngineStateStore(
						dBManagerService, ces.getParticipantId());
				Participant participant = dBManagerService.getModelObjectById(
						Participant.class, ces.getParticipantId());
				if (participant != null) {
					try {
						ChatEngine engine = prepareChatEngine(null, participant,
								chatEngineStateStore);
						chatEngineStateStore.restoreState(engine);
						engine.run();
					} catch (Exception e) {
						// TODO: should we delete the state if we cannot restore
						// it, maybe after 2 days?
						log.error(
								"Error restoring chat engine: "
										+ StringHelpers.getStackTraceAsLine(e),
								e);
					}
				}
			}
		}

		this.messagingService.startReceiving(); // now that all the listeners
												// have been set, we can start
												// receiving
	}

	public void sendMessage(String sender, ObjectId recipient, String message)
			throws ExecutionException {

		final String START_CONVERSATION_PREFIX = "start-conversation:";
		if (message.startsWith(START_CONVERSATION_PREFIX)) {

			ChatEngineStateStore chatEngineStateStore = new ChatEngineStateStore(
					dBManagerService, recipient);
			Participant participant = dBManagerService
					.getModelObjectById(Participant.class, recipient);

			ChatEngine engine = null;

			if (chatEngines.containsKey(participant.getId())) {
				// there's already a conversation going on for this
				// participants... re-set it
				engine = chatEngines.get(participant.getId());

			} else {
				try {
					engine = prepareChatEngine(sender, participant,
							chatEngineStateStore);
				} catch (Exception e) {
					log.error(e.getMessage() + " "
							+ StringHelpers.getStackTraceAsLine(e), e);
					throw e;
				}
			}

			String conversation = message
					.substring(START_CONVERSATION_PREFIX.length());
			engine.startConversation(conversation);

		} else {
			// stop conversation
			if (chatEngines.containsKey(recipient)) {
				chatEngines.remove(recipient);
			}

			// send a message
			messagingService.sendMessage(sender, recipient, message);
		}
	}

	private ChatEngine prepareChatEngine(String sender, Participant participant,
			ChatEngineStateStore chatEngineStateStore) {
		ConversationRepository repository = conversationManagementService
				.getRepository(null); // TODO: use Intervention id to get the
										// repository

		Logger logger = new Logger() {

			@Override
			public void logError(String message) {
				log.error(message);
			}

			@Override
			public void logDebug(String message) {
				log.debug(message);
			}

			@Override
			public void logInfo(String message) {
				log.info(message);
			}
		};

		VariableStore variableStore = createVariableStore(participant.getId());
		Translator translator = new Translator(participant.getLanguage(),
				Constants.getXmlScriptsFolder()
						+ "/pathmate2/translation_en_ch.csv");

		MattermostConnector ui = new MattermostConnector(sender,
				participant.getId());
		HelpersRepository helpers = new HelpersRepository();

		ChatEngine engine = new ChatEngine(repository, ui, variableStore,
				helpers, translator, chatEngineStateStore);
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
		
		new TestHelpersFactory(engine, ui).addHelpers(helpers);

		messagingService.setListener(participant.getId(), ui);

		ui.setUserReplyListener(new UserReplyListener() {
			@Override
			public void userReplied(Input input) {

				ObjectId participantId = ui.getRecipient();

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

		engine.setOnTerminated(new Runnable() {

			@Override
			public void run() {
				// TODO: clean up the engine
				// - unregister listeners (web socket and timer) to release the
				// engine for garbage collection
			}

		});

		return engine;
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

		public MattermostConnector(String sender, ObjectId recipient) {

			this.sender = sender;
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

				post.getParameters().putAll(message.parameters);
				post.setHidden(message.hidden);

				messagingService.sendMessage(sender, recipient, post);
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
			timer.cancelAll();
		}

		/**
		 * Note: this assumes that the local time refers to the same day.
		 */
		@Override
		public long getMillisecondsUntil(LocalTime time) {
			return InternalDateTime.getMillisecondsUntil(time);
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
