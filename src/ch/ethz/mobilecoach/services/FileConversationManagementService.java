package ch.ethz.mobilecoach.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import ch.ethz.mobilecoach.chatlib.engine.ConversationRepository;
import ch.ethz.mobilecoach.chatlib.engine.xml.DomParser;
import lombok.extern.log4j.Log4j2;


/*
 *  ConversationManagementService implementation that loads conversations from resource files.
 */

@Log4j2
public class FileConversationManagementService implements
		ConversationManagementService {
	
	ConversationRepository repository = new ConversationRepository();
	
	private FileConversationManagementService(){
	}
	
	public static FileConversationManagementService start(String interventionPath) {
		FileConversationManagementService result = new FileConversationManagementService();
		try {
			result.loadFromFolder(interventionPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	@Override
	public ConversationRepository getRepository(String interventionId) {
		return repository;
	}
	
	public void loadFromFolder(String path) throws Exception{
		List<java.nio.file.Path> paths = Files.walk(Paths.get(path)).filter(Files::isRegularFile).collect(Collectors.toList());
		
		for (Path p : paths){
			File f = p.toFile();
			if (f.getName().endsWith(".xml")){
				log.debug("Loading " + f.getName());
				loadResourceFile(f);
			}
		}
		
		repository.freeze(); // for now we assume that the repository should not change after loading from the folder
	}
	
	
	public void loadResourceFile(File file) throws Exception {
		InputStream stream = new FileInputStream(file);
		DomParser parser = new DomParser(repository, null);
		parser.parse(stream);
	}

}
