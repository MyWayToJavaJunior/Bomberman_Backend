package bomberman.service;

import constants.Constants;
import main.websockets.MessageSendable;
import org.javatuples.Pair;
import org.junit.BeforeClass;
import org.junit.Test;
import rest.UserProfile;

import java.util.ArrayList;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;


public class RoomManagerImplTest {

    @BeforeClass
    public static void init() {
        MOCK_USERS.add(new Pair<>(Constants.customMockUserProfile("u1", "p1", "sid1", 1), mock(MessageSendable.class)));
        MOCK_USERS.add(new Pair<>(Constants.customMockUserProfile("u2", "p2", "sid2", 2), mock(MessageSendable.class)));
        MOCK_USERS.add(new Pair<>(Constants.customMockUserProfile("u3", "p3", "sid3", 3), mock(MessageSendable.class)));
        MOCK_USERS.add(new Pair<>(Constants.customMockUserProfile("u4", "p4", "sid4", 4), mock(MessageSendable.class)));
        MOCK_USERS.add(new Pair<>(Constants.customMockUserProfile("u5", "p5", "sid5", 5), mock(MessageSendable.class)));
    }

    @Test
    public void testAssignUserToFreeRoom() throws Exception {
        final RoomManager roomManager = new RoomManagerImpl();
        final Room room1 = roomManager.assignUserToFreeRoom(MOCK_USERS.get(0).getValue0(), MOCK_USERS.get(0).getValue1());

        assertEquals(room1, roomManager.assignUserToFreeRoom(MOCK_USERS.get(1).getValue0(), MOCK_USERS.get(1).getValue1()));
        assertEquals(room1, roomManager.assignUserToFreeRoom(MOCK_USERS.get(2).getValue0(), MOCK_USERS.get(2).getValue1()));
        assertEquals(room1, roomManager.assignUserToFreeRoom(MOCK_USERS.get(3).getValue0(), MOCK_USERS.get(3).getValue1()));
        assertNotEquals(room1, roomManager.assignUserToFreeRoom(MOCK_USERS.get(4).getValue0(), MOCK_USERS.get(4).getValue1()));
    }

    @Test
    public void testRemoveUserFromRoom() throws Exception {
        final RoomManager roomManager = new RoomManagerImpl();
        final Room room1 = roomManager.assignUserToFreeRoom(MOCK_USERS.get(0).getValue0(), MOCK_USERS.get(0).getValue1());
        assertEquals(true, room1.hasPlayer(MOCK_USERS.get(0).getValue0()));
        roomManager.removeUserFromRoom(MOCK_USERS.get(0).getValue0());
        assertEquals(false, room1.hasPlayer(MOCK_USERS.get(0).getValue0()));
        assertNotEquals(room1, roomManager.assignUserToFreeRoom(MOCK_USERS.get(0).getValue0(), MOCK_USERS.get(0).getValue1()));
    }

    private static final ArrayList<Pair<UserProfile, MessageSendable>> MOCK_USERS = new ArrayList<>();
}