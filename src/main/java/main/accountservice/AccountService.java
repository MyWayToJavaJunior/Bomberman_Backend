package main.accountservice;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rest.UserProfile;

import java.util.Collection;

public interface AccountService {
    void loginUser(@NotNull UserProfile user);

    @Nullable
    UserProfile getBySessionID(@Nullable String sessionID);

    boolean hasSessionID(@Nullable String sessionID);

    boolean logoutUser(@Nullable String sessionID);

    @Nullable
    UserProfile createNewUser(@NotNull String login, @NotNull String password, boolean isGuest);

    @Deprecated
    @Nullable
    UserProfile createNewUser(@NotNull String login, @NotNull String password);

    @Nullable
    Collection<UserProfile> getAllUsers();

    @Nullable
    Collection<UserProfile> getTop10Users();

    @Nullable
    UserProfile getUser(@NotNull Long id);

    @Nullable
    UserProfile getUser(@NotNull String login);

    void updateUserpic(UserProfile user, String path);

    void updateScore(UserProfile user, int delta);

    void deleteUser(@NotNull Long id);

    void updateUser(UserProfile user);

}
