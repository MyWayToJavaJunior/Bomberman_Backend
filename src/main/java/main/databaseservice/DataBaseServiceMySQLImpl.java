package main.databaseservice;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.*;
import org.jetbrains.annotations.Nullable;
import rest.UserProfile;

import java.sql.*;
import java.util.*;

import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

public class DataBaseServiceMySQLImpl implements DataBaseService, AutoCloseable {

    public DataBaseServiceMySQLImpl(Map<String, String> parameters) throws Exception {
        config = new Config(parameters, DBTYPE.PRODUCTION);
        createDBAndSetup();
    }

    public DataBaseServiceMySQLImpl(Map<String, String> parameters, DBTYPE dbtype) throws Exception {
        config = new Config(parameters, dbtype);
        createDBAndSetup();
    }

    public void createDBAndSetup() throws Exception {
        try {
            setup();
        } catch (HibernateException ex) {
            //noinspection OverlyBroadCatchBlock
            try {
                LOGGER.warn("Could not connect to DB. Attempting to create it.");
                connectToDBOrDie();
                setup();
            } catch (Exception ex2) {
                LOGGER.fatal("Check if MySQL is running and is compatible with 5.1.38 mysql/j.", ex2);
                throw ex2;
            }
        }
    }

    @Override
    public void save(UserProfileData dataSet) {
        try (Session session = sessionFactory.openSession()) {
            final Transaction transaction = session.beginTransaction();
            final UserProfileDataDAO dao = new UserProfileDataDAO(session);
            dao.save(dataSet);
            transaction.commit();
        }
    }

    @Override
    @Nullable
    public UserProfile getById(long id) {
        try (Session session = sessionFactory.openSession()) {
            final UserProfileDataDAO dao = new UserProfileDataDAO(session);
            final UserProfileData data = dao.read(id);
            if (data == null)
                return null;
            else
                return new UserProfile(dao.read(id));
        } catch (HibernateException ex) {
            final String reason = "Could not get user #" + id + " by ID!";
            LOGGER.info(reason, ex);
            return null;
        }
    }

    @Override
    @Nullable
    public UserProfile getByLogin(String name) {
        try (Session session = sessionFactory.openSession()) {
            final UserProfileDataDAO dao = new UserProfileDataDAO(session);
            final UserProfileData data = dao.readByName(name);
            if (data == null)
                return null;
            else
                return new UserProfile(dao.readByName(name));
        } catch (HibernateException ex) {
            final String reason = "Could not get user \"" + name + "\" by login!";
            LOGGER.info(reason, ex);
            return null;
        }
    }

    @Override
    @Nullable
    public Collection<UserProfile> getUsers() {
        try (Session session = sessionFactory.openSession()) {
            final UserProfileDataDAO dao = new UserProfileDataDAO(session);

            final LinkedList<UserProfile> result = new LinkedList<>();
            dao.readAll().stream().forEach((data) -> result.add(new UserProfile(data)));
            return result;
        } catch (HibernateException ex) {
            final String reason = "Could not get all users somewhy! O_o";
            LOGGER.error(reason, ex);
            return null;
        }

    }

    @Override
    @Nullable
    public Collection<UserProfile> getTop10Users() {
        try (Session session = sessionFactory.openSession()) {
            final UserProfileDataDAO dao = new UserProfileDataDAO(session);

            final LinkedList<UserProfile> result = new LinkedList<>();
            dao.readTop10().stream().forEach((data) -> result.add(new UserProfile(data)));
            return result;
        } catch (HibernateException ex) {
            final String reason = "Could not get Top10 users somewhy! O_o";
            LOGGER.error(reason, ex);
            return null;
        }

    }

    @Override
    @Nullable
    public UserProfile addUser(String login, String password, boolean isGuest) {
        if (containsLogin(login))
            return null;
        final UserProfileData newUser = new UserProfileData(login, password, isGuest);
        save(newUser);
        return getByLogin(login);
    }

    @Override
    public boolean containsID(Long id) {
        return getById(id) != null;
    }

    @Override
    public boolean containsLogin(String name) {
        return getByLogin(name) != null;
    }

    @Override
    public void deleteUser(Long id) {
        try (Session session = sessionFactory.openSession()) {
            final UserProfileDataDAO dao = new UserProfileDataDAO(session);
            dao.delete(id);
        } catch (HibernateException ex) {
            final String reason = "Could not delete user #" + id + '!';
            LOGGER.error(reason, ex);
        }
    }

    @Override
    public void close() {
        sessionFactory.close();
    }

    
    private static SessionFactory createSessionFactory(Configuration configuration) {
        final StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder();
        builder.applySettings(configuration.getProperties());
        final ServiceRegistry serviceRegistry = builder.build();
        return configuration.buildSessionFactory(serviceRegistry);
    }
    
    private void setup() throws HibernateException {
        LOGGER.info("-----Initializing Hibernate-----");
        final Configuration configuration = new Configuration();
        configuration.addAnnotatedClass(UserProfileData.class);

        configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        configuration.setProperty("hibernate.connection.driver_class", "com.mysql.jdbc.Driver");
        configuration.setProperty("hibernate.connection.url", config.getAddress() + config.getDbName());
        configuration.setProperty("hibernate.connection.username", config.getLogin());
        configuration.setProperty("hibernate.connection.password", config.getPassword());
        configuration.setProperty("hibernate.show_sql", "true");
        configuration.setProperty("hibernate.hbm2ddl.auto", config.getCreationMethod());

        sessionFactory = createSessionFactory(configuration);
        LOGGER.info("-----Hibernate  Initialized-----");
    }

    @SuppressWarnings({"JDBCResourceOpenedButNotSafelyClosed"})
    private void connectToDBOrDie() throws Exception {
        Connection rootConnection = null;
        //noinspection OverlyBroadCatchBlock
        try {
            final Driver driver = (Driver) Class.forName("com.mysql.jdbc.Driver").newInstance();
            DriverManager.registerDriver(driver);
            DriverManager.setLoginTimeout(1);
            rootConnection = DriverManager.getConnection(config.getAddress(), "root", config.getRootPassword());
            final Statement statement = rootConnection.createStatement();

            statement.execute("DROP USER IF EXISTS " + config.getLoginDomain());
            statement.execute("CREATE USER " + config.getLoginDomain() + " IDENTIFIED BY \"" + config.getPassword() + "\";");

            statement.execute("DROP DATABASE IF EXISTS " + config.getDbName() + ';');
            statement.execute("CREATE DATABASE IF NOT EXISTS " + config.getDbName() + ';');

            statement.execute("GRANT ALL ON " + config.getDbName() + ".* TO " + config.getLoginDomain() + ';');

            LOGGER.info("Database succesfully created!");
        } catch (Exception ex) {
            LOGGER.fatal("Could not create database!", ex);
            throw ex;
        } finally {
            if (rootConnection != null) try {rootConnection.close();} catch (SQLException ignore) {/*ignore.printStackTrace();*/}
        }
    }

    private SessionFactory sessionFactory;
    private final Config config;

    private static final Logger LOGGER = LogManager.getLogger(DataBaseServiceMySQLImpl.class);

    public enum DBTYPE {
        PRODUCTION, DEBUG
    }

    private static final class Config {

        private Config(Map<String, String> externalParameters, DBTYPE dbtype) {
            parameters = externalParameters;

            switch (dbtype) {
                case PRODUCTION:
                    makeProduction();
                    break;
                case DEBUG:
                    makeDebug();
                    break;
            }
        }

        private void makeProduction() {
            dbName = parameters.get("db_name");
            login = parameters.get("db_user");
            password = parameters.get("db_password");
            loginDomain = parameters.get("db_user") + '@' + parameters.get("db_domain");
            creationMethod = parameters.get("db_creation_method");
            address = "jdbc:mysql://" + parameters.get("db_domain") + ':' + parameters.get("db_port") + '/';
            rootPassword = parameters.get("db_root_password");
        }

        private void makeDebug() {
            dbName = parameters.get("db_name_debug");
            login = parameters.get("db_user_debug");
            password = parameters.get("db_password_debug");
            loginDomain = parameters.get("db_user_debug") + '@' + parameters.get("db_domain");
            creationMethod = parameters.get("db_creation_method_debug");
            address = "jdbc:mysql://" + parameters.get("db_domain") + ':' + parameters.get("db_port") + '/';
            rootPassword = parameters.get("db_root_password");
        }

        public String getLoginDomain() {
            return loginDomain;
        }

        public String getDbName() {
            return dbName;
        }

        public String getRootPassword() {
            return rootPassword;
        }

        public String getLogin() {
            return login;

        }

        public String getPassword() {
            return password;
        }

        public String getCreationMethod() {
            return creationMethod;
        }

        public String getAddress() {
            return address;
        }

        private String dbName;
        private String login;
        private String password;
        private String rootPassword;
        private String address;
        private String loginDomain;
        private String creationMethod;

        private final Map<String, String> parameters;
    }

}
