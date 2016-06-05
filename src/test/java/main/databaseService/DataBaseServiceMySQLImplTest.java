package main.databaseservice;

import constants.Constants;
import main.config.ServerInitializer;
import org.junit.Before;
import org.junit.Test;
import rest.UserProfile;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class DataBaseServiceMySQLImplTest {

    @Before
    public void init() throws Exception {
        final ServerInitializer serverInitializer = new ServerInitializer(null);
        final Map<String, String> properties = serverInitializer.getPropertiesMap();
        dataBase = new DataBaseServiceMySQLImpl(properties, DataBaseServiceMySQLImpl.DBTYPE.DEBUG);
    }

    @Test
    public void testAddUser() throws Exception {
        UserProfile user = dataBase.addUser(Constants.USER_LOGIN, Constants.USER_PASSWORD);

        assertNotNull(user);
        assertEquals( dataBase.getById(user.getId()),  dataBase.getByLogin(user.getLogin()));

        user =  dataBase.addUser(Constants.USER_LOGIN, Constants.USER_PASSWORD);

        assertNull(user);
    }

    @Test
    public void testDeleteUser() throws Exception {
        final UserProfile user =   dataBase.addUser(Constants.USER_LOGIN, Constants.USER_PASSWORD);

        assertNotNull(user);

        final String login = user.getLogin();
        final long id = user.getId();

         dataBase.deleteUser(id);

        assertNull( dataBase.getById(id));
        assertNull( dataBase.getByLogin(login));
    }

    @Test
    public void testGetUsers() throws Exception {
        final UserProfile user1 =  dataBase.addUser(Constants.USER_LOGIN, Constants.USER_PASSWORD);
        final UserProfile user2 =  dataBase.addUser("user1", "user1");

        assertNotNull(user1);
        assertNotNull(user2);

        final Map<Long, UserProfile> map = new HashMap<>();
        map.put(user1.getId(), user1);
        map.put(user2.getId(), user2);

        final Collection<UserProfile> userData = dataBase.getUsers();

        assertNotNull(userData);

        assertEquals(map.values().toString(), userData.toString());
    }

    @Test
    public void testContainsID() throws Exception {
        final UserProfile user =  dataBase.addUser(Constants.USER_LOGIN, Constants.USER_PASSWORD);

        assertNotNull(user);
        long id = user.getId();

        assertEquals(true,  dataBase.containsID(id));

        id = -1L;

        assertEquals(false,  dataBase.containsID(id));

    }

    @Test
    public void testSave() throws Exception {
        UserProfile user = dataBase.addUser(Constants.USER_LOGIN, Constants.USER_PASSWORD);

        assertNotNull(user);

        final int desiredScore = 100;
        user.setScore(desiredScore);

        user = dataBase.getByLogin(Constants.USER_LOGIN);
        assertNotNull(user);
        assertNotEquals(desiredScore, user.getScore());

        user.setScore(desiredScore);
        dataBase.save(user.getData());

        user = dataBase.getByLogin(Constants.USER_LOGIN);
        assertNotNull(user);
        assertEquals(desiredScore, user.getScore());
    }

    @Test
    public void testGetById() throws Exception {
        final UserProfile user = dataBase.addUser(Constants.USER_LOGIN, Constants.USER_PASSWORD);

        assertNotNull(user);
        final long id = user.getId();

        assertEquals(user, dataBase.getById(id));
    }

    @Test
    public void testGetByLogin() throws Exception {
        final UserProfile user = dataBase.addUser(Constants.USER_LOGIN, Constants.USER_PASSWORD);

        assertNotNull(user);

        assertEquals(user, dataBase.getByLogin(Constants.USER_LOGIN));
    }

    @Test
    public void testContainsLogin() throws Exception {
        final UserProfile user = dataBase.addUser(Constants.USER_LOGIN, Constants.USER_PASSWORD);

        assertNotNull(user);

        assertEquals(true, dataBase.containsLogin(Constants.USER_LOGIN));

        assertEquals(false, dataBase.containsLogin(""));
    }

    private DataBaseServiceMySQLImpl dataBase;

}