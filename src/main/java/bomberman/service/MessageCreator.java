package bomberman.service;

import bomberman.mechanics.WorldEvent;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import rest.UserProfile;

public class MessageCreator {
    public static String createUserJoinedMessage(UserProfile joinee, boolean isReady, boolean contentLoaded) {
        final JSONObject messageTemplate = new JSONObject();

        messageTemplate.put("type", "user_joined");
        messageTemplate.put("id", joinee.getId());
        messageTemplate.put("isReady", isReady);
        messageTemplate.put("contentLoaded", contentLoaded);
        messageTemplate.put("name", joinee.getLogin());
        messageTemplate.put("score", joinee.getScore());
        if (joinee.getUserpicPath() == null)
            messageTemplate.put("userpic_path", JSONObject.NULL);
        else
            messageTemplate.put("userpic_path", joinee.getUserpicPath());

        return messageTemplate.toString();
    }

    public static String createUserLeftMessage(UserProfile joinee) {
        final JSONObject messageTemplate = new JSONObject();

        messageTemplate.put("type", "user_left");
        messageTemplate.put("id", joinee.getId());

        return messageTemplate.toString();
    }

    public static String createObjectDestroyedMessage(WorldEvent event) {
        final JSONObject messageTemplate = new JSONObject();

        messageTemplate.put("type", "object_destroyed");
        messageTemplate.put("id", event.getEntityID());

        return messageTemplate.toString();
    }

    @SuppressWarnings("OverlyComplexMethod")
    public static String createObjectSpawnedMessage(WorldEvent event) throws IllegalArgumentException {
        final JSONObject messageTemplate = new JSONObject();
        final String objectType;

        switch (event.getEntityType()) {
            case BOMBERMAN:
                throw new IllegalArgumentException();
            case DESTRUCTIBLE_WALL:
                objectType = "destructible_wall";
                break;
            case UNDESTRUCTIBLE_WALL:
                objectType = "undestructible_wall";
                break;
            case BONUS_DROPBOMBONDEATH:
                objectType = "bonus_drop_bomb_on_death";
                break;
            case BONUS_INCMAXHP:
                objectType = "bonus_increase_max_hp";
                break;
            case BONUS_INCMAXRANGE:
                objectType = "bonus_increase_bomb_range";
                break;
            case BONUS_DECBOMBSPAWN:
                objectType = "bonus_decrease_bomb_spawn_delay";
                break;
            case BONUS_INVUL:
                objectType = "bonus_invulnerability";
                break;
            case BONUS_INCSPEED:
                objectType = "bonus_increase_speed";
                break;
            case BONUS_MOREBOMBS:
                objectType = "bonus_bombs_amount";
                break;
            case BOMB:
                objectType = "bomb";
                break;
            case BOMB_RAY:
                objectType = "bomb_ray";
                break;
            default:
                objectType = "error_type";
                break;
        }

        messageTemplate.put("type", "object_spawned");
        messageTemplate.put("id", event.getEntityID());
        messageTemplate.put("object_type", objectType);
        messageTemplate.put("x", event.getX());
        messageTemplate.put("y", event.getY());

        return messageTemplate.toString();
    }

    public static String createBombermanSpawnedMessage(WorldEvent event, int userID) throws IllegalArgumentException {
        final JSONObject messageTemplate = new JSONObject();

        messageTemplate.put("type", "bomberman_spawned");
        messageTemplate.put("id", event.getEntityID());
        messageTemplate.put("user_id", userID);
        messageTemplate.put("x", event.getX());
        messageTemplate.put("y", event.getY());

        return messageTemplate.toString();
    }

    public static String createObjectChangedMessage(WorldEvent event) {
        final JSONObject messageTemplate = new JSONObject();

        messageTemplate.put("type", "object_changed");
        messageTemplate.put("id", event.getEntityID());
        messageTemplate.put("x", event.getX());
        messageTemplate.put("y", event.getY());

        return messageTemplate.toString();
    }

    public static String createGameOverMessage(@Nullable UserProfile winner) {
        final JSONObject messageTemplate = new JSONObject();

        messageTemplate.put("type", "game_over");
        if (winner != null)
            messageTemplate.put("id", winner.getId());
        else
            messageTemplate.put("id", JSONObject.NULL);

        return messageTemplate.toString();
    }

    public static String createWorldCreatedMessage(String title, int width, int height) {
        final JSONObject messageTemplate = new JSONObject();

        messageTemplate.put("type", "world_created");
        messageTemplate.put("title", title);
        messageTemplate.put("width", width);
        messageTemplate.put("height", height);

        return messageTemplate.toString();
    }

    public static String createUserStateChangedMessage(UserProfile user, boolean isReady, boolean contentLoaded) {
        final JSONObject messageTemplate = new JSONObject();

        messageTemplate.put("type", "user_state_changed");
        messageTemplate.put("id", user.getId());
        messageTemplate.put("isReady", isReady);
        messageTemplate.put("contentLoaded", contentLoaded);

        return messageTemplate.toString();
    }

    public static String createBotsEnableMessage(boolean areBotsActive) {
        final JSONObject messageTemplate = new JSONObject();

        messageTemplate.put("type", "enable_bots");
        messageTemplate.put("value", areBotsActive);

        return messageTemplate.toString();
    }

    public static String createControllerGetName(String controllerName) {
        final JSONObject messageTemplate = new JSONObject();

        messageTemplate.put("type", "get_controller_name");
        messageTemplate.put("name", controllerName);

        return messageTemplate.toString();
    }

}
