package org.isgf.mhc.model.server;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import org.isgf.mhc.model.ModelObject;
import org.jongo.Oid;

/**
 * {@link ModelObject} to represent an {@link DialogMessage}
 * 
 * @author Andreas Filler <andreas@filler.name>
 */
@AllArgsConstructor
public class DialogMessage extends ModelObject {
	/**
	 * The recipient of the {@link DialogMessage}
	 */
	@Getter
	@Setter
	private Oid		participant;

	/**
	 * The {@link Intervention} as part of which the {@link DialogMessage} has
	 * been sent
	 */
	@Getter
	@Setter
	private Oid		intervention;

	/**
	 * The {@link InterventionMessage} sent to the {@link Participant}
	 */
	@Getter
	@Setter
	private Oid		message;

	/**
	 * Timestamp when the {@link DialogMessage} has been sent
	 */
	@Getter
	@Setter
	private long	messageSent;

	/**
	 * The response retrieved from the {@link Participant}
	 */
	@Getter
	@Setter
	private String	response;

	/**
	 * Timestamp when the respone has been retrieved
	 */
	@Getter
	@Setter
	private long	responseRetrieved;

	/**
	 * Marker showing if the {@link DialogMessage} has been sent manually by an
	 * {@link Author}
	 */
	@Getter
	@Setter
	private boolean	manuallySent;

	/**
	 * Marker showing if a response to a {@link DialogMessage} can not be
	 * automatically processed by the system
	 */
	@Getter
	@Setter
	private boolean	notAutomaticallyProcessable;

}
