package org.isgf.mhc.model.server;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.bson.types.ObjectId;
import org.isgf.mhc.model.ModelObject;
import org.isgf.mhc.model.types.DialogOptionTypes;

/**
 * {@link ModelObject} to represent an {@link DialogOption}
 * 
 * A {@link DialogOption} describes by which options a {@link Participant} can
 * be contacted. Several {@link DialogOption}s can exist for one
 * {@link Participant}.
 * 
 * @author Andreas Filler
 */
@NoArgsConstructor
@AllArgsConstructor
public class DialogOption extends ModelObject {
	/**
	 * The {@link Participant} which provides this {@link DialogOption}
	 */
	@Getter
	@Setter
	private ObjectId			participant;

	/**
	 * The {@link DialogOptionTypes} which describes this {@link DialogOption}
	 */
	@Getter
	@Setter
	private DialogOptionTypes	type;

	/**
	 * The data required to reach the {@link Participant} using this
	 * {@link DialogOption}, e.g. a phone number or an email address
	 */
	@Getter
	@Setter
	private String				data;
}
