package bomberman.mechanics;

import bomberman.mechanics.interfaces.Updateable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BombermanBot extends Bomberman {

    public BombermanBot(int id, World world, float[] spawnCoordinates) {
        super(id, world, spawnCoordinates);
        behavior = new SimpleBotBehavior(this, world);
    }

    @Override
    public void update(long deltaT) {
        super.update(deltaT);
        behavior.update(deltaT);
    }

    private final Updateable behavior;
    private static final Logger LOGGER = LogManager.getLogger(BombermanBot.class);
}
