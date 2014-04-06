package org.isgf.mhc.services.types;

import org.isgf.mhc.model.persistent.FeedbackSlide;
import org.isgf.mhc.model.persistent.ScreeningSurveySlide;

/**
 * Contains all template fields that can be available in the HTML template of a
 * {@link ScreeningSurveySlide} or a {@link FeedbackSlide}
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
public enum GeneralSlideTemplateFieldTypes {
	/**
	 * Contains the base URL of the website:
	 * 
	 * <code>&lt;head&gt;&lt;base href="{{base_url}}"&gt;&lt;/head&gt;</code>
	 */
	BASE_URL,
	/**
	 * Contains the URL of the screening survey feedback
	 */
	FEEDBACK_URL,
	/**
	 * Contains the name of the survey or feedback
	 */
	NAME,
	/**
	 * <strong>OPTIONAL:</strong> Can contain an optional value for e.g. a
	 * specific css layout class
	 */
	OPTIONAL_LAYOUT_ATTRIBUTE,
	/**
	 * <strong>OPTIONAL:</strong> Can contain optional values for e.g. a
	 * specific css layout classes and is created by splitting the
	 * OPTIONAL_LAYOUT_ATTRIBUTE by comma
	 */
	OPTIONAL_LAYOUT_ATTRIBUTE_LIST,
	/**
	 * <strong>OPTIONAL:</strong> One of the items contained in
	 * OPTIONAL_LAYOUT_ATTRIBUTE_LIST
	 */
	OPTIONAL_LAYOUT_ATTRIBUTE_ITEM,
	/**
	 * Contains the title of the slide
	 */
	TITLE,
	/**
	 * <strong>OPTIONAL:</strong> Can contain the URL of an media object that
	 * should be shown in the slide
	 */
	MEDIA_OBJECT_URL,
	/**
	 * <strong>OPTIONAL:</strong> Can contain the type of an media object that
	 * should be shown in the slide; only one is true at a time and only if
	 * MEDIA_OBJECT_URL is also set
	 */
	MEDIA_OBJECT_TYPE_HTML_TEXT, MEDIA_OBJECT_TYPE_IMAGE, MEDIA_OBJECT_TYPE_AUDIO, MEDIA_OBJECT_TYPE_VIDEO,
	/**
	 * Contains the variable name and value of a hidden variable for consistency
	 * checks
	 */
	HIDDEN_CHECK_VARIABLE, HIDDEN_CHECK_VARIABLE_VALUE,
	/**
	 * <strong>ONLY</strong> internal, can't be used in templates
	 */
	TEMPLATE_FOLDER;

	/**
	 * Creates the appropriate variable name of the
	 * {@link GeneralSlideTemplateFieldTypes}
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
