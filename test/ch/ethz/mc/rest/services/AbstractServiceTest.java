package ch.ethz.mc.rest.services;

import javax.servlet.http.HttpSession;
import javax.ws.rs.WebApplicationException;

import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import ch.ethz.mc.model.persistent.AppToken;
import ch.ethz.mc.services.RESTManagerService;
import ch.ethz.mc.services.types.GeneralSessionAttributeTypes;

@RunWith(MockitoJUnitRunner.class)
public class AbstractServiceTest {

	@Mock
	private RESTManagerService restService;

	@Mock
	private HttpSession session;

	private ObjectId participantId = new ObjectId();

	@Test
	public void testAccessWithSessionToken_success() {
		when(session.getAttribute(GeneralSessionAttributeTypes.TOKEN.toString())).thenReturn("1234");
		when(session.getAttribute(GeneralSessionAttributeTypes.VALIDATOR.toString())).thenReturn(Boolean.TRUE);
		when(session.getAttribute(GeneralSessionAttributeTypes.CURRENT_PARTICIPANT.toString())).thenReturn(participantId);
		String token = "1234";
		AbstractService testee = new AbstractService(restService) {
		};
		ObjectId result = testee.checkAccessAndReturnParticipantId(token, session);
		assertEquals(participantId, result);

	}

	@Test(expected=WebApplicationException.class)
	public void testAccessWithSessionTokenWrongToken_fail() {
		when(session.getAttribute(GeneralSessionAttributeTypes.TOKEN.toString())).thenReturn("1111");
		when(session.getAttribute(GeneralSessionAttributeTypes.VALIDATOR.toString())).thenReturn(Boolean.TRUE);
		when(session.getAttribute(GeneralSessionAttributeTypes.CURRENT_PARTICIPANT.toString())).thenReturn(participantId);
		String token = "1234";
		AbstractService testee = new AbstractService(restService) {
		};
		testee.checkAccessAndReturnParticipantId(token, session);

	}

	@Test
	public void testAccessWithAppToken_success() {
		String token = AppToken.createToken();
		when(restService.findParticipantIdForAppToken(token)).thenReturn(participantId);
		AbstractService testee = new AbstractService(restService) {
		};
		ObjectId result = testee.checkAccessAndReturnParticipantId(token, session);
		assertEquals(participantId, result);
	}

	@Test(expected=WebApplicationException.class)
	public void testAccessWithAppToken_nonexisting() {
		String token = AppToken.createToken();
		when(restService.findParticipantIdForAppToken(token)).thenReturn(null);
		AbstractService testee = new AbstractService(restService) {
		};
		testee.checkAccessAndReturnParticipantId(token, session);
	}
}
