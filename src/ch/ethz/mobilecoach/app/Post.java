package ch.ethz.mobilecoach.app;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ch.ethz.mobilecoach.chatlib.engine.Input;
import lombok.Getter;
import lombok.Setter;


public class Post {
	
	public static final String REQUEST_TYPE_SELECT_ONE = "select_one";
	public static final String POST_TYPE_TEXT = "text";
	public static final String POST_TYPE_REQUEST = "request";
	
	@Getter
	@Setter
	private String message;
	
	@Getter
	@Setter
	private Input input;
	
	@Getter
	@Setter
	private Boolean hidden = false;
	
	
	@Setter
	private String postType;
	
	public String getPost_type(){
		return postType;
	}
	
	@Getter
	@Setter
	private List<Option> options = new LinkedList<Option>();
		
	
	@Getter
	@Setter
	private Map<String, Object> parameters = new HashMap<String, Object>(); 
	
	
	@Setter
	private String requestType;
	
	public String getRequest_type(){
		return requestType;
	}
	
	
	@Getter
	@Setter
	private String instructions;
	
	
	@Getter
	@Setter
	private String requestId;
	
	
	@Getter
	@Setter
	private Results results;
	
	
	@Getter
	@Setter
	private long createAt;
	
	@Getter
	@Setter
	private String id;
	
	
	@Getter
	@Setter
	private String channelId;
}
