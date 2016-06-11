package bomberman.mechanics;

import bomberman.mechanics.interfaces.EntityType;
import bomberman.mechanics.interfaces.ITile;
import bomberman.mechanics.interfaces.Updateable;
import bomberman.service.TimeHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javatuples.Pair;
import org.javatuples.Triplet;

import java.util.*;

public class SimpleBotBehavior implements Updateable {
    SimpleBotBehavior(Bomberman owner, World world) {
        this.owner = owner;
        this.world = world;
    }

    @Override
    public void update(long deltaT) {
        //noinspection OverlyBroadCatchBlock
        try {
            selectNextTargetIfNeeded();
            passMovementsToTarget();
        } catch (Exception ex) {
            LOGGER.error("Something happened in bot's " + owner + " behavior " + this, ex);
        }
    }

    private boolean selectNextTargetIfNeeded() {
        if (target.wasReached() && target.canLayBomb()) {
            world.tryPlacingBomb(owner.getID(), false);
            movementsToTarget.clear();
            pathFinder.makeEvasionFromBomb();
            return false;
        }

        if (target.isInvalid() || target.wasReached()) {
            movementsToTarget.clear();
            pathFinder.selectNewTarget();
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

        public void invalidateTarget() {
            x = -1;
            y = -1;
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

        public Target makeLayBomb(@SuppressWarnings("ParameterHidesMemberVariable") boolean shouldLayBomb) {
            this.shouldLayBomb = shouldLayBomb;
            return this;
        }

        public boolean canLayBomb() {
            if (shouldLayBomb && owner.canSpawnBomb())
                if (owner.getCoordinates()[0] == x && owner.getCoordinates()[1] == y)
                    if (distanceTo(x, y) < owner.getBombExplosionRange())
                        return true;
            return false;
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
            /*
            final double distanceX = Math.abs(owner.getCoordinates()[0] - anotherX);
            final double distanceY = Math.abs(owner.getCoordinates()[1] - anotherY);
            return Math.sqrt(distanceX * distanceX + distanceY * distanceY);
            */
            return pathFinder.distanceBetween(owner.getCoordinates()[0], owner.getCoordinates()[1], anotherX, anotherY);
        }

        public boolean isBonus(ITile tile) {
            boolean result = false;

            if (tile != null)
                if (tile.getType() == EntityType.BONUS_DECBOMBSPAWN ||
                        tile.getType() == EntityType.BONUS_DROPBOMBONDEATH ||
                        tile.getType() == EntityType.BONUS_INCMAXHP ||
                        tile.getType() == EntityType.BONUS_INCMAXRANGE ||
                        tile.getType() == EntityType.BONUS_INCSPEED ||
                        tile.getType() == EntityType.BONUS_INVUL ||
                        tile.getType() == EntityType.BONUS_MOREBOMBS)
                    result = true;

            return result;
        }

        public boolean isDestructibleWall(ITile tile) {
            boolean result = false;

            if (tile != null)
                if (tile.getType() == EntityType.DESTRUCTIBLE_WALL)
                    result = true;

            return result;
        }



        @SuppressWarnings("InstanceVariableNamingConvention")
        private int x = -1;
        @SuppressWarnings("InstanceVariableNamingConvention")
        private int y = -1;
        private boolean shouldLayBomb = false;
        private boolean isEvasionTarget = false;
    }
    private final Target target = new Target();

    private class PathFinder {
        public void calculatePathToTarget() {
            calculatePathTo(target.getX(), target.getY());
        }

        public void makeEvasionFromBomb() {
            final int bombX = (int) owner.getCoordinates()[0];
            final int bombY = (int) owner.getCoordinates()[1];
            final int bombRadius = owner.getBombExplosionRange();
            final List<Pair<Integer, Integer>> movementsList = calculateEvasionPath(bombX, bombY, 0, bombX, bombY, bombRadius);

            target.makeEvasionTarget(true);
            target.makeLayBomb(false);

            storeMovements(movementsList);
        }

        private void storeMovements(List<Pair<Integer, Integer>> movementsList) {
            long movementStartTime = TimeHelper.now();
            int prevTileX = (int) owner.getCoordinates()[0];
            int prevTileY = (int) owner.getCoordinates()[1];
            for (Pair<Integer, Integer> movement : movementsList) {
                movementsToTarget.add(new Triplet<>((float) prevTileX - movement.getValue0(), (float) prevTileY - movement.getValue1(), movementStartTime));
                movementStartTime += getMovementCompletitonTime(distanceBetween(prevTileX, prevTileY, movement.getValue0(), movement.getValue1()));
                prevTileX = movement.getValue0();
                prevTileY = movement.getValue1();
            }
        }

        public double distanceBetween(double otherX, double otherY, double anotherX, double anotherY) {
            final double distanceX = Math.abs(otherX - anotherX);
            final double distanceY = Math.abs(otherY - anotherY);
            return Math.sqrt(distanceX * distanceX + distanceY * distanceY);
        }

        public void selectNewTarget() {
            target.makeEvasionTarget(false);

            if (markBombermanAsATarget(KILL_ENEMY_NOW_RADIUS))
                target.makeLayBomb(true);
            else if (seekForASpecificTile(GATHER_BONUS_RADIUS, target::isBonus))
                target.makeLayBomb(false);
            else if (seekForASpecificTile(BREAK_WALL_RADIUS, target::isDestructibleWall))
                target.makeLayBomb(true);
            else if (markBombermanAsATarget(KILL_ENEMY_ANYWAY))
                target.makeLayBomb(true);
        }

        private boolean markBombermanAsATarget(int notFartherThan) {
            Bomberman enemy = null;

            for (Bomberman bomberman : world.getBombermen())
                if (bomberman.getID() != owner.getID())
                    if (target.distanceTo(bomberman.getCoordinates()[0], bomberman.getCoordinates()[1]) < notFartherThan) {
                        enemy = bomberman;
                        break;
                    }

            if (enemy != null) {
                target.setX((int) enemy.getCoordinates()[0]);
                target.setY((int) enemy.getCoordinates()[1]);
                return true;
            }

            return false;
        }

        private boolean seekForASpecificTile(int notFartherThan, SpecificTileCondition cond) {
            final List<Pair<Integer, Integer>> movementsToASpecificTile = movementsToASpecificTile((int) owner.getCoordinates()[0], (int) owner.getCoordinates()[1], 0, notFartherThan, cond);

            if (!movementsToASpecificTile.isEmpty()) {
                storeMovements(movementsToASpecificTile);
                return true;
            } else
                return false;
        }

        private List<Pair<Integer, Integer>> movementsToASpecificTile(int x, int y, int iterationDepth, int notFartherThan, SpecificTileCondition cond) {
            List<Pair<Integer, Integer>> movementsToReachSafeTile = new LinkedList<>();
            final ArrayList<List<Pair<Integer, Integer>>> sortArray = new ArrayList<>(4);

            for (int dx = -1; dx < 2; dx += 2)
                for (int dy = -1; dy < 2; dy += 2) {
                    final ITile tile = world.getTiles()[y + dy][x + dx];
                    if (cond.isThisKindOfITile(tile)) {
                        final LinkedList<Pair<Integer, Integer>> safeMovement = new LinkedList<>();

                        safeMovement.add(new Pair<>(x + dx, y + dy));

                        return safeMovement;
                    } else if (iterationDepth <= notFartherThan && isTileSafe(x + dx, y + dy)) {
                        final List<Pair<Integer, Integer>> nextMovements = movementsToASpecificTile(x + dx, y + dy, iterationDepth + 1, notFartherThan, cond);

                        if (!nextMovements.isEmpty()) {
                            nextMovements.add(0, new Pair<>(x + dx, y + dy));
                            sortArray.add(nextMovements);
                        }
                    }
                }

            boolean movementsToTargetNotTouched = true;

            if (!sortArray.isEmpty())
                for (List<Pair<Integer, Integer>> movementList : sortArray)
                    if (movementsToTargetNotTouched || movementList.size() < movementsToReachSafeTile.size()) {
                        movementsToTargetNotTouched = false;
                        movementsToReachSafeTile = movementList;
                    }

            return movementsToReachSafeTile;
        }

        // TODO: Move bombY, bombX, bombRadius into a separate lambda and unite this method with previous one.
        private List<Pair<Integer, Integer>> calculateEvasionPath(int x, int y, int iterationDepth, int bombX, int bombY, int bombRadius) {
            List<Pair<Integer, Integer>> movementsToReachSafeTile = new LinkedList<>();
            final ArrayList<List<Pair<Integer, Integer>>> sortArray = new ArrayList<>(4);

            for (int dx = -1; dx < 2; dx += 2)
                for (int dy = -1; dy < 2; dy += 2)
                    if (isTileSafeFromExplosion(x + dx, y + dy, bombX, bombY, bombRadius)) {
                        final LinkedList<Pair<Integer, Integer>> safeMovement = new LinkedList<>();

                        safeMovement.add(new Pair<>(x + dx, y + dy));

                        return safeMovement;
                    } else
                        if (iterationDepth <= getMaximalRecursionDepth()) {
                            final List<Pair<Integer, Integer>> nextMovements = calculateEvasionPath(x + dx, y + dy, iterationDepth + 1, bombX, bombY, bombRadius);

                            if (!nextMovements.isEmpty()) {
                                nextMovements.add(0, new Pair<>(x + dx, y + dy));
                                sortArray.add(nextMovements);
                            }
                        }

            boolean movementsToTargetNotTouched = true;

            if (!sortArray.isEmpty())
                for (List<Pair<Integer, Integer>> movementList : sortArray)
                    if (movementsToTargetNotTouched || movementList.size() < movementsToReachSafeTile.size()) {
                        movementsToTargetNotTouched = false;
                        movementsToReachSafeTile = movementList;
                    }

            return movementsToReachSafeTile;
        }

        private void calculatePathTo(double x, double y) {

        }

        private void calculatePathTo(int x, int y) {
            calculatePathTo(Math.floor(x) + 0.5, Math.floor(y) + 0.5);
        }

        private long getMovementCompletitonTime(double distance) {
            return (long) Math.floor(distance / owner.getMaximalSpeed());
        }

        private boolean isTileSafeFromExplosion(int tileX, int tileY, int bombX, int bombY, int bombRadius) {
            if (isTileSafe(tileX, tileY)) {
                if (tileX != bombX || tileY != bombY)
                    return true;
                if (distanceBetween(tileX, tileY, bombX, bombY) > bombRadius)
                    return true;
            }
            //Lazy to check any obstacles
            return false;
        }

        private boolean isTileSafe(int tileX, int tileY) {
            if (tileX < 0 || tileX > world.getWidth())
                return false;
            if (tileY < 0 || tileY > world.getHeight())
                return false;

            final ITile tile = world.getTiles()[tileY][tileX];

            return tile == null || tile.isPassable() && tile.getType() != EntityType.BOMB_RAY;
        }

        private int getMaximalRecursionDepth() {
            return owner.getBombExplosionRange();
        }

        public static final int KILL_ENEMY_NOW_RADIUS = 4; // any enemy bomberman within 5 tile radius will be a target.
        public static final int GATHER_BONUS_RADIUS = 8; // any bonus within 8 tile radius will be a target.
        public static final int BREAK_WALL_RADIUS = 12; // covers ~2/3 of the map. Any breakable wall will be attemted to be removed
        public static final int KILL_ENEMY_ANYWAY = 256; // if the map is scorched and nothing else can be broken, scout and destroy enemies!
    }
    private final PathFinder pathFinder = new PathFinder();

    interface SpecificTileCondition {
        boolean isThisKindOfITile(ITile tile);
    }

    private final Bomberman owner;
    private final World world;

    private final Queue<Triplet<Float, Float, Long>> movementsToTarget = new LinkedList<>();

    private static final float LOOK_FOR_ENEMY_RADIUS = 5.0f;   // 10 tiles in diameter
    private static final Logger LOGGER = LogManager.getLogger(SimpleBotBehavior.class);
}
