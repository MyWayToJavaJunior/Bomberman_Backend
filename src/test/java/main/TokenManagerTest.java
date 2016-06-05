package main;

import constants.Constants;
import org.junit.Test;

import static org.junit.Assert.*;

public class TokenManagerTest {

    @Test
    public void testGetNewRandomSessionID() throws Exception {
        final String cookie1 = UserTokenManager.getNewRandomSessionID("random", "data");
        final String cookie2 = UserTokenManager.getNewRandomSessionID("for", "different", "cookies");
        final String cookie1duplicate = UserTokenManager.getNewRandomSessionID("random", "data");

        assertNotEquals(cookie1, cookie2);
        assertEquals(cookie1, cookie1duplicate);
    }

    @Test
    public void testGetSIDStringFromHeaders() throws Exception {
        assertEquals(Constants.USER_SESSION_ID, UserTokenManager.getSIDStringFromHeaders(Constants.RestApplicationMocks.getOkCookieHeaders()));
        assertEquals(null, UserTokenManager.getSIDStringFromHeaders(Constants.RestApplicationMocks.getNoCookieHeaders()));
    }
}