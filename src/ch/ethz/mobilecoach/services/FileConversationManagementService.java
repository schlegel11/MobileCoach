package ch.ethz.mobilecoach.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ch.ethz.mobilecoach.chatlib.engine.ConversationRepository;
import ch.ethz.mobilecoach.chatlib.engine.checking.ReferenceChecker;
import ch.ethz.mobilecoach.chatlib.engine.xml.DomParser;
import lombok.extern.log4j.Log4j2;


/*
 *  ConversationManagementService implementation that loads conversations from resource files.
 */

@Log4j2
public class FileConversationManagementService implements
		ConversationManagementService {
	
	Map<String, ConversationRepository> repositoryByName = new HashMap<>();
	Map<String, ConversationRepository> repositoryByHash = new HashMap<>();
	ReferenceChecker referenceChecker = new ReferenceChecker();
	
	private final String DEFAULT_REPOSITORY_NAME = "pathmate2";
	
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
		if (repositoryByName.containsKey(interventionId)){
			return repositoryByName.get(interventionId);
		}
		if (repositoryByHash.containsKey(interventionId)){
			return repositoryByHash.get(interventionId);
		}
		if (interventionId == null){
			return repositoryByName.get(DEFAULT_REPOSITORY_NAME);
		}
		return null;
	}
	
	
	
	public void loadFromFolder(String path) throws Exception {
		// List all folders
		List<Path> paths = Files.list(Paths.get(path)).filter(Files::isDirectory).collect(Collectors.toList());
		for (Path p : paths){
			String dirName = p.getFileName().toString();
			String interventionId = dirName;
			
			ConversationRepository repository = new ConversationRepository();
			repository.path = p.toString();
			
			loadRepositoryFromFolder(p, repository);
			
			repositoryByName.put(interventionId, repository);
			repositoryByHash.put(repository.getHash(), repository);
		}
		
	}
	
	public void loadRepositoryFromFolder(Path path, ConversationRepository repository) throws Exception{
		List<Path> paths = Files.walk(path).filter(Files::isRegularFile).collect(Collectors.toList());
		
		for (Path p : paths){
			File f = p.toFile();
			if (f.getName().endsWith(".xml")){
				log.debug("Loading " + f.getName());
				loadResourceFile(f, repository);
			}
		}
		
		repository.freeze(); // for now we assume that the repository should not change after loading from the folder
		
		// checks
		
		for (String error: referenceChecker.check()){
			log.error(error);
		}
	}
	
	
	public void loadResourceFile(File file, ConversationRepository repository) throws Exception {
		InputStream stream = new FileInputStream(file);
		DomParser parser = new DomParser(repository, null, referenceChecker);
		parser.parse(stream);
	}
}
