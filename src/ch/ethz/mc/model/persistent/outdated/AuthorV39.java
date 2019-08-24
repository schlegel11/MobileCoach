package ch.ethz.mc.model.persistent.outdated;

/* ##LICENSE## */
import ch.ethz.mc.model.ModelObject;
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
public class AuthorV39 extends ModelObject {
	private static final long	serialVersionUID	= 9068910637074662586L;

	/**
	 * Admin rights of {@link AuthorV39}
	 */
	@Getter
	@Setter
	private boolean				admin;

	/**
	 * Username of {@link AuthorV39} required to authenticate
	 */
	@Getter
	@Setter
	@NonNull
	private String				username;

	/**
	 * Hash of password of {@link AuthorV39} required to authenticate
	 */
	@Getter
	@Setter
	@NonNull
	private String				passwordHash;
}
