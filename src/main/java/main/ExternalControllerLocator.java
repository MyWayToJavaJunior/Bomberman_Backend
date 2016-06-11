package main;

import main.websockets.ExternalControllerWebSocketConnection;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class ExternalControllerLocator {

    public static void registerController(String name, ExternalControllerWebSocketConnection websocket) {
        controllers.put(name, websocket);
    }

    public static ExternalControllerWebSocketConnection getByName(String name) {
        return controllers.get(name);
    }

    private static ConcurrentHashMap<String, ExternalControllerWebSocketConnection> controllers = new ConcurrentHashMap<>();
}
