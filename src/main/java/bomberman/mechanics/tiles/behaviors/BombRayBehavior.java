package bomberman.mechanics.tiles.behaviors;

import bomberman.mechanics.World;
import bomberman.mechanics.WorldEvent;
import bomberman.mechanics.interfaces.EventType;

public class BombRayBehavior extends ActionTileAbstractBehavior {
    public BombRayBehavior(World eventList) {
        super(eventList);
        bombTimer = BOMB_RAY_DURATION;
        hasDissipated = false;
    }

    @Override
    public void behave(long deltaT) {
        bombTimer -= deltaT;
        if (bombTimer <= 0 && !hasDissipated)
        {
            eventList.addWorldEvent(new WorldEvent(EventType.TILE_REMOVED, owner.getType(), owner.getID(), 0, 0, null));
            hasDissipated = true;
        }
    }

    private long bombTimer;
    private boolean hasDissipated;

    public static final long BOMB_RAY_DURATION = 1000; // 1 second
}
