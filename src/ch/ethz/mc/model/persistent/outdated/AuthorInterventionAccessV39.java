package ch.ethz.mc.model.persistent.outdated;

/* ##LICENSE## */
import org.bson.types.ObjectId;

import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.persistent.Intervention;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

/**
 * CAUTION: Will only be used for conversion from data model 39 to 40
 *
 * @author Andreas Filler
 */

@NoArgsConstructor
@AllArgsConstructor
public class AuthorInterventionAccessV39 extends ModelObject {
	private static final long	serialVersionUID	= -2686891854353434099L;

	/**
	 * {@link AuthorV39} who is allowed to administrate {@link Intervention}
	 */
	@Getter
	@Setter
	@NonNull
	private ObjectId			author;

	/**
	 * {@link Intervention} that can be administrated by the {@link AuthorV39}
	 */
	@Getter
	@Setter
	@NonNull
	private ObjectId			intervention;
}
