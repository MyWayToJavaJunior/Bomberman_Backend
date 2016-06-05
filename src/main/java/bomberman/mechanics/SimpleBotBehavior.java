package bomberman.mechanics;

import bomberman.mechanics.interfaces.Updateable;

public class SimpleBotBehavior implements Updateable {
    SimpleBotBehavior(Bomberman owner, World world) {
        this.owner = owner;
        this.world = world;
    }

    @Override
    public void update(long deltaT) {
        selectNextTarget();
    }


    private boolean isNotFirstTimeTarget() {
        return (targetX != -1) && (targetY != -1);
    }

    private boolean isTargetInvalid() {
        boolean isValid = false;

        if (isNotFirstTimeTarget() && world.getTiles()[targetY][targetX] != null && world.getTiles()[targetY][targetX].isDestructible())
            isValid = true;

        for (Bomberman bomberman: world.getBombermen()) {
            final double distanceX = Math.abs(owner.getCoordinates()[0] - bomberman.getCoordinates()[0]);
            final double distanceY = Math.abs(owner.getCoordinates()[1] - bomberman.getCoordinates()[1]);
            final double distance = Math.sqrt(distanceX * distanceX + distanceY * distanceY);
            if (distance > LOOK_FOR_ENEMY_RADIUS) {
                // If distance has grown and enemy stands still.
                isValid = false;
            } else {
                // If distance is ok and enemy moves.
                final int enemyX = (int)Math.floor(bomberman.getCoordinates()[0]);
                final int enemyY = (int)Math.floor(bomberman.getCoordinates()[0]);
                if (targetX != enemyX || targetY != enemyY)
                    isValid = false;
            }

        }

        return isValid;
    }

    private void selectNextTarget () {
        if (isTargetInvalid()) {

        }
    }

    private int targetX = -1;
    private int targetY = -1;

    private final Bomberman owner;
    private final World world;

    private static final float LOOK_FOR_ENEMY_RADIUS = 5.0f;   // 10 tiles in diameter
}
