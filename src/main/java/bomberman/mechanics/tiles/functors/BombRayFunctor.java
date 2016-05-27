package bomberman.mechanics.tiles.functors;

import bomberman.mechanics.Bomberman;
import bomberman.mechanics.World;

public class BombRayFunctor extends ActionTileAbstractFunctor {

    public BombRayFunctor(World eventList, Bomberman owner) {
        super(eventList);
        this.placer = owner;
    }

    @Override
    public void applyAction(Bomberman bomberman) {
        bomberman.affectHealth(Bomberman.MAX_HEALTH_BASE_VALUE, (placer == null) ? null : placer.getID());
    }

    private final Bomberman placer;
}
