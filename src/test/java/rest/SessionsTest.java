package rest;

import constants.Constants;
import main.accountservice.AccountService;
import main.UserTokenManager;
import main.config.Context;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import org.glassfish.jersey.test.JerseyTest;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.*;

public class SessionsTest extends JerseyTest {

    @Test
    public void testOkLogin() {
        testLogin(RequestFactory.getLoginTestData(RequestFactory.LoginRequestType.LOGIN_OK), true);
    }
    @Test
    public void testWrongLoginLogin() {
        testLogin(RequestFactory.getLoginTestData(RequestFactory.LoginRequestType.LOGIN_WRONG_LOGIN), false);
    }
    @Test
    public void testWrongPasswordLogin() {
        testLogin(RequestFactory.getLoginTestData(RequestFactory.LoginRequestType.LOGIN_WRONG_PASSWORD), false);
    }

    @Test
    public void testOkIsAuthenticated() {
        testIsAuthenticated(RequestFactory.getIsAuthenticatedTestData(RequestFactory.IsAuthRequestType.IS_AUTH_OK));
    }
    @Test
    public void testNoCookieIsAuthenticated() {
        testIsAuthenticated(RequestFactory.getIsAuthenticatedTestData(RequestFactory.IsAuthRequestType.IS_AUTH_NO_COOKIE));
    }
    @Test
    public void testWrongCookieIsAuthenticated() {
        testIsAuthenticated(RequestFactory.getIsAuthenticatedTestData(RequestFactory.IsAuthRequestType.IS_AUTH_WRONG_COOKIE));
    }

    @Test
    public void testOkLogout() {
        testLogout(RequestFactory.getLogoutTestData(RequestFactory.LogoutRequestType.LOGOUT_LOGGED));
    }
    @Test
    public void testNoCookieLogout() {
        testLogout(RequestFactory.getLogoutTestData(RequestFactory.LogoutRequestType.LOGOUT_NOT_LOGGED));
    }
    @Test
    public void testWrongCookieLogout() {
        testLogout(RequestFactory.getLogoutTestData(RequestFactory.LogoutRequestType.LOGOUT_WRONG_COOKIE));
    }

    @Before
    public void setContext() {
        sessions.setContext(CONTEXT);
    }



    public void testLogin(Triplet<String, HttpHeaders, Response> data, boolean shouldHaveCookie){
        final Response response = sessions.loginUser(data.getValue0(), data.getValue1());

        assertEquals(data.getValue2().toString(), response.toString());
        assertEquals(data.getValue2().getEntity().toString(), response.getEntity().toString());
        if (shouldHaveCookie)
            assertEquals(SID, response.getCookies().get(UserTokenManager.COOKIE_NAME).getValue());
        else
            assertEquals(false, response.getCookies().containsKey(UserTokenManager.COOKIE_NAME));
    }

    public void testIsAuthenticated(Pair<HttpHeaders, Response> data) {
        final Response response = sessions.isAuthenticated(data.getValue0());

        assertEquals(data.getValue1().toString(), response.toString());
        assertEquals(data.getValue1().getEntity().toString(), response.getEntity().toString());
    }

    public void testLogout(Pair<HttpHeaders, Response> data) {
        final Response response = sessions.logoutUser(data.getValue0());

        assertEquals(data.getValue1().toString(), response.toString());
        assertEquals(data.getValue1().getEntity().toString(), response.getEntity().toString());
        assertNotSame(SID, response.getCookies().get(UserTokenManager.COOKIE_NAME).getValue());
    }

    @BeforeClass
    public static void makeContext() throws InstantiationException {
        final AccountService mockedAccountService = Constants.RestApplicationMocks.getMockedAccountService();
        CONTEXT.put(AccountService.class, mockedAccountService);
    }

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

            return config;
        } catch (Exception ex) {
            ex.printStackTrace();
            fail();
        }
        return null;
    }

    private static class RequestFactory {
        public static Triplet<String, HttpHeaders, Response> getLoginTestData(LoginRequestType type){
            switch (type)
            {
                case LOGIN_OK:
                    return Triplet.with(okLoginJSON(), NO_COOKIE_HEADERS, okLoginResponse());
                case LOGIN_WRONG_LOGIN:
                    return Triplet.with(wrongLoginLoginJSON(), NO_COOKIE_HEADERS, wrongLoginLoginResponse());
                case LOGIN_WRONG_PASSWORD:
                    return Triplet.with(wrongPasswordLoginJSON(), NO_COOKIE_HEADERS, wrongPasswordLoginResponse());
            }
            throw new IllegalArgumentException();
        }

        public static Pair<HttpHeaders, Response> getIsAuthenticatedTestData(IsAuthRequestType type) {
            switch (type) {
                case IS_AUTH_OK:
                    return Pair.with(OK_COOKIE_HEADERS, okIsAuthResponse());
                case IS_AUTH_NO_COOKIE:
                    return Pair.with(NO_COOKIE_HEADERS, wrongIsAuthResponse());
                case IS_AUTH_WRONG_COOKIE:
                    return Pair.with(WRONG_COOKIE_HEADERS, wrongIsAuthResponse());
            }
            throw new IllegalArgumentException();
        }

        public static Pair<HttpHeaders, Response> getLogoutTestData(LogoutRequestType type) {
            switch (type) {
                case LOGOUT_LOGGED:
                    return Pair.with(OK_COOKIE_HEADERS, okLogoutResponse());
                case LOGOUT_NOT_LOGGED:
                    return Pair.with(NO_COOKIE_HEADERS, wrongLogoutResponse());
                case LOGOUT_WRONG_COOKIE:
                    return Pair.with(WRONG_COOKIE_HEADERS, wrongLogoutResponse());
            }
            throw new IllegalArgumentException();
        }


        private static String okLoginJSON() {
            return new JSONObject().put("login", LOGIN).put("password", PASSWORD).toString();
        }
        private static Response okLoginResponse() {
            return Response.ok(new JSONObject().put("id", ID).toString()).build();
        }
        private static String wrongLoginLoginJSON() {
            return new JSONObject().put("login", "").put("password", PASSWORD).toString();
        }
        private static Response wrongLoginLoginResponse() {
            return WebErrorManager.authorizationRequired("Wrong login!");
        }
        private static String wrongPasswordLoginJSON() {
            return new JSONObject().put("login", LOGIN).put("password", "").toString();
        }
        private static Response wrongPasswordLoginResponse() {
            return WebErrorManager.authorizationRequired("Wrong login-password pair!");
        }
        private static Response okIsAuthResponse() {
            return Response.ok(new JSONObject().put("id", ID).toString()).build();
        }
        private static Response wrongIsAuthResponse() {
            return WebErrorManager.authorizationRequired();
        }
        private static Response okLogoutResponse() {
            return WebErrorManager.okRaw("You have succesfully logged out.").cookie(UserTokenManager.getNewNullCookie()).build();
        }
        private static Response wrongLogoutResponse() {
            return WebErrorManager.ok("You was not logged in.");
        }

        @SuppressWarnings("InnerClassTooDeeplyNested")
        public enum LoginRequestType {LOGIN_OK, LOGIN_WRONG_LOGIN, LOGIN_WRONG_PASSWORD}
        @SuppressWarnings("InnerClassTooDeeplyNested")
        public enum IsAuthRequestType {IS_AUTH_OK, IS_AUTH_WRONG_COOKIE, IS_AUTH_NO_COOKIE}
        @SuppressWarnings("InnerClassTooDeeplyNested")
        public enum LogoutRequestType {LOGOUT_LOGGED, LOGOUT_NOT_LOGGED, LOGOUT_WRONG_COOKIE}
    }


    private static final Context CONTEXT = new Context();
    private final Sessions sessions = new Sessions();

    private static final HttpHeaders NO_COOKIE_HEADERS = Constants.RestApplicationMocks.getNoCookieHeaders();
    private static final HttpHeaders OK_COOKIE_HEADERS = Constants.RestApplicationMocks.getOkCookieHeaders();
    private static final HttpHeaders WRONG_COOKIE_HEADERS = Constants.RestApplicationMocks.getWrongCookieHeaders();
    @SuppressWarnings("ConstantNamingConvention")
    private static final long ID = Constants.USER_ID;
    private static final String LOGIN = Constants.USER_LOGIN;
    private static final String PASSWORD = Constants.USER_PASSWORD;
    private static final String SID = Constants.USER_SESSION_ID;
}
