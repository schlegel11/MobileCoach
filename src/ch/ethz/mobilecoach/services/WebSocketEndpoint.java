package ch.ethz.mobilecoach.services;


import java.util.ArrayList;
import java.util.Map;
import java.util.List;

import javax.websocket.ClientEndpoint;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.OnOpen;
import javax.websocket.Session;



public class WebSocketEndpoint extends Endpoint {

	@OnOpen
	public void onOpen(Session session, EndpointConfig config) {

		session.addMessageHandler(new MessageHandler.Whole<String>() {

			@Override
			public void onMessage(String msg) {
				System.out.println(msg);
			}
		});
		
		
	}
}