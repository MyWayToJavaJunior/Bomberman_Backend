package bomberman.mechanics.tiles.behaviors;

import bomberman.mechanics.World;
import bomberman.mechanics.WorldEvent;
import bomberman.mechanics.interfaces.EventType;

public class BombBehavior extends ActionTileAbstractBehavior {
    public BombBehavior(World eventList, long timer) {
        super(eventList);
        bombTimer = timer;
        hasExploded = false;
    }

    @Override
    public void behave(long deltaT) {
        bombTimer -= deltaT;
        if (bombTimer <= 0 && !hasExploded)
        {
            eventList.addWorldEvent(new WorldEvent(EventType.TILE_REMOVED, owner.getType(), owner.getID(), 0, 0, null));
            hasExploded = true;
        }
    }

    private boolean hasExploded;
    private long bombTimer;
}
