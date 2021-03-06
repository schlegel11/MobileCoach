package ch.ethz.mc.model;

/* ##LICENSE## */
import java.util.Hashtable;

import ch.ethz.mc.model.persistent.BackendUser;
import ch.ethz.mc.model.persistent.DashboardMessage;
import ch.ethz.mc.model.persistent.DialogMessage;
import ch.ethz.mc.model.persistent.DialogOption;
import ch.ethz.mc.model.persistent.DialogStatus;
import ch.ethz.mc.model.persistent.FeedbackSlide;
import ch.ethz.mc.model.persistent.FeedbackSlideRule;
import ch.ethz.mc.model.persistent.IntermediateSurveyAndFeedbackParticipantShortURL;
import ch.ethz.mc.model.persistent.InterventionVariableWithValue;
import ch.ethz.mc.model.persistent.MediaObjectParticipantShortURL;
import ch.ethz.mc.model.persistent.MicroDialog;
import ch.ethz.mc.model.persistent.MicroDialogDecisionPoint;
import ch.ethz.mc.model.persistent.MicroDialogMessage;
import ch.ethz.mc.model.persistent.MicroDialogMessageRule;
import ch.ethz.mc.model.persistent.MicroDialogRule;
import ch.ethz.mc.model.persistent.MonitoringMessage;
import ch.ethz.mc.model.persistent.MonitoringMessageGroup;
import ch.ethz.mc.model.persistent.MonitoringMessageRule;
import ch.ethz.mc.model.persistent.MonitoringReplyRule;
import ch.ethz.mc.model.persistent.MonitoringRule;
import ch.ethz.mc.model.persistent.Participant;
import ch.ethz.mc.model.persistent.ParticipantVariableWithValue;
import ch.ethz.mc.model.persistent.ScreeningSurveySlide;
import ch.ethz.mc.model.persistent.ScreeningSurveySlideRule;
import lombok.val;

/**
 * Describes all indices that shall be created in the database
 *
 * @author Andreas Filler
 */
public class Indices {
	private static final String[]	backendUserIndices										= new String[] {
			"{'username':1}" };

	private static final String[]	dialogMessageIndices									= new String[] {
			"{'participant':1,'status':1}", "{'participant':1,'order':1}",
			"{'participant':1,'status':1,'shouldBeSentTimestamp':1}",
			"{'participant':1,'clientId':1}",
			"{'participant':1,'relatedMicroDialogForActivation':1}" };
	private static final String[]	dialogOptionIndices										= new String[] {
			"{'participant':1,'type':1}", "{'type':1,'data':1}" };
	private static final String[]	dialogStatusIndices										= new String[] {
			"{'participant':1}",
			"{'participant':1,'lastVisitedScreeningSurveySlideTimestamp':1}" };

	private static final String[]	dashboardMessageIndices									= new String[] {
			"{'participant':1,'order':1}" };

	private static final String[]	participantIndices										= new String[] {
			"{'intervention':1}", "{'intervention':1,'monitoringActive':1}",
			"{'intervention':1,'group':1,'monitoringActive':1}" };

	private static final String[]	participantVariableWithValuesIndices					= new String[] {
			"{'participant':1}", "{'participant':1,'name':1}" };
	private static final String[]	interventionVariableWithValuesIndices					= new String[] {
			"{'intervention':1}", "{'intervention':1,'name':1}" };

	private static final String[]	monitoringRuleIndices									= new String[] {
			"{'intervention':1,'isSubRuleOfMonitoringRule':1}",
			"{'intervention':1,'type':1}" };
	private static final String[]	monitoringReplyRuleIndices								= new String[] {
			"{'isSubRuleOfMonitoringRule':1}" };

	private static final String[]	screeningSurveySlideIndices								= new String[] {
			"{'screeningSurvey':1}" };
	private static final String[]	screeningSurveySlideRuleIndices							= new String[] {
			"{'belongingScreeningSurveySlide':1}" };

	private static final String[]	feedbackSlideIndices									= new String[] {
			"{'feedback':1}" };
	private static final String[]	feedbackSlideRuleIndices								= new String[] {
			"{'belongingFeedbackSlide':1}" };

	private static final String[]	mediaObjectParticipantShortURLIndices					= new String[] {
			"{'shortId':1}", "{'dialogMessage':1,'mediaObject':1}" };
	private static final String[]	intermediateSurveyAndFeedbackParticipantShortURLIndices	= new String[] {
			"{'shortId':1}", "{'participant':1,'survey':1}",
			"{'participant':1,'feedback':1}" };

	private static final String[]	monitoringMessageGroupIndices							= new String[] {
			"{'intervention':1}" };
	private static final String[]	monitoringMessageIndices								= new String[] {
			"{'monitoringMessageGroup':1}" };
	private static final String[]	monitoringMessageRuleIndices							= new String[] {
			"{'belongingMonitoringMessage':1}" };

	private static final String[]	microDialogIndices										= new String[] {
			"{'intervention':1}" };
	private static final String[]	microDialogMessageIndices								= new String[] {
			"{'microDialog':1}", "{'microDialog':1,'order':1}" };
	private static final String[]	microDialogDecisionPointIndices							= new String[] {
			"{'microDialog':1}", "{'microDialog':1,'order':1}" };
	private static final String[]	microDialogMessageRuleIndices							= new String[] {
			"{'belongingMicroDialogMessage':1}" };
	private static final String[]	microDialogRuleIndices									= new String[] {
			"{'microDialogDecisionPoint':1,'isSubRuleOfMonitoringRule':1}" };

	/**
	 * Creates a hashtable containing all indices for all {@link ModelObject}
	 *
	 * @return
	 */
	public static Hashtable<Class<? extends ModelObject>, String[]> getIndices() {
		val indices = new Hashtable<Class<? extends ModelObject>, String[]>();

		indices.put(BackendUser.class, backendUserIndices);

		indices.put(DialogMessage.class, dialogMessageIndices);
		indices.put(DialogOption.class, dialogOptionIndices);
		indices.put(DialogStatus.class, dialogStatusIndices);

		indices.put(DashboardMessage.class, dashboardMessageIndices);

		indices.put(Participant.class, participantIndices);

		indices.put(ParticipantVariableWithValue.class,
				participantVariableWithValuesIndices);
		indices.put(InterventionVariableWithValue.class,
				interventionVariableWithValuesIndices);

		indices.put(MonitoringRule.class, monitoringRuleIndices);
		indices.put(MonitoringReplyRule.class, monitoringReplyRuleIndices);

		indices.put(ScreeningSurveySlide.class, screeningSurveySlideIndices);
		indices.put(ScreeningSurveySlideRule.class,
				screeningSurveySlideRuleIndices);

		indices.put(FeedbackSlide.class, feedbackSlideIndices);
		indices.put(FeedbackSlideRule.class, feedbackSlideRuleIndices);

		indices.put(MediaObjectParticipantShortURL.class,
				mediaObjectParticipantShortURLIndices);
		indices.put(IntermediateSurveyAndFeedbackParticipantShortURL.class,
				intermediateSurveyAndFeedbackParticipantShortURLIndices);

		indices.put(MonitoringMessageGroup.class,
				monitoringMessageGroupIndices);
		indices.put(MonitoringMessage.class, monitoringMessageIndices);
		indices.put(MonitoringMessageRule.class, monitoringMessageRuleIndices);

		indices.put(MicroDialog.class, microDialogIndices);
		indices.put(MicroDialogMessage.class, microDialogMessageIndices);
		indices.put(MicroDialogDecisionPoint.class,
				microDialogDecisionPointIndices);
		indices.put(MicroDialogMessageRule.class,
				microDialogMessageRuleIndices);
		indices.put(MicroDialogRule.class, microDialogRuleIndices);

		return indices;
	}
}
