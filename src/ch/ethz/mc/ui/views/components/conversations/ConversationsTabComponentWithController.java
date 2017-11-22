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

import ch.ethz.mc.MC;
import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.model.ui.UIConversation;
import ch.ethz.mc.model.ui.UIParticipant;
import ch.ethz.mobilecoach.chatlib.engine.ConversationRepository;
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
	private final BeanContainer<String, ConversationRepository> repositories;

	private final BeanContainer<ObjectId, UIConversation>	beanContainer;

	public ConversationsTabComponentWithController() {
		super();

		// table options
		val conversationsEditComponent = getConversationsEditComponent();
		val conversationsTable = conversationsEditComponent.getConversationsTable();

		// conversations table content
		beanContainer = createBeanContainerForModelObjects(UIConversation.class,
				getRichConversationService().getAllConversations());

		conversationsTable.setContainerDataSource(beanContainer);
		conversationsTable.setSortContainerPropertyId(UIConversation.getSortColumn());
		conversationsTable.setVisibleColumns(UIConversation.getVisibleColumns());
		conversationsTable.setColumnHeaders(UIConversation.getColumnHeaders());
		
		// intervention scripts table content
		repositories = new BeanContainer<String, ConversationRepository>(ConversationRepository.class);
		val conversationRepositoriesTable = getConversationRepositoriesComponent().getConversationRepositoriesTable();
		conversationRepositoriesTable.setContainerDataSource(repositories);
		conversationRepositoriesTable.setVisibleColumns(new Object[] {"shortHash", "path", "numberOfConversations", "numberOfActions"});
		conversationRepositoriesTable.setColumnHeaders(new String[] {"Hash", "Path", "Number Of Conversations", "Number Of Actions"});
		repositories.setBeanIdProperty("path");
		repositories.addAll(MC.getInstance().getFileConversationManagementService().getAllRepositories());

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
		conversationsEditComponent.getDeleteAllButton().addClickListener(
				buttonClickListener);
		conversationsEditComponent.getDeleteButton().addClickListener(
				buttonClickListener);
		conversationsEditComponent.getRefreshButton().addClickListener(
				buttonClickListener);
		getConversationRepositoriesComponent().getRefreshButton().addClickListener(
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
			} else if (event.getButton() == conversationsEditComponent.getDeleteAllButton()) {
				deleteAllConversations();
			} else if (event.getButton() == getConversationRepositoriesComponent().getRefreshButton()) {
				refreshRepositories();
			}
		}
	}
	
	public void refresh() {
		refreshBeanContainer(beanContainer, UIConversation.class, getRichConversationService().getAllConversations());
	}
	
	public void refreshRepositories() {
		MC.getInstance().getFileConversationManagementService().refresh();
		repositories.removeAllItems();
		repositories.addAll(MC.getInstance().getFileConversationManagementService().getAllRepositories());
		getAdminUI().showInformationNotification(
				AdminMessageStrings.NOTIFICATION__UPLOAD_SUCCESSFUL); // TODO: put better message
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
	
	public void deleteAllConversations() {
		log.debug("Delete all conversations");
		showConfirmationWindow(new ExtendableButtonClickListener() {

			@Override
			public void buttonClick(final ClickEvent event) {
				try {
					getRichConversationService().deleteAllChatEnginePersistentStates();

				} catch (final Exception e) {
					closeWindow();
					handleException(e);
					return;
				}
				closeWindow();
			}
		}, null);
	}
}
