package ch.ethz.mc.rest.services.v02;

/* ##LICENSE## */
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.apache.commons.lang3.StringUtils;

import com.twilio.twiml.MessagingResponse;

import ch.ethz.mc.model.memory.ReceivedMessage;
import ch.ethz.mc.model.persistent.types.DialogOptionTypes;
import ch.ethz.mc.services.RESTManagerService;
import ch.ethz.mc.tools.InternalDateTime;
import ch.ethz.mc.tools.StringHelpers;
import lombok.Getter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Service to retrieve SMS messages from TWILIO using REST
 *
 * @author Andreas Filler
 */
@Path("/v02/twilio")
@Log4j2
public class TWILIOMessageRetrievalServiceV02 extends AbstractServiceV02 {
	@Getter
	private static TWILIOMessageRetrievalServiceV02	instance	= null;

	private final List<ReceivedMessage>				receivedMessages;

	public TWILIOMessageRetrievalServiceV02(
			final RESTManagerService restManagerService) {
		super(restManagerService);

		instance = this;

		receivedMessages = new ArrayList<ReceivedMessage>();
	}

	@GET
	@Path("/receiveMessage")
	@Produces("application/xml")
	public String receiveMessage(@QueryParam("From") final String sender,
			@QueryParam("Body") final String message) {

		if (StringUtils.isBlank(sender) || StringUtils.isBlank(message)) {
			return null;
		}

		try {
			log.debug("Received message from TWILIO for {}", sender);
			val receivedMessage = new ReceivedMessage();
			receivedMessage.setType(DialogOptionTypes.SMS);
			receivedMessage.setTypeIntention(false);
			receivedMessage.setRelatedMessageIdBasedOnOrder(-1);
			receivedMessage.setSender(StringHelpers.cleanPhoneNumber(sender));
			receivedMessage
					.setReceivedTimestamp(InternalDateTime.currentTimeMillis());
			receivedMessage.setMessage(message);

			synchronized (receivedMessages) {
				receivedMessages.add(receivedMessage);
			}
		} catch (final Exception e) {
			throw e;
		}

		final MessagingResponse twiml = new MessagingResponse.Builder().build();
		return twiml.toXml();
	}

	/**
	 * Get all messages received by TWILIO since the last check
	 * 
	 * @param receivedMessages
	 */
	public void getReceivedMessages(
			final List<ReceivedMessage> receivedMessages) {
		synchronized (this.receivedMessages) {
			receivedMessages.addAll(this.receivedMessages);
			this.receivedMessages.clear();
		}
	}
}
