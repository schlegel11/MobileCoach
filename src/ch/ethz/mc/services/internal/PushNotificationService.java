package ch.ethz.mc.services.internal;

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
	final static String							DATA				= "data";
	final static String							NOTIFICATION		= "notification";
	final static String							BODY				= "body";
	final static String							SOUND				= "sound";
	final static String							BADGES				= "badges";
	final static String							DEFAULT				= "default";
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
	 */
	public void asyncSendPushNotification(final DialogOption dialogOption,
			final String messageWithPotentialNewMessageSplitter,
			final int messagesSentSinceLastLogout) {

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
							messagesSentSinceLastLogout, subMessage);
				} else if (androidActive
						&& unSplittedToken.startsWith(ANDROID_IDENTIFIER)) {
					sendAndroidPushNotification(dialogOption, unSplittedToken,
							messageToSend, messageEncryped,
							messagesSentSinceLastLogout, subMessage);
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
	 */
	private void sendIOSPushNotification(final DialogOption dialogOption,
			final String unSplittedToken, String message,
			final boolean messageEncrypted,
			final int messagesSentSinceLastLogout, final int subMessage) {

		// Unencrypted messages only send out the first text
		if (!messageEncrypted) {
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
			payloadBuilder.setContentAvailable(true);

			if (messageEncrypted) {
				payloadBuilder.addCustomProperty(BLOB, message);
				payloadBuilder.addCustomProperty(KEY, dialogOption.getData()
						.substring(dialogOption.getData().length() - 16));
				payloadBuilder.setBadgeNumber(0);
			} else {
				if (message != null) {
					payloadBuilder.setAlertBody(message);
					payloadBuilder.setSoundFileName(DEFAULT);
				}
				payloadBuilder.setBadgeNumber(messagesSentSinceLastLogout);
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
							log.debug(
									"Notification rejected by the APNs gateway: ",
									pushNotificationResponse
											.getRejectionReason());

							if (pushNotificationResponse.getRejectionReason()
									.equals(BAD_DEVICE_TOKEN)
									|| pushNotificationResponse
											.getTokenInvalidationTimestamp() != null) {
								log.debug(
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
	 */
	private void sendAndroidPushNotification(final DialogOption dialogOption,
			final String unSplittedToken, String message,
			final boolean messageEncrypted,
			final int messagesSentSinceLastLogout, final int subMessage) {

		// Unencrypted messages only send out the first text
		if (!messageEncrypted) {
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

			conn.setRequestMethod("POST");
			conn.setRequestProperty("Authorization", "key=" + androidAuthKey);
			conn.setRequestProperty("Content-Type", "application/json");

			final JsonObject jsonObject = new JsonObject();

			jsonObject.addProperty(TO, token);

			if (messageEncrypted) {
				final JsonObject info = new JsonObject();
				info.addProperty(BLOB, message);
				info.addProperty(KEY, dialogOption.getData()
						.substring(dialogOption.getData().length() - 16));
				jsonObject.add(DATA, info);
			} else {
				final JsonObject notification = new JsonObject();
				if (message != null) {
					notification.addProperty(BODY, message);
					notification.addProperty(SOUND, DEFAULT);
				}
				jsonObject.add(NOTIFICATION, notification);
				final JsonObject info = new JsonObject();
				info.addProperty(BADGES, messagesSentSinceLastLogout);
				jsonObject.add(DATA, info);
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

				log.debug("Push notification accepted by FCM gateway: {}.",
						response);

				if (response.equals("error:NotRegistered")) {
					interventionExecutionManagerService
							.dialogOptionRemovePushNotificationToken(
									dialogOption.getId(),
									PushNotificationTypes.ANDROID, token);
				}
			} else {
				log.debug("Notification rejected by the FCM gateway: ", status);
			}
			conn.disconnect();
		} catch (final Exception e) {
			log.debug("Notification rejected by the FCM gateway: ",
					e.getMessage());
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
	}
}
