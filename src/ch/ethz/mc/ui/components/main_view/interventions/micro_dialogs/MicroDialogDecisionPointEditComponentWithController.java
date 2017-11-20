package ch.ethz.mc.ui.components.main_view.interventions.micro_dialogs;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;

import ch.ethz.mc.conf.AdminMessageStrings;
import ch.ethz.mc.model.persistent.Intervention;
import ch.ethz.mc.model.persistent.MicroDialogDecisionPoint;
import ch.ethz.mc.ui.components.basics.ShortStringEditComponent;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Extends the monitoring rule edit component with a controller
 *
 * @author Andreas Filler
 */
@SuppressWarnings("serial")
@Log4j2
public class MicroDialogDecisionPointEditComponentWithController
		extends MicroDialogDecisionPointEditComponent {

	private final MicroDialogDecisionPoint microDialogDecisionPoint;

	public MicroDialogDecisionPointEditComponentWithController(
			final Intervention intervention,
			final MicroDialogDecisionPoint microDialogDecisionPoint) {
		super();

		this.microDialogDecisionPoint = microDialogDecisionPoint;

		// Configure integrated components
		final val microDialogRulesEditComponentWithController = getMicroDialogRulesEditComponentWithController();
		microDialogRulesEditComponentWithController.init(intervention,
				microDialogDecisionPoint.getMicroDialog(),
				microDialogDecisionPoint.getId());

		// handle buttons
		val buttonClickListener = new ButtonClickListener();
		getCommentVariableTextFieldComponent().getButton()
				.addClickListener(buttonClickListener);

		adjust();
	}

	private class ButtonClickListener implements Button.ClickListener {
		@Override
		public void buttonClick(final ClickEvent event) {
			if (event.getButton() == getCommentVariableTextFieldComponent()
					.getButton()) {
				editCommentText();
			}
			event.getButton().setEnabled(true);
		}
	}

	private void adjust() {
		getCommentVariableTextFieldComponent()
				.setValue(microDialogDecisionPoint.getComment());
	}

	public void editCommentText() {
		log.debug("Edit comment");
		showModalStringValueEditWindow(
				AdminMessageStrings.ABSTRACT_STRING_EDITOR_WINDOW__EDIT_COMMENT,
				microDialogDecisionPoint.getComment(), null,
				new ShortStringEditComponent(),
				new ExtendableButtonClickListener() {

					@Override
					public void buttonClick(final ClickEvent event) {
						try {
							// Change comment
							getInterventionAdministrationManagerService()
									.microDialogDecisionPointSetComment(
											microDialogDecisionPoint,
											getStringValue());
						} catch (final Exception e) {
							handleException(e);
							return;
						}

						adjust();

						closeWindow();
					}

				}, null);
	}
}
