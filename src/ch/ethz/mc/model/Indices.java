package ch.ethz.mc.model;

/*
 * © 2013-2017 Center for Digital Health Interventions, Health-IS Lab a joint
 * initiative of the Institute of Technology Management at University of St.
 * Gallen and the Department of Management, Technology and Economics at ETH
 * Zurich
 * 
 * For details see README.md file in the root folder of this project.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.util.Hashtable;

import ch.ethz.mc.model.persistent.Author;
import ch.ethz.mc.model.persistent.DialogMessage;
import ch.ethz.mc.model.persistent.DialogOption;
import ch.ethz.mc.model.persistent.DialogStatus;
import ch.ethz.mc.model.persistent.FeedbackSlide;
import ch.ethz.mc.model.persistent.FeedbackSlideRule;
import ch.ethz.mc.model.persistent.IntermediateSurveyAndFeedbackParticipantShortURL;
import ch.ethz.mc.model.persistent.InterventionVariableWithValue;
import ch.ethz.mc.model.persistent.MediaObjectParticipantShortURL;
import ch.ethz.mc.model.persistent.MicroDialog;
import ch.ethz.mc.model.persistent.MicroDialogMessage;
import ch.ethz.mc.model.persistent.MicroDialogMessageRule;
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
	private static final String[]	authorIndices											= new String[] {
			"{'username':1}" };

	private static final String[]	dialogMessageIndices									= new String[] {
			"{'participant':1,'status':1}", "{'participant':1,'order':1}",
			"{'participant':1,'status':1,'shouldBeSentTimestamp':1}",
			"{'participant':1,'status':1,'isUnansweredAfterTimestamp':1}" };
	private static final String[]	dialogOptionIndices										= new String[] {
			"{'participant':1,'type':1}" };
	private static final String[]	dialogStatusIndices										= new String[] {
			"{'participant':1,'dataForMonitoringParticipationAvailable':1,'screeningSurveyPerformed':1,'monitoringPerformed':1}",
			"{'participant':1,'lastVisitedScreeningSurveySlideTimestamp':1}" };

	private static final String[]	participantIndices										= new String[] {
			"{'intervention':1}", "{'intervention':1,'monitoringActive':1}",
			"{'intervention':1,'group':1,'monitoringActive':1}" };

	private static final String[]	participantVariableWithValuesIndices					= new String[] {
			"{'participant':1}", "{'participant':1,'name':1}" };
	private static final String[]	interventionVariableWithValuesIndices					= new String[] {
			"{'intervention':1}" };

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
			"{'microDialog':1}" };
	private static final String[]	microDialogMessageRuleIndices							= new String[] {
			"{'belongingMicroDialogMessage':1}" };

	/**
	 * Creates a hashtable containing all indices for all {@link ModelObject}
	 *
	 * @return
	 */
	public static Hashtable<Class<? extends ModelObject>, String[]> getIndices() {
		val indices = new Hashtable<Class<? extends ModelObject>, String[]>();

		indices.put(Author.class, authorIndices);

		indices.put(DialogMessage.class, dialogMessageIndices);
		indices.put(DialogOption.class, dialogOptionIndices);
		indices.put(DialogStatus.class, dialogStatusIndices);

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
		indices.put(MicroDialogMessageRule.class,
				microDialogMessageRuleIndices);

		return indices;
	}
}
