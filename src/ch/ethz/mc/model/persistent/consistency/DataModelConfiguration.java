package ch.ethz.mc.model.persistent.consistency;

/* ##LICENSE## */
import lombok.Getter;
import lombok.Setter;

import org.bson.types.ObjectId;
import org.jongo.marshall.jackson.oid.MongoId;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Stores the current version of the data model
 * 
 * @author Andreas Filler
 */
public class DataModelConfiguration {
	/**
	 * The id of the {@link DataModelConfiguration}
	 */
	@MongoId
	@JsonProperty("_id")
	@Getter
	private ObjectId	id;

	/**
	 * The current version of the database
	 */
	@Getter
	@Setter
	private int			version;

}
