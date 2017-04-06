package ch.ethz.mc.rest.services;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.WebApplicationException;

import org.bson.types.ObjectId;
import org.junit.Test;

import ch.ethz.mc.model.persistent.AppToken;
import ch.ethz.mc.rest.services.AppAuthenticationService.ParticipantInformation;
import ch.ethz.mc.services.RESTManagerService;

public class AppAuthenticationServiceTest {
	
	@Test
	public void authenticationValid() {
		RESTManagerService restManagerService = mock(RESTManagerService.class);
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpSession session = mock(HttpSession.class);
		when(session.getId()).thenReturn("1");
		when(request.getSession()).thenReturn(session);
		String appToken = AppToken.createToken();
		ObjectId participantId = ObjectId.get();
		when(restManagerService.findParticipantIdForAppToken(appToken)).thenReturn(participantId);

		AppAuthenticationService testee = new AppAuthenticationService(restManagerService);

		ParticipantInformation actual = testee.checkparticipantid(appToken, request);
		assertEquals(participantId.toHexString(), actual.getParticipantId());
		
	}

	@Test(expected = WebApplicationException.class)
	public void authenticationInvalid() {
		RESTManagerService restManagerService = mock(RESTManagerService.class);
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpSession session = mock(HttpSession.class);
		when(session.getId()).thenReturn("1");
		when(request.getSession()).thenReturn(session);
		String appToken = AppToken.createToken();

		AppAuthenticationService testee = new AppAuthenticationService(restManagerService);

		testee.checkparticipantid(appToken, request);
	}

}
