package ch.ethz.mobilecoach.services;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.websocket.ClientEndpointConfig;

public class WebSocketConfigurator extends ClientEndpointConfig.Configurator {
	
	private String	authToken;

	public WebSocketConfigurator(String authToken){		
		this.authToken = authToken;
	}
    
	@Override
    public void beforeRequest(Map<String, List<String>> headers) {
        headers.put("Authorization", Arrays.asList("Bearer " + authToken));
    }
	
};
