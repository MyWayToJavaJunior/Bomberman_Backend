package bomberman.service;

import main.accountservice.AccountService;
import main.websockets.MessageSendable;
import rest.UserProfile;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RoomManagerConcurImpl implements RoomManager {

    public RoomManagerConcurImpl(AccountService as, int numOfManagers) {
        roomManagers = new RoomManager[numOfManagers];

        for (int i = 0; i < numOfManagers; ++i)
            roomManagers[i] = new RoomManagerImpl(as);
    }

    @Override
    public Room assignUserToFreeRoom(UserProfile user, MessageSendable socket) {
        final RoomManager manager = getAppropriateRoomManager();

        playerWhereabouts.put(user, manager);
        return manager.assignUserToFreeRoom(user, socket);
    }

    @Override
    public void removeUserFromRoom(UserProfile user) {
        final RoomManager manager = playerWhereabouts.get(user);
        playerWhereabouts.remove(user);

        manager.removeUserFromRoom(user);
    }

    @Override
    public void run() {

        for (RoomManager manager: roomManagers)
            (new Thread(manager)).start();
    }

    private RoomManager getAppropriateRoomManager() {
        if (roomManagers[lastManager].getCurrentRoom().isActive())
            lastManager = lastManager++ % roomManagers.length;
        return roomManagers[lastManager];
    }

    @Override
    public Room getCurrentRoom() {
        return roomManagers[lastManager].getCurrentRoom();
    }

    @Override
    public Collection<Room> getRoomList() {
        final List<Room> list = new LinkedList<>();

        for (RoomManager roomManager: roomManagers)
            list.addAll(roomManager.getRoomList());

        return list;
    }

    public static final int DEFAULT_THREADS_AMOUNT = 4;

    private int lastManager = 0;
    private final RoomManager[] roomManagers;
    private final Map<UserProfile, RoomManager> playerWhereabouts = new ConcurrentHashMap<>();

}
