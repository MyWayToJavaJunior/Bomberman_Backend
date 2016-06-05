package bomberman.service;

import constants.Constants;
import main.websockets.MessageSendable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
        for (int i = 0; i < NUMBER_OF_USERS; ++i)
            MOCK_USERS.add(new Pair<>(Constants.customMockUserProfile("u" + i, "p" + i, "sid" + i, i), mock(MessageSendable.class)));
    }

    @Test
    public void testAssignUserToFreeRoom() throws Exception {
        final RoomManager roomManager = new RoomManagerImpl(null);
        final Room room1 = roomManager.assignUserToFreeRoom(MOCK_USERS.get(0).getValue0(), MOCK_USERS.get(0).getValue1());

        assertEquals(room1, roomManager.assignUserToFreeRoom(MOCK_USERS.get(1).getValue0(), MOCK_USERS.get(1).getValue1()));
        assertEquals(room1, roomManager.assignUserToFreeRoom(MOCK_USERS.get(2).getValue0(), MOCK_USERS.get(2).getValue1()));
        assertEquals(room1, roomManager.assignUserToFreeRoom(MOCK_USERS.get(3).getValue0(), MOCK_USERS.get(3).getValue1()));
        assertNotEquals(room1, roomManager.assignUserToFreeRoom(MOCK_USERS.get(4).getValue0(), MOCK_USERS.get(4).getValue1()));
    }

    @Test
    public void testAssignUsersToMultipleRooms() throws Exception {
        final RoomManager roomManager = new RoomManagerImpl(null);

        Room prevRoom = null;
        int userNum = 0;
        for (int i = 0; i < MOCK_USERS.size() / ROOM_CAPACTITY; ++i) {
            final Room room = roomManager.assignUserToFreeRoom(MOCK_USERS.get(userNum).getValue0(), MOCK_USERS.get(userNum++).getValue1());

            assertNotEquals(prevRoom, room);
            prevRoom = room;

            //assertEquals(ROOM_CAPACTITY, room.getCapacity());
            if (room.getCapacity() != ROOM_CAPACTITY) {
                LOGGER.warn("World \"" + room.getWorld().getName() + "\" has more spawns than " + ROOM_CAPACTITY + "! It's room " + room);
                fail();
            }

            for (int cap = 1; cap < room.getCapacity(); ++cap)
                assertEquals(room, roomManager.assignUserToFreeRoom(MOCK_USERS.get(userNum).getValue0(), MOCK_USERS.get(userNum++).getValue1()));
        }
    }

    @Test
    public void testRemoveUserFromRoom() throws Exception {
        final RoomManager roomManager = new RoomManagerImpl(null);

        final Room room1 = roomManager.assignUserToFreeRoom(MOCK_USERS.get(0).getValue0(), MOCK_USERS.get(0).getValue1());
        assertEquals(true, room1.hasPlayer(MOCK_USERS.get(0).getValue0()));

        roomManager.removeUserFromRoom(MOCK_USERS.get(0).getValue0());
        assertEquals(false, room1.hasPlayer(MOCK_USERS.get(0).getValue0()));
        assertNotEquals(room1, roomManager.assignUserToFreeRoom(MOCK_USERS.get(0).getValue0(), MOCK_USERS.get(0).getValue1()));
    }

    private static final ArrayList<Pair<UserProfile, MessageSendable>> MOCK_USERS = new ArrayList<>();
    private static final Logger LOGGER = LogManager.getLogger(RoomManagerImplTest.class);
    private static final int NUMBER_OF_USERS = 20;
    private static final int ROOM_CAPACTITY = 4;
}