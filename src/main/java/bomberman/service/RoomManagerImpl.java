package bomberman.service;

import bomberman.mechanics.WorldBuilderForeman;
import main.accountservice.AccountService;
import main.websockets.MessageSendable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import rest.UserProfile;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

public class RoomManagerImpl implements RoomManager {

    public RoomManagerImpl(AccountService as) {
        accountService = as;
        createNewRoom();
    }

    @Override
    public Room assignUserToFreeRoom(UserProfile user, MessageSendable socket) {
        removeUserFromRoom(user);

        final Room room = getNonFilledNotActiveRoom();

        room.insertPlayer(user, socket);
        playerWhereabouts.put(user, room);
        if (room.isFilled())
            nonFilledRooms.remove();

        return room;
    }

    @Override
    public void removeUserFromRoom(UserProfile user) {
        final Room room = getRoomByUser(user);

        if (room != null) {
            playerWhereabouts.remove(user);
            room.removePlayer(user);

            if (room.isEmpty()) {
                allRooms.remove(room);
                nonFilledRooms.remove(room);
            } else
                if (!room.isActive() && !nonFilledRooms.contains(room))
                    nonFilledRooms.add(room);
        }
    }

    private Room getNonFilledNotActiveRoom() {
        while (true) {
            final Room room = nonFilledRooms.peek();
            if (room == null)
                return createNewRoom();
            else
                if (room.isActive())
                    nonFilledRooms.remove();
                else
                    return room;
        }
    }

    private Room createNewRoom() {
        final Room room = new Room(accountService);

        room.createNewWorld(WorldBuilderForeman.getRandomWorldName());
        nonFilledRooms.add(room);
        allRooms.add(room);

        return room;
    }

    @Nullable
    private Room getRoomByUser(UserProfile user) {
        if (playerWhereabouts.containsKey(user))
            return playerWhereabouts.get(user);
        else
            return null;
    }

    @Override
    public Room getCurrentRoom() {
        return getNonFilledNotActiveRoom();
    }

    @Override
    public void run() {
        long previousTickDuration = Room.MINIMAL_TIME_STEP;

        //noinspection InfiniteLoopStatement
        while (true){
            final long beforeUpdate = TimeHelper.now();
            boolean wereAnyRoomUpdated = false;

            for (Room room: allRooms) {
                boolean wasRoomUpdated = false;
                //noinspection OverlyBroadCatchBlock
                try {
                    wasRoomUpdated = room.updateIfNeeded(previousTickDuration);
                } catch (Exception e) {
                    LOGGER.error("Room (" + room + ") has failed! Removing...", e);
                    //noinspection OverlyBroadCatchBlock
                    try {room.broadcast(MessageCreator.createGameOverMessage(null)); } catch (Exception e2) {LOGGER.error("Could not forceover in (" + room + ") due to fatal crash!", e);}
                    allRooms.remove(room);
                    nonFilledRooms.remove(room);
                }
                if (!wereAnyRoomUpdated && wasRoomUpdated)
                    wereAnyRoomUpdated = true;
            }

            final long totalUpdateTook = TimeHelper.now() - beforeUpdate;

            TimeHelper.sleepFor(Room.MINIMAL_TIME_STEP - totalUpdateTook);

            previousTickDuration = TimeHelper.now() - beforeUpdate;
            logGameCycleTime(totalUpdateTook);

            if (!wereAnyRoomUpdated)
                TimeHelper.sleepFor(100);
        }
    }

    private void logGameCycleTime(long timeSpentWhileRunning) {
        if (timeSpentWhileRunning >= Room.MINIMAL_TIME_STEP)
            LOGGER.warn("RoomManager " + this.toString() + " updated. It took " + timeSpentWhileRunning + " >= " + Room.MINIMAL_TIME_STEP + "! Fix the bugs!");
        else if (timeSpentWhileRunning != 0)
            LOGGER.debug("RoomManager " + this.toString() + " updated. It took " + timeSpentWhileRunning + " < " + Room.MINIMAL_TIME_STEP + ". OK.");
    }

    private AccountService accountService;

    private final Queue<Room> nonFilledRooms = new ConcurrentLinkedQueue<>();
    private final CopyOnWriteArrayList<Room> allRooms = new CopyOnWriteArrayList<>();
    private final Map<UserProfile, Room> playerWhereabouts = new ConcurrentHashMap<>();

    private static final Logger LOGGER = LogManager.getLogger(RoomManagerImpl.class);
}
