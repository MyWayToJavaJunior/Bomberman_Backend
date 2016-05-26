package bomberman.mechanics;

import bomberman.mechanics.interfaces.EntityType;
import bomberman.mechanics.interfaces.ITile;
import bomberman.mechanics.tiles.ActionTile;
import bomberman.mechanics.tiles.DestructibleWall;
import bomberman.mechanics.tiles.OwnedActionTile;
import bomberman.mechanics.tiles.UndestructibleWall;
import bomberman.mechanics.tiles.behaviors.BombBehavior;
import bomberman.mechanics.tiles.behaviors.BombRayBehavior;
import bomberman.mechanics.tiles.behaviors.NullBehavior;
import bomberman.mechanics.tiles.functors.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Singleton;

@Singleton
public class TileFactory {
    public static TileFactory getInstance()
    {
        return SINGLETON;
    }

    public ITile getNewTile(EntityType type, int id) throws IllegalArgumentException {
        //noinspection EnumSwitchStatementWhichMissesCases
        switch (type)
        {
            case UNDESTRUCTIBLE_WALL:
                return newUndestructibleWall(id);
            case DESTRUCTIBLE_WALL:
                return newDestructibleWall(id);
            default:
                LOGGER.error("Impossible to spawn " + type + "with two arguments method.");
                throw new IllegalArgumentException();
        }

    }

    public ITile getNewTile(EntityType type, World list, int id) throws IllegalArgumentException {
        //noinspection EnumSwitchStatementWhichMissesCases
        switch (type)
        {
            case BONUS_INCMAXHP:
                return newBonusIncreaseMaxHP(id, list);
            case BONUS_INCMAXRANGE:
                return newBonusIncreaseBombRange(id, list);
            case BONUS_DECBOMBFUSE:
                return newBonusDecreaseExplosionDelay(id, list);
            case BONUS_DECBOMBSPAWN:
                return newBonusDecreaseSpawnDelay(id, list);
            case BONUS_INCSPEED:
                return newBonusIncreaseBombermanSpeed(id, list);
            case BONUS_MOREBOMBS:
                return newBonusIncreaseMaxBombs(id, list);
            case BONUS_DROPBOMBONDEATH:
                return newBonusDropBombOnDeath(id, list);
            default:
                LOGGER.error("Impossible to spawn " + type + "with three arguments method.");
                throw new IllegalArgumentException();
        }
    }

    public ITile getNewTile(EntityType type, World list, Bomberman owner, int id) throws IllegalArgumentException {
        //noinspection EnumSwitchStatementWhichMissesCases
        switch (type)
        {
            case BOMB:
                return newBomb(id, list, owner);
            case BOMB_RAY:
                return newBombRay(id, list, owner);
            default:
                LOGGER.error("Impossible to spawn " + type + "with four arguments method.");
                throw new IllegalArgumentException();
        }
    }

    public static int getBonusCount() { return BONUS_COUNT; }

    private ITile newUndestructibleWall(int id) {
        return new UndestructibleWall(id);
    }
    private ITile newDestructibleWall(int id ) {
        return new DestructibleWall(id);
    }

    private ITile newBomb(int id, World list, Bomberman owner) {
        return new OwnedActionTile(id, new NullFunctor(list), new BombBehavior(list, owner.getBombExplosionDelay()), EntityType.BOMB, owner);
    }
    private ITile newBombRay(int id, World list, Bomberman owner) {
        return new OwnedActionTile(id, new BombRayFunctor(list, owner), new BombRayBehavior(list), EntityType.BOMB_RAY, owner);
    }

    private ITile newBonusDropBombOnDeath(int id, World list) {
        return new ActionTile(id, new DropBombOnDeathFunctor(list), new NullBehavior(list), EntityType.BONUS_DROPBOMBONDEATH);
    }
    private ITile newBonusIncreaseMaxHP(int id, World list){
        return new ActionTile(id, new IncreaseMaxHPFunctor(list), new NullBehavior(list), EntityType.BONUS_INCMAXHP);
    }
    private ITile newBonusIncreaseBombRange(int id, World list){
        return new ActionTile(id, new IncreaseBombRangeFunctor(list), new NullBehavior(list), EntityType.BONUS_INCMAXRANGE);
    }
    private ITile newBonusDecreaseSpawnDelay(int id, World list){
        return new ActionTile(id, new DecreaseBombSpawnDelayFunctor(list), new NullBehavior(list), EntityType.BONUS_DECBOMBSPAWN);
    }
    private ITile newBonusDecreaseExplosionDelay(int id, World list){
        return new ActionTile(id, new DecreaseBombExplosionDelayFunctor(list), new NullBehavior(list), EntityType.BONUS_DECBOMBFUSE);
    }
    private ITile newBonusIncreaseBombermanSpeed(int id, World list){
        return new ActionTile(id, new IncreaseSpeedFunctor(list), new NullBehavior(list), EntityType.BONUS_INCSPEED);
    }

    private ITile newBonusIncreaseMaxBombs(int id, World list) {
        return new ActionTile(id, new IncreaseSpawnableBombAmountFunctor(list), new NullBehavior(list), EntityType.BONUS_INCSPEED);
    }

    private static final TileFactory SINGLETON = new TileFactory();
    private static final int BONUS_COUNT = 7;
    private static final Logger LOGGER = LogManager.getLogger(TileFactory.class);
}
