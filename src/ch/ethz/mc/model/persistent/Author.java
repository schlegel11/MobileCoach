package ch.ethz.mc.model.persistent;

/*
 * Copyright (C) 2013-2017 MobileCoach Team at the Health-IS Lab at the Health-IS Lab
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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;
import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.conf.Messages;
import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.Queries;
import ch.ethz.mc.model.ui.UIAuthor;
import ch.ethz.mc.model.ui.UIModelObject;

/**
 * {@link ModelObject} to represent an {@link Author}
 * 
 * Authors are the backend users of the system. They can be normal authors or
 * administrators.
 * 
 * @author Andreas Filler
 */
@NoArgsConstructor
@AllArgsConstructor
public class Author extends ModelObject {
	/**
	 * Admin rights of {@link Author}
	 */
	@Getter
	@Setter
	private boolean	admin;

	/**
	 * Username of {@link Author} required to authenticate
	 */
	@Getter
	@Setter
	@NonNull
	private String	username;

	/**
	 * Hash of password of {@link Author} required to authenticate
	 */
	@Getter
	@Setter
	@NonNull
	private String	passwordHash;

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.ethz.mc.model.ModelObject#toUIModelObject()
	 */
	@Override
	public UIModelObject toUIModelObject() {
		final val author = new UIAuthor(
				username,
				admin ? Messages
						.getAdminString(AdminMessageStrings.UI_MODEL__ADMINISTRATOR)
						: Messages
								.getAdminString(AdminMessageStrings.UI_MODEL__AUTHOR));

		author.setRelatedModelObject(this);

		return author;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.ethz.mc.model.ModelObject#performOnDelete()
	 */
	@Override
	public void performOnDelete() {
		val authorInterventionAccessesToDelete = ModelObject.find(
				AuthorInterventionAccess.class,
				Queries.AUTHOR_INTERVENTION_ACCESS__BY_AUTHOR, getId());

		ModelObject.delete(authorInterventionAccessesToDelete);
	}
}
