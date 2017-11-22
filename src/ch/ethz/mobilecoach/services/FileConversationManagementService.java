package ch.ethz.mobilecoach.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
	String interventionPath = null;
	
	private final String DEFAULT_REPOSITORY_NAME = "pathmate2";
	
	private FileConversationManagementService(String interventionPath){
		this.interventionPath = interventionPath;
	}
	
	public static FileConversationManagementService start(String interventionPath){
		FileConversationManagementService result = new FileConversationManagementService(interventionPath);
		try {
			result.loadFromFolder();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
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
	
	public Collection<ConversationRepository> getAllRepositories(){
		Set<ConversationRepository> allRepositories = new HashSet<ConversationRepository>();
		allRepositories.addAll(repositoryByName.values());
		allRepositories.addAll(repositoryByHash.values());		
		ArrayList<ConversationRepository> result = new ArrayList<ConversationRepository>(allRepositories);
		result.sort(new Comparator<ConversationRepository>(){
			@Override
			public int compare(ConversationRepository o1,
					ConversationRepository o2) {
				return o1.getPath().compareTo(o2.getPath());
			}
		});
		return result;
	}
	
	public void loadFromFolder() throws Exception {
		String path = this.interventionPath;
		
		// List all folders
		List<Path> paths = Files.list(Paths.get(path)).filter(Files::isDirectory).collect(Collectors.toList());
		for (Path p : paths){
			String dirName = p.getFileName().toString();
			String interventionId = dirName;
			
			ConversationRepository repository = new ConversationRepository(p.toString());

			try {
				loadRepositoryFromFolder(p, repository);
				
				repositoryByName.put(interventionId, repository);
				repositoryByHash.put(repository.getHash(), repository);
				
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		
	}
	
	public void loadRepositoryFromFolder(Path path, ConversationRepository repository) throws Exception{
		List<Path> paths = Files.walk(path).filter(Files::isRegularFile).collect(Collectors.toList());
		ReferenceChecker referenceChecker = new ReferenceChecker();
		
		boolean hasErrors = false;
		
		paths.sort(new Comparator<Path>(){
			@Override
			public int compare(Path o1, Path o2) {
				return o1.toString().compareTo(o2.toString());
			}
		});
		
		for (Path p : paths){
			File f = p.toFile();
			if (f.getName().endsWith(".xml")){
				log.debug("Loading " + f.getName());
				try {
					loadResourceFile(f, repository, referenceChecker);
				} catch (Exception e){
					log.error(f.getName() + " : " + e.getMessage(), e);
					hasErrors = true;
				}
			}
		}
		
		repository.freeze(); // for now we assume that the repository should not change after loading from the folder
		
		// checks
		for (String error: referenceChecker.check()){
			log.error(error);
			hasErrors = true;
		}
		
		if (hasErrors){
			throw new Exception("Error loading conversations from " + path.toString());
		}
	}
	
	
	public void loadResourceFile(File file, ConversationRepository repository, ReferenceChecker referenceChecker) throws Exception {
		InputStream stream = new FileInputStream(file);
		DomParser parser = new DomParser(repository, null, referenceChecker);
		parser.parse(stream);
	}

	@Override
	public void refresh() {
		try {
			loadFromFolder();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
}
