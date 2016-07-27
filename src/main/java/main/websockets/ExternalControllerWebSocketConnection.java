package main.websockets;

import bomberman.service.*;
import main.config.Context;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.jetbrains.annotations.TestOnly;
import org.json.JSONException;
import org.json.JSONObject;
import rest.UserProfile;

@SuppressWarnings("unused")
@WebSocket
public class ExternalControllerWebSocketConnection implements MessageSendable{

    public ExternalControllerWebSocketConnection(Context globalContext) {
        roomManager = (RoomManager) globalContext.get(RoomManager.class);
    }

    @OnWebSocketMessage
    public void onMessage(String data) {
        final JSONObject message;
        try {
            message = new JSONObject(data);
        } catch (JSONException ex) {
            sendMessage("Bad json!");
            return;
        }
        if (user != null) {
            if (!new ReceivedMessageHandler(user, room, message).execute()) {
                sendMessage("Bad message type!");
                LOGGER.error("Could not handle message from controller #" + name + "! Message text: " + message);
            }
        } else {
            sendMessage("Controller not assigned");
        }
    }

    @OnWebSocketConnect
    public void onOpen(Session sess) {
        session = sess;
        sendMessage(MessageCreator.createControllerGetName(name));
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        LOGGER.error("Closed socket with session \"" + session + "\" for controller #" + name + '!');
    }

    @Override
    public void sendMessage(String message) {
        //noinspection OverlyBroadCatchBlock
        try {
            if (session.isOpen())
                session.getRemote().sendString(message);
        } catch (Exception ex) {
            LOGGER.error("Could not send message to controller #" + name + '!', ex);
        }
    }

    public String getName() {
         return name;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public void setUser(UserProfile user) {
        this.user = user;
    }

    private Session session;
    private Room room;
    private UserProfile user = null;

    private final String name = "CLK" +TimeHelper.now() % 65537;
    private final RoomManager roomManager;

    private static final Logger LOGGER = LogManager.getLogger(ExternalControllerWebSocketConnection.class);
}
