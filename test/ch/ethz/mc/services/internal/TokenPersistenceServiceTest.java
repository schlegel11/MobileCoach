package ch.ethz.mc.services.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import ch.ethz.mc.model.ModelObject;
import ch.ethz.mc.model.persistent.OneTimeToken;

@RunWith(MockitoJUnitRunner.class)
public class TokenPersistenceServiceTest {

	@Mock
	private DatabaseManagerService dbService;
	private ObjectId participantId = new ObjectId();
	private Date baseDate = new Date();
	private OneTimeToken t1 = new OneTimeToken(participantId, "111111", new Date(baseDate.getTime() + 1));
	private OneTimeToken t2 = new OneTimeToken(participantId, "222222", new Date(baseDate.getTime() + 2));
	private OneTimeToken t3 = new OneTimeToken(participantId, "333333", new Date(baseDate.getTime() + 3));
	private OneTimeToken t4 = new OneTimeToken(participantId, "444444", new Date(baseDate.getTime() - (1000 * 172000)));

	@Test
	public void testCreateOneTimeToken() {
		TokenPersistenceService testee = new TokenPersistenceService(dbService);
		OneTimeToken actual = testee.createOneTimeTokenForParticipant(participantId);
		assertNotNull(actual);
		verify(dbService).saveModelObject(actual);
	}

	@Test
	public void testGetOrCreateRecentOneTimeToken_oneExisting() {
		when(dbService.findModelObjects(OneTimeToken.class, "{participantId:#}", participantId))
				.thenReturn(Arrays.asList(t1));
		TokenPersistenceService testee = new TokenPersistenceService(dbService);

		String token = testee.getOrCreateRecentOneTimeToken(participantId);

		assertEquals(t1.getToken(), token);
	}

	@Test
	public void testGetOrCreateRecentOneTimeToken_multipleExisting_Newest() {
		when(dbService.findModelObjects(OneTimeToken.class, "{participantId:#}", participantId))
				.thenReturn(Arrays.asList(t1, t3, t2));
		TokenPersistenceService testee = new TokenPersistenceService(dbService);

		String token = testee.getOrCreateRecentOneTimeToken(participantId);

		assertEquals(t3.getToken(), token);
	}

	@Test
	public void testGetOrCreateRecentOneTimeToken_nonExisting_New() {
		when(dbService.findModelObjects(OneTimeToken.class, "{participantId:#}", participantId))
				.thenReturn(Collections.emptyList());
		TokenPersistenceService testee = new TokenPersistenceService(dbService);

		String token = testee.getOrCreateRecentOneTimeToken(participantId);

		ArgumentCaptor<ModelObject> captor = ArgumentCaptor.forClass(ModelObject.class);
		verify(dbService).saveModelObject(captor.capture());
		assertEquals(((OneTimeToken) captor.getValue()).getToken(), token);
	}

	@Test
	public void testGetOrCreateRecentOneTimeToken_existingOld_New() {
		when(dbService.findModelObjects(OneTimeToken.class, "{participantId:#}", participantId))
				.thenReturn(Arrays.asList(t4));
		TokenPersistenceService testee = new TokenPersistenceService(dbService);

		String token = testee.getOrCreateRecentOneTimeToken(participantId);

		ArgumentCaptor<ModelObject> captor = ArgumentCaptor.forClass(ModelObject.class);
		verify(dbService).saveModelObject(captor.capture());
		assertEquals(((OneTimeToken) captor.getValue()).getToken(), token);
	}

	@Test
	public void testConsumeToken_existing() {
		when(dbService.findOneModelObject(OneTimeToken.class, "{token:#}", "111111")).thenReturn(t1);
		TokenPersistenceService testee = new TokenPersistenceService(dbService);

		ObjectId oneTimeToken = testee.consumeOneTimeToken("111111");

		assertEquals(participantId, oneTimeToken);
	}

	@Test
	public void testConsumeToken_existingWrongCase() {
		when(dbService.findOneModelObject(OneTimeToken.class, "{token:#}", "AAAAAA")).thenReturn(t1);
		TokenPersistenceService testee = new TokenPersistenceService(dbService);

		ObjectId oneTimeToken = testee.consumeOneTimeToken("aaaaaa");

		assertEquals(participantId, oneTimeToken);
	}

	@Test
	public void testConsumeToken_nonExisting() {
		TokenPersistenceService testee = new TokenPersistenceService(dbService);

		ObjectId oneTimeToken = testee.consumeOneTimeToken("111111");

		assertNull(oneTimeToken);
	}

}
