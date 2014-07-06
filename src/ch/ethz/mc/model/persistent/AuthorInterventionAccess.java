package ch.ethz.mc.model.persistent;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import org.bson.types.ObjectId;

import ch.ethz.mc.model.ModelObject;

/**
 * {@link ModelObject} to represent an {@link AuthorInterventionAccess}
 * 
 * The {@link AuthorInterventionAccess} describes, which {@link Author} is
 * allowed to administrate a specific {@link Intervention}.
 * 
 * @author Andreas Filler
 */
@NoArgsConstructor
@AllArgsConstructor
public class AuthorInterventionAccess extends ModelObject {
	/**
	 * {@link Author} who is allowed to administrate {@link Intervention}
	 */
	@Getter
	@Setter
	@NonNull
	private ObjectId	author;

	/**
	 * {@link Intervention} that can be administrated by the {@link Author}
	 */
	@Getter
	@Setter
	@NonNull
	private ObjectId	intervention;
}