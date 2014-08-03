package ch.ethz.mc.model.persistent.consistency;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.bson.types.ObjectId;
import org.jongo.marshall.jackson.oid.Id;

import ch.ethz.mc.model.ModelObject;

/**
 * Stores the current version of the data model
 * 
 * @author Andreas Filler
 */
@ToString
public class DataModelConfiguration {
	/**
	 * The id of the {@link ModelObject}
	 */
	@Id
	@Getter
	private ObjectId	id;

	/**
	 * The current version of the database
	 */
	@Getter
	@Setter
	private int			version;

}
