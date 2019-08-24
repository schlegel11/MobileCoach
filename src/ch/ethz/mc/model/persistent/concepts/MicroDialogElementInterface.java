package ch.ethz.mc.model.persistent.concepts;

/* ##LICENSE## */
import org.bson.types.ObjectId;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.persistent.MicroDialog;

/**
 * {@link ModelObject} to represent an {@link MicroDialogElementInterface}
 *
 * {@link MicroDialog}s are build as a list of
 * {@link MicroDialogElementInterface}s
 *
 * @author Andreas Filler
 */
public interface MicroDialogElementInterface {
	/**
	 * The {@link MicroDialog} this {@link MicroDialogElementInterface} belongs
	 * to
	 */
	public ObjectId getMicroDialog();

	/**
	 * The position of the {@link MicroDialogElementInterface} compared to all
	 * other {@link MicroDialogElementInterface}s in the same
	 * {@link MicroDialog}
	 */
	public int getOrder();

	public void setOrder(int order);

	@JsonIgnore
	public String toTable();
}
