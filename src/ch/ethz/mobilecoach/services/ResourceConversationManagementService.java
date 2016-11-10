package ch.ethz.mobilecoach.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.servlet.ServletContext;

import ch.ethz.mobilecoach.chatlib.engine.ConversationRepository;
import ch.ethz.mobilecoach.chatlib.engine.xml.DomParser;


/*
 *  ConversationManagementService implementation that loads conversations from resource files.
 */
public class ResourceConversationManagementService implements
		ConversationManagementService {
	
	ConversationRepository repository = new ConversationRepository();
	ServletContext servletContext;
	
	private ResourceConversationManagementService(ServletContext servletContext){
		this.servletContext = servletContext;
	}
	
	public static ResourceConversationManagementService start(ServletContext servletContext) {
		ResourceConversationManagementService result = new ResourceConversationManagementService(servletContext);
		try {
			result.loadResourceFile("test-conversation1.xml");
			result.loadResourceFile("milestone1-test.xml");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	@Override
	public ConversationRepository getRepository(String interventionId) {
		return repository;
	}
	
	
	public void loadResourceFile(String fileName) throws Exception {
		String path = servletContext.getRealPath(fileName);
		File file = new File(path);
		InputStream stream = new FileInputStream(file); //this.getClass().getResourceAsStream(path);
		DomParser parser = new DomParser(repository, null);
		parser.parse(stream);
	}



}
