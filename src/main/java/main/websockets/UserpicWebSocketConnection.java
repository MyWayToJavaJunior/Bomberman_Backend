package main.websockets;

import bomberman.service.*;
import main.config.Context;
import main.databaseservice.SQLClass;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.json.JSONException;
import org.json.JSONObject;
import rest.UserProfile;
import rest.WebErrorManager;

@SuppressWarnings("unused")
@WebSocket
public class UserpicWebSocketConnection implements MessageSendable{

    public UserpicWebSocketConnection(Context globalContext) {

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
        final String messageType = message.getString("type");

        try {
            if (messageType.equals("get_picture"))
                if (WebErrorManager.showFieldsNotPresent(message, "id") != null)
                    sendMessage("No 'id' field!");
                else
                    sendMessage(getPicture(message.getString("id")).toString());

            if (messageType.equals("set_picture"))
                if (WebErrorManager.showFieldsNotPresent(message, "id", "data") != null)
                    sendMessage("No 'id' or 'data' field!");
                else {
                    setPicture(message.getString("id"), message.getString("data"));
                    sendMessage("{}");
                }
        } catch (Exception ignore) {
            ignore.printStackTrace();
            sendMessage(ignore.toString());
        }
    }

    @OnWebSocketConnect
    public void onOpen(Session sess) {
        session = sess;
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
    }

    @Override
    public void sendMessage(String message) {
        //noinspection OverlyBroadCatchBlock
        try {
            if (session.isOpen())
                session.getRemote().sendString(message);
        } catch (Exception ex) {
        }
    }

    private JSONObject getPicture(String userID) {
        final JSONObject template = new JSONObject();
        template.put("type", "picture");
        template.put("id", userID);

        Object picture = SQLClass.ReadObject("SELECT data FROM maintable WHERE user_id = " + userID, SQLClass.CON_PATH);

        if (picture == null)
            template.put("data", JSONObject.NULL);
        else
            template.put("data", picture);

        return template;
    }

    private void setPicture(String userID, String data) {
        Object count = SQLClass.ReadObject("SELECT count(*) FROM maintable WHERE user_id = " + userID, SQLClass.CON_PATH);
        System.out.println("Selected count;");
        if (count != null) {
            System.out.println("Count is not null");
            Long cnt = (Long)count;
            System.out.println("Cnt is " + cnt);
            if (cnt == 0) {
                System.out.println("Cnt equals to 0 ");
                SQLClass.Execute("INSERT INTO maintable (user_id, data) VALUES (" + userID + ", '" + data + "')", SQLClass.CON_PATH);
                System.out.println("Inserted new data");
            }else {
                System.out.println("Cnt does not equal to 0 ");
                SQLClass.Execute("UPDATE maintable SET data = '" + data + "' WHERE user_id = " + userID, SQLClass.CON_PATH);
                System.out.println("Updated data");
            }
        } else {
            System.out.println("Count is null");
            SQLClass.Execute("INSERT INTO maintable (user_id, data) VALUES (" + userID + ", '" + data + "')", SQLClass.CON_PATH);
            System.out.println("Inserted new data");
        }
        System.out.println("Finished");
    }

    private Session session;

    private static final Logger LOGGER = LogManager.getLogger(UserpicWebSocketConnection.class);
}
