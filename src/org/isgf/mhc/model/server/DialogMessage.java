package org.isgf.mhc.model.server;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import org.bson.types.ObjectId;
import org.isgf.mhc.model.ModelObject;

/**
 * {@link ModelObject} to represent an {@link DialogMessage}
 * 
 * @author Andreas Filler
 */
@NoArgsConstructor
@AllArgsConstructor
public class DialogMessage extends ModelObject {
	/**
	 * The recipient of the {@link DialogMessage}
	 */
	@Getter
	@Setter
	@NonNull
	private ObjectId	participant;

	/**
	 * The {@link Intervention} as part of which the {@link DialogMessage} has
	 * been sent
	 */
	@Getter
	@Setter
	@NonNull
	private ObjectId	intervention;

	/**
	 * The {@link InterventionMessage} sent to the {@link Participant}
	 */
	@Getter
	@Setter
	@NonNull
	private ObjectId	message;

	/**
	 * Timestamp when the {@link DialogMessage} has been sent
	 */
	@Getter
	@Setter
	private long		messageSent;

	/**
	 * The response retrieved from the {@link Participant}
	 */
	@Getter
	@Setter
	@NonNull
	private String		response;

	/**
	 * Timestamp when the respone has been retrieved
	 */
	@Getter
	@Setter
	private long		responseRetrieved;

	/**
	 * Marker showing if the {@link DialogMessage} has been sent manually by an
	 * {@link Author}
	 */
	@Getter
	@Setter
	private boolean		manuallySent;

	/**
	 * The seconds a {@link Participant} viewed the presented media content (if
	 * integrated in the {@link InterventionMessage})
	 */
	@Getter
	@Setter
	private int			secondsMediaContentViewed;

	/**
	 * Marker showing if a response to a {@link DialogMessage} can not be
	 * automatically processed by the system
	 */
	@Getter
	@Setter
	private boolean		notAutomaticallyProcessable;
}
