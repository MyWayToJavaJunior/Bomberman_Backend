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

public class RoomTest {

    @BeforeClass
    public static void init() {
        MOCK_USERS.add(new Pair<>(Constants.customMockUserProfile("u1", "p1", "sid1", 1), mock(MessageSendable.class)));
        MOCK_USERS.add(new Pair<>(Constants.customMockUserProfile("u2", "p2", "sid2", 2), mock(MessageSendable.class)));
        MOCK_USERS.add(new Pair<>(Constants.customMockUserProfile("u3", "p3", "sid3", 3), mock(MessageSendable.class)));
        MOCK_USERS.add(new Pair<>(Constants.customMockUserProfile("u4", "p4", "sid4", 4), mock(MessageSendable.class)));
        MOCK_USERS.add(new Pair<>(Constants.customMockUserProfile("u5", "p5", "sid5", 5), mock(MessageSendable.class)));
    }

    @Test
    public void testGetCurrentCapacity() throws Exception {
        final Room room = new Room();
        assertEquals(0, room.getCurrentCapacity());
        room.insertPlayer(MOCK_USERS.get(0).getValue0(), MOCK_USERS.get(0).getValue1());
        assertEquals(1, room.getCurrentCapacity());
        room.insertPlayer(MOCK_USERS.get(1).getValue0(), MOCK_USERS.get(1).getValue1());
        assertEquals(2, room.getCurrentCapacity());
        room.removePlayer(MOCK_USERS.get(0).getValue0());
        assertEquals(1, room.getCurrentCapacity());
    }

    @Test
    public void testIsFilled() throws Exception {
        final Room room = new Room();
        assertEquals(false, room.isFilled());
        room.insertPlayer(MOCK_USERS.get(0).getValue0(), MOCK_USERS.get(0).getValue1());
        room.insertPlayer(MOCK_USERS.get(1).getValue0(), MOCK_USERS.get(1).getValue1());
        room.insertPlayer(MOCK_USERS.get(2).getValue0(), MOCK_USERS.get(2).getValue1());
        room.insertPlayer(MOCK_USERS.get(3).getValue0(), MOCK_USERS.get(3).getValue1());
        assertEquals(true, room.isFilled());
        assertEquals(false, room.insertPlayer(MOCK_USERS.get(4).getValue0(), MOCK_USERS.get(4).getValue1()));
    }

    @Test
    public void testIsEmpty() throws Exception {
        final Room room = new Room();
        assertEquals(true, room.isEmpty());
        room.insertPlayer(MOCK_USERS.get(0).getValue0(), MOCK_USERS.get(0).getValue1());
        assertEquals(false, room.isEmpty());
    }

    @Test
    public void testInsertPlayer() throws Exception {
        final Room room = new Room();
        assertEquals(false, room.hasPlayer(MOCK_USERS.get(0).getValue0()));
        room.insertPlayer(MOCK_USERS.get(0).getValue0(), MOCK_USERS.get(0).getValue1());
        assertEquals(true, room.hasPlayer(MOCK_USERS.get(0).getValue0()));
    }

    @Test
    public void testHasPlayer() throws Exception {
        final Room room = new Room();
        assertEquals(false, room.hasPlayer(MOCK_USERS.get(0).getValue0()));
        room.insertPlayer(MOCK_USERS.get(0).getValue0(), MOCK_USERS.get(0).getValue1());
        assertEquals(true, room.hasPlayer(MOCK_USERS.get(0).getValue0()));
    }

    @Test
    public void testRemovePlayer() throws Exception {
        final Room room = new Room();
        room.insertPlayer(MOCK_USERS.get(0).getValue0(), MOCK_USERS.get(0).getValue1());
        assertEquals(true, room.hasPlayer(MOCK_USERS.get(0).getValue0()));
        room.removePlayer(MOCK_USERS.get(0).getValue0());
        assertEquals(false, room.hasPlayer(MOCK_USERS.get(0).getValue0()));
    }

    private static final ArrayList<Pair<UserProfile, MessageSendable>> MOCK_USERS = new ArrayList<>();
}