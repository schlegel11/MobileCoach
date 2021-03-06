package ch.ethz.mc.services.internal;

/* ##LICENSE## */
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.Base64.Encoder;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.io.Charsets;

import com.google.gson.JsonObject;
import com.turo.pushy.apns.ApnsClient;
import com.turo.pushy.apns.ApnsClientBuilder;
import com.turo.pushy.apns.PushNotificationResponse;
import com.turo.pushy.apns.util.ApnsPayloadBuilder;
import com.turo.pushy.apns.util.SimpleApnsPushNotification;
import com.turo.pushy.apns.util.TokenUtil;

import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.model.persistent.DialogOption;
import ch.ethz.mc.model.persistent.types.PushNotificationTypes;
import ch.ethz.mc.services.InterventionExecutionManagerService;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.Cleanup;
import lombok.Getter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Handles the communication with the push notifications services
 * 
 * @author Andreas Filler
 */
@Log4j2
public class PushNotificationService {
	@Getter
	private static PushNotificationService		instance			= null;

	private InterventionExecutionManagerService	interventionExecutionManagerService;

	private static final String					IOS_IDENTIFIER		= PushNotificationTypes.IOS
			.toString();
	private static final String					ANDROID_IDENTIFIER	= PushNotificationTypes.ANDROID
			.toString();

	final static String							BLOB				= "blob";
	final static String							KEY					= "key";
	final static String							TO					= "to";
	final static String							PRIORITY			= "priority";
	final static String							DATA				= "data";
	final static String							NOTIFICATION		= "notification";
	final static String							BODY				= "body";
	final static String							SOUND				= "sound";
	final static String							ICON				= "icon";
	final static String							ICON_NAME			= "ic_notification";
	final static String							BADGE				= "badge";
	final static String							DEFAULT				= "default";
	final static String							PRIORITY_VALUE		= "high";
	final static String							BAD_DEVICE_TOKEN	= "BadDeviceToken";

	public final static String					GOOGLE_FCM_API_URL	= "https://fcm.googleapis.com/fcm/send";

	private final boolean						iOSActive;
	private final boolean						androidActive;

	private final boolean						iOSEncrypted;
	private final boolean						androidEncrypted;

	private final String						iOSAppIdentifier;
	private final String						androidAuthKey;

	final ApnsClient							apnsClient;
	final Encoder								encoder;

	private PushNotificationService(final boolean pushNotificationsIOSActive,
			final boolean pushNotificationsIOSEncrypted,
			final boolean pushNotificationsAndroidActive,
			final boolean pushNotificationsAndroidEncrypted,
			final boolean pushNotificationsProductionMode,
			final String pushNotificationsIOSAppIdentifier,
			final String pushNotificationsIOSCertificateFile,
			final String pushNotificationsIOSCertificatePassword,
			final String pushNotificationsAndroidAuthKey) throws Exception {
		iOSActive = pushNotificationsIOSActive;
		androidActive = pushNotificationsAndroidActive;

		iOSEncrypted = pushNotificationsIOSEncrypted;
		androidEncrypted = pushNotificationsAndroidEncrypted;

		iOSAppIdentifier = pushNotificationsIOSAppIdentifier;
		androidAuthKey = pushNotificationsAndroidAuthKey;

		if (iOSActive) {
			val iOSCertificateFile = new File(
					pushNotificationsIOSCertificateFile);

			apnsClient = new ApnsClientBuilder()
					.setApnsServer(pushNotificationsProductionMode
							? ApnsClientBuilder.PRODUCTION_APNS_HOST
							: ApnsClientBuilder.DEVELOPMENT_APNS_HOST)
					.setClientCredentials(iOSCertificateFile,
							pushNotificationsIOSCertificatePassword)
					.build();
		} else {
			apnsClient = null;
		}

		encoder = Base64.getEncoder();
	}

	public static PushNotificationService prepare(
			final boolean pushNotificationsIOSActive,
			final boolean pushNotificationsIOSEncrypted,
			final boolean pushNotificationsAndroidActive,
			final boolean pushNotificationsAndroidEncrypted,
			final boolean pushNotificationsProductionMode,
			final String pushNotificationsIOSAppIdentifier,
			final String pushNotificationsIOSCertificateFile,
			final String pushNotificationsIOSCertificatePassword,
			final String pushNotificationsAndroidAuthKey) throws Exception {
		log.info("Preparing service...");
		if (instance == null) {
			instance = new PushNotificationService(pushNotificationsIOSActive,
					pushNotificationsIOSEncrypted,
					pushNotificationsAndroidActive,
					pushNotificationsAndroidEncrypted,
					pushNotificationsProductionMode,
					pushNotificationsIOSAppIdentifier,
					pushNotificationsIOSCertificateFile,
					pushNotificationsIOSCertificatePassword,
					pushNotificationsAndroidAuthKey);
		}
		log.info("Prepared.");
		return instance;
	}

	public void start(
			final InterventionExecutionManagerService interventionExecutionManagerService)
			throws Exception {
		log.info("Starting service...");

		this.interventionExecutionManagerService = interventionExecutionManagerService;

		log.info("Started.");
	}

	public void stop() throws Exception {
		log.info("Stopping service...");

		if (apnsClient != null) {
			try {
				final Future<Void> closeFuture = apnsClient.close();
				closeFuture.await();
			} catch (final Exception e) {
				log.warn("Could not close apns connection: {}", e.getMessage());
			}
		}

		log.info("Stopped.");
	}

	/*
	 * Public methods
	 */
	/**
	 * Sends push notifications asynchronously
	 * 
	 * @param dialogOption
	 * @param messageWithPotentialNewMessageSplitter
	 * @param messagesSentSinceLastLogout
	 * @param forcedPushMessage
	 */
	public void asyncSendPushNotification(final DialogOption dialogOption,
			final String messageWithPotentialNewMessageSplitter,
			final int messagesSentSinceLastLogout,
			final boolean forcedPushMessage) {

		int subMessage = 0;
		for (val message : messageWithPotentialNewMessageSplitter.split(
				ImplementationConstants.PLACEHOLDER_NEW_MESSAGE_APP_IDENTIFIER)) {
			subMessage++;

			for (val unSplittedToken : dialogOption
					.getPushNotificationTokens()) {

				val messageContent = message.replace(
						ImplementationConstants.PLACEHOLDER_LINKED_SURVEY, "🔗")
						.replace(
								ImplementationConstants.PLACEHOLDER_LINKED_MEDIA_OBJECT,
								"🖼");

				String messageToSend;
				boolean messageEncryped;
				if (iOSEncrypted && unSplittedToken.startsWith(IOS_IDENTIFIER)
						|| androidEncrypted && unSplittedToken
								.startsWith(ANDROID_IDENTIFIER)) {
					// Encrypt message
					messageEncryped = true;
					try {
						final String key = dialogOption.getData().substring(0,
								16);
						final String iv = "4537823546456123";

						final Cipher cipher = Cipher
								.getInstance("AES/CBC/NoPadding");
						final int blockSize = cipher.getBlockSize();

						final byte[] dataBytes = messageContent
								.getBytes(Charsets.UTF_8);
						int plaintextLength = dataBytes.length;
						if (plaintextLength % blockSize != 0) {
							plaintextLength = plaintextLength + blockSize
									- plaintextLength % blockSize;
						}

						final byte[] plaintext = new byte[plaintextLength];
						System.arraycopy(dataBytes, 0, plaintext, 0,
								dataBytes.length);

						final SecretKeySpec keyspec = new SecretKeySpec(
								key.getBytes(), "AES");
						final IvParameterSpec ivspec = new IvParameterSpec(
								iv.getBytes());

						cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);
						final byte[] encrypted = cipher.doFinal(plaintext);

						messageToSend = Base64.getEncoder()
								.encodeToString(encrypted);
					} catch (final Exception e) {
						log.error("Error at encoding message: ",
								e.getMessage());
						continue;
					}
				} else {
					// Unencrypted message
					messageEncryped = false;
					messageToSend = messageContent;
				}

				if (iOSActive && unSplittedToken.startsWith(IOS_IDENTIFIER)) {
					sendIOSPushNotification(dialogOption, unSplittedToken,
							messageToSend, messageEncryped,
							messagesSentSinceLastLogout, subMessage,
							forcedPushMessage);
				} else if (androidActive
						&& unSplittedToken.startsWith(ANDROID_IDENTIFIER)) {
					sendAndroidPushNotification(dialogOption, unSplittedToken,
							messageToSend, messageEncryped,
							messagesSentSinceLastLogout, subMessage,
							forcedPushMessage);
				}
			}
		}
	}

	/*
	 * Class methods
	 */
	/**
	 * Sends a push notification using Apple servers
	 * 
	 * @param dialogOption
	 * @param unSplittedToken
	 * @param message
	 * @param messageEncrypted
	 * @param messagesSentSinceLastLogout
	 * @param subMessage
	 * @param forcedPushMessage
	 */
	private void sendIOSPushNotification(final DialogOption dialogOption,
			final String unSplittedToken, String message,
			final boolean messageEncrypted,
			final int messagesSentSinceLastLogout, final int subMessage,
			final boolean forcedPushMessage) {

		// Unencrypted messages only send out the first text
		if (!messageEncrypted && !forcedPushMessage) {
			if (messagesSentSinceLastLogout == 2) {
				if (subMessage == 1) {
					message = "...";
				} else {
					message = null;
				}
			} else if (messagesSentSinceLastLogout > 2) {
				message = null;
			}
		}

		val token = unSplittedToken.substring(IOS_IDENTIFIER.length());
		log.debug("Trying to send iOS push notification to token {}", token);

		final SimpleApnsPushNotification pushNotification;
		{
			final ApnsPayloadBuilder payloadBuilder = new ApnsPayloadBuilder();

			if (messageEncrypted) {
				payloadBuilder.setContentAvailable(true);
				payloadBuilder.addCustomProperty(BLOB, message);
				payloadBuilder.addCustomProperty(KEY, dialogOption.getData()
						.substring(dialogOption.getData().length() - 16));
				payloadBuilder.setBadgeNumber(0);
			} else {
				if (message != null) {
					payloadBuilder.setAlertBody(message);
					payloadBuilder.setSoundFileName(DEFAULT);
				}
				if (!forcedPushMessage) {
					payloadBuilder.setBadgeNumber(messagesSentSinceLastLogout);
				}
			}

			final String payload = payloadBuilder
					.buildWithDefaultMaximumLength();

			pushNotification = new SimpleApnsPushNotification(
					TokenUtil.sanitizeTokenString(token), iOSAppIdentifier,
					payload);
		}

		final Future<PushNotificationResponse<SimpleApnsPushNotification>> sendNotificationFuture = apnsClient
				.sendNotification(pushNotification);

		sendNotificationFuture.addListener(
				new GenericFutureListener<Future<? super PushNotificationResponse<SimpleApnsPushNotification>>>() {
					@Override
					public void operationComplete(
							final Future<? super PushNotificationResponse<SimpleApnsPushNotification>> future)
							throws Exception {
						final PushNotificationResponse<SimpleApnsPushNotification> pushNotificationResponse = sendNotificationFuture
								.get();

						if (pushNotificationResponse.isAccepted()) {
							log.debug(
									"Push notification accepted by APNs gateway.");
						} else {
							log.warn(
									"Notification rejected by the APNs gateway: ",
									pushNotificationResponse
											.getRejectionReason());

							if (pushNotificationResponse.getRejectionReason()
									.equals(BAD_DEVICE_TOKEN)
									|| pushNotificationResponse
											.getTokenInvalidationTimestamp() != null) {
								log.warn(
										"Token is invalid (since {}) and will be removed for the appropriate participant",
										pushNotificationResponse
												.getTokenInvalidationTimestamp());

								interventionExecutionManagerService
										.dialogOptionRemovePushNotificationToken(
												dialogOption.getId(),
												PushNotificationTypes.IOS,
												token);
							}
						}
					}
				});
	}

	/**
	 * Sends a push notification using Google servers
	 * 
	 * @param dialogOption
	 * @param unSplittedToken
	 * @param message
	 * @param messageEncrypted
	 * @param messagesSentSinceLastLogout
	 * @param subMessage
	 * @param forcedPushMessage
	 */
	private void sendAndroidPushNotification(final DialogOption dialogOption,
			final String unSplittedToken, String message,
			final boolean messageEncrypted,
			final int messagesSentSinceLastLogout, final int subMessage,
			final boolean forcedPushMessage) {

		// Unencrypted messages only send out the first text
		if (!messageEncrypted && !forcedPushMessage) {
			if (messagesSentSinceLastLogout == 2) {
				if (subMessage == 1) {
					message = "...";
				} else {
					message = null;
				}
			} else if (messagesSentSinceLastLogout > 2) {
				message = null;
			}
		}

		val token = unSplittedToken.substring(ANDROID_IDENTIFIER.length());
		log.debug("Trying to send Android push notification to token {}",
				token);

		HttpURLConnection conn = null;
		try {
			final URL url = new URL(GOOGLE_FCM_API_URL);
			conn = (HttpURLConnection) url.openConnection();

			conn.setUseCaches(false);
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setConnectTimeout(ImplementationConstants.PUSH_SERVER_TIMEOUT);
			conn.setReadTimeout(ImplementationConstants.PUSH_SERVER_TIMEOUT);

			conn.setRequestMethod("POST");
			conn.setRequestProperty("Authorization", "key=" + androidAuthKey);
			conn.setRequestProperty("Content-Type", "application/json");

			final JsonObject jsonObject = new JsonObject();

			jsonObject.addProperty(TO, token);
			jsonObject.addProperty(PRIORITY, PRIORITY_VALUE);

			if (messageEncrypted) {
				final JsonObject dataContent = new JsonObject();
				dataContent.addProperty(BLOB, message);
				dataContent.addProperty(KEY, dialogOption.getData()
						.substring(dialogOption.getData().length() - 16));
				final JsonObject data = new JsonObject();
				data.add(DATA, dataContent);
				data.addProperty(BADGE, "0");
				jsonObject.add(DATA, data);
			} else {
				if (message != null) {
					final JsonObject notification = new JsonObject();
					notification.addProperty(BODY, message);
					notification.addProperty(ICON, ICON_NAME);
					notification.addProperty(SOUND, DEFAULT);
					jsonObject.add(NOTIFICATION, notification);
				}
				if (!forcedPushMessage) {
					final JsonObject data = new JsonObject();
					data.addProperty(BADGE,
							String.valueOf(messagesSentSinceLastLogout));
					jsonObject.add(DATA, data);
				}
			}

			@Cleanup
			final OutputStreamWriter wr = new OutputStreamWriter(
					conn.getOutputStream());
			wr.write(jsonObject.toString());
			wr.flush();
			int status = 0;

			if (null != conn) {
				status = conn.getResponseCode();
			}

			if (status == 200) {
				@Cleanup
				val reader = new BufferedReader(
						new InputStreamReader(conn.getInputStream()));
				val response = reader.readLine();

				if (response.contains("\"success\":0")) {
					log.warn("Push notification rejected by FCM gateway: {}.",
							response);

					if (response.contains("\"error\":\"NotRegistered\"")) {
						log.debug("Removing push token for participant");
						interventionExecutionManagerService
								.dialogOptionRemovePushNotificationToken(
										dialogOption.getId(),
										PushNotificationTypes.ANDROID, token);
					}
				} else {
					log.debug("Push notification accepted by FCM gateway: {}.",
							response);
				}

			} else {
				log.warn(
						"Notification rejected by the FCM gateway: Status is {}",
						status);
			}
			conn.disconnect();
		} catch (final Exception e) {
			log.warn("Notification rejected by the FCM gateway: ",
					e.getMessage());
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
	}
}
