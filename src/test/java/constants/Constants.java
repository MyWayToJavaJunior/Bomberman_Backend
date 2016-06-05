package constants;

import bomberman.mechanics.Bomberman;
import bomberman.mechanics.World;
import bomberman.service.Room;
import main.accountservice.AccountService;
import main.accountservice.AccountServiceImpl;
import main.UserTokenManager;
import main.config.Context;
import main.websockets.MessageSendable;
import main.websockets.WebSocketConnection;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.common.WebSocketSession;
import org.json.JSONObject;
import org.mockito.stubbing.Answer;
import rest.UserProfile;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class Constants {

    public static class RestApplicationMocks {

        public static AccountService getMockedAccountService() {
            configure();
            return MOCKED_ACCOUNT_SERVICE;
        }

        public static HttpHeaders getNoCookieHeaders() {
            configure();
            return NO_COOKIE_HEADERS;
        }

        public static HttpHeaders getOkCookieHeaders() {
            configure();
            return OK_COOKIE_HEADERS;
        }

        public static HttpHeaders getWrongCookieHeaders() {
            configure();
            return WRONG_COOKIE_HEADERS;
        }

        private static void configure() {
            if (!isConfigured) {
                when(MOCKED_ACCOUNT_SERVICE.getUser(USER_LOGIN)).thenReturn(MOCKED_USER_PROFILE);
                when(MOCKED_ACCOUNT_SERVICE.getUser(USER_ID)).thenReturn(MOCKED_USER_PROFILE);
                when(MOCKED_ACCOUNT_SERVICE.getBySessionID(USER_SESSION_ID)).thenReturn(MOCKED_USER_PROFILE);
                when(MOCKED_ACCOUNT_SERVICE.hasSessionID(USER_SESSION_ID)).thenReturn(true);
                when(MOCKED_ACCOUNT_SERVICE.logoutUser(USER_SESSION_ID)).thenReturn(true);
                when(MOCKED_ACCOUNT_SERVICE.createNewUser(USER_LOGIN, USER_PASSWORD)).thenReturn(MOCKED_USER_PROFILE);
                when(MOCKED_ACCOUNT_SERVICE.createNewUser(USER_PASSWORD, USER_LOGIN)).thenReturn(null);

                when(MOCKED_USER_PROFILE.toJson()).thenReturn(new JSONObject().put("id", USER_ID).put("login", USER_LOGIN).put("score", 0L));
                when(MOCKED_USER_PROFILE.getId()).thenReturn(USER_ID);
                when(MOCKED_USER_PROFILE.getLogin()).thenReturn(USER_LOGIN);
                when(MOCKED_USER_PROFILE.getPassword()).thenReturn(USER_PASSWORD);
                when(MOCKED_USER_PROFILE.getSessionID()).thenReturn(USER_SESSION_ID);

                final Map<String, Cookie> noCookieMap = new HashMap<>();
                final Map<String, Cookie> okCookieMap = new HashMap<>();
                okCookieMap.put(UserTokenManager.COOKIE_NAME, UserTokenManager.getNewCookieWithSessionID(USER_SESSION_ID));
                final Map<String, Cookie> badCookieMap = new HashMap<>();
                badCookieMap.put(UserTokenManager.COOKIE_NAME, UserTokenManager.getNewCookieWithSessionID("ERRONEOUS DATA"));

                when(NO_COOKIE_HEADERS.getCookies()).thenReturn(noCookieMap);
                when(OK_COOKIE_HEADERS.getCookies()).thenReturn(okCookieMap);
                when(WRONG_COOKIE_HEADERS.getCookies()).thenReturn(badCookieMap);

                isConfigured = true;
            }
        }

        private static final AccountService MOCKED_ACCOUNT_SERVICE = mock(AccountServiceImpl.class);
        private static final UserProfile MOCKED_USER_PROFILE = mock(UserProfile.class);

        private static final HttpHeaders NO_COOKIE_HEADERS = mock(HttpHeaders.class);
        private static final HttpHeaders OK_COOKIE_HEADERS = mock(HttpHeaders.class);
        private static final HttpHeaders WRONG_COOKIE_HEADERS = mock(HttpHeaders.class);

        private static boolean isConfigured = false;

    }

    public static class GameMechanicsMocks {

        public static Session createMockedSession(Answer messageHandler) throws IOException {
            final Session session = mock(WebSocketSession.class);
            final RemoteEndpoint remote = mock(RemoteEndpoint.class);
            when(session.getRemote()).thenReturn(remote);
            doAnswer(messageHandler).when(remote).sendString(anyString());

            return session;
        }

        public static MessageSendable createMessageSendable(UserProfile owner, Room room) {
            final WebSocketConnection connection = new WebSocketConnection(owner, mock(Context.class));

            try {
                connection.setRoom(room);
                //noinspection ConstantConditions
                connection.setSession(createMockedSession((invocationOnMock) -> null));
            } catch (IOException ex) {
                System.err.println("Sure your code haven't changed?");
                ex.printStackTrace();
            }

            return connection;
        }

        public static World getMockedWorld() {
            configure();
            return WORLD;
        }

        public static Bomberman getBomberman() {
            configure();
            return BOMBERMAN;
        }

        private static void configure() {
            if (!isConfigured) {

                final AtomicInteger mockedIDGenerator = new AtomicInteger();
                when(WORLD.getNextID()).thenReturn(mockedIDGenerator.getAndIncrement());

                when(BOMBERMAN.getID()).thenReturn((int) USER_ID);

                isConfigured = true;
            }
        }

        private static final World WORLD = mock(World.class);
        private static final Bomberman BOMBERMAN = mock(Bomberman.class);

        private static boolean isConfigured = false;
    }

    public static UserProfile customMockUserProfile(String login, String password, String sessionID, Integer score) {
        final UserProfile mocked = mock(UserProfile.class);

        final long id = RANDOM_ID_GENERATOR.getAndIncrement();
        when(mocked.getId()).thenReturn(id);
        when(mocked.getLogin()).thenReturn(login);
        when(mocked.getPassword()).thenReturn(password);
        when(mocked.getSessionID()).thenReturn(sessionID);
        when(mocked.getScore()).thenReturn(score);
        when(mocked.toJson()).thenReturn(new JSONObject().put("id", id).put("login", login).put("score", score));

        return mocked;
    }

    public static final String USER_LOGIN = "login";
    public static final String USER_PASSWORD = "password";
    public static final long USER_ID = 0xDEADBEEF;
    public static final String USER_SESSION_ID = "TEST_SESSION_ID";

    private static final AtomicLong RANDOM_ID_GENERATOR = new AtomicLong(1);

    public static final float SOME_ERROR_DELTA = 10e-3f;
}
