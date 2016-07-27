package main.websockets;

import main.config.Context;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import javax.servlet.annotation.WebServlet;

@WebServlet(name = "WebSocketConnectionServlet", urlPatterns = {"/game_userpic"})
public class UserpicWebSocketConnectionServlet extends WebSocketServlet {

    public UserpicWebSocketConnectionServlet(Context context, int timeout) {
        idleTime = timeout;
        this.context = context;
    }

    @Override
    public void configure(WebSocketServletFactory webSocketServletFactory) {
        webSocketServletFactory.getPolicy().setIdleTimeout(idleTime);
        webSocketServletFactory.setCreator(new UserpicWebSocketConnectionCreator(context));
    }

    private final int idleTime;
    private final Context context;
}
