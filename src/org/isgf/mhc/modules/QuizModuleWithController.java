package org.isgf.mhc.modules;

import org.bson.types.ObjectId;

/**
 * Extends the QuizModule with a controller
 * 
 * @author Andreas Filler
 * 
 */
@SuppressWarnings("serial")
public class QuizModuleWithController extends QuizModule {

	protected QuizModuleWithController(final ObjectId interventionId) {
		super(interventionId);
	}

}
