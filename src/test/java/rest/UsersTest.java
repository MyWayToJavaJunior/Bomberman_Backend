package rest;

import constants.Constants;
import main.accountservice.AccountService;
import main.UserTokenManager;
import main.config.Context;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

public class UsersTest extends JerseyTest {

    @Test
    public void testOkCreateUser() {
         testCreateUser(RequestFactory.getCreateUserTestData(RequestFactory.CreateUserType.CREATE_OK), true);
    }
    @Test
    public void testAlreadyExistsCreateUser() {
         testCreateUser(RequestFactory.getCreateUserTestData(RequestFactory.CreateUserType.CREATE_USER_EXISTS), false);
    }

    @Test
    public void testOkGetUserByID() {
        testGetUserByID(RequestFactory.getUserByIDTestData(RequestFactory.GetByIDType.GET_BY_ID_OK));
    }
    @Test
    public void testNoUserGetUserByID() {
        testGetUserByID(RequestFactory.getUserByIDTestData(RequestFactory.GetByIDType.GET_BY_WRONG_ID));
    }

    @Test
    public void testOkUpdateUser() {
        testUpdateUser(RequestFactory.getUpdateUserTestData(RequestFactory.UpdateUserType.UPDATE_OK));
    }
    @Test
    public void testWrongCookieUpdateUser() {
        testUpdateUser(RequestFactory.getUpdateUserTestData(RequestFactory.UpdateUserType.UPDATE_ANOTHER_USER));
    }

    @Test
    public void testOkDeleteUser() {
        testDeleteUser(RequestFactory.getDeleteUserTestData(RequestFactory.DeleteUserType.DELETE_OK));
    }
    @Test
    public void testNoCookieDeleteUser() {
        testDeleteUser(RequestFactory.getDeleteUserTestData(RequestFactory.DeleteUserType.DELETE_NOT_LOGGED));
    }
    @Test
    public void testAnotherUserDeleteUser() {
        testDeleteUser(RequestFactory.getDeleteUserTestData(RequestFactory.DeleteUserType.DELETE_ANOTHER_USER));
    }

    @Before
    public void setContext() {
        users.setContext(CONTEXT);
    }

    @BeforeClass
    public static void makeContext() throws InstantiationException {
        final AccountService mockedAccountService = Constants.RestApplicationMocks.getMockedAccountService();
        CONTEXT.put(AccountService.class, mockedAccountService);

        final Map<String, String> properties = new HashMap<>(3);
        properties.put("static_path", "static/");
        properties.put("userpic_width", "80");
        properties.put("userpic_height", "80");
        CONTEXT.put(Properties.class, properties);
    }
    
    public void testCreateUser(Triplet<String, HttpHeaders, Response> data, boolean shouldHaveCookie){
        final Response response = users.createUser(data.getValue0(), data.getValue1());

        assertEquals(data.getValue2().toString(), response.toString());
        assertEquals(data.getValue2().getEntity().toString(), response.getEntity().toString());
        if (shouldHaveCookie)
            assertEquals(SID, response.getCookies().get(UserTokenManager.COOKIE_NAME).getValue());
        else
            assertEquals(false, response.getCookies().containsKey(UserTokenManager.COOKIE_NAME));
    }

    public void testGetUserByID(Pair<Long, Response> data) {
        final Response response = users.getUserByID(data.getValue0());

        assertEquals(data.getValue1().toString(), response.toString());
        assertEquals(data.getValue1().getEntity().toString(), response.getEntity().toString());
    }

    public void testUpdateUser(Triplet<String, HttpHeaders, Response> data) {
        final Response response = users.updateUser(data.getValue0(), data.getValue1());

        assertEquals(data.getValue2().toString(), response.toString());
        assertEquals(data.getValue2().getEntity().toString(), response.getEntity().toString());
    }

    public void testDeleteUser(Triplet<Long, HttpHeaders, Response> data) {
        final Response response = users.deleteUser(data.getValue0(), data.getValue1());

        assertEquals(data.getValue2().toString(), response.toString());
        assertEquals(data.getValue2().getEntity().toString(), response.getEntity().toString());
    }

    // A bit of magic, without which nothing works.
    @Override
    protected Application configure() {
        //noinspection OverlyBroadCatchBlock
        try {
            final ResourceConfig config = new ResourceConfig(Sessions.class);
            final HttpServletRequest request = mock(HttpServletRequest.class);
            //noinspection AnonymousInnerClassMayBeStatic
            config.register(new AbstractBinder() {
                @Override
                protected void configure() {
                    bind(CONTEXT);
                    bind(request).to(HttpServletRequest.class);
                }
            });

            config.getInstances();
            return config;
        } catch (Exception ex) {
            ex.printStackTrace();
            fail();
        }
        return null;
    }

    private static class RequestFactory {
        public static Triplet<String, HttpHeaders, Response> getCreateUserTestData(CreateUserType type){
            switch (type)
            {
                case CREATE_OK:
                    return Triplet.with(okCreateJSON(), NO_COOKIE_HEADERS, okCreateResponse());
                case CREATE_USER_EXISTS:
                    return Triplet.with(wrongCreateJSON(), NO_COOKIE_HEADERS, userExistsCreateResponse());
            }
            throw new IllegalArgumentException();
        }

        public static Pair<Long, Response> getUserByIDTestData(GetByIDType type) {
            switch (type) {
                case GET_BY_ID_OK:
                    return Pair.with(ID, okGetByIDResponse());
                case GET_BY_WRONG_ID:
                    return Pair.with(0L, wrongGetByIDResponse());
            }
            throw new IllegalArgumentException();
        }

        public static Triplet<String, HttpHeaders, Response> getUpdateUserTestData(UpdateUserType type) {
            switch (type) {
                case UPDATE_OK:
                    return Triplet.with(okUpdateJSON(), OK_COOKIE_HEADERS, okUpdateResponse());
                case UPDATE_ANOTHER_USER:
                    return Triplet.with(okUpdateJSON(), WRONG_COOKIE_HEADERS, wrongUpdateResponse());
            }
            throw new IllegalArgumentException();
        }

        public static Triplet<Long, HttpHeaders, Response> getDeleteUserTestData(DeleteUserType type) {
            switch (type) {
                case DELETE_OK:
                    return Triplet.with(ID, OK_COOKIE_HEADERS, okDeleteResponse());
                case DELETE_ANOTHER_USER:
                    return Triplet.with(0L, OK_COOKIE_HEADERS, wrongUserDeleteResponse());
                case DELETE_NOT_LOGGED:
                    return Triplet.with(ID, NO_COOKIE_HEADERS, noCookieDeleteResponse());
            }
            throw new IllegalArgumentException();
        }


        private static String okCreateJSON() {
            return new JSONObject().put("login", LOGIN).put("password", PASSWORD).toString();
        }
        private static String wrongCreateJSON() {
            return new JSONObject().put("login", PASSWORD).put("password", LOGIN).toString();       // Supposing this user is already registered;
        }
        private static Response okCreateResponse() {
            return Response.ok(new JSONObject().put("id", ID).toString()).build();
        }
        private static Response userExistsCreateResponse() {
            return WebErrorManager.accessForbidden("User already exists!");
        }

        private static Response okGetByIDResponse() {
            return Response.ok(new JSONObject().put("id", ID).put("login", LOGIN).put("score", 0L).toString()).build();
        }
        private static Response wrongGetByIDResponse() {
            return WebErrorManager.accessForbidden();
        }

        private static String okUpdateJSON() {
            return new JSONObject().put("login", LOGIN).put("password", PASSWORD).toString();
        }
        private static Response okUpdateResponse() {
            return Response.ok(new JSONObject().put("id", ID).toString()).build();
        }
        private static Response wrongUpdateResponse() {
            return WebErrorManager.authorizationRequired("Not logged in!");
        }

        private static Response okDeleteResponse() {
            return WebErrorManager.ok();
        }
        private static Response wrongUserDeleteResponse() {
            return WebErrorManager.accessForbidden("Not your user!");
        }
        private static Response noCookieDeleteResponse() {
            return WebErrorManager.authorizationRequired("Not logged in!");
        }

        @SuppressWarnings("InnerClassTooDeeplyNested")
        public enum CreateUserType {CREATE_OK, CREATE_USER_EXISTS}
        @SuppressWarnings("InnerClassTooDeeplyNested")
        public enum GetByIDType {GET_BY_ID_OK, GET_BY_WRONG_ID}
        @SuppressWarnings("InnerClassTooDeeplyNested")
        public enum UpdateUserType {UPDATE_OK, UPDATE_ANOTHER_USER}
        @SuppressWarnings("InnerClassTooDeeplyNested")
        public enum DeleteUserType {DELETE_OK, DELETE_ANOTHER_USER, DELETE_NOT_LOGGED}
    }


    private static final Context CONTEXT = new Context();
    private final Users users = new Users();
    private static final HttpHeaders NO_COOKIE_HEADERS = Constants.RestApplicationMocks.getNoCookieHeaders();
    private static final HttpHeaders OK_COOKIE_HEADERS = Constants.RestApplicationMocks.getOkCookieHeaders();
    private static final HttpHeaders WRONG_COOKIE_HEADERS = Constants.RestApplicationMocks.getWrongCookieHeaders();
    @SuppressWarnings("ConstantNamingConvention")
    private static final long ID = Constants.USER_ID;
    private static final String LOGIN = Constants.USER_LOGIN;
    private static final String PASSWORD = Constants.USER_PASSWORD;
    private static final String SID = Constants.USER_SESSION_ID;


}
