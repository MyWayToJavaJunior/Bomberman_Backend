package bomberman.service;

import main.accountservice.AccountService;
import main.websockets.MessageSendable;
import rest.UserProfile;

public interface RoomManager extends Runnable {
    Room assignUserToFreeRoom(UserProfile user, MessageSendable socket);
    void removeUserFromRoom(UserProfile user);

    Room getCurrentRoom();
}
