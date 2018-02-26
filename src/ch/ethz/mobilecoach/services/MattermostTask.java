package ch.ethz.mobilecoach.services;

import lombok.extern.log4j.Log4j;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
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
	
	public MattermostTask(String url){
		this.url = url;
		this.payload = null;
	}
	
	public MattermostTask<RESULT> setToken(String token){
		this.token = token;
		return this;
	}
	
	public RESULT run(){			
		HttpClient client = new HttpClient();
		int numberOfTriesCompleted = 0;
		boolean success = false;
		
		final int MAX_TRIES = 3;
		
		while (!success && numberOfTriesCompleted < MAX_TRIES){
			HttpMethodBase method;
			
			
			if (payload != null){
				method = new PostMethod(this.url);
			} else {
				method = new GetMethod(this.url);
			}
			
						
			if (this.token != null){
				method.setRequestHeader("Authorization", "Bearer " + this.token);
			}
			
			try {
				if (this.payload != null){
					((PostMethod) method).setRequestEntity(new StringRequestEntity(this.payload.toString(), "application/json", "UTF-8"));
				}
	            int responseCode = client.executeMethod(method);
	            if (responseCode == HttpStatus.SC_OK) {
	            	success = true;
	                return handleResponse(method);
	            } else {
	            	throw new Exception("Status " + new Integer(responseCode) + ": " + method.getResponseBodyAsString());
	            }
			} catch (Exception e) {
				log.error("Error completing Mattermost task: " + url + " " + payload + " " + e.getMessage(), e);
				numberOfTriesCompleted++;
				if (MAX_TRIES <= numberOfTriesCompleted){
					throw new RuntimeException(e);
				}
			}
		}
		
		return null;
	}
	
	protected RESULT handleResponse(HttpMethodBase method) throws Exception {
		return null;
	};
	
	
}
