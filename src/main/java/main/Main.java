package main;

import bomberman.service.RoomManager;
import main.accountservice.AccountService;
import main.config.Context;
import main.config.ServerInitializer;
import main.websocketconnection.WebSocketConnectionServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

@SuppressWarnings("OverlyBroadThrowsClause")
public class Main {
    public static void main(String[] args) throws Exception {
        Context context = null;
        Map<String, String> properties = null;

        try {
            String propertyFileName = null;
            if (args.length >= 1)
                propertyFileName = args[0];
            final ServerInitializer serverInitializer = new ServerInitializer(propertyFileName);

            properties = serverInitializer.getPropertiesMap();
            context = serverInitializer.fillNewContext();
        } catch (Exception ex) {
            LOGGER.fatal("Could not setup server. Aborting...", ex);
            System.exit(1);
        }


        final Context bindableContext = context;
        final ResourceConfig config = new ResourceConfig(RestApplication.class);
        config.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(bindableContext);
            }
        });

        final int port = Integer.parseInt(properties.get("port"));
        LOGGER.info("Starting at " + port + " port");
        final Server server = new Server(port);

        final ServletContextHandler contextHandler = new ServletContextHandler(server, "/api/", ServletContextHandler.SESSIONS);
        final ServletHolder restServletHolder = new ServletHolder(ServletContainer.class);
        restServletHolder.setInitParameter("javax.ws.rs.Application", "main.RestApplication");

        contextHandler.addServlet(restServletHolder, "/*");
        contextHandler.addServlet(new ServletHolder(new WebSocketConnectionServlet(((RoomManager) context.get(RoomManager.class)), ((AccountService) context.get(AccountService.class)))), "/game");

        server.start();
        server.join();
    }

    private static final Logger LOGGER = LogManager.getLogger(Main.class);
}
