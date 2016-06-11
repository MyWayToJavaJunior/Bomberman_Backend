package main.websockets;

import main.config.Context;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import javax.servlet.annotation.WebServlet;

@WebServlet(name = "WebSocketConnectionServlet", urlPatterns = {"/game_external_controller"})
public class ExternalControllerWebSocketConnectionServlet extends WebSocketServlet {

    public ExternalControllerWebSocketConnectionServlet(Context context, int timeout) {
        idleTime = timeout;
        this.context = context;
    }

    @Override
    public void configure(WebSocketServletFactory webSocketServletFactory) {
        webSocketServletFactory.getPolicy().setIdleTimeout(idleTime);
        webSocketServletFactory.setCreator(new ExternalControllerWebSocketConnectionCreator(context));
    }

    private final int idleTime;
    private final Context context;
}
