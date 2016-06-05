package main.databaseservice;

import constants.Constants;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class UserProfileDataTest {

    @Before
    public void init() {
        user = new UserProfileData(Constants.USER_LOGIN, Constants.USER_PASSWORD);
        user.setId(Constants.USER_ID);
    }

    @Test
    public void testHashCode() throws Exception {
        final UserProfileData user2 = new UserProfileData(Constants.USER_LOGIN, Constants.USER_PASSWORD);

        assertNotEquals(user.hashCode(), user2.hashCode());

        user2.setId(Constants.USER_ID);

        assertEquals(user.hashCode(), user2.hashCode());
    }

    @Test
    public void testEquals() throws Exception {
        assertNotEquals(user, null);

        final UserProfileData user2 = new UserProfileData(Constants.USER_LOGIN, Constants.USER_PASSWORD);

        assertNotEquals(user, user2);

        user2.setId(Constants.USER_ID);

        assertEquals(user, user2);
    }

    private UserProfileData user;
}