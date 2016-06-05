package bomberman.mechanics;

import bomberman.mechanics.interfaces.*;
import bomberman.mechanics.tiles.behaviors.BombRayBehavior;
import bomberman.mechanics.worldbuilders.WorldData;
import bomberman.service.TimeHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class World {

    public World(String worldType) {
        final IWorldBuilder builder = WorldBuilderForeman.getWorldBuilderInstance(worldType);
        final WorldData worldData = builder.getWorldData(this);

        tileArray = worldData.getTileArray();
        spawnLocations = worldData.getSpawnList();
        name = worldData.getName();
        registerNewTiles();
    }

    public void addWorldEvent(WorldEvent worldEvent) {
        newEventQueue.add(worldEvent);
    }

    public int getNextID() {
        return uidManager.getAndIncrement();
    }

    public Queue<WorldEvent> getFreshEvents() {
        final Queue<WorldEvent> newQueue = new LinkedList<>(processedEventQueue);
        processedEventQueue.clear();
        return newQueue;
    }

    // For linking to Players via Room
    public int[] getBombermenIDs(){
        final int[] ids = new int[bombermen.size()];
        int i = 0;

        for (Bomberman bomberman : bombermen)
            ids[i++] = bomberman.getID();

        return ids;
    }

    public int getNumberOfSpawns() {
        return spawnLocations.length;
    }

    public void spawnBombermen(int amount){
        if (spawnLocations.length < amount)
            throw new ArrayIndexOutOfBoundsException();
        for (int i = 0; i < amount; ++i) {
            final Bomberman newBomberman = new Bomberman(getNextID(), this, spawnLocations[i]);
            newBomberman.setCoordinates(spawnLocations[i]);
            bombermen.add(newBomberman);
            processedEventQueue.add(new WorldEvent(EventType.TILE_SPAWNED, newBomberman.getType(), newBomberman.getID(), spawnLocations[i][0], spawnLocations[i][1], null));
        }
    }

    public void spawnBots() {
        final int bombermenAlreadySpawned = bombermen.size();
        final int numberOfBotsToBeSpawned = spawnLocations.length - bombermen.size();
        for (int i = 0; i < numberOfBotsToBeSpawned; ++i) {
            final Bomberman newBomberman = new BombermanBot(getNextID(), this, spawnLocations[i + bombermenAlreadySpawned]);
            newBomberman.setCoordinates(spawnLocations[i + bombermenAlreadySpawned]);
            bombermen.add(newBomberman);
            processedEventQueue.add(new WorldEvent(EventType.TILE_SPAWNED, newBomberman.getType(), newBomberman.getID(), spawnLocations[i + bombermenAlreadySpawned][0], spawnLocations[i + bombermenAlreadySpawned][1], null));
        }
    }

    public String getName() {
        return name;
    }

    public int getWidth() {
        return tileArray[0].length;
    }

    public int getHeight() {
        return tileArray.length;
    }

    public boolean shouldBeUpdated() {
        return selfUpdatingEntities > 0 || !newEventQueue.isEmpty() || !delayedEventQueue.isEmpty();
    }

    public void runGameLoop(long deltaT) {
        updateEverything(deltaT);

        while (!newEventQueue.isEmpty())
        {
            final WorldEvent elderEvent = newEventQueue.poll();
            switch (elderEvent.getEventType()){
                case ENTITY_UPDATED:
                    processEntityUpdatedEvent(elderEvent);
                    break;
                case TILE_SPAWNED:
                    processTileSpawnedEvent(elderEvent);
                    break;
                case TILE_REMOVED:
                    processTileRemovedEvent(elderEvent);
                    break;
            }
        }

        tryMovingBombermen(deltaT);
    }

    public int getBombermanCount() {
        return bombermen.size();
    }

    //// For bot

    public ITile[][] getTiles() {
        return tileArray;
    }

    public ArrayList<Bomberman> getBombermen() {
        return bombermen;
    }

    ////

    private void processEntityUpdatedEvent(WorldEvent event) {
        LOGGER.debug("Processing object_updated");
        assignBombermanMovement(event);
    }

    // Run only once at the very beginning
    private void registerNewTiles() {
        for (int y = 0; y < tileArray.length; ++y) {
            final ITile[] row = tileArray[y];
            for (int x = 0; x < tileArray[y].length; ++x) {
                final ITile tile = row[x];
                if (tile != null)
                    processedEventQueue.add(new WorldEvent(EventType.TILE_SPAWNED, tile.getType(), tile.getID(), x, y, null));
            }
        }
    }

    private void processTileSpawnedEvent(WorldEvent event) {
        LOGGER.debug("Processing object_spawned");
        if (event.getEntityType() == EntityType.BOMB)
            tryPlacingBomb(event.getEntityID(), false);
        else placeTile(event);
    }

    private void processTileRemovedEvent(WorldEvent event) {
        LOGGER.debug("Processing object_destroyed");
        if (event.getEntityType() == EntityType.BOMB)
            explodeBomb(event);
        else if (event.getEntityType() == EntityType.BOMB_RAY)
            dissipateBombRay(event);
        else if (event.getEntityType() == EntityType.BOMBERMAN)
            killBomberman(event);
        else removeTileByID(event.getEntityID(), event.getInitiator());
    }

    private void assignBombermanMovement(WorldEvent event) {
        final Bomberman actor = getBombermanByID(event.getEntityID());

        if (actor == null) {
            LOGGER.warn("No bomberman with id \"" + event.getEntityID() + "\" exists!");
            return;
        }

        actor.addMovement(new Triplet<>(event.getX(), event.getY(), event.getTimestamp()));
    }

    // Movements are not at the same time, they have bomberman spawn priorities over timestap priorities! Bad, yet should work on small deltaT intervals.
    private void tryMovingBombermen(long deltaT) {

        for (Bomberman bomberman : bombermen) {
            Triplet<Float, Float, Long> previousMovement = bomberman.getMovementDirection();
            final boolean wasUpdating = bomberman.shouldBeUpdated();
            long timeSpentMoving = 0;

            while (bomberman.getMovementsDuringTick().peek() != null) {
                final Triplet<Float, Float, Long> movement = bomberman.getMovementsDuringTick().poll();

                long dt = movement.getValue2() - previousMovement.getValue2();
                if (dt > deltaT)
                    dt = deltaT;

                timeSpentMoving += dt;

                tryMovingBomberman(bomberman, movement.getValue0(), movement.getValue1(), dt);
                activateTilesWereSteppedOn(bomberman);

                previousMovement = movement;
            }

            bomberman.setMovementDirection(previousMovement);

            if (!wasUpdating && bomberman.shouldBeUpdated())
                selfUpdatingEntities++;
            if (wasUpdating && !bomberman.shouldBeUpdated())
                selfUpdatingEntities--;

            tryMovingBomberman(bomberman, previousMovement.getValue0(), previousMovement.getValue1(), deltaT - timeSpentMoving);
            activateTilesWereSteppedOn(bomberman);
        }
    }

    // Hard maths. PM @Xobotun to get an explanation, don't be shy!
    @SuppressWarnings("OverlyComplexMethod")
    private void tryMovingBomberman(Bomberman actor, float dx, float dy, long deltaT) {
        if (dx == 0 && dy == 0 || deltaT <= 0)
            return;

        final int worldWidth = tileArray[0].length;
        final int worldHeight = tileArray.length;

        final float x = actor.getCoordinates()[0];
        final int ix = (int) Math.floor(x);
        final float y = actor.getCoordinates()[1];
        final int iy = (int) Math.floor(y);

        final float xSpeed = actor.getMaximalSpeed() * (float) (dx / Math.sqrt(dx * dx + dy * dy));
        final float ySpeed = actor.getMaximalSpeed() * (float) (dy / Math.sqrt(dx * dx + dy * dy));

        float predictedX = x + xSpeed * deltaT;
        float predictedY = y + ySpeed * deltaT;
        final float radius = Bomberman.DIAMETER / 2;

        float xBoundary = ix;
        float yBoundary = iy;
        if (dx == 0 && x + radius > xBoundary + 1 || dx > 0)
            xBoundary++;
        if (dy == 0 && y + radius > yBoundary + 1 || dy > 0)
            yBoundary++;


        if (predictedX - radius < 0 && x < 1)        // If leaving world borders to left
            predictedX = radius;
        else if (predictedX + radius > worldWidth && x > worldWidth - 1)  // If leaving world borders to right
            predictedX = worldWidth - radius;

        else if (dx < 0 && predictedX - radius < xBoundary) {    // If moving left and entering left tile
                final ITile leftTile = tileArray[iy][ix - 1];
                if (leftTile != null && !leftTile.isPassable())
                    predictedX = xBoundary + radius;        // If should collide, collide
        }
        else if (dx > 0 && predictedX + radius > xBoundary) {
            final ITile rightTile = tileArray[iy][ix + 1];
            if (rightTile != null && !rightTile.isPassable())
                predictedX = xBoundary - radius;
        }


        if (predictedY - radius < 0 && y < 1)
            predictedY = radius;
        else if (predictedY + radius > worldHeight && y > worldHeight - 1)
            predictedY = worldHeight - radius;

        else if (dy < 0 && predictedY - radius < yBoundary) {
            final ITile upTile = tileArray[iy - 1][ix];
            if (upTile != null && !upTile.isPassable())
                predictedY = yBoundary + radius;
        }
        else if (dy > 0 && predictedY + radius > yBoundary) {
            final ITile downTile = tileArray[iy + 1][ix];
            if (downTile != null && !downTile.isPassable())
                predictedY = yBoundary - radius;
        }


        //noinspection OverlyComplexBooleanExpression,MagicNumber
        if (x > 0.5 && x < worldWidth - 0.5 && y > 0.5 && y < worldHeight - 0.5) {
            final float xDeviationFromTileCenter = - (ix + 0.5f - predictedX);
            final float yDeviationFromTileCenter = - (iy + 0.5f - predictedY);

            final boolean shouldCheckX = Math.abs(xDeviationFromTileCenter) > 0.5f - radius;
            final boolean shouldCheckY = Math.abs(yDeviationFromTileCenter) > 0.5f - radius;

            if (shouldCheckX && shouldCheckY) {
                final float distanceToXBorder = (0.5f - Math.abs(xDeviationFromTileCenter));
                final float distanceToYBorder = (0.5f - Math.abs(yDeviationFromTileCenter));
                final double squaredDistanceToCorner = distanceToXBorder * distanceToXBorder + distanceToYBorder * distanceToYBorder;

                if (squaredDistanceToCorner < radius * radius) {
                    final int yTile = iy + ((yDeviationFromTileCenter > 0) ? 1 : -1);
                    final int xTile = ix + ((xDeviationFromTileCenter > 0) ? 1 : -1);
                    final ITile cornerTile = tileArray[yTile][xTile];

                    if (cornerTile != null && !cornerTile.isPassable())
                        if (Math.abs(xSpeed) > Math.abs(ySpeed)) {
                            final float yIntersectionCorrection = (float) Math.sqrt(radius * radius - distanceToXBorder * distanceToXBorder) - distanceToYBorder;
                            final int direction = (yDeviationFromTileCenter > 0) ? -1 : 1;
                            predictedY += yIntersectionCorrection * direction;
                        } else {
                            final float xIntersectionCorrection = (float) Math.sqrt(radius * radius - distanceToYBorder * distanceToYBorder) - distanceToXBorder;
                            final int direction = (xDeviationFromTileCenter > 0) ? -1 : 1;
                            predictedX += xIntersectionCorrection * direction;
                        }
                }
            }
        }

        for (Bomberman bomberman : bombermen)           // Low-quality inter-bomberman collision checker.
            if (bomberman.getID() != actor.getID()) {
                final float xDistance = Math.abs(predictedX - bomberman.getCoordinates()[0]);
                final float yDistance = Math.abs(predictedY - bomberman.getCoordinates()[1]);
                final float distance = (float) Math.sqrt(xDistance * xDistance + yDistance * yDistance);
                if (distance <= radius * 2 + ACTION_TILE_HANDICAP_DIAMETER)
                    return;
            }

        // Sanity check if timestep is too big
        if (predictedX - radius < 0)
            predictedX = radius;
        if (predictedX + radius > worldWidth)
            predictedX = worldWidth - radius;

        if (predictedY - radius < 0)
            predictedY = radius;
        if (predictedY + radius > worldHeight)
            predictedY = worldHeight - radius;

        actor.setCoordinates(new float[]{ predictedX, predictedY});
        processedEventQueue.add(new WorldEvent(EventType.ENTITY_UPDATED, EntityType.BOMBERMAN, actor.getID(), predictedX, predictedY, actor.getID()));
    }

    private void activateTilesWereSteppedOn(Bomberman actor) {
        final float x = actor.getCoordinates()[0];
        final float y = actor.getCoordinates()[1];

        final float handicappedRadius = (Bomberman.DIAMETER - ACTION_TILE_HANDICAP_DIAMETER) / 2;

        final Set<Pair<Integer, Integer>> uniqueTileCoordinates = new HashSet<>(4);

        uniqueTileCoordinates.add(new Pair<>((int) Math.floor(x - handicappedRadius), (int) Math.floor(y - handicappedRadius)));
        uniqueTileCoordinates.add(new Pair<>((int) Math.floor(x - handicappedRadius), (int) Math.floor(y + handicappedRadius)));
        uniqueTileCoordinates.add(new Pair<>((int) Math.floor(x + handicappedRadius), (int) Math.floor(y - handicappedRadius)));
        uniqueTileCoordinates.add(new Pair<>((int) Math.floor(x + handicappedRadius), (int) Math.floor(y + handicappedRadius)));

        final Set<ITile> uniqueTiles = new HashSet<>(4);
        uniqueTiles.addAll(uniqueTileCoordinates.stream().map(uniqueCoordinate -> tileArray[uniqueCoordinate.getValue1()][uniqueCoordinate.getValue0()]).collect(Collectors.toList()));

        uniqueTiles.stream().filter(uniqueTile -> uniqueTile != null).forEach(uniqueTile -> uniqueTile.applyAction(actor));
    }

    public void tryPlacingBomb(int bombermanID, boolean force) {
        final Bomberman actor = getBombermanByID(bombermanID);
        if (actor == null)
            return;

        final int x = (int) Math.floor(actor.getCoordinates()[0]);
        final int y = (int) Math.floor(actor.getCoordinates()[1]);

        if (actor.canSpawnBomb() && tileArray[y][x] == null || force)
        {
            tileArray[y][x] = TileFactory.getInstance().getNewTile(EntityType.BOMB, this, actor, getNextID());
            actor.takeOnePlaceableBomb();
            actor.resetBombTimer();
            selfUpdatingEntities++;
            processedEventQueue.add(new WorldEvent(EventType.TILE_SPAWNED, EntityType.BOMB, tileArray[y][x].getID(), x, y, bombermanID));
        }
    }

    @SuppressWarnings("OverlyComplexMethod")
    private void explodeBomb(WorldEvent event) {
        int x = -1;
        int y = -1;
        Ownable bomb = null;

        for (int j = 0; j < tileArray.length; ++j)
            for (int i = 0; i < tileArray[0].length; ++i)
                if (tileArray[j][i] != null && tileArray[j][i].getID() == event.getEntityID()) {
                    x = i;
                    y = j;
                    bomb = (Ownable) tileArray[y][x];
                }

        if (bomb == null) {
            LOGGER.error("Non-existent bomb #" + event.getEntityID() + " has exploded! O_o");
            return;
        }


        final Bomberman owner = bomb.getOwner();
        final int radius = owner.getBombExplosionRange();

        tileArray[y][x] = null;
        owner.returnOnePlaceableBomb();
        selfUpdatingEntities--;
        processedEventQueue.add(event);

        for (int i = 0; i >= -radius; --i)
            if (x + i >= 0)
                if (destroyTileAndSpawnRay(x + i, y, owner))
                    break;
        for (int i = 1; i <= radius; ++i)
            if (x + i < tileArray[0].length)
                if (destroyTileAndSpawnRay(x + i, y, owner))
                    break;

        for (int i = -1; i >= -radius; --i)
            if (y + i >= 0)
                if (destroyTileAndSpawnRay(x, y + i, owner))
                    break;
        for (int i = 1; i <= radius; ++i)
            if (y + i < tileArray.length)
                if (destroyTileAndSpawnRay(x, y + i, owner))
                    break;
    }

    private boolean destroyTileAndSpawnRay(int x, int y, Bomberman owner) {
        if (tileArray[y][x] != null && !tileArray[y][x].isDestructible())   // stop if undestuctible
            return true;
        boolean result = false;

        if (tileArray[y][x] != null && tileArray[y][x].isDestructible()) {
            while (tileArray[y][x] != null)
                processTileRemovedEvent(new WorldEvent(EventType.TILE_REMOVED, tileArray[y][x].getType(), tileArray[y][x].getID(), x, y, owner.getID()));

            result = true;      // if destructible, destroy tile, spawn ray and break loop.
        }
        if (tileArray[y][x] == null) {
            tileArray[y][x] = TileFactory.getInstance().getNewTile(EntityType.BOMB_RAY, this, owner, getNextID());
            selfUpdatingEntities++;
            processedEventQueue.add(new WorldEvent(EventType.TILE_SPAWNED, EntityType.BOMB_RAY, tileArray[y][x].getID(), x, y, owner.getID()));
        }
        return result;
    }

    private void dissipateBombRay(WorldEvent event) {
        removeTileByID(event.getEntityID(), event.getInitiator());
        selfUpdatingEntities--;
        processedEventQueue.add(event);
    }

    private void killBomberman(WorldEvent event) {
        final Bomberman deadOne = getBombermanByID(event.getEntityID());

        if (deadOne != null && deadOne.shouldDropBombOnDeath())
            tryPlacingBomb(deadOne.getID(), true);

        bombermen.remove(deadOne);
        processedEventQueue.add(event);
    }

    private void decideToSpawnRandomBonus(int x, int y) {
        if (randomizer.nextInt(100) < PERCENT_TO_SPAWN_BONUS) {
            final EntityType type;
            switch (Math.abs(randomizer.nextInt() % TileFactory.getBonusCount())) {
                case 0:
                    type = EntityType.BONUS_INCMAXRANGE;
                    LOGGER.debug("Spawning " + type + " at x: " + x + ", y: " + y);
                    break;
                case 1:
                    type = EntityType.BONUS_DECBOMBSPAWN;
                    LOGGER.debug("Spawning " + type + " at x: " + x + ", y: " + y);
                    break;
                case 2:
                    type = EntityType.BONUS_INCMAXHP;
                    LOGGER.debug("Spawning " + type + " at x: " + x + ", y: " + y);
                    break;
                case 3:
                    type = EntityType.BONUS_INCSPEED;
                    LOGGER.debug("Spawning " + type + " at x: " + x + ", y: " + y);
                    break;
                case 4:
                    type = EntityType.BONUS_MOREBOMBS;
                    LOGGER.debug("Spawning " + type + " at x: " + x + ", y: " + y);
                    break;
                case 5:
                    type = EntityType.BONUS_DROPBOMBONDEATH;
                    LOGGER.debug("Spawning " + type + " at x: " + x + ", y: " + y);
                    break;
                case 6:
                    type = EntityType.BONUS_INVUL;
                    LOGGER.debug("Spawning " + type + " at x: " + x + ", y: " + y);
                    break;
                default:
                    LOGGER.warn("Random bonus numbergenerator failed! It is greater than 5!");
                    return;
            }

            delayedEventQueue.add(new Pair<>(new WorldEvent(EventType.TILE_SPAWNED, type, getNextID(), x, y, null), TimeHelper.now() + BombRayBehavior.BOMB_RAY_DURATION));
        }
    }

    @Nullable
    private Bomberman getBombermanByID(int id) {
        Bomberman result = null;

        for (Bomberman bomberman : bombermen)
            if (bomberman.getID() == id)
                result = bomberman;

        return result;
    }

    private void updateEverything(long deltaT) {
        for (Bomberman bomberman : bombermen)
            bomberman.update(deltaT);

        for (ITile[] row: tileArray)
            for (ITile tile: row)
                if (tile != null)
                    tile.update(deltaT);

        delayedEventQueue.stream().filter(event -> TimeHelper.now() >= event.getValue1()).forEach(event -> {
            newEventQueue.add(event.getValue0());
            delayedEventQueue.remove(event);
        });
    }

    private void removeTileByID(int id, @Nullable Integer initiator) {
        for (int y = 0; y < tileArray.length; ++y)
            for (int x = 0; x < tileArray[0].length; ++x)
                if (tileArray[y][x] != null && tileArray[y][x].getID() == id) {
                    if (tileArray[y][x].shouldSpawnBonusOnDestruction())
                        decideToSpawnRandomBonus(x, y);

                    processedEventQueue.add(new WorldEvent(EventType.TILE_REMOVED, tileArray[y][x].getType(), tileArray[y][x].getID(), x, y, initiator));
                    tileArray[y][x] = null;
                }
    }

    private void placeTile(WorldEvent event) {
        final ITile newTile = TileFactory.getInstance().getNewTile(event.getEntityType(), this, event.getEntityID());
        tileArray[(int) event.getY()][(int) event.getX()] = newTile;
        processedEventQueue.add(event);
    }

    private final Queue<WorldEvent> newEventQueue = new ConcurrentLinkedQueue<>();       // Here are new events are stashed
    private final Queue<WorldEvent> processedEventQueue = new ConcurrentLinkedQueue<>(); // State describer will take events from this list.
    private final Queue<Pair<WorldEvent, Long>> delayedEventQueue = new ConcurrentLinkedQueue<>();

    private final AtomicInteger uidManager = new AtomicInteger(0);
    private final Random randomizer = new Random();

    private final String name;

    private final ITile[][] tileArray;
    private final ArrayList<Bomberman> bombermen = new ArrayList<>(4);

    private final float[][] spawnLocations;

    private int selfUpdatingEntities = 0;

    public static final float ACTION_TILE_HANDICAP_DIAMETER = 0.05f; // 0.75-0.05 will
    public static final int PERCENT_TO_SPAWN_BONUS = 20;

    private static final Logger LOGGER = LogManager.getLogger(World.class);
}
