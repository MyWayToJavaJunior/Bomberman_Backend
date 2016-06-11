package main.websockets;


import main.ExternalControllerLocator;
import main.UserTokenManager;
import main.accountservice.AccountService;
import main.config.Context;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.jetbrains.annotations.Nullable;
import rest.UserProfile;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.HttpCookie;

public class ExternalControllerWebSocketConnectionCreator implements WebSocketCreator {

    public ExternalControllerWebSocketConnectionCreator(Context context) {
        bindableContext = context;
        accountService = (AccountService) bindableContext.get(AccountService.class);
    }

    @Override
    @Nullable
    public Object createWebSocket(ServletUpgradeRequest servletUpgradeRequest, ServletUpgradeResponse servletUpgradeResponse) {

        ExternalControllerWebSocketConnection websocket = new ExternalControllerWebSocketConnection(bindableContext);

        ExternalControllerLocator.registerController(websocket.getName(), websocket);

        return websocket;
    }

    private final AccountService accountService;
    private final Context bindableContext;

    private static final Logger LOGGER = LogManager.getLogger(ExternalControllerWebSocketConnectionCreator.class);
}
