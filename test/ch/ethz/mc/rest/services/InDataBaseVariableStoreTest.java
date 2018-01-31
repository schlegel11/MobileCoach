package ch.ethz.mc.rest.services;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.ethz.mc.conf.Constants;
import ch.ethz.mc.model.persistent.DialogStatus;
import ch.ethz.mc.model.persistent.Participant;
import ch.ethz.mc.services.internal.DatabaseManagerService;
import ch.ethz.mc.services.internal.InDataBaseVariableStore;
import ch.ethz.mc.services.internal.VariablesManagerService;
import ch.ethz.mc.tools.InternalDateTime;
import ch.ethz.mobilecoach.chatlib.engine.variables.VariableException;

public class InDataBaseVariableStoreTest {

	private DatabaseManagerService databaseManagerService;
	private VariablesManagerService variableManagerService;
	private Participant participant;
	private InDataBaseVariableStore inDataBaseVariableStore;
	private DialogStatus dialogStatus;
	private final static String VALID_VARIABLE_NAME = "$MCHAMMER";
	private final static String VARIABLE_VALUE2 = "FALSE";
	private final static String WRONG_VARIABLE_NAME = "LOLO";
	private final static String VARIABLE_VALUE = "TRUE";
	/*
	 * Modify the content of the following file, so that it correspond to path where your configuration.properties is located.
	 * 
	 */
	private final static String FILE_NAME = "/pathToMobileCoachConfigPropertiesFile.txt";
	

	@Before
	public void preparation() throws IOException{
		
		BufferedReader brTest = null;
	
		String userPath = System.getProperty("user.dir");
		String packagePath = this.getClass().getPackage().getName().replaceAll("\\.", "/"); 
		
		try {
			brTest = new BufferedReader(new FileReader(userPath + "/test/" + packagePath + FILE_NAME));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String configPropPath = brTest.readLine();
	
		Constants.injectConfiguration(configPropPath, null);
		try {
			this.databaseManagerService = DatabaseManagerService.start(Constants.DATA_MODEL_VERSION);
		} catch (Exception e) {			
			e.printStackTrace();
		}
		ObjectId intervention = new ObjectId();
		String globalUniqueId = "Lolo";
		ObjectId id = new ObjectId();

		participant = new Participant(
				intervention,
				null,
				InternalDateTime.currentTimeMillis(), "",
				Constants.getInterventionLocales()[0], null,
				id, globalUniqueId,
				null, null, true, "", "");

		databaseManagerService.saveModelObject(participant);

		final long currentTimestamp = InternalDateTime.currentTimeMillis();
		dialogStatus = new DialogStatus(participant.getId(), "", null,
				null, currentTimestamp, false, currentTimestamp, 0, false, 0,
				0, false, 0);

		databaseManagerService.saveModelObject(dialogStatus);


		try {
			variableManagerService = VariablesManagerService.start(databaseManagerService);
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.inDataBaseVariableStore = new InDataBaseVariableStore(variableManagerService, participant.getId(), participant);
	}



	@Test
	public void shouldFailToReadVariableFromDataBase(){

		assertEquals(inDataBaseVariableStore.containsVariable(WRONG_VARIABLE_NAME), false);

	}

	
	
	@Test
	public void shouldInitializeSetandReadVariableFromDataBase(){
		
		try {
			inDataBaseVariableStore.initialize(VALID_VARIABLE_NAME, VARIABLE_VALUE);
		} catch (VariableException e) {
			e.printStackTrace();
		}

		try {
			inDataBaseVariableStore.set(VALID_VARIABLE_NAME, VARIABLE_VALUE2);
		} catch (VariableException e) {
			e.printStackTrace();
		}

		try {
			assertEquals(inDataBaseVariableStore.get(VALID_VARIABLE_NAME), VARIABLE_VALUE2);
		} catch (VariableException e) {
			e.printStackTrace();
		}
	}
	


	@After
	public void deleteObjectInDataBase(){
		databaseManagerService.deleteModelObject(participant);
		databaseManagerService.deleteModelObject(dialogStatus);
	}
}
