package ch.ethz.mc.services.types;

/* ##LICENSE## */

/**
 * Contains all template fields that can be available in the HTML template of a
 * dashboard
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
public enum DashboardTemplateFields {
	/**
	 * Contains the base URL of the website:
	 *
	 * <code>&lt;head&gt;&lt;base href="{{base_url}}"&gt;&lt;/head&gt;</code>
	 */
	BASE_URL,
	/**
	 * Contains the token that enables REST access
	 */
	TOKEN,
	/**
	 * Contains the base URL of the REST interface
	 */
	REST_API_URL,
	/**
	 * Password from page request
	 */
	PASSWORD;

	/**
	 * Creates the appropriate variable name of the
	 * {@link DashboardTemplateFields}
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
