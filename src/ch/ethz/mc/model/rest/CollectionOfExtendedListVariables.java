package ch.ethz.mc.model.rest;

/* ##LICENSE## */
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * Wrapper for variables with values for REST (extended to describe if the
 * variable belongs to the participant)
 *
 * @author Andreas Filler
 */
public class CollectionOfExtendedListVariables {
	@Getter
	private final List<ExtendedListVariable>	variableListing	= new ArrayList<ExtendedListVariable>();
	@Getter
	@Setter
	private int									size;
}
