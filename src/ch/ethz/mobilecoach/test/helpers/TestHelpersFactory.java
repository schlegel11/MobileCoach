package ch.ethz.mobilecoach.test.helpers;

import ch.ethz.mobilecoach.chatlib.engine.ChatEngine;
import ch.ethz.mobilecoach.chatlib.engine.HelpersRepository;
import ch.ethz.mobilecoach.chatlib.engine.conversation.ConversationUI;
import ch.ethz.mobilecoach.chatlib.engine.variables.VariableException;
import ch.ethz.mobilecoach.chatlib.engine.variables.VariableStore;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 
 * @author Dominik Rüegger
 */


@AllArgsConstructor
public class TestHelpersFactory {
	
	@Getter
	ChatEngine engine;
	
	@Getter
	ConversationUI ui;
	
	public void addHelpers(HelpersRepository helpersRepository){
		helpersRepository.addHelper("test-TestModeOn", new HelpersRepository.Helper(){
			public void run(VariableStore variableStore) throws VariableException {
				ui.setDelayEnabled(false);
			}
		});
		
		helpersRepository.addHelper("test-TestModeOff", new HelpersRepository.Helper(){
			public void run(VariableStore variableStore) throws VariableException {
				ui.setDelayEnabled(true);
			}
		});
		
	}
}
