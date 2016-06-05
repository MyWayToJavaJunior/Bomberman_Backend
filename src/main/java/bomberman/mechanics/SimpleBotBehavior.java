package bomberman.mechanics;

import bomberman.mechanics.interfaces.Updateable;
import bomberman.service.TimeHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javatuples.Triplet;

import java.util.LinkedList;
import java.util.Queue;

public class SimpleBotBehavior implements Updateable {
    SimpleBotBehavior(Bomberman owner, World world) {
        this.owner = owner;
        this.world = world;
    }

    @Override
    public void update(long deltaT) {
        try {
            if (selectNextTargetIfNeeded())
                pathFinder.calculatePathToTarget();
            passMovementsToTarget();
        } catch (Exception ex) {
            LOGGER.error("Something happened in bot's " + owner + " behavior " + this, ex);
        }
    }

    private boolean selectNextTargetIfNeeded() {
        if (target.wasReached() && target.shouldLayBomb() && owner.canSpawnBomb()) {
            world.tryPlacingBomb(owner.getID(), false);
            movementsToTarget.clear();
            targetSelector.selectEvasionTarget();
            return false;
        }

        if (target.isInvalid() || target.wasReached()) {
            movementsToTarget.clear();
            targetSelector.selectNewTarget();
            return true;
        }

        return false;
    }

    private void passMovementsToTarget() {
        final Triplet<Float, Float, Long> nextMovement = movementsToTarget.peek();

        if (nextMovement != null && nextMovement.getValue2() >= TimeHelper.now()) {
            owner.addMovement(nextMovement);
            movementsToTarget.remove();
        }
    }

    private class Target {

        public Target setX(int x) {
            this.x = x;
            return this;
        }

        public Target setY(int y) {
            this.y = y;
            return this;
        }

        public int getY() {
            return y;
        }

        public int getX() {
            return x;
        }

        public boolean shouldLayBomb() {
            return shouldLayBomb;
        }

        public Target makeLayBomb(boolean shouldLayBomb) {
            this.shouldLayBomb = shouldLayBomb;
            return this;
        }

        public boolean isEvasionTarget() {
            return isEvasionTarget;
        }

        public void makeEvasionTarget(boolean evasionTarget) {
            isEvasionTarget = evasionTarget;
        }

        public boolean isFirstTime() {
            return (x != -1) && (y != -1);
        }

        private boolean isInvalid() {
            if (isFirstTime() || wasReached())
                return true;
            if (isEvasionTarget)
                return false;

            boolean isInvalid = true;

            if (world.getTiles()[y][x] != null && world.getTiles()[y][x].isDestructible())
                // If target is tile
                isInvalid = false;
            else {
                boolean isOneOfThemIsValid = false;
                // If target is something else
                for (Bomberman bomberman : world.getBombermen())
                    if (!bomberman.equals(owner))
                        if (distanceTo(bomberman.getCoordinates()[0], bomberman.getCoordinates()[1]) < LOOK_FOR_ENEMY_RADIUS) {
                            // If distance is ok
                            final int enemyX = (int) Math.floor(bomberman.getCoordinates()[0]);
                            final int enemyY = (int) Math.floor(bomberman.getCoordinates()[0]);
                            if (x == enemyX || y == enemyY)
                                // If enemy has not moved
                                isOneOfThemIsValid = true;
                        }
                if (isOneOfThemIsValid)
                    isInvalid = false;
            }

            return isInvalid;
        }

        public boolean wasReached() {
            return distanceTo(x, y) < 0.45f;
        }

        public double distanceTo(double anotherX, double anotherY) {
            final double distanceX = Math.abs(owner.getCoordinates()[0] - anotherX);
            final double distanceY = Math.abs(owner.getCoordinates()[1] - anotherY);
            return Math.sqrt(distanceX * distanceX + distanceY * distanceY);
        }

        private int x = -1;
        private int y = -1;
        private boolean shouldLayBomb = false;
        private boolean isEvasionTarget = false;
    }
    private final Target target = new Target();

    private class PathFinder {
        public void calculatePathToTarget() {
            calculatePathTo(target.getX(), target.getY());
        }
        
        private void calculatePathTo(double x, double y) {

        }

        private void calculatePathTo(int x, int y) {
            calculatePathTo(Math.floor(x) + 0.5, Math.floor(y) + 0.5);
        }

        private long getMovementCompletitonTime(double distance) {
            return (long) Math.floor(1000.0 * distance / owner.getMaximalSpeed());
        }

    }
    private final PathFinder pathFinder = new PathFinder();

    private class TargetSelector {
        public void selectEvasionTarget() {

        }

        public void selectNewTarget() {

        }
    }
    private final TargetSelector targetSelector = new TargetSelector();

    private final Bomberman owner;
    private final World world;

    private final Queue<Triplet<Float, Float, Long>> movementsToTarget = new LinkedList<>();

    private static final float LOOK_FOR_ENEMY_RADIUS = 5.0f;   // 10 tiles in diameter
    private static final Logger LOGGER = LogManager.getLogger(SimpleBotBehavior.class);
}
