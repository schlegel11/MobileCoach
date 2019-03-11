package ch.ethz.mc.model.persistent.outdated.helpers;

/* ##LICENSE## */
import lombok.Getter;

import org.bson.types.ObjectId;
import org.jongo.marshall.jackson.oid.MongoId;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Used for several data update steps
 *
 * @author Andreas Filler
 */
public class MinimalStringObject {
	@MongoId
	@JsonProperty("_id")
	@Getter
	private ObjectId id;
}
