package ch.ethz.mc.ui.views.components.integrations;

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
import lombok.val;
import lombok.extern.log4j.Log4j2;

import org.bson.types.ObjectId;

import ch.ethz.mc.MC;
import ch.ethz.mobilecoach.services.MattermostManagementService;
import ch.ethz.mobilecoach.services.MattermostManagementService.TeamConfiguration;
import ch.ethz.mobilecoach.services.MattermostMessagingService;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;

/**
 * Extends the access control tab component with a controller
 * 
 * @author Dominik Rüegger
 */
@SuppressWarnings("serial")
@Log4j2
public class IntegrationsTabComponentWithController extends
		IntegrationsTabComponent {

	public IntegrationsTabComponentWithController() {
		super();
		
		refresh();
	}
	
	
	public void refresh() {
		MattermostManagementService mmManagementService = MC.getInstance().getMattermostManagementService();
		MattermostMessagingService mmMessagingService = MC.getInstance().getMattermostMessagingService();
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("\nMattermost: "+ mmManagementService.api_url.replace("api/v3/", "") + "\n");
		
		sb.append("\nTeams:\n");
		for (TeamConfiguration c: mmManagementService.getTeamConfigurations()){
			sb.append(c.toString() + ", " + mmManagementService.getTeamName(c.teamId) + "\n"); 
		}
		
		if (mmManagementService.getObserverUserId() != null){
			sb.append("\nObserver:");
			sb.append("\nUsername: " + mmManagementService.getObserverUserName());
			sb.append("\nPassword: " + mmManagementService.getObserverUserPassword());
		}
		
		this.getTextArea().setValue(sb.toString());
	}

}
