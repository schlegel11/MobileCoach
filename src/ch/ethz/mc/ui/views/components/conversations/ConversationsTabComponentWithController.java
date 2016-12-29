package ch.ethz.mc.ui.views.components.conversations;

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

import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.model.ui.UIConversation;
import ch.ethz.mc.model.ui.UIParticipant;
import ch.ethz.mobilecoach.model.persistent.ChatEnginePersistentState;

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
public class ConversationsTabComponentWithController extends
		ConversationsTabComponent {

	private UIConversation								selectedUIConversation			= null;
	private BeanItem<UIConversation>					selectedUIConversationBeanItem	= null;

	private final BeanContainer<ObjectId, UIConversation>	beanContainer;

	public ConversationsTabComponentWithController() {
		super();

		// table options
		val conversationsEditComponent = getConversationsEditComponent();
		val conversationsTable = conversationsEditComponent.getConversationsTable();

		// table content
		beanContainer = createBeanContainerForModelObjects(UIConversation.class,
				getRichConversationService().getAllConversations());

		conversationsTable.setContainerDataSource(beanContainer);
		conversationsTable.setSortContainerPropertyId(UIConversation.getSortColumn());
		conversationsTable.setVisibleColumns(UIConversation.getVisibleColumns());
		conversationsTable.setColumnHeaders(UIConversation.getColumnHeaders());

		// handle selection change
		conversationsTable.addValueChangeListener(new ValueChangeListener() {

			@Override
			public void valueChange(final ValueChangeEvent event) {
				val objectId = conversationsTable.getValue();
				if (objectId == null) {
					conversationsEditComponent.setNothingSelected();
					selectedUIConversation = null;
					selectedUIConversationBeanItem = null;
				} else {
					selectedUIConversation = getUIModelObjectFromTableByObjectId(
							conversationsTable, UIConversation.class, objectId);
					selectedUIConversationBeanItem = getBeanItemFromTableByObjectId(
							conversationsTable, UIConversation.class, objectId);
					conversationsEditComponent.setSomethingSelected();
				}
			}
		});

		// handle buttons
		val buttonClickListener = new ButtonClickListener();
		conversationsEditComponent.getDeleteButton().addClickListener(
				buttonClickListener);
		conversationsEditComponent.getRefreshButton().addClickListener(
				buttonClickListener);
	}

	private class ButtonClickListener implements Button.ClickListener {

		@Override
		public void buttonClick(final ClickEvent event) {
			val conversationsEditComponent = getConversationsEditComponent();

			if (event.getButton() == conversationsEditComponent.getRefreshButton()) {
				refresh();
			} else if (event.getButton() == conversationsEditComponent.getDeleteButton()) {
				deleteConversation();
			}
		}
	}
	
	public void refresh() {
		refreshBeanContainer(beanContainer, UIConversation.class, getRichConversationService().getAllConversations());
	}



	public void deleteConversation() {
		log.debug("Delete conversation");
		showConfirmationWindow(new ExtendableButtonClickListener() {

			@Override
			public void buttonClick(final ClickEvent event) {
				try {
					val selectedConversation = selectedUIConversation.getRelatedModelObject(ChatEnginePersistentState.class);
					getRichConversationService().deleteChatEnginePersistentState(selectedConversation.getId());

				} catch (final Exception e) {
					closeWindow();
					handleException(e);
					return;
				}

				// Adapt UI
				getConversationsEditComponent().getConversationsTable().removeItem(
						selectedUIConversation.getRelatedModelObject(ChatEnginePersistentState.class)
								.getId());
				getAdminUI().showInformationNotification(
						AdminMessageStrings.NOTIFICATION__ACCOUNT_DELETED); // TID

				closeWindow();
			}
		}, null);
	}
}