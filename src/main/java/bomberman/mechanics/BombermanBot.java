package bomberman.mechanics;

import bomberman.mechanics.interfaces.EntityType;
import bomberman.mechanics.interfaces.EventType;
import bomberman.mechanics.interfaces.IEntity;
import bomberman.service.TimeHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javatuples.Triplet;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.Queue;

public class BombermanBot extends Bomberman {

    public BombermanBot(int id, World world, float[] spawnCoordinates) {
        super(id, world, spawnCoordinates);
    }

    @Override
    public void update(long deltaT) {
        super.update(deltaT);
    }

    private static final Logger LOGGER = LogManager.getLogger(BombermanBot.class);
}
