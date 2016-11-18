package ch.ethz.mobilecoach.services;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.json.JSONObject;

import lombok.extern.log4j.Log4j;
@Log4j
public class OneSignalTask<RESULT> {
	private final String url;
	private final JSONObject payload;
	private LinkedHashMap<String, String> headers;
	
	public OneSignalTask(String url, JSONObject payload, LinkedHashMap<String, String> headers){
		this.url = url;
		this.payload = payload;
		this.headers = headers;
	}
	
	
	public RESULT run(){			
		HttpClient client = new HttpClient();
		int numberOfTriesCompleted = 0;
		boolean success = false;
		
		final int MAX_TRIES = 3;
		
		while (!success && numberOfTriesCompleted < MAX_TRIES){
			PostMethod post = new PostMethod(this.url);
			for(Entry <String, String>  entry : this.headers.entrySet()){
				post.addParameter(entry.getKey(), entry.getValue());
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
				log.error("Error completing OneSignal task", e);
				numberOfTriesCompleted++;
				if (MAX_TRIES <= numberOfTriesCompleted){
					throw new RuntimeException(e);
				}
			}
		}		
		return null;
	}
	
	RESULT handleResponse(PostMethod method) throws Exception {
		return null;
	};
}