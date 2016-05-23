package bomberman.mechanics.tiles.functors;

import bomberman.mechanics.Bomberman;
import bomberman.mechanics.World;
import bomberman.mechanics.WorldEvent;
import bomberman.mechanics.interfaces.EventType;

public class IncreaseSpeedFunctor extends ActionTileAbstractFunctor {

    public IncreaseSpeedFunctor(World eventList) {
        super(eventList);
    }

    @Override
    public void applyAction(Bomberman bomberman) {
        bomberman.increaseMaximalSpeed();
        eventList.addWorldEvent(new WorldEvent(EventType.TILE_REMOVED, owner.getType(), owner.getID(), 0, 0));
    }
}
