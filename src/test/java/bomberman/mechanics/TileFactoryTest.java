package bomberman.mechanics;

import bomberman.mechanics.interfaces.EntityType;
import bomberman.mechanics.interfaces.ITile;
import bomberman.mechanics.interfaces.Ownable;
import constants.Constants;
import org.junit.Test;

import static org.junit.Assert.*;

@SuppressWarnings("OverlyBroadThrowsClause")
public class TileFactoryTest {

    @Test
    public void testGetNewDestructibleWall() throws Exception {
        final TileFactory factory = TileFactory.getInstance();
        final ITile tile = factory.getNewTile(EntityType.DESTRUCTIBLE_WALL, 0);

        assertNotNull(tile);
        assertEquals(EntityType.DESTRUCTIBLE_WALL, tile.getType());
    }

    @Test
    public void testGetNewUndestructibleWall() throws Exception {
        final TileFactory factory = TileFactory.getInstance();
        final ITile tile = factory.getNewTile(EntityType.UNDESTRUCTIBLE_WALL, 0);

        assertNotNull(tile);
        assertEquals(EntityType.UNDESTRUCTIBLE_WALL, tile.getType());
    }

    @Test
    public void testGetNewRangeBonus() throws Exception {
        final TileFactory factory = TileFactory.getInstance();
        final ITile tile = factory.getNewTile(EntityType.BONUS_INCMAXRANGE, Constants.GameMechanicsMocks.getMockedWorld(), 0);

        assertNotNull(tile);
        assertEquals(EntityType.BONUS_INCMAXRANGE, tile.getType());
    }

    @Test
    public void testGetNewSpawnTimeBonus() throws Exception {
        final TileFactory factory = TileFactory.getInstance();
        final ITile tile = factory.getNewTile(EntityType.BONUS_DECBOMBSPAWN, Constants.GameMechanicsMocks.getMockedWorld(), 0);

        assertNotNull(tile);
        assertEquals(EntityType.BONUS_DECBOMBSPAWN, tile.getType());
    }

    @Test
    public void testGetNewBomb() throws Exception {
        final TileFactory factory = TileFactory.getInstance();
        final ITile tile = factory.getNewTile(EntityType.BOMB, Constants.GameMechanicsMocks.getMockedWorld(), Constants.GameMechanicsMocks.getBomberman(), 0);

        assertNotNull(tile);
        assertEquals(EntityType.BOMB, tile.getType());
        assertEquals(Constants.GameMechanicsMocks.getBomberman(), ((Ownable) tile).getOwner());
    }

    @Test
    public void testGetNewBombRay() throws Exception {
        final TileFactory factory = TileFactory.getInstance();
        final ITile tile = factory.getNewTile(EntityType.BOMB_RAY, Constants.GameMechanicsMocks.getMockedWorld(), Constants.GameMechanicsMocks.getBomberman(), 0);

        assertNotNull(tile);
        assertEquals(EntityType.BOMB_RAY, tile.getType());
        assertEquals(Constants.GameMechanicsMocks.getBomberman(), ((Ownable) tile).getOwner());
    }
}