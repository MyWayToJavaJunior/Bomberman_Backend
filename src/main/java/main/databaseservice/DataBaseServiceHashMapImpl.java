package main.databaseservice;

import org.jetbrains.annotations.Nullable;
import rest.UserProfile;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class DataBaseServiceHashMapImpl implements DataBaseService {
    @Override
    public void save(UserProfileData dataSet) {

    }

    @Override
    @Nullable
    public UserProfile addUser(String login, String password, boolean isGuest) {
        if (containsLogin(login))
            return null;
        final UserProfileData newUserData = new UserProfileData(login, password, isGuest);
        newUserData.setId(idCounter.incrementAndGet());
        final UserProfile newUser = new UserProfile(newUserData);
        loginToUser.put(login, newUser);
        idToUser.put(newUser.getId(), newUser);
        return newUser;
    }

    @Override
    @Nullable
    public UserProfile getById(long id) {
        return idToUser.get(id);
    }

    @Override
    @Nullable
    public UserProfile getByLogin(String name) {

        return loginToUser.get(name);
    }

    @Override
    public Collection<UserProfile> getUsers() {
        return idToUser.values();
    }

    @Override
    public boolean containsID(Long id) {
        return idToUser.containsKey(id);
    }

    @Override
    public boolean containsLogin(String name) {
        return loginToUser.containsKey(name);
    }

    @Override
    public void deleteUser(Long id) {
        final UserProfile deletingUser = idToUser.get(id);
        idToUser.remove(id);
        loginToUser.remove(deletingUser.getLogin());
    }

    @Nullable
    @Override
    @Deprecated
    public Collection<UserProfile> getTop10Users() {
        return getUsers(); // I'm lazy.
    }

    private final AtomicLong idCounter = new AtomicLong();
    private final Map<String, UserProfile> loginToUser = new HashMap<>();
    private final Map<Long, UserProfile> idToUser = new HashMap<>();

}
