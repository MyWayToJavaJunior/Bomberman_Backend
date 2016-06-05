package main.databaseservice;

import constants.Constants;
import org.junit.Before;
import org.junit.Test;
import rest.UserProfile;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class DataBaseServiceHashMapImplTest {

    @Before
    public void init() throws Exception {
        dataBaseService = new DataBaseServiceHashMapImpl();
    }

    @Test
    public void testAddUser() throws Exception {
        UserProfile user = dataBaseService.addUser(Constants.USER_LOGIN, Constants.USER_PASSWORD);

        assertNotNull(user);
        assertEquals( dataBaseService.getById(user.getId()),  dataBaseService.getByLogin(user.getLogin()));

        user =  dataBaseService.addUser(Constants.USER_LOGIN, Constants.USER_PASSWORD);

        assertNull(user);
    }

    @Test
    public void testDeleteUser() throws Exception {
        final UserProfile user =   dataBaseService.addUser(Constants.USER_LOGIN, Constants.USER_PASSWORD);

        assertNotNull(user);

        final String login = user.getLogin();
        final long id = user.getId();

         dataBaseService.deleteUser(id);

        assertNull( dataBaseService.getById(id));
        assertNull( dataBaseService.getByLogin(login));
    }

    @Test
    public void testGetUsers() throws Exception {
        final UserProfile user1 =  dataBaseService.addUser(Constants.USER_LOGIN, Constants.USER_PASSWORD);
        final UserProfile user2 =  dataBaseService.addUser("user1", "user1");

        assertNotNull(user1);
        assertNotNull(user2);

        final Map<Long, UserProfile> map = new HashMap<>();
        map.put(user1.getId(), user1);
        map.put(user2.getId(), user2);

        final Collection<UserProfile> userData = dataBaseService.getUsers();

        assertNotNull(userData);

        assertEquals(map.values().toString(), userData.toString());
    }

    @Test
    public void testContainsID() throws Exception {
        final UserProfile user =  dataBaseService.addUser(Constants.USER_LOGIN, Constants.USER_PASSWORD);

        assertNotNull(user);
        long id = user.getId();

        assertEquals(true,  dataBaseService.containsID(id));

        id = -1L;

        assertEquals(false,  dataBaseService.containsID(id));

    }

    @Test
    public void testGetById() throws Exception {
        final UserProfile user = dataBaseService.addUser(Constants.USER_LOGIN, Constants.USER_PASSWORD);

        assertNotNull(user);
        final long id = user.getId();

        assertEquals(user, dataBaseService.getById(id));
    }

    @Test
    public void testGetByLogin() throws Exception {
        final UserProfile user = dataBaseService.addUser(Constants.USER_LOGIN, Constants.USER_PASSWORD);

        assertNotNull(user);

        assertEquals(user, dataBaseService.getByLogin(Constants.USER_LOGIN));
    }

    @Test
    public void testContainsLogin() throws Exception {
        final UserProfile user = dataBaseService.addUser(Constants.USER_LOGIN, Constants.USER_PASSWORD);

        assertNotNull(user);

        assertEquals(true, dataBaseService.containsLogin(Constants.USER_LOGIN));

        assertEquals(false, dataBaseService.containsLogin(""));
    }

    private DataBaseService dataBaseService;

}