package org.isgf.mhc.ui.views.components.welcome;

import lombok.val;

import org.bson.types.ObjectId;
import org.isgf.mhc.conf.AdminMessageStrings;
import org.isgf.mhc.ui.views.components.interventions.MonitoringMessageEditComponentWithController;

/**
 * Extends the welcome tab component with a controller
 * 
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
public class WelcomeTabComponentWithController extends WelcomeTabComponent {
	public WelcomeTabComponentWithController() {
		super();

		// TODO DEBUG START
		val monitoringMessage = getInterventionAdministrationManagerService()
				.getMonitoringMessage(new ObjectId("530b6845c2e6f4b892e977df"));
		showModalModelObjectEditWindow(
				AdminMessageStrings.ABSTRACT_MODEL_OBJECT_EDIT_WINDOW__EDIT_MONITORING_MESSAGE,
				new MonitoringMessageEditComponentWithController(
						monitoringMessage, new ObjectId(
								"52db1576c2e6e76b91dde868")), null);
		// DEBUG END
	}
}
