package ch.ethz.mc.services.internal;

import java.io.File;

import org.apache.commons.lang3.NotImplementedException;

import com.turo.pushy.apns.ApnsClient;
import com.turo.pushy.apns.ApnsClientBuilder;
import com.turo.pushy.apns.PushNotificationResponse;
import com.turo.pushy.apns.util.ApnsPayloadBuilder;
import com.turo.pushy.apns.util.SimpleApnsPushNotification;
import com.turo.pushy.apns.util.TokenUtil;

import ch.ethz.mc.model.persistent.DialogOption;
import ch.ethz.mc.model.persistent.types.PushNotificationTypes;
import ch.ethz.mc.services.InterventionExecutionManagerService;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
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

	private final boolean						iOSActive;
	private final boolean						androidActive;

	private final String						iOSAppIdentifier;

	final ApnsClient							apnsClient;

	private PushNotificationService(final boolean pushNotificationsIOSActive,
			final boolean pushNotificationsAndroidActive,
			final boolean pushNotificationsProductionMode,
			final String pushNotificationsIOSAppIdentifier,
			final String pushNotificationsIOSCertificateFile,
			final String pushNotificationsIOSCertificatePassword)
			throws Exception {
		iOSActive = pushNotificationsIOSActive;
		androidActive = pushNotificationsAndroidActive;

		iOSAppIdentifier = pushNotificationsIOSAppIdentifier;

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
	}

	public static PushNotificationService prepare(
			final boolean pushNotificationsIOSActive,
			final boolean pushNotificationsAndroidActive,
			final boolean pushNotificationsProductionMode,
			final String pushNotificationsIOSAppIdentifier,
			final String pushNotificationsIOSCertificateFile,
			final String pushNotificationsIOSCertificatePassword)
			throws Exception {
		log.info("Preparing service...");
		if (instance == null) {
			instance = new PushNotificationService(pushNotificationsIOSActive,
					pushNotificationsAndroidActive,
					pushNotificationsProductionMode,
					pushNotificationsIOSAppIdentifier,
					pushNotificationsIOSCertificateFile,
					pushNotificationsIOSCertificatePassword);
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

	public void asyncSendPushNotification(final DialogOption dialogOption,
			final String message) {
		for (val unSplittedToken : dialogOption.getPushNotificationTokens()) {
			if (iOSActive && unSplittedToken.startsWith(IOS_IDENTIFIER)) {
				val token = unSplittedToken.substring(IOS_IDENTIFIER.length());
				log.debug("Trying to send iOS push notification to token {}",
						token);

				final SimpleApnsPushNotification pushNotification;
				{
					final ApnsPayloadBuilder payloadBuilder = new ApnsPayloadBuilder();
					payloadBuilder.setAlertBody("Example!");

					final String payload = payloadBuilder
							.buildWithDefaultMaximumLength();

					pushNotification = new SimpleApnsPushNotification(
							TokenUtil.sanitizeTokenString(token),
							iOSAppIdentifier, payload);
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
											"Notification rejected by the APNs gateway: "
													+ pushNotificationResponse
															.getRejectionReason());

									if (pushNotificationResponse
											.getTokenInvalidationTimestamp() != null) {
										log.debug(
												"Token is invalid since {} and will be removed for the appropriate participant"
														+ pushNotificationResponse
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
			} else if (androidActive
					&& unSplittedToken.startsWith(ANDROID_IDENTIFIER)) {
				val token = unSplittedToken
						.substring(ANDROID_IDENTIFIER.length());
				log.debug(
						"Trying to send Android push notification to token {}",
						token);

				// TODO Android push implementation
				throw new NotImplementedException(
						"Android push notifications implementation missing");
			}
		}
	}

	/*
	 * Public methods
	 */

	/*
	 * Class methods
	 */
}
