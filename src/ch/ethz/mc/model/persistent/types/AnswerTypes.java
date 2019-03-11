package ch.ethz.mc.model.persistent.types;

/* ##LICENSE## */
/**
 * Supported {@link AnswerTypes}
 *
 * @author Andreas Filler
 */
public enum AnswerTypes {
	FREE_TEXT,
	FREE_TEXT_MULTILINE,
	FREE_TEXT_RAW,
	FREE_TEXT_MULTILINE_RAW,
	FREE_NUMBERS,
	FREE_NUMBERS_RAW,
	LIKERT,
	LIKERT_SILENT,
	LIKERT_SLIDER,
	SELECT_ONE,
	SELECT_ONE_RAW,
	SELECT_ONE_LIST,
	SELECT_MANY,
	SELECT_MANY_RAW,
	SELECT_MANY_MODAL,
	SELECT_ONE_IMAGES,
	SELECT_MANY_IMAGES,
	DATE,
	TIME,
	DATE_AND_TIME,
	IMAGE,
	VIDEO,
	AUDIO,
	GRAPHICAL_CODE,
	CUSTOM;

	@Override
	public String toString() {
		return name().toLowerCase().replace("_", " ");
	}

	public String toJSONField() {
		return nameWithoutRaw().toLowerCase().replace("_", "-");
	}

	private String nameWithoutRaw() {
		switch (this) {
			case FREE_NUMBERS_RAW:
			case FREE_TEXT_MULTILINE_RAW:
			case FREE_TEXT_RAW:
			case SELECT_ONE_RAW:
			case SELECT_MANY_RAW:
				return name().replace("_RAW", "");
			default:
				return name();
		}
	}

	/**
	 * RAW types are not cleaned on saving
	 * 
	 * @return
	 */
	public boolean isRAW() {
		switch (this) {
			case FREE_NUMBERS_RAW:
			case FREE_TEXT_MULTILINE_RAW:
			case FREE_TEXT_RAW:
			case LIKERT:
			case LIKERT_SILENT:
			case LIKERT_SLIDER:
			case SELECT_ONE:
			case SELECT_ONE_RAW:
			case SELECT_ONE_LIST:
			case SELECT_MANY:
			case SELECT_MANY_RAW:
			case SELECT_MANY_MODAL:
			case SELECT_ONE_IMAGES:
			case SELECT_MANY_IMAGES:
			case DATE:
			case TIME:
			case DATE_AND_TIME:
			case IMAGE:
			case VIDEO:
			case AUDIO:
			case GRAPHICAL_CODE:
			case CUSTOM:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Key-values-based answer types are converted to JSON
	 * 
	 * @return
	 */
	public boolean isKeyValueBased() {
		switch (this) {
			case LIKERT:
			case LIKERT_SILENT:
			case LIKERT_SLIDER:
			case SELECT_ONE:
			case SELECT_ONE_RAW:
			case SELECT_ONE_LIST:
			case SELECT_MANY:
			case SELECT_MANY_RAW:
			case SELECT_MANY_MODAL:
			case SELECT_ONE_IMAGES:
			case SELECT_MANY_IMAGES:
			case DATE:
			case TIME:
			case DATE_AND_TIME:
			case IMAGE:
			case VIDEO:
			case AUDIO:
			case CUSTOM:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Raw key-values-based answer types can contain "unsafe" variable values to
	 * enable concatenation of answer options
	 * 
	 * @return
	 */
	public boolean isRawKeyValueBased() {
		switch (this) {
			case SELECT_ONE_RAW:
			case SELECT_MANY_RAW:
				return true;
			default:
				return false;
		}
	}
}
