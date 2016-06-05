package main.accountservice;

import constants.Constants;
import main.config.ServerInitializer;
import main.databaseservice.DataBaseServiceHashMapImpl;
import main.databaseservice.DataBaseServiceMySQLImpl;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import rest.UserProfile;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class AccountServiceImplTest {

    @BeforeClass
    public static void setup() {
        asstorage = ASSTORAGE.REAL; // Change it if you wish...
    }

    @Before
    public void init() throws Exception {

        switch (asstorage) {
            case HASHMAP:
                accountService = new AccountServiceImpl(new DataBaseServiceHashMapImpl());
                break;
            case REAL:
                final ServerInitializer serverInitializer = new ServerInitializer(null);
                final Map<String, String> properties = serverInitializer.getPropertiesMap();
                accountService = new AccountServiceImpl(new DataBaseServiceMySQLImpl(properties, DataBaseServiceMySQLImpl.DBTYPE.DEBUG));
                break;
        }
    }

    @Test
    public void testLoginUser() throws Exception {
        final UserProfile user = accountService.createNewUser(Constants.USER_LOGIN, Constants.USER_PASSWORD);

        assertNotNull(user);

        final String sid = user.getSessionID();
        accountService.loginUser(user);

        assertEquals(user, accountService.getBySessionID(sid));

    }

    @Test
    public void testGetBySessionID() throws Exception {

        final UserProfile user = accountService.createNewUser(Constants.USER_LOGIN, Constants.USER_PASSWORD);

        assertNotNull(user);

        final String sid = user.getSessionID();
        accountService.loginUser(user);

        assertEquals(user, accountService.getBySessionID(sid));

        assertNull(accountService.getBySessionID(""));
    }

    @Test
    public void testHasSessionID() throws Exception {
        final UserProfile user = accountService.createNewUser(Constants.USER_LOGIN, Constants.USER_PASSWORD);

        assertNotNull(user);

        final String sid = user.getSessionID();

        assertEquals(false, accountService.hasSessionID(sid));

        accountService.loginUser(user);

        assertEquals(true, accountService.hasSessionID(sid));
    }

    @Test
    public void testLogoutUser() throws Exception {
        final UserProfile user = accountService.createNewUser(Constants.USER_LOGIN, Constants.USER_PASSWORD);

        assertNotNull(user);

        final String sid = user.getSessionID();

        assertEquals(false, accountService.logoutUser(sid));

        accountService.loginUser(user);

        assertEquals(true, accountService.logoutUser(sid));
    }

    @Test
    public void testCreateNewUser() throws Exception {
        UserProfile newUser = accountService.createNewUser(Constants.USER_LOGIN, Constants.USER_PASSWORD);

        assertNotNull(newUser);
        assertEquals(Constants.USER_LOGIN, newUser.getLogin());
        assertEquals(Constants.USER_PASSWORD, newUser.getPassword());

        newUser = accountService.createNewUser(Constants.USER_LOGIN, Constants.USER_PASSWORD);

        assertNull(newUser);
    }

    @Test
    public void testGetAllUsers() throws Exception {
        final UserProfile user1 = accountService.createNewUser(Constants.USER_LOGIN, Constants.USER_PASSWORD);

        assertNotNull(user1);

        final Map<Long, UserProfile> map = new HashMap<>();
        map.put(user1.getId(), user1);

        final Collection<UserProfile> userData = accountService.getAllUsers();
        assertNotNull(userData);

        assertEquals(map.values().toString(), userData.toString());
    }

    @Test
    public void testGetUserByID() throws Exception {
        final UserProfile user = accountService.createNewUser(Constants.USER_LOGIN, Constants.USER_PASSWORD);

        assertNotNull(user);

        Long id = user.getId();

        assertEquals(user, accountService.getUser(id));

        id = Constants.USER_ID;   // Should not happen to have.

        assertNull(accountService.getUser(id));
    }

    @Test
    public void testGetUserByLogin() throws Exception {
        final UserProfile newUser = accountService.createNewUser(Constants.USER_LOGIN, Constants.USER_PASSWORD);

        assertNotNull(newUser);

        UserProfile user = accountService.getUser(Constants.USER_LOGIN);

        assertEquals(newUser, user);

        user = accountService.getUser("");

        assertNull(user);
    }

    @Test
    public void testDeleteUser() throws Exception {
        final UserProfile user = accountService.createNewUser(Constants.USER_LOGIN, Constants.USER_PASSWORD);

        assertNotNull(user);

        Long id = user.getId();
        accountService.deleteUser(id);

        assertNull(accountService.getUser(id));

        id = -1L;
        accountService.deleteUser(id);

        assertNull(accountService.getUser(id));
    }

    AccountService accountService;

    @Test
    public void testUpdateUser() throws Exception {
        UserProfile user = accountService.createNewUser(Constants.USER_LOGIN, Constants.USER_PASSWORD);

        assertNotNull(user);

        final int desiredScore = 100;

        // Different behavior on different internal classes. not good. Better to orient on REAL case.
        if (asstorage == ASSTORAGE.HASHMAP) {
            user.setScore(desiredScore);
            user = accountService.getUser(Constants.USER_LOGIN);
            assertNotNull(user);
            assertNotEquals(desiredScore, user.getScore());
        }

        user.setScore(desiredScore);
        accountService.updateUser(user);

        user = accountService.getUser(Constants.USER_LOGIN);
        assertNotNull(user);
        assertEquals(desiredScore, user.getScore());
    }

    private static ASSTORAGE asstorage;
    private enum ASSTORAGE { HASHMAP, REAL}
}