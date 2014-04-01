package org.isgf.mhc.model.persistent;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;

import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.conf.Messages;
import org.isgf.mhc.model.ModelObject;
import org.isgf.mhc.model.Queries;
import org.isgf.mhc.model.ui.UIAuthor;
import org.isgf.mhc.model.ui.UIModelObject;

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
	 * @see org.isgf.mhc.model.ModelObject#toUIModelObject()
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
	 * @see org.isgf.mhc.model.ModelObject#performOnDelete()
	 */
	@Override
	public void performOnDelete() {
		val authorInterventionAccessesToDelete = ModelObject.find(
				AuthorInterventionAccess.class,
				Queries.AUTHOR_INTERVENTION_ACCESS__BY_AUTHOR, getId());

		ModelObject.delete(authorInterventionAccessesToDelete);
	}
}
