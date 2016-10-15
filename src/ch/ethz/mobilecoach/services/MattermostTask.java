package ch.ethz.mobilecoach.services;

import lombok.extern.log4j.Log4j;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.json.JSONObject;

@Log4j
public class MattermostTask<RESULT> {
	private final String url;
	private final JSONObject payload;
	private String token = null;
	
	public MattermostTask(String url, JSONObject payload){
		this.url = url;
		this.payload = payload;
	}
	
	public MattermostTask setToken(String token){
		this.token = token;
		return this;
	}
	
	public RESULT run(){			
		HttpClient client = new HttpClient();
		int numberOfTriesCompleted = 0;
		boolean success = false;
		
		while (!success && numberOfTriesCompleted < 3){
			PostMethod post = new PostMethod(this.url);
			
			if (this.token != null){
				post.setRequestHeader("Authorization", "Bearer " + this.token);
			}
			
			try {
				post.setRequestEntity(new StringRequestEntity(this.payload.toString(), "application/json", "UTF-8"));
	            int responseCode = client.executeMethod(post);
	            if (responseCode == HttpStatus.SC_OK) {
	            	success = true;
	                return handleResponse(post);
	            } else {
	            	throw new Exception("Status " + new Integer(responseCode) + ": " + post.getResponseBodyAsString());
	            }
			} catch (Exception e) {
				log.error("Error connecting to Mattermost", e);
				numberOfTriesCompleted++;
			}
		}
		
		// TODO: throw exception if retrying failed
		return null;
	}
	
	RESULT handleResponse(PostMethod method) throws Exception {
		return null;
	};	
}
