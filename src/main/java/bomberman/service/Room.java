package bomberman.service;

import bomberman.mechanics.World;
import bomberman.mechanics.WorldEvent;
import bomberman.mechanics.interfaces.EntityType;
import bomberman.mechanics.interfaces.EventType;
import main.accountservice.AccountService;
import main.websockets.MessageSendable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javatuples.Pair;
import org.jetbrains.annotations.Nullable;
import rest.UserProfile;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Room {

    @Deprecated
    public Room() {
        id = ID_COUNTER.getAndIncrement();
    }

    public Room(@Nullable AccountService as){
        accountService = as;
        id = ID_COUNTER.getAndIncrement();
    }

    public Room(@Nullable AccountService as, int overrideCapacity) {
        accountService = as;
        capacity = overrideCapacity;
        id = ID_COUNTER.getAndIncrement();
    }

    public void createNewWorld(String type)    {
        world = new World(type);
        capacity = world.getNumberOfSpawns();
        worldSpawnDetails = new LinkedList<>(world.getFreshEvents());

    }

    public void assignBombermenToPlayers() {
        world.spawnBombermen(websocketMap.size());
        final int[] bombermen = world.getBombermenIDs();
        int i = 0;
        for (Map.Entry<UserProfile, MessageSendable> player : websocketMap.entrySet()) {
            playerMap.put(bombermen[i], player.getKey());
            reversePlayerMap.put(player.getKey(), bombermen[i]);
            ++i;
        }
        if (shouldHaveBots.get())
            world.spawnBots();
    }

    public int getCurrentCapacity() {
        return websocketMap.size();
    }

    public int getCapacity() {
        return capacity;
    }


    public boolean isFilled()
    {
        return websocketMap.size() >= capacity;
    }

    public boolean isEmpty() {
        return websocketMap.isEmpty();
    }

    public synchronized boolean insertPlayer(UserProfile user, MessageSendable socket) {
        if (isFilled())
            return false;

        isEveryoneReady.getAndSet(false);
        hasEveryoneLoadedContent.getAndSet(false);

        for (Map.Entry<UserProfile, MessageSendable> entry : websocketMap.entrySet())
            socket.sendMessage(MessageCreator.createUserJoinedMessage(entry.getKey(), readinessMap.get(entry.getKey()).getValue0(), readinessMap.get(entry.getKey()).getValue0()));

        for (WorldEvent spawnEvent: worldSpawnDetails)
            transmit(spawnEvent, socket);

        timeToKickMap.put(user, TimeHelper.now() + TIME_TO_KICK);
        websocketMap.put(user, socket);
        readinessMap.put(user, new Pair<>(false, false));
        broadcast(MessageCreator.createUserJoinedMessage(user, readinessMap.get(user).getValue0(), readinessMap.get(user).getValue1()));
        return true;
    }

    public boolean hasPlayer(UserProfile user) {
        return websocketMap.containsKey(user);
    }

    public synchronized void removePlayer(UserProfile user) {
        if (websocketMap.containsKey(user)) {
            if (accountService != null)
                accountService.updateUser(user);

            broadcast(MessageCreator.createUserLeftMessage(user));

            websocketMap.remove(user);
            readinessMap.remove(user);
            timeToKickMap.remove(user);

            recalculateReadiness();
        }
    }

    public void updatePlayerState(UserProfile user, boolean isReady, boolean contentLoaded) {
        readinessMap.remove(user);
        readinessMap.put(user, new Pair<>(isReady, contentLoaded));
        broadcast(MessageCreator.createUserStateChangedMessage(user, isReady, contentLoaded));

        refreshUserKickTimer(user);

        recalculateReadiness();
        startGameIfEveryoneIsReady();
    }

    private void recalculateReadiness() {
        boolean isEveryoneReadyTMP = true;
        boolean hasEveryoneLoadedContentTMP = true;

        for (Map.Entry<UserProfile, Pair<Boolean, Boolean>> entry : readinessMap.entrySet())
        {
            if (!entry.getValue().getValue0())
                isEveryoneReadyTMP = false;
            if (!entry.getValue().getValue1())
                hasEveryoneLoadedContentTMP = false;
        }
        if (readinessMap.isEmpty()) {
            hasEveryoneLoadedContentTMP = false;
            isEveryoneReadyTMP = false;
        }

        isEveryoneReady.compareAndSet(!isEveryoneReadyTMP, isEveryoneReadyTMP);
        hasEveryoneLoadedContent.compareAndSet(!hasEveryoneLoadedContentTMP, hasEveryoneLoadedContentTMP);

    }

    private void startGameIfEveryoneIsReady() {
        //noinspection OverlyComplexBooleanExpression
        if ((websocketMap.size() > 1 || shouldHaveBots.get()) && hasEveryoneLoadedContent.get() && isEveryoneReady.get()) {
            hasCountDownBegan.compareAndSet(false, true);
            TimeHelper.executeAfter(TIME_TO_WAIT_AFTER_READY, () ->
            {
                recalculateReadiness();
                //noinspection OverlyComplexBooleanExpression
                if ((websocketMap.size() > 1 || shouldHaveBots.get()) && hasEveryoneLoadedContent.get() && isEveryoneReady.get() && !isActive.get()) {
                    assignBombermenToPlayers();
                    transmitEventsOnWorldCreation();
                    broadcast(MessageCreator.createWorldCreatedMessage(world.getName(), world.getWidth(), world.getHeight()));
                    if (websocketMap.size() == 1 && shouldHaveBots.get())
                        isSinglePlayer = true;
                } else
                    hasCountDownBegan.compareAndSet(true, false);
            });
        }
    }

    public void activateBots(boolean flag) {
        shouldHaveBots.compareAndSet(!flag, flag);
        broadcast(MessageCreator.createBotsEnableMessage(shouldHaveBots.get()));
        startGameIfEveryoneIsReady();
    }
    
    public void broadcast(String message) {
        for (Map.Entry<UserProfile, MessageSendable> entry: websocketMap.entrySet())
            entry.getValue().sendMessage(message);
    }

    public boolean isActive() {
        return isActive.get();
    }

    public void scheduleBombermanMovement(UserProfile user, int dirX, int dirY) {
        if (isActive.get() && !isFinished.get() && reversePlayerMap.containsKey(user)) {
            final int bombermanID = reversePlayerMap.get(user);
                scheduledActions.add(new WorldEvent(EventType.ENTITY_UPDATED, EntityType.BOMBERMAN, bombermanID, dirX, dirY, null, TimeHelper.now()));  // TODO: Should TimeHelper.now() be client's timestamp?
        }
    }

    public void scheduleBombPlacement(UserProfile user) {
        if (isActive.get() && !isFinished.get() && reversePlayerMap.containsKey(user)) {
            final int bombermanID = reversePlayerMap.get(user);

            scheduledActions.add(new WorldEvent(EventType.TILE_SPAWNED, EntityType.BOMB, bombermanID, 0, 0, null));
        }
    }

    public World getWorld() {
        return world;
    }

    private void passScheduledMovementsToWorld() {
        WorldEvent event = scheduledActions.poll();
        while (event != null) {
            world.addWorldEvent(event);
            event = scheduledActions.poll();
        }
    }

    private void transmit(WorldEvent event, MessageSendable socket) {
        switch (event.getEventType()) {
            case ENTITY_UPDATED:
                socket.sendMessage(MessageCreator.createObjectChangedMessage(event));
                break;
            case TILE_SPAWNED:
                socket.sendMessage(MessageCreator.createObjectSpawnedMessage(event));
                break;
            case TILE_REMOVED:
                socket.sendMessage(MessageCreator.createObjectDestroyedMessage(event));
                break;
        }
    }

    private void transmitEventsOnWorldCreation() {
        isActive.compareAndSet(false, true);
        broadcastFreshEvents();
    }

    private void broadcastFreshEvents() {
        for (WorldEvent event : world.getFreshEvents()) {
            if (accountService != null)
                rewardPlayers(event);
            
            switch (event.getEventType()) {
                case ENTITY_UPDATED:
                    broadcast(MessageCreator.createObjectChangedMessage(event));
                    break;
                case TILE_SPAWNED:
                    if (event.getEntityType() == EntityType.BOMBERMAN) {
                        int playerID = -1;
                        if (playerMap.containsKey(event.getEntityID()))
                            playerID = (int) playerMap.get(event.getEntityID()).getId();

                        broadcast(MessageCreator.createBombermanSpawnedMessage(event, playerID));
                    }
                    else
                        broadcast(MessageCreator.createObjectSpawnedMessage(event));
                    break;
                case TILE_REMOVED:
                    if (event.getEntityType() == EntityType.BOMBERMAN)
                        removeBombermenMappingOnDie(event.getEntityID());
                    broadcast(MessageCreator.createObjectDestroyedMessage(event));
                    break;
            }
        }
    }

    // http://gafferongames.com/game-physics/fix-your-timestep/
    // Variable delta time variant
    public boolean updateIfNeeded(long deltaT) {
        if (isActive.get() && !isFinished.get() && willWorldStateChangeOnNextTick()) {
            update(deltaT);
            return true;
        } else
            if (!hasCountDownBegan.get())
                kickPlayersIfNeeded();

        return false;
    }

    public void refreshUserKickTimer(UserProfile user) {
        timeToKickMap.remove(user);
        if (websocketMap.containsKey(user))
            timeToKickMap.put(user, TimeHelper.now() + TIME_TO_KICK);
    }

    private boolean willWorldStateChangeOnNextTick() {
        return world.shouldBeUpdated() || !scheduledActions.isEmpty();
    }

    private void update(long deltaT) {
        passScheduledMovementsToWorld();
        world.runGameLoop(deltaT);
        broadcastFreshEvents();
        stopIfGameIsOver();
    }

    private void removeBombermenMappingOnDie(int bombermanID) {
        final UserProfile user = playerMap.get(bombermanID);

        playerMap.remove(bombermanID);
        reversePlayerMap.remove(user);
        if (accountService != null)
            accountService.updateUser(user);
    }

    private void stopIfGameIsOver() {
        if (world.getBombermanCount() == 1 && !isSinglePlayer) {
            TimeHelper.executeAfter(TIME_TO_WAIT_ON_GAME_OVER, () -> {
                if (!isFinished.get() && world.getBombermanCount() == 1) {
                    isFinished.compareAndSet(false, true);

                    if (accountService != null)
                        accountService.updateScore(playerMap.get(world.getBombermenIDs()[0]), SCORE_ON_GAME_WON);

                    broadcast(MessageCreator.createGameOverMessage(playerMap.get(world.getBombermenIDs()[0])));
                }
            });
        }
        if (world.getBombermanCount() == 0) {
            isFinished.compareAndSet(false, true);
            broadcast(MessageCreator.createGameOverMessage(null));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Room room = (Room) o;

        return id == room.id;

    }

    private void kickPlayersIfNeeded() {
        final List<UserProfile> scheduledKicks = new LinkedList<>();
        if (!isActive.get() && !hasCountDownBegan.get())
            for (Map.Entry<UserProfile, Long> timeToKick: timeToKickMap.entrySet()) {
                final UserProfile user = timeToKick.getKey();

                if (user != null && !readinessMap.get(user).getValue0()){
                    if (timeToKickMap.get(user) - TimeHelper.now() < 0)
                        scheduledKicks.add(user);
                }
            }

        for (UserProfile user: scheduledKicks) {
            LOGGER.info("Kicking Player #" + user.getId() + " \"" + user.getLogin() + "\" for inactivity.");
            removePlayer(user);
        }
    }

    private void rewardPlayers(WorldEvent event) {
        if (event.getInitiator() != null && event.getEventType() == EventType.TILE_REMOVED && !isSinglePlayer) {
            final UserProfile initiator = playerMap.get(event.getInitiator());

            if (initiator != null) {
                if (event.getEntityType() == EntityType.DESTRUCTIBLE_WALL)
                    accountService.updateScore(initiator, SCORE_ON_WALL_BROKEN);

                if (event.getEntityType() == EntityType.BONUS_DECBOMBSPAWN || event.getEntityType() == EntityType.BONUS_DROPBOMBONDEATH ||
                        event.getEntityType() == EntityType.BONUS_INCMAXHP || event.getEntityType() == EntityType.BONUS_INCMAXRANGE ||
                        event.getEntityType() == EntityType.BONUS_INCSPEED || event.getEntityType() == EntityType.BONUS_MOREBOMBS)
                    accountService.updateScore(initiator, SCORE_ON_BONUS_PICKED_UP);

                if (event.getEntityType() == EntityType.BOMBERMAN) {
                    final UserProfile deadOne = playerMap.get(event.getEntityID());

                    if (deadOne != null)
                        accountService.updateScore(deadOne, -SCORE_ON_BOMBERMAN_KILL);

                    if (deadOne != null && !deadOne.equals(initiator))
                        accountService.updateScore(initiator, SCORE_ON_BOMBERMAN_KILL);
                }
            }
        }
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Nullable
    private AccountService accountService = null;

    private final int id;
    private int capacity = DEFAULT_CAPACITY;
    private boolean isSinglePlayer = false;
    private final AtomicBoolean isEveryoneReady = new AtomicBoolean(false);
    private final AtomicBoolean hasEveryoneLoadedContent = new AtomicBoolean(false);

    private final Map<Integer, UserProfile> playerMap = new ConcurrentHashMap<>(4);
    private final Map<UserProfile, Integer> reversePlayerMap = new ConcurrentHashMap<>(4);
    private final Map<UserProfile, MessageSendable> websocketMap = new ConcurrentHashMap<>(4);
    private final Map<UserProfile, Pair<Boolean, Boolean>> readinessMap = new ConcurrentHashMap<>(4);
    private final Map<UserProfile, Long> timeToKickMap = new ConcurrentHashMap<>(4);

    private World world;
    private final AtomicBoolean isActive = new AtomicBoolean(false);
    private final AtomicBoolean hasCountDownBegan = new AtomicBoolean(false);
    private final AtomicBoolean isFinished = new AtomicBoolean(false);
    private final AtomicBoolean shouldHaveBots = new AtomicBoolean(true);
    public static final int MINIMAL_TIME_STEP = 25; //ms

    private final Queue<WorldEvent> scheduledActions = new ConcurrentLinkedQueue<>();
    private List<WorldEvent> worldSpawnDetails = new LinkedList<>();       // worldHistory, heh?

    public static final int DEFAULT_CAPACITY = 4;
    public static final int TIME_TO_WAIT_AFTER_READY = 3000; // ms
    public static final int TIME_TO_WAIT_ON_GAME_OVER = 1500; // ms
    public static final int TIME_TO_KICK = 60_000; // 1 minute

    public static final int SCORE_ON_GAME_WON = 500;
    public static final int SCORE_ON_BOMBERMAN_KILL = 100;
    public static final int SCORE_ON_BONUS_PICKED_UP = 2;
    public static final int SCORE_ON_WALL_BROKEN = 1;

    private static final AtomicInteger ID_COUNTER = new AtomicInteger();
    private static final Logger LOGGER = LogManager.getLogger(Room.class);
}
