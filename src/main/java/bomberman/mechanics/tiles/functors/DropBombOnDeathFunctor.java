package bomberman.mechanics.tiles.functors;

import bomberman.mechanics.Bomberman;
import bomberman.mechanics.World;

public class DropBombOnDeathFunctor extends ActionTileAbstractFunctor {
    public DropBombOnDeathFunctor(World eventList) {
        super(eventList);
    }

    @Override
    public void applyAction(Bomberman bomberman) {
        bomberman.makeDropBombOnDeath();
    }
}
