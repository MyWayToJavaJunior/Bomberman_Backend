package bomberman.service;

import org.json.JSONObject;
import rest.UserProfile;
import rest.WebErrorManager;

public class ReceivedMessageHandler /*implements Runnable*/ {
    public ReceivedMessageHandler(UserProfile userProfile, Room userRoom, JSONObject jsonMessage) {
        room = userRoom;
        message = jsonMessage;
        user = userProfile;
    }

    public boolean execute() {
        final String messageType = message.getString("type");

        if (!messageType.equals("ping"))
            room.refreshUserKickTimer(user);

        if (messageType.equals("object_changed")) {
            if( WebErrorManager.showFieldsNotPresent(message, "x", "y") != null)
                return false;
            else
                room.scheduleBombermanMovement(user, message.getInt("x"), message.getInt("y"));
            return true;
        }
        if (messageType.equals("bomb_spawned")) {
            room.scheduleBombPlacement(user);
            return true;
        }
        if (messageType.equals("user_state_changed")) {
            if (WebErrorManager.showFieldsNotPresent(message, "isReady", "contentLoaded") != null)
                return false;
            else
                room.updatePlayerState(user, message.getBoolean("isReady"), message.getBoolean("contentLoaded"));
            return true;
        }
        if (messageType.equals("chat_message")) {
            if (WebErrorManager.showFieldsNotPresent(message, "user_id", "text") != null)
                return false;

            room.broadcast(message.toString());

            return true;
        }
        //noinspection RedundantIfStatement
        if (messageType.equals("ping")) {
            return true;
        }
        return false;
    }

    private final UserProfile user;
    private final Room room;
    private final JSONObject message;
}
