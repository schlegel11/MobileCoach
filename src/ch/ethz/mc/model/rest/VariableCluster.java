package ch.ethz.mc.model.rest;

/* ##LICENSE## */
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * Wrapper for the clustered values of a variable for REST
 *
 * @author Andreas Filler
 */
public class VariableCluster {
	@Getter
	@Setter
	private String						variable;
	@Getter
	private final List<ClusterValue>	clusteredValues	= new ArrayList<ClusterValue>();
}
