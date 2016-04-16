package bomberman.mechanics.tiles;

import bomberman.mechanics.interfaces.EntityType;

public class UndestructibleWall extends AbstractTile{
    public UndestructibleWall(int id, int x, int y) {
        super(id, x, y);
    }

    @Override
    public EntityType getType() {
        return EntityType.UNDESTRUCTIBLE_WALL;
    }

    @Override
    public boolean isPassable() {
        return false;
    }

    @Override
    public boolean shouldBeDestroyed() {
        return false;
    }
}