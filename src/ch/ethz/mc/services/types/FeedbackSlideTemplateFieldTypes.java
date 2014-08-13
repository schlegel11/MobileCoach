package ch.ethz.mc.services.types;

import ch.ethz.mc.model.persistent.FeedbackSlide;

/**
 * Contains all template fields that can be available in the HTML template of a
 * {@link FeedbackSlide}
 * 
 * All fields can be used as <code>{{field}}</code> to get the content, as
 * <code>{{#field}}...{{/field}}</code> for loops and existence checks as well
 * as <code>{{^field}}...{{/field}}</code> for non-existence checks
 * 
 * Detailed information regarding the template system can be found in the
 * <a href="http://mustache.github.io/mustache.5.html">Mustache
 * documentation</a>
 * 
 * @author Andreas Filler
 */
public enum FeedbackSlideTemplateFieldTypes {
	/**
	 * Exists if it's a feedback slide
	 */
	IS_FEEDBACK,
	/**
	 * Contains the feedback text
	 */
	TEXT,
	/**
	 * Is true if the participant is directly coming form a screening survey in
	 * the same session
	 */
	FROM_SCREENING_SURVEY,
	/**
	 * Contains the variable name and values of a hidden variable for
	 * navigation; the variable has to occur in the form with one of the two
	 * possible values
	 */
	HIDDEN_NAVIGATION_VARIABLE, HIDDEN_NAVIGATION_VARIABLE_NAVIGATE_PREVIOUS, HIDDEN_NAVIGATION_VARIABLE_NAVIGATE_NEXT,
	/**
	 * Is true, when the slide is the first slide of the feedback
	 */
	IS_FIRST_SLIDE,
	/**
	 * Is true, when the slide is the last slide of the screening survey
	 */
	IS_LAST_SLIDE;

	/**
	 * Creates the appropriate variable name of the
	 * {@link FeedbackSlideTemplateFieldTypes}
	 * 
	 * @return The appropriate variable name
	 */
	public String toVariable() {
		return toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString() {
		return super.toString().toLowerCase();
	}
}
