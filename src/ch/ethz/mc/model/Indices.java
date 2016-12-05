package ch.ethz.mc.model;

import java.util.ArrayList;
import java.util.Collection;
/*
 * Copyright (C) 2013-2016 MobileCoach Team at the Health-IS Lab
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
import java.util.List;

import ch.ethz.mc.model.persistent.AppToken;
import ch.ethz.mc.model.persistent.Author;
import ch.ethz.mc.model.persistent.DialogMessage;
import ch.ethz.mc.model.persistent.DialogOption;
import ch.ethz.mc.model.persistent.DialogStatus;
import ch.ethz.mc.model.persistent.FeedbackSlide;
import ch.ethz.mc.model.persistent.FeedbackSlideRule;
import ch.ethz.mc.model.persistent.IntermediateSurveyAndFeedbackParticipantShortURL;
import ch.ethz.mc.model.persistent.InterventionVariableWithValue;
import ch.ethz.mc.model.persistent.MediaObjectParticipantShortURL;
import ch.ethz.mc.model.persistent.MonitoringReplyRule;
import ch.ethz.mc.model.persistent.MonitoringRule;
import ch.ethz.mc.model.persistent.OneTimeToken;
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
	private static final String[]	authorIndices											= new String[] { "{'username':1}" };
	private static final String[]	dialogMessageIndices									= new String[] { "{'participant':1,'status':1,'shouldBeSentTimestamp':1}" };
	private static final String[]	dialogOptionIndices										= new String[] { "{'participant':1,'type':1}" };
	private static final String[]	dialogStatusIndices										= new String[] {
		"{'participant':1,'dataForMonitoringParticipationAvailable':1,'screeningSurveyPerformed':1,'monitoringPerformed':1}",
	"{'participant':1,'lastVisitedScreeningSurveySlideTimestamp':1}"				};
	private static final String[]	participantIndices										= new String[] {
		"{'intervention':1}", "{'intervention':1,'monitoringActive':1}",
	"{'intervention':1,'group':1,'monitoringActive':1}"							};

	private static final String[]	participantVariableWithValuesIndices					= new String[] {
		"{'participant':1}", "{'participant':1,'name':1}"								};
	private static final String[]	interventionVariableWithValuesIndices					= new String[] { "{'intervention':1}" };

	private static final String[]	monitoringRuleIndices									= new String[] { "{'intervention':1,'isSubRuleOfMonitoringRule':1}" };
	private static final String[]	monitoringReplyRuleIndices								= new String[] { "{'isSubRuleOfMonitoringRule':1}" };

	private static final String[]	screeningSurveySlideIndices								= new String[] { "{'screeningSurvey':1}" };
	private static final String[]	screeningSurveySlideRuleIndices							= new String[] { "{'belongingScreeningSurveySlide':1}" };

	private static final String[]	feedbackSlideIndices									= new String[] { "{'feedback':1}" };
	private static final String[]	feedbackSlideRuleIndices								= new String[] { "{'belongingFeedbackSlide':1}" };

	private static final String[]	mediaObjectParticipantShortURLIndices					= new String[] {
		"{'shortId':1}", "{'dialogMessage':1,'mediaObject':1}"							};
	private static final String[]	intermediateSurveyAndFeedbackParticipantShortURLIndices	= new String[] {
		"{'shortId':1}", "{'participant':1,'survey':1}",
	"{'participant':1,'feedback':1}"												};
	private static final String[][]	appTokenIndices											= new String[][] {{"{'token':1}", "{unique: true}"}};
	private static final String[][]	oneTimeTokenIndices										= new String[][] {{"{'token':1}", "{unique: true}"},{"{'createdAt':1}","{expireAfterSeconds:172800}"}};

	/**
	 * Creates a hashtable containing all indices for all {@link ModelObject}
	 *
	 * @return
	 */
	public static Hashtable<Class<? extends ModelObject>, Collection<IndexSpec>> getIndices() {
		val indices = new Hashtable<Class<? extends ModelObject>, Collection<IndexSpec>>();

		indices.put(Author.class, convert(authorIndices));
		indices.put(DialogMessage.class, convert(dialogMessageIndices));
		indices.put(DialogOption.class, convert(dialogOptionIndices));
		indices.put(DialogStatus.class, convert(dialogStatusIndices));
		indices.put(Participant.class, convert(participantIndices));

		indices.put(ParticipantVariableWithValue.class,
				convert(participantVariableWithValuesIndices));
		indices.put(InterventionVariableWithValue.class,
				convert(interventionVariableWithValuesIndices));

		indices.put(MonitoringRule.class, convert(monitoringRuleIndices));
		indices.put(MonitoringReplyRule.class, convert(monitoringReplyRuleIndices));

		indices.put(ScreeningSurveySlide.class, convert(screeningSurveySlideIndices));
		indices.put(ScreeningSurveySlideRule.class,
				convert(screeningSurveySlideRuleIndices));

		indices.put(FeedbackSlide.class, convert(feedbackSlideIndices));
		indices.put(FeedbackSlideRule.class, convert(feedbackSlideRuleIndices));

		indices.put(MediaObjectParticipantShortURL.class,
				convert(mediaObjectParticipantShortURLIndices));
		indices.put(IntermediateSurveyAndFeedbackParticipantShortURL.class,
				convert(intermediateSurveyAndFeedbackParticipantShortURLIndices));
		indices.put(AppToken.class,convert(appTokenIndices));
		indices.put(OneTimeToken.class, convert(oneTimeTokenIndices));

		return indices;
	}
	
	private static Collection<IndexSpec> convert(String[] indices) {
		List<IndexSpec> specs = new ArrayList<>();
		for(String index : indices) {
			specs.add(new IndexSpec(index));
		}
		return specs;
	}
	
	private static Collection<IndexSpec> convert(String[][] indicesWithOptions) {
		List<IndexSpec> specs = new ArrayList<>();
		for(String[] index : indicesWithOptions) {
			specs.add(IndexSpec.create(index));
		}
		return specs;
	}
}
