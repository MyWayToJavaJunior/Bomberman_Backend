package main;

import main.database.DataBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rest.UserProfile;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AccountServiceImpl implements AccountService {

    public AccountServiceImpl() {
        //registeredUsers = new DataBaseRealImpl();
    }

    @Override
    public void loginUser(@NotNull UserProfile user){
        activeUsers.put(user.getSessionID(), user.getId());
    }

    @Override
    @Nullable
    public UserProfile getBySessionID(@Nullable String sessionID){
        if (activeUsers.containsKey(sessionID))
            return registeredUsers.getById(activeUsers.get(sessionID));
        return null;
    }

    @Override
    public boolean hasSessionID(@Nullable String sessionID){
        return activeUsers.containsKey(sessionID);
    }

    @Override
    public boolean logoutUser(@Nullable String sessionID){
        if (!activeUsers.containsKey(sessionID))
            return false;
        activeUsers.remove(sessionID);
        return true;
    }

    @Override
    @Nullable
    public UserProfile createNewUser(@NotNull String login,@NotNull String password) {
        if (registeredUsers.containsLogin(login))
            return null;
        return registeredUsers.addUser(login, password);
    }

    @Override
    @Nullable
    public Collection<UserProfile> getAllUsers() {
        return registeredUsers.getUsers();
    }

    @Override
    @Nullable
    public UserProfile getUser(@NotNull Long id) {
        return registeredUsers.getById(id);
    }

    @Override
    @Nullable
    public UserProfile getUser(@NotNull String login) {
        return registeredUsers.getByLogin(login);
    }

    @Override
    public void deleteUser(@NotNull Long id) {
        if (!registeredUsers.containsID(id))
            return;
        registeredUsers.deleteUser(id);
    }

    @Override
    public void updateUser(UserProfile user) {
        registeredUsers.save(user.getData());
    }

    @Override
    public void changeDB(DataBase dataBase) {
        registeredUsers = dataBase;
    }

    private final Map<String, Long> activeUsers = new HashMap<>();
    private DataBase registeredUsers;
}
