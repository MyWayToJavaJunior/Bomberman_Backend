package bomberman.service;

import constants.Constants;
import org.junit.Before;
import org.junit.Test;
import rest.UserProfile;

import java.util.HashSet;
import java.util.Set;

import static junit.framework.TestCase.assertEquals;

public class RoomFuncTest {

    private void createPlayers() {
        users = new HashSet<>();

        for (int i = 0; i < 4; ++i)
            users.add(Constants.customMockUserProfile("user" + i, "pass" + i, "sid" + i, i));
    }

    @Before
    public void setupRoom() {
        createPlayers();

        room = new Room();
        room.createNewWorld("spiral-world");

        users.forEach((user) -> room.insertPlayer(user, Constants.GameMechanicsMocks.createMessageSendable(user, room)));

    }

    @Test
    public void testWorldActivationOnUsersReady() {
        users.forEach((user) -> room.updatePlayerState(user, true, false));

        assertEquals(false, room.isActive());

        users.forEach((user) -> room.updatePlayerState(user, false, true));

        assertEquals(false, room.isActive());

        users.forEach((user) -> room.updatePlayerState(user, true, true));

        assertEquals(false, room.isActive());

        TimeHelper.sleepFor(Room.TIME_TO_WAIT_AFTER_READY + ALLOWED_TIME_ERROR);

        assertEquals(true, room.isActive());
    }

    @Test
    public void testGameOverAndDraw() {
        users.forEach((user) -> room.updatePlayerState(user, true, true));
        TimeHelper.sleepFor(Room.TIME_TO_WAIT_AFTER_READY + ALLOWED_TIME_ERROR);

        users.forEach((user) -> room.scheduleBombPlacement(user));

        for (int i = 0; i < LONG_ENOUGH_TIME / Room.MINIMAL_TIME_STEP; ++i)
            room.updateIfNeeded(Room.MINIMAL_TIME_STEP);

        TimeHelper.sleepFor(Room.TIME_TO_WAIT_ON_GAME_OVER + ALLOWED_TIME_ERROR);

        assertEquals(0, room.getWorld().getBombermenIDs().length);
    }

    private Set<UserProfile> users;
    private Room room;

    private static final int ALLOWED_TIME_ERROR = 10; // ms
    private static final int LONG_ENOUGH_TIME = 10000; // ms
}
