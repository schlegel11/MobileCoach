package ch.ethz.mc.model.persistent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import org.bson.types.ObjectId;

import static org.junit.Assert.*;

import org.junit.Test;

public class AppTokenTest {
	
	@Test
	public void testRandomGeneration() {
		String t1 = AppToken.createToken();
		String t2 = AppToken.createToken();
		assertEquals(54, t1.length());
		assertEquals("app:", t1.substring(0,4));
		assertNotSame(t1, t2);
	}
	
	@Test
	public void testTokenRecognitionValid() {
		assertTrue(AppToken.isAppToken("app:12345678901234567890123456789012345678901234567890"));
	}
	
	@Test
	public void testTokenRecognitionTooShort() {
		assertFalse(AppToken.isAppToken("app:1234567890123456789012345678901234567890123456789"));
	}
	
	@Test
	public void testTokenRecognitionTooLong() {
		assertFalse(AppToken.isAppToken("app:123456789012345678901234567890123456789012345678901"));
	}
	
	@Test
	public void testTokenRecognitionWrongPrefix() {
		assertFalse(AppToken.isAppToken("abb:12345678901234567890123456789012345678901234567890"));
	}
	
	@Test
	public void testCreateTokenWithParticipantId() {
		ObjectId participantId = new ObjectId();
		AppToken appToken = AppToken.create(participantId);
		assertEquals(participantId, appToken.getParticipantId());
		assertNotNull(appToken.getCreatedAt());
		assertNotNull(appToken.getToken());
	}

}
