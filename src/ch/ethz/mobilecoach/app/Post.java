package ch.ethz.mobilecoach.app;

import java.util.LinkedList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;


public class Post {
	
	public static final String POST_TYPE_SELECT_ONE = "select_one";
	public static final String POST_TYPE_TEXT = "text";
	
	@Getter
	@Setter
	private String message;
	
	
	@Getter
	@Setter
	private String postType;
	
	@Getter
	@Setter
	private List<Option> options = new LinkedList<Option>(); 
	
	
	@Getter
	@Setter
	private String requestType;
	
	
	@Getter
	@Setter
	private String instructions;
	
	
	@Getter
	@Setter
	private String requestId;
	
	@Getter
	@Setter
	private Results results;

}
