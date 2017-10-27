package ch.ethz.mobilecoach.interventions;

import org.bson.types.ObjectId;

import ch.ethz.mc.services.internal.DatabaseManagerService;
import ch.ethz.mc.services.internal.VariablesManagerService;

public abstract class AbstractIntervention {
	
	DatabaseManagerService db;
	VariablesManagerService vars;
	
	public AbstractIntervention(DatabaseManagerService db, VariablesManagerService vars){
		this.db = db;
		this.vars = vars;
	}
	
	public abstract Object getDashboard(ObjectId participantId);

}
