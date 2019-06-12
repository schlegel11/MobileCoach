package ch.ethz.mc.model.persistent;

/* ##LICENSE## */
import java.util.List;

import org.bson.types.ObjectId;

import ch.ethz.mc.conf.ImplementationConstants;
import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.persistent.types.DialogOptionTypes;
import ch.ethz.mc.services.internal.DeepstreamCommunicationService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;

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
	private static final long	serialVersionUID	= -1281955047718719223L;

	/**
	 * The {@link Participant} which provides this {@link DialogOption}
	 */
	@Getter
	@Setter
	@NonNull
	private ObjectId			participant;

	/**
	 * The {@link DialogOptionTypes} which describes this {@link DialogOption}
	 */
	@Getter
	@Setter
	@NonNull
	private DialogOptionTypes	type;

	/**
	 * The data required to reach the {@link Participant} using this
	 * {@link DialogOption}, e.g. a phone number or an email address
	 */
	@Getter
	@Setter
	@NonNull
	private String				data;

	/**
	 * The tokens required to reach the {@link Participant}'s push notification
	 * service using this {@link DialogOption}
	 */
	@Getter
	@Setter
	private String[]			pushNotificationTokens;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ch.ethz.mc.model.ModelObject#collectThisAndRelatedModelObjectsForExport
	 * (java.util.List)
	 */
	@Override
	protected void collectThisAndRelatedModelObjectsForExport(
			final List<ModelObject> exportList) {
		exportList.add(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.ethz.mc.model.ModelObject#performOnDelete()
	 */
	@Override
	public void performOnDelete() {
		switch (type) {
			case SMS:
			case SUPERVISOR_SMS:
			case EMAIL:
			case SUPERVISOR_EMAIL:
				break;
			case EXTERNAL_ID:
			case SUPERVISOR_EXTERNAL_ID:
				if (data.startsWith(
						ImplementationConstants.DIALOG_OPTION_IDENTIFIER_FOR_DEEPSTREAM)) {
					val deepstreamCommunicationService = DeepstreamCommunicationService
							.getInstance();

					if (deepstreamCommunicationService != null) {
						deepstreamCommunicationService
								.cleanupForParticipantOrSupervisor(
										data.substring(
												ImplementationConstants.DIALOG_OPTION_IDENTIFIER_FOR_DEEPSTREAM
														.length()));
					}
				}
				break;
		}
	}
}
