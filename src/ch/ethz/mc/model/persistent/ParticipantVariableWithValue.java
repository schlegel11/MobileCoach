package ch.ethz.mc.model.persistent;

/*
 * © 2013-2017 Center for Digital Health Interventions, Health-IS Lab a joint
 * initiative of the Institute of Technology Management at University of St.
 * Gallen and the Department of Management, Technology and Economics at ETH
 * Zurich
 * 
 * For details see README.md file in the root folder of this project.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;

import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.Messages;
import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.persistent.concepts.AbstractVariableWithValue;
import ch.ethz.mc.model.ui.UIParticipantVariableWithParticipant;
import ch.ethz.mc.services.internal.FileStorageManagerService.FILE_STORES;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * {@link ModelObject} to represent an {@link ParticipantVariableWithValue}
 *
 * Participant variables belong to the referenced {@link Participant} and
 * consist of a
 * name, order, timestamp and value. Their type is implicitly retrieved from the
 * appropriate
 * intervention variable.
 *
 * @author Andreas Filler
 */
@NoArgsConstructor
@Log4j2
public class ParticipantVariableWithValue extends AbstractVariableWithValue {
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

	/**
	 * {@link Participant} to which this variable and its value belong to
	 */
	@Getter
	@Setter
	@NonNull
	private ObjectId	participant;

	/**
	 * The moment in time when the variable was created
	 */
	@Getter
	@Setter
	private long		timestamp;

	@Getter
	@Setter
	private boolean		describesMediaUpload;

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
				group == null ? Messages
						.getAdminString(AdminMessageStrings.UI_MODEL__NOT_SET)
						: group, organization, organizationUnit, getName(),
				getValue(), new Date(timestamp));

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
		}

		super.performOnDelete();
	}
}
