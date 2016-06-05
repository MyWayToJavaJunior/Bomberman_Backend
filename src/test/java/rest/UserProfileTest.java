package rest;

import main.databaseservice.UserProfileData;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

public class UserProfileTest {

    @Test
    public void testToJson() throws JSONException {
        final JSONObject userJson = new UserProfile(new UserProfileData("", "")).toJson();
        final JSONObject fieldsJson = new JSONObject().put("id", -1).put("login", "").put("score", 0).put("userpic_path", JSONObject.NULL);

        assertEquals(fieldsJson.toString(), userJson.toString());
    }

    
    @Test
    public void testGetDifferentSessionID() {
        final UserProfile user1 = new UserProfile(new UserProfileData("user1", ""));
        final UserProfile user2 = new UserProfile(new UserProfileData("user2", ""));

        assertNotSame(user1.getSessionID(), user2.getSessionID());
    }

    
    @Test
    public void testGetPassword() throws Exception {
        final String password = "password";
        final UserProfile user = new UserProfile(new UserProfileData("login", password));

        assertEquals(password, user.getPassword());
    }

    
    @Test
    public void testSetPassword() throws Exception {
        String password = "password";
        final UserProfile user = new UserProfile(new UserProfileData("login", password));
        password = "login";

        assertNotSame(password, user.getPassword());

        user.setPassword(password);

        assertEquals(password, user.getPassword());

    }

    
    @Test
    public void testGetScore() throws Exception {
        final int score = 0;
        final UserProfile user = new UserProfile(new UserProfileData("login", "password"));

        assertEquals(score, user.getScore());
    }

    
    @Test
    public void testSetScore() throws Exception {
        final int score = 1;
        final UserProfile user = new UserProfile(new UserProfileData("login", "password"));

        assertNotSame(score, user.getScore());

        user.setScore(score);

        assertEquals(score, user.getScore());
    }

    
    @Test
    public void testHashCode() throws Exception {
        final UserProfile user1 = new UserProfile(new UserProfileData("admin", "admin"));
        user1.getData().setId(1);
        final UserProfile user2 = new UserProfile(new UserProfileData("guest", "1234"));

        assertEquals(user1.hashCode(), user1.hashCode());
        assertNotSame(user1.hashCode(), user2.hashCode());

         final Map<Long, UserProfile> map = new HashMap<>();

        map.put(user1.getId(), user1);

        assertEquals(false, map.isEmpty());
        assertEquals(user1, map.get(user1.getId()));

        map.put(user2.getId(), user2);

        assertEquals(false, map.isEmpty());
        assertEquals(user1, map.get(user1.getId()));
        assertEquals(user2, map.get(user2.getId()));
        assertNotSame(user1, map.get(user2.getId()));
        assertNotSame(user2, map.get(user1.getId()));

    }

    
    @Test
    public void testEquals() throws Exception {
        final UserProfile user1 = new UserProfile(new UserProfileData("admin", "admin"));
        final UserProfile user2 = new UserProfile(new UserProfileData("guest", "1234"));

        assertEquals(user1, user1);
        assertEquals(false, user2.equals(user1));
    }
}