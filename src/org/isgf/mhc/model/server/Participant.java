package org.isgf.mhc.model.server;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.bson.types.ObjectId;
import org.isgf.mhc.model.ModelObject;

/**
 * {@link ModelObject} to represent an {@link Participant}
 * 
 * A {@link Participant} is the person who participates in {@link Intervention}
 * s. To communicate with the {@link Participant} the system needs to know its
 * name. It furthermore stores if the {@link Participant} already performed the
 * screening survey and if the messaging is active for her/him.
 * 
 * @author Andreas Filler
 */
@NoArgsConstructor
@AllArgsConstructor
public class Participant extends ModelObject {
	/**
	 * The {@link Intervention} the {@link Participant} participates in
	 */
	@Getter
	@Setter
	private ObjectId	intervention;

	/**
	 * The timestamp when the {@link Participant} has been created
	 */
	@Getter
	@Setter
	private long		created;

	/**
	 * The nickname of the {@link Participant}
	 */
	@Getter
	@Setter
	private String		nickname;

	/**
	 * Stores if the {@link Participant} already performed a
	 * {@link ScreeningSurvey} of this {@link Intervention}
	 */
	@Getter
	@Setter
	private boolean		performedScreeningSurvey;

	/**
	 * Stores if the {@link Participant} is activated for the rule-based
	 * messaging; If a {@link Participant} should never participate in the
	 * {@link Intervention} based on this results from the
	 * {@link ScreeningSurvey} this should remain false
	 */
	@Getter
	@Setter
	private boolean		activeForMessaging;

	/**
	 * The organization the {@link Participant} belongs to; can e.g. be used for
	 * groups
	 */
	@Getter
	@Setter
	private String		organization;

	/**
	 * The organization unit the {@link Participant} belongs to; can e.g. be
	 * used for groups
	 */
	@Getter
	@Setter
	private String		organizationUnit;
}
