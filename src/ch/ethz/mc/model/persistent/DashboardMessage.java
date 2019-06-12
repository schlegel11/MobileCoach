package ch.ethz.mc.model.persistent;

/* ##LICENSE## */
import org.bson.types.ObjectId;

import ch.ethz.mc.model.ModelObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

/**
 * {@link ModelObject} to represent an {@link DashboardMessage}
 *
 * @author Andreas Filler
 */
@NoArgsConstructor
@AllArgsConstructor
public class DashboardMessage extends ModelObject {
	private static final long	serialVersionUID	= -7374360119955812479L;

	/**
	 * The recipient of the {@link DashboardMessage}
	 */
	@Getter
	@Setter
	@NonNull
	private ObjectId			participant;

	/**
	 * The original id of the message on any client (if required)
	 */
	@Getter
	@Setter
	@NonNull
	private String				clientId;

	/**
	 * The role the message has sent
	 */
	@Getter
	@Setter
	@NonNull
	private String				role;

	/**
	 * The position of the {@link DashboardMessage} compared to all other
	 * {@link DashboardMessage}s of a specific {@link Participant}
	 */
	@Getter
	@Setter
	private int					order;

	/**
	 * The message sent to the {@link Participant}
	 */
	@Getter
	@Setter
	@NonNull
	private String				message;

	/**
	 * Timestamp when the {@link DashboardMessage} has been sent or received
	 */
	@Getter
	@Setter
	private long				timestamp;
}
