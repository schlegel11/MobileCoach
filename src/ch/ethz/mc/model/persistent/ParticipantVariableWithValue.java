package ch.ethz.mc.model.persistent;

/* ##LICENSE## */
import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.bson.types.ObjectId;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.Messages;
import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.memory.MemoryVariable;
import ch.ethz.mc.model.persistent.concepts.AbstractVariableWithValue;
import ch.ethz.mc.model.ui.UIParticipantVariableWithParticipant;
import ch.ethz.mc.services.internal.FileStorageManagerService.FILE_STORES;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * {@link ModelObject} to represent an {@link ParticipantVariableWithValue}
 *
 * Participant variables belong to the referenced {@link Participant} and
 * consist of a name, order, timestamp and value. Their type is implicitly
 * retrieved from the appropriate intervention variable.
 *
 * @author Andreas Filler
 */
@NoArgsConstructor
@Log4j2
public class ParticipantVariableWithValue extends AbstractVariableWithValue {
	private static final long serialVersionUID = 2109551204186856678L;

	/**
	 * Default constructor
	 */
	public ParticipantVariableWithValue(final ObjectId participant,
			final long timestamp, final String name, final String value) {
		super(name, value);

		this.participant = participant;
		this.timestamp = timestamp;
		describesMediaUpload = false;
	}

	@NoArgsConstructor
	@AllArgsConstructor
	public static class FormerVariableValue implements Serializable {
		private static final long	serialVersionUID	= -8122702756200214420L;

		@Getter
		@Setter
		private long				timestamp;

		@Getter
		@Setter
		@NonNull
		private String				value;

		@Getter
		@Setter
		private boolean				describesMediaUpload;
	}

	/**
	 * {@link Participant} to which this variable and its value belong to
	 */
	@Getter
	@Setter
	@NonNull
	private ObjectId					participant;

	/**
	 * The moment in time when the variable was created
	 */
	@Getter
	@Setter
	private long						timestamp;

	@Getter
	@Setter
	private boolean						describesMediaUpload;

	@Getter
	@Setter
	private List<FormerVariableValue>	formerVariableValues	= new LinkedList<FormerVariableValue>();

	/**
	 * Remembers former variable value
	 */
	public void rememberFormerValue(final int maxVariableHistory) {
		if (maxVariableHistory != 0) {
			val formerValue = new FormerVariableValue(getTimestamp(),
					getValue(), isDescribesMediaUpload());
			formerVariableValues.add(formerValue);

			if (maxVariableHistory > -1
					&& formerVariableValues.size() > maxVariableHistory) {
				formerVariableValues.remove(0);
			}
		}
	}

	/**
	 * Converts current instance to {@link MemoryVariable}
	 * 
	 * @return
	 */
	public MemoryVariable toMemoryVariable() {
		val memoryVariable = new MemoryVariable();
		memoryVariable.setName(getName());
		memoryVariable.setValue(getValue());
		return memoryVariable;
	}

	/**
	 * Creates a {@link UIParticipantVariableWithParticipant} with the belonging
	 * {@link Participant}
	 *
	 * @param participantName
	 * @return
	 */
	public UIParticipantVariableWithParticipant toUIVariableWithParticipant(
			final String participantId, final String participantName,
			final String group, final String organization,
			final String organizationUnit) {
		final UIParticipantVariableWithParticipant variable;

		variable = new UIParticipantVariableWithParticipant(participantId,
				participantName,
				group == null ? Messages.getAdminString(
						AdminMessageStrings.UI_MODEL__NOT_SET) : group,
				organization, organizationUnit, getName(), getValue(),
				new Date(timestamp));

		variable.setRelatedModelObject(this);

		return variable;
	}

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
	@JsonIgnore
	protected void performOnDelete() {
		if (describesMediaUpload) {
			log.debug("Deleting file with reference {}", getValue());
			try {
				getFileStorageManagerService().deleteFile(getValue(),
						FILE_STORES.MEDIA_UPLOAD);
			} catch (final Exception e) {
				log.warn(
						"File belonging to file reference {} could not be deleted: {}",
						getValue(), e.getMessage());
			}

			for (val formerValue : formerVariableValues) {
				if (formerValue.isDescribesMediaUpload()) {
					log.debug("Deleting file with reference {}",
							formerValue.getValue());
					try {
						getFileStorageManagerService().deleteFile(
								formerValue.getValue(),
								FILE_STORES.MEDIA_UPLOAD);
					} catch (final Exception e) {
						log.warn(
								"File belonging to file reference {} could not be deleted: {}",
								formerValue.getValue(), e.getMessage());
					}
				}
			}
		}

		super.performOnDelete();
	}
}
