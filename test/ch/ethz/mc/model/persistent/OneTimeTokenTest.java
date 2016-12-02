package ch.ethz.mc.model.persistent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;

import org.junit.Test;

public class OneTimeTokenTest {

	@Test
	public void testCreate() {
		HashSet<String> previousTokens = new HashSet<String>();
		for (int i = 0; i < 10000; i++) {
			String token = OneTimeToken.createToken();
			assertNotNull(token);
			assertEquals(6, token.length());
			assertFalse("Duplicate token after " + i + " repetitions", previousTokens.contains(token));
			assertTrue("Invalid token: "+token, OneTimeToken.isOneTimeToken(token));
			previousTokens.add(token);
		}
	}

}
