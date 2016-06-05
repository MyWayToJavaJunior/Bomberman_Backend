package bomberman.mechanics;

import bomberman.mechanics.interfaces.EntityType;
import bomberman.mechanics.interfaces.EventType;
import bomberman.mechanics.interfaces.IEntity;
import bomberman.mechanics.interfaces.Updateable;
import bomberman.service.TimeHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javatuples.Triplet;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.Queue;

public class Bomberman implements IEntity, Updateable {

    public Bomberman(int id, World world, float[] spawnCoordinates) {
        this.id = id;
        bombSpawnTimerInitValue = BOMB_SPAWN_TIMER_BASE_VALUE;
        bombExplosionRange = BOMB_BASE_RANGE;
        maxHealth = MAX_HEALTH_BASE_VALUE;
        health = maxHealth;
        bombExplosionDelay = BOMB_BASE_EXPLOSION_DELAY;
        maxBombsCanBePlaced = BASE_BOMB_AMOUNT;
        currentPlaceableBombs = maxBombsCanBePlaced;
        maximalSpeed = BASE_MAX_SPEED;
        invulnerabilityTimer = INVULNERABILITY_TIME;
        this.spawnCoordinates = spawnCoordinates;
        this.world = world;
    }

    public float[] getCoordinates() {
        return new float[]{x, y};
    }

    public void setCoordinates(float[] coords) {
        x = coords[0];
        y = coords[1];
    }

    public void resetCoordinates() {
        x = spawnCoordinates[0];
        y = spawnCoordinates[1];
    }

    @Override
    public EntityType getType() {
        return EntityType.BOMBERMAN;
    }

    @Override
    public int getID() {
        return id;
    }

    @Override
    public void update(long deltaT) {
        if (bombSpawnTimer >= 0)
            bombSpawnTimer -= deltaT;

        if (invulnerabilityTimer >= 0)
            invulnerabilityTimer -= deltaT;
    }

    public boolean canSpawnBomb() {
        return bombSpawnTimer <= 0 && currentPlaceableBombs > 0;
    }


    //
    // Health Actions
    //

    // Use negative amounts for healing! =D
    public void affectHealth(int amount, @Nullable Integer initiator) {
        if (health > maxHealth)
            health = maxHealth;

        if (amount > 0) {
            LOGGER.debug("Damaging bomberman \"" + this + "\" for " + amount + " hp.");
            if (invulnerabilityTimer <= 0) {
                health -= amount;
                LOGGER.debug("Damaged bomberman \"" + this + "\". Now he has " + health + " hp. Activating invulnerability.");
            } else  {
                LOGGER.debug("Could not damage bomberman \"" + this + "\". He had invulnerability activated.");
            }
            activateInvulnerabilityTimer();
        } else {
            LOGGER.debug("Healing bomberman \"" + this + "\" for " + -amount + " hp.");
            health -= amount;
            if (health > maxHealth)
                health = maxHealth;
        }

        if (health <= 0) {
            LOGGER.debug("Bomberman \"" + this + "\" has died.");
            world.addWorldEvent(new WorldEvent(EventType.TILE_REMOVED, EntityType.BOMBERMAN, id, x, y, initiator, TimeHelper.now()));
        }
    }

    public void increaseMaxHealth() {
        maxHealth += MAX_HEALTH_INCREMENT;
        health += MAX_HEALTH_INCREMENT;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public int getCurrentHealth() {
        return health;
    }

    public void activateInvulnerabilityTimerBonus() {
        invulnerabilityTimer = INVULNERABILITY_TIME * 1000;
    }

    private void activateInvulnerabilityTimer() {
        invulnerabilityTimer = INVULNERABILITY_TIME;
    }

    //
    // Bomb Range Actions
    //

    public int getBombExplosionRange() {
        return bombExplosionRange;
    }

    public void increaseExplosionRange()
    {
        bombExplosionRange += BOMB_RANGE_INCREMENT;
    }


    //
    // Bomb SpawnTimer Actions
    //

    public void resetBombTimer()
    {
        bombSpawnTimer = bombSpawnTimerInitValue;
    }

    public void shortenBombSpawnTimer()
    {
         bombSpawnTimerInitValue *= BOMB_SPAWN_TIMER_MULTIPLIER;
    }


    //
    // Bomb Explosion Delay Actions
    //

    public void shortenBombExplosionDelay() {
        bombExplosionDelay *= BOMB_EXPLOSION_DELAY_MULTIPLIER;
    }

    public long getBombExplosionDelay() {
        return bombExplosionDelay;
    }


    //
    // Bomb Placement Actions
    //

    public void takeOnePlaceableBomb() {
        currentPlaceableBombs--;
    }

    public void returnOnePlaceableBomb() {
        currentPlaceableBombs++;
    }

    public void increaseMaxPlaceableBombs() {
        maxBombsCanBePlaced += BOMB_AMOUNT_INCREMENT;
        currentPlaceableBombs += BOMB_AMOUNT_INCREMENT;
    }

    public void makeDropBombOnDeath() {
        shouldDropBombOnDeath = true;
    }

    public boolean shouldDropBombOnDeath() {
        return shouldDropBombOnDeath;
    }

    //
    // Speed Actions
    //

    public void increaseMaximalSpeed() {
        maximalSpeed += MAX_SPEED_INCREMENT;
    }

    public float getMaximalSpeed() {
        return maximalSpeed;
    }

    public Triplet<Float, Float, Long> getMovementDirection() {
        return movementDirection;
    }

    public void setMovementDirection(Triplet<Float, Float, Long> movementDirectionChange) {
        movementDirection = movementDirectionChange;
    }

    public Queue<Triplet<Float, Float, Long>> getMovementsDuringTick() {
        return movementsDuringTick;
    }

    public void addMovement(Triplet<Float, Float, Long> movementDirectionChange) {
        movementsDuringTick.add(movementDirectionChange);
    }

    public boolean shouldBeUpdated() {
        return movementDirection.getValue0() != 0 || movementDirection.getValue1() != 0;
    }

    // In-World desctription
    @SuppressWarnings("InstanceVariableNamingConvention")
    private float x;                // I know they're short, but I don't think that 'x' may mean something else than "xCoordinate"
    @SuppressWarnings("InstanceVariableNamingConvention")
    private float y;                // "yCoordinate" -> 'y'
    private final int id;           // "uniqueIdentificationNumber" -> "id"
    final World world;

    private final float[] spawnCoordinates;

    // Health Description
    private int health;
    private int maxHealth;
    public static final int MAX_HEALTH_BASE_VALUE = 1;
    public static final int MAX_HEALTH_INCREMENT = 1;   // One powerup for double life. Three powerups for triple life. ()

    private long invulnerabilityTimer;
    public static final long INVULNERABILITY_TIME = 1100; // 1.1 seconds

    // Bomb Description
    private long bombSpawnTimer;
    private long bombSpawnTimerInitValue;
    public static final long BOMB_SPAWN_TIMER_BASE_VALUE = 2500; // 2.5 seconds
    public static final float BOMB_SPAWN_TIMER_MULTIPLIER = 0.8f; // Will be reduced by 1/5 of current value evry time. i.e 2.5->2->1.6->1.28

    private int bombExplosionRange;
    public static final int BOMB_BASE_RANGE = 1;    // 1 tile
    public static final int BOMB_RANGE_INCREMENT = 1;

    private long bombExplosionDelay;
    public static final long BOMB_BASE_EXPLOSION_DELAY = 2000;    // 2 seconds
    public static final float BOMB_EXPLOSION_DELAY_MULTIPLIER = 0.8f;

    private int currentPlaceableBombs;
    private int maxBombsCanBePlaced;
    public static final int BASE_BOMB_AMOUNT = 1;    // 1 tile
    public static final int BOMB_AMOUNT_INCREMENT = 1;

    private boolean shouldDropBombOnDeath = false;

    // Speed Description
    private float maximalSpeed;
    public static final float BASE_MAX_SPEED = 3f / 1000f;    // 3 tiles per second
    public static final float MAX_SPEED_INCREMENT = 0.5f / 1000f; // 3.0 → 3.5 → 4 → 4.5 → 5.5 → 6.0/ Higher the harder. :)
    private Triplet<Float, Float, Long> movementDirection = new Triplet<>(0f, 0f, 0L);
    private final Queue<Triplet<Float, Float, Long>> movementsDuringTick = new LinkedList<>();

    public static final float DIAMETER = 0.75f; // ¾ of a tile.

    private static final Logger LOGGER = LogManager.getLogger(Bomberman.class);
}
