package main.websockets;


import main.ExternalControllerLocator;
import main.accountservice.AccountService;
import main.config.Context;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.jetbrains.annotations.Nullable;

public class UserpicWebSocketConnectionCreator implements WebSocketCreator {

    public UserpicWebSocketConnectionCreator(Context context) {
        bindableContext = context;
        accountService = (AccountService) bindableContext.get(AccountService.class);
    }

    @Override
    @Nullable
    public Object createWebSocket(ServletUpgradeRequest servletUpgradeRequest, ServletUpgradeResponse servletUpgradeResponse) {

        UserpicWebSocketConnection websocket = new UserpicWebSocketConnection(bindableContext);

        return websocket;
    }

    private final AccountService accountService;
    private final Context bindableContext;

    private static final Logger LOGGER = LogManager.getLogger(UserpicWebSocketConnectionCreator.class);
}
