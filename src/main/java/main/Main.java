package main;

import bomberman.service.RoomManager;
import main.config.Context;
import main.config.ServerInitializer;
import main.websockets.ExternalControllerWebSocketConnectionServlet;
import main.websockets.UserpicWebSocketConnectionServlet;
import main.websockets.WebSocketConnectionServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rest.Sessions;
import rest.Users;

import java.util.Map;

@SuppressWarnings("OverlyBroadThrowsClause")
public class Main {
    public static void main(String[] args) throws Exception {
        Context context = null;
        Map<String, String> properties = null;

        //noinspection OverlyBroadCatchBlock
        try {
            String propertyFileName = null;
            if (args.length >= 1)
                propertyFileName = args[0];
            final ServerInitializer serverInitializer = new ServerInitializer(propertyFileName);

            properties = serverInitializer.getPropertiesMap();
            context = serverInitializer.fillNewContext();
            UserTokenManager.changeHost(properties.get("host"));
            UserTokenManager.changeMaxAge(Integer.parseInt(properties.get("cookie_max_age")));

        } catch (Exception ex) {
            LOGGER.fatal("Could not setup server. Aborting...", ex);
            System.exit(1);
        }

        final int port = Integer.parseInt(properties.get("port"));
        LOGGER.info("Starting at " + port + " port");
        final Server server = new Server(port);

        final ResourceConfig config = createNewInjectableConfig(context);

        final ServletHolder restServletHolder = new ServletHolder(new ServletContainer(config));

        final ServletHolder websocketServletHolder = new ServletHolder(new WebSocketConnectionServlet(context, Integer.parseInt(properties.get("ws_timeout"))));
        final ServletHolder externalControlWebsocketServletHolder = new ServletHolder(new ExternalControllerWebSocketConnectionServlet(context, Integer.parseInt(properties.get("ws_timeout"))));
        final ServletHolder userpicWebsocketServletHolder = new ServletHolder(new UserpicWebSocketConnectionServlet(context, Integer.parseInt(properties.get("ws_timeout"))));

        final ServletContextHandler contextHandler = new ServletContextHandler(server, "/*");
        contextHandler.addServlet(websocketServletHolder, "/game");
        contextHandler.addServlet(externalControlWebsocketServletHolder, "/game_external_controller");
        contextHandler.addServlet(userpicWebsocketServletHolder, "/game_userpic");
        contextHandler.addServlet(restServletHolder, "/api/*");

        server.setHandler(contextHandler);

        new Thread((RoomManager) context.get(RoomManager.class)).start();

        server.start();
        server.join();
    }

    private static ResourceConfig createNewInjectableConfig(Context context) {
        final ResourceConfig rc  = new ResourceConfig(Users.class, Sessions.class);
        rc.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(context);
            }
        });
        return rc;
    }

    private static final Logger LOGGER = LogManager.getLogger(Main.class);
}
