package main.config;

import bomberman.service.RoomManager;
import bomberman.service.RoomManagerConcurImpl;
import main.accountservice.AccountService;
import main.accountservice.AccountServiceImpl;
import main.databaseservice.DataBaseService;
import main.databaseservice.DataBaseServiceHashMapImpl;
import main.databaseservice.DataBaseServiceMySQLImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;


import java.util.Map;
import java.util.Properties;

public class ServerInitializer {
    public ServerInitializer(@Nullable String propertyFile) {
        if (propertyFile != null)
            reader = new PropertyReader(propertyFile);
        else
            reader = new PropertyReader();
    }

    public ServerInitializer() {
        reader = new PropertyReader();
    }

    public Map<String, String> getPropertiesMap() {
        return reader.getPropertyMap();
    }

    @SuppressWarnings("OverlyBroadThrowsClause")
    public Context fillNewContext() throws Exception {
        final Context context = new Context();

        context.put(DataBaseService.class, createNewDBS());
        context.put(AccountService.class, createNewAccountService((DataBaseService) context.get(DataBaseService.class)));
        context.put(RoomManager.class, createNewRoomManager((AccountService) context.get(AccountService.class)));
        context.put(Properties.class, getPropertiesMap());

        return context;
    }

    private DataBaseService createNewDBS() throws Exception {
        LOGGER.info("Instantiating DataBaseService...");
        final DataBaseService dataBaseService;
        try {
            if (reader.getPropertyMap().containsKey("db_type"))
                switch (reader.getPropertyMap().get("db_type")) {
                    case "hash":
                        LOGGER.info("Launching with HashDB");
                        dataBaseService = new DataBaseServiceHashMapImpl();
                        break;
                    case "debug":
                        LOGGER.info("Launching with debug DB");
                        //noinspection resource
                        dataBaseService = new DataBaseServiceMySQLImpl(reader.getPropertyMap(), DataBaseServiceMySQLImpl.DBTYPE.DEBUG);
                        break;
                    case "production":
                        LOGGER.info("Launching with production DB");
                        //noinspection resource
                        dataBaseService = new DataBaseServiceMySQLImpl(reader.getPropertyMap(), DataBaseServiceMySQLImpl.DBTYPE.PRODUCTION);
                        break;
                    default:
                        LOGGER.fatal("Unknown dbtype argument!");
                        throw new Exception();
                }
            else {
                LOGGER.info("No DB type specified. Launching with production DB.");
                //noinspection resource
                dataBaseService = new DataBaseServiceMySQLImpl(reader.getPropertyMap());
            }
        } catch (Exception ex) {
            LOGGER.fatal("Cannot instantiate DataBaseService.");
            throw ex;
        }
        LOGGER.info("OK.");

        return dataBaseService;
    }

    private AccountService createNewAccountService(DataBaseService dataBaseService) {
        LOGGER.info("Instantiating AccountService...");
        final AccountService accountService = new AccountServiceImpl(dataBaseService);
        LOGGER.info("OK.");
        return accountService;
    }

    private RoomManager createNewRoomManager(AccountService accountService) {
        LOGGER.info("Instantiating RoomManager...");
        int numOfThreads = RoomManagerConcurImpl.DEFAULT_THREADS_AMOUNT;
        if (reader.getPropertyMap().containsKey("game_threads_number"))
            numOfThreads = Integer.parseInt(reader.getPropertyMap().get("game_threads_number"));

        final RoomManager roomManager = new RoomManagerConcurImpl(accountService, numOfThreads);
        LOGGER.info("OK.");
        return roomManager;
    }

    private final PropertyReader reader;

    private static final Logger LOGGER = LogManager.getLogger(ServerInitializer.class);
}
