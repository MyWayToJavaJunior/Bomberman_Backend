package bomberman.mechanics;

import bomberman.mechanics.interfaces.EntityType;
import bomberman.mechanics.interfaces.EventType;
import bomberman.mechanics.interfaces.IWorldBuilder;
import bomberman.mechanics.worldbuilders.TextWorldBuilder;
import bomberman.mechanics.worldbuilders.WorldData;
import constants.Constants;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;

import static org.junit.Assert.*;

@SuppressWarnings("MagicNumber")
public class WorldTest {

    @Before
    public void setupStrangeWorld() {
        //noinspection OverlyBroadCatchBlock
        try {
            world = new World("basic-world");

            final Field tileArray = world.getClass().getDeclaredField("tileArray");
            tileArray.setAccessible(true);
            final Field spawnLocations = world.getClass().getDeclaredField("spawnLocations");
            spawnLocations.setAccessible(true);

            final IWorldBuilder strangeWorldBuilder = new TextWorldBuilder(new File("data/movement-test-world-do-not-alter.txt"));
            final WorldData worldData = strangeWorldBuilder.getWorldData(world);

            tileArray.set(world, worldData.getTileArray());
            spawnLocations.set(world, worldData.getSpawnList());
            world.getFreshEvents();

            world.spawnBombermen(BOMBERMEN_AMOUNT);
            setupTestLocations();

        } catch (Exception ex) {
            ex.printStackTrace();
            fail();
        }

    }

    @SuppressWarnings("OverlyComplexMethod")
    private void setupTestLocations() {
        final Queue<WorldEvent> spawns = world.getFreshEvents();
        assertEquals(BOMBERMEN_AMOUNT, spawns.size());

        while (!spawns.isEmpty()) {
            final WorldEvent event = spawns.poll();

            if (event.getY() == 0.5 && event.getX() == 0.5)
                bombermen.put(Location.TOP_LEFT_EDGE, event.getEntityID());
            if (event.getY() == 0.5 && event.getX() == 31.5)
                bombermen.put(Location.TOP_RIGHT_EDGE, event.getEntityID());

            if (event.getY() == 2.5 && event.getX() == 2.5)
                bombermen.put(Location.TOP_LEFT, event.getEntityID());
            if (event.getY() == 2.5 && event.getX() == 29.5)
                bombermen.put(Location.TOP_RIGHT, event.getEntityID());

            if (event.getY() == 29.5 && event.getX() == 2.5)
                bombermen.put(Location.BOTTOM_LEFT, event.getEntityID());
            if (event.getY() == 29.5 && event.getX() == 29.5)
                bombermen.put(Location.BOTTOM_RIGHT, event.getEntityID());

            if (event.getY() == 31.5 && event.getX() == 0.5)
                bombermen.put(Location.BOTTOM_LEFT_EDGE, event.getEntityID());
            if (event.getY() == 31.5 && event.getX() == 31.5)
                bombermen.put(Location.BOTTOM_RIGHT_EDGE, event.getEntityID());
        }
    }

    @Test
    public void testBorderHorizontalCollisions() {
        final TestType variant = TestType.BORDER_HORIZONTAL;

        final List<WorldEvent> expected = new TestConditions().addEventsandGetExpectedResults(variant);

        world.runGameLoop(10000);

        assertMovementsEqual(expected, world.getFreshEvents(), variant);
    }
    @Test
    public void testInnerHorizontalCollisions() {
        final TestType variant = TestType.INNER_HORIZONTAL;

        final List<WorldEvent> expected = new TestConditions().addEventsandGetExpectedResults(variant);

        world.runGameLoop(10000);

        assertMovementsEqual(expected, world.getFreshEvents(), variant);
    }

    @Test
    public void testBorderVerticalCollisions() {
        final TestType variant = TestType.BORDER_VERTICAL;

        final List<WorldEvent> expected = new TestConditions().addEventsandGetExpectedResults(variant);

        world.runGameLoop(10000);

        assertMovementsEqual(expected, world.getFreshEvents(), variant);
    }
    @Test
    public void testInnerVerticalCollisions() {
        final TestType variant = TestType.INNER_VERTICAL;

        final List<WorldEvent> expected = new TestConditions().addEventsandGetExpectedResults(variant);

        world.runGameLoop(10000);

        assertMovementsEqual(expected, world.getFreshEvents(), variant);
    }

    @Test
    public void testBorderDiagonalCollisions() {
        final TestType variant = TestType.BORDER_DIAGONAL;

        final List<WorldEvent> expected = new TestConditions().addEventsandGetExpectedResults(variant);

        world.runGameLoop(10000);

        assertMovementsEqual(expected, world.getFreshEvents(), variant);
    }
    @Test
    public void testInnerDiagonalCollisions () {
        final TestType variant = TestType.INNER_DIAGONAL;

        final List<WorldEvent> expected = new TestConditions().addEventsandGetExpectedResults(variant);

        world.runGameLoop(10000);

        assertMovementsEqual(expected, world.getFreshEvents(), variant);
    }

    private static void assertMovementsEqual(List<WorldEvent> expected, Queue<WorldEvent> actual, TestType type) {
        int id = 0;
        try {
            while (!actual.isEmpty()) {
                final WorldEvent actualEvent = actual.poll();
                for (WorldEvent expectedEvent : expected)
                    if (expectedEvent.getEntityID() == actualEvent.getEntityID()) {
                        id = expectedEvent.getEntityID();
                        assertEquals(expectedEvent.getX(), actualEvent.getX(), Constants.SOME_ERROR_DELTA);
                        assertEquals(expectedEvent.getY(), actualEvent.getY(), Constants.SOME_ERROR_DELTA);
                    }

            }
        } catch (AssertionError ex) {
            System.out.println();
            System.out.println("Assertion failed during specimen #" + id + ' ' + type + " test");
            ex.printStackTrace(System.out);
            fail();
        }
    }

    @SuppressWarnings("MagicNumber")
    private class TestConditions {
        public List<WorldEvent> addEventsandGetExpectedResults(TestType type) {
            switch (type) {
                case BORDER_HORIZONTAL:
                    addBorderHorizontalEvents();
                    return expectedBorderHorizontalEvents();
                case BORDER_VERTICAL:
                    addBorderVerticalEvents();
                    return expectedBorderVerticalEvents();
                case BORDER_DIAGONAL:
                    addBorderDiagonalEvents();
                    return expectedBorderDiagonalEvents();
                case INNER_HORIZONTAL:
                    addInnerHorizontalEvents();
                    return expectedInnerHorizontalEvents();
                case INNER_VERTICAL:
                    addInnerVerticalEvents();
                    return expectedInnerVerticalEvents();
                case INNER_DIAGONAL:
                    addInnerDiagonalEvents();
                    return expectedInnerDiagonalEvents();
            }
            return new LinkedList<>();
        }

        private void addBorderHorizontalEvents() {
            world.addWorldEvent(new WorldEvent(EventType.ENTITY_UPDATED, EntityType.BOMBERMAN, bombermen.get(Location.TOP_LEFT_EDGE), -1f, 0f, 0));
            world.addWorldEvent(new WorldEvent(EventType.ENTITY_UPDATED, EntityType.BOMBERMAN, bombermen.get(Location.BOTTOM_LEFT_EDGE), -1f, 0f, 0));

            world.addWorldEvent(new WorldEvent(EventType.ENTITY_UPDATED, EntityType.BOMBERMAN, bombermen.get(Location.TOP_RIGHT_EDGE), 1f, 0f, 0));
            world.addWorldEvent(new WorldEvent(EventType.ENTITY_UPDATED, EntityType.BOMBERMAN, bombermen.get(Location.BOTTOM_RIGHT_EDGE), 1f, 0f, 0));
        }
        private List<WorldEvent> expectedBorderHorizontalEvents() {
            final List<WorldEvent> result = new LinkedList<>();

            result.add(new WorldEvent(EventType.ENTITY_UPDATED, EntityType.BOMBERMAN, bombermen.get(Location.TOP_LEFT_EDGE), 0.375f, 0.5f));
            result.add(new WorldEvent(EventType.ENTITY_UPDATED, EntityType.BOMBERMAN, bombermen.get(Location.TOP_RIGHT_EDGE), 31.625f, 0.5f));

            result.add(new WorldEvent(EventType.ENTITY_UPDATED, EntityType.BOMBERMAN, bombermen.get(Location.BOTTOM_LEFT_EDGE), 0.375f, 31.5f));
            result.add(new WorldEvent(EventType.ENTITY_UPDATED, EntityType.BOMBERMAN, bombermen.get(Location.BOTTOM_RIGHT_EDGE), 31.625f, 31.5f));

            return result;
        }

        private void addBorderVerticalEvents() {
            world.addWorldEvent(new WorldEvent(EventType.ENTITY_UPDATED, EntityType.BOMBERMAN, bombermen.get(Location.TOP_LEFT_EDGE), 0f, -1f, 0));
            world.addWorldEvent(new WorldEvent(EventType.ENTITY_UPDATED, EntityType.BOMBERMAN, bombermen.get(Location.TOP_RIGHT_EDGE), 0f, -1f, 0));

            world.addWorldEvent(new WorldEvent(EventType.ENTITY_UPDATED, EntityType.BOMBERMAN, bombermen.get(Location.BOTTOM_LEFT_EDGE), 0f, 1f, 0));
            world.addWorldEvent(new WorldEvent(EventType.ENTITY_UPDATED, EntityType.BOMBERMAN, bombermen.get(Location.BOTTOM_RIGHT_EDGE), 0f, 1f, 0));
        }
        private List<WorldEvent> expectedBorderVerticalEvents() {
            final List<WorldEvent> result = new LinkedList<>();

            result.add(new WorldEvent(EventType.ENTITY_UPDATED, EntityType.BOMBERMAN, bombermen.get(Location.TOP_LEFT_EDGE), 0.5f, 0.375f));
            result.add(new WorldEvent(EventType.ENTITY_UPDATED, EntityType.BOMBERMAN, bombermen.get(Location.BOTTOM_LEFT_EDGE), 0.5f, 31.625f));

            result.add(new WorldEvent(EventType.ENTITY_UPDATED, EntityType.BOMBERMAN, bombermen.get(Location.TOP_RIGHT_EDGE), 31.5f, 0.375f));
            result.add(new WorldEvent(EventType.ENTITY_UPDATED, EntityType.BOMBERMAN, bombermen.get(Location.BOTTOM_RIGHT_EDGE), 31.5f, 31.625f));

            return result;
        }

        private void addBorderDiagonalEvents() {
            world.addWorldEvent(new WorldEvent(EventType.ENTITY_UPDATED, EntityType.BOMBERMAN, bombermen.get(Location.TOP_LEFT_EDGE), -1f, -1f, 0));
            world.addWorldEvent(new WorldEvent(EventType.ENTITY_UPDATED, EntityType.BOMBERMAN, bombermen.get(Location.TOP_RIGHT_EDGE), 1f, -1f, 0));
            world.addWorldEvent(new WorldEvent(EventType.ENTITY_UPDATED, EntityType.BOMBERMAN, bombermen.get(Location.BOTTOM_LEFT_EDGE), -1f, 1f, 0));
            world.addWorldEvent(new WorldEvent(EventType.ENTITY_UPDATED, EntityType.BOMBERMAN, bombermen.get(Location.BOTTOM_RIGHT_EDGE), 1f, 1f, 0));
        }
        private List<WorldEvent> expectedBorderDiagonalEvents() {
            final List<WorldEvent> result = new LinkedList<>();

            result.add(new WorldEvent(EventType.ENTITY_UPDATED, EntityType.BOMBERMAN, bombermen.get(Location.TOP_LEFT_EDGE), 0.375f, 0.375f));
            result.add(new WorldEvent(EventType.ENTITY_UPDATED, EntityType.BOMBERMAN, bombermen.get(Location.BOTTOM_LEFT_EDGE), 0.375f, 31.625f));
            result.add(new WorldEvent(EventType.ENTITY_UPDATED, EntityType.BOMBERMAN, bombermen.get(Location.TOP_RIGHT_EDGE), 31.625f, 0.375f));
            result.add(new WorldEvent(EventType.ENTITY_UPDATED, EntityType.BOMBERMAN, bombermen.get(Location.BOTTOM_RIGHT_EDGE), 31.625f, 31.625f));

            return result;
        }

        private void addInnerHorizontalEvents() {
            world.addWorldEvent(new WorldEvent(EventType.ENTITY_UPDATED, EntityType.BOMBERMAN, bombermen.get(Location.TOP_LEFT), -1f, 0f, 0));
            world.addWorldEvent(new WorldEvent(EventType.ENTITY_UPDATED, EntityType.BOMBERMAN, bombermen.get(Location.BOTTOM_LEFT), -1f, 0f, 0));

            world.addWorldEvent(new WorldEvent(EventType.ENTITY_UPDATED, EntityType.BOMBERMAN, bombermen.get(Location.TOP_RIGHT), 1f, 0f, 0));
            world.addWorldEvent(new WorldEvent(EventType.ENTITY_UPDATED, EntityType.BOMBERMAN, bombermen.get(Location.BOTTOM_RIGHT), 1f, 0f, 0));
        }
        private List<WorldEvent> expectedInnerHorizontalEvents() {
            final List<WorldEvent> result = new LinkedList<>();

            result.add(new WorldEvent(EventType.ENTITY_UPDATED, EntityType.BOMBERMAN, bombermen.get(Location.TOP_LEFT), 2.375f, 2.5f));
            result.add(new WorldEvent(EventType.ENTITY_UPDATED, EntityType.BOMBERMAN, bombermen.get(Location.TOP_RIGHT), 29.625f, 2.5f));

            result.add(new WorldEvent(EventType.ENTITY_UPDATED, EntityType.BOMBERMAN, bombermen.get(Location.BOTTOM_LEFT), 2.375f, 29.5f));
            result.add(new WorldEvent(EventType.ENTITY_UPDATED, EntityType.BOMBERMAN, bombermen.get(Location.BOTTOM_RIGHT), 29.625f, 29.5f));

            return result;
        }

        private void addInnerVerticalEvents() {
            world.addWorldEvent(new WorldEvent(EventType.ENTITY_UPDATED, EntityType.BOMBERMAN, bombermen.get(Location.TOP_LEFT), 0f, -1f, 0));
            world.addWorldEvent(new WorldEvent(EventType.ENTITY_UPDATED, EntityType.BOMBERMAN, bombermen.get(Location.TOP_RIGHT), 0f, -1f, 0));

            world.addWorldEvent(new WorldEvent(EventType.ENTITY_UPDATED, EntityType.BOMBERMAN, bombermen.get(Location.BOTTOM_LEFT), 0f, 1f, 0));
            world.addWorldEvent(new WorldEvent(EventType.ENTITY_UPDATED, EntityType.BOMBERMAN, bombermen.get(Location.BOTTOM_RIGHT), 0f, 1f, 0));
        }
        private List<WorldEvent> expectedInnerVerticalEvents() {
            final List<WorldEvent> result = new LinkedList<>();

            result.add(new WorldEvent(EventType.ENTITY_UPDATED, EntityType.BOMBERMAN, bombermen.get(Location.TOP_LEFT), 2.5f, 2.375f));
            result.add(new WorldEvent(EventType.ENTITY_UPDATED, EntityType.BOMBERMAN, bombermen.get(Location.BOTTOM_LEFT), 2.5f, 29.625f));

            result.add(new WorldEvent(EventType.ENTITY_UPDATED, EntityType.BOMBERMAN, bombermen.get(Location.TOP_RIGHT), 29.5f, 2.375f));
            result.add(new WorldEvent(EventType.ENTITY_UPDATED, EntityType.BOMBERMAN, bombermen.get(Location.BOTTOM_RIGHT), 29.5f, 29.625f));

            return result;
        }

        private void addInnerDiagonalEvents() {
            world.addWorldEvent(new WorldEvent(EventType.ENTITY_UPDATED, EntityType.BOMBERMAN, bombermen.get(Location.TOP_LEFT), -1f, -1f, 0));
            world.addWorldEvent(new WorldEvent(EventType.ENTITY_UPDATED, EntityType.BOMBERMAN, bombermen.get(Location.TOP_RIGHT), 1f, -1f, 0));
            world.addWorldEvent(new WorldEvent(EventType.ENTITY_UPDATED, EntityType.BOMBERMAN, bombermen.get(Location.BOTTOM_LEFT), -1f, 1f, 0));
            world.addWorldEvent(new WorldEvent(EventType.ENTITY_UPDATED, EntityType.BOMBERMAN, bombermen.get(Location.BOTTOM_RIGHT), 1f, 1f, 0));
        }
        private List<WorldEvent> expectedInnerDiagonalEvents() {
            final List<WorldEvent> result = new LinkedList<>();

            result.add(new WorldEvent(EventType.ENTITY_UPDATED, EntityType.BOMBERMAN, bombermen.get(Location.TOP_LEFT), 2.375f, 2.375f));
            result.add(new WorldEvent(EventType.ENTITY_UPDATED, EntityType.BOMBERMAN, bombermen.get(Location.BOTTOM_LEFT), 2.375f, 29.625f));
            result.add(new WorldEvent(EventType.ENTITY_UPDATED, EntityType.BOMBERMAN, bombermen.get(Location.TOP_RIGHT), 29.625f, 2.375f));
            result.add(new WorldEvent(EventType.ENTITY_UPDATED, EntityType.BOMBERMAN, bombermen.get(Location.BOTTOM_RIGHT), 29.625f, 29.625f));

            return result;
        }



    }

    private World world;
    private final EnumMap<Location, Integer> bombermen = new EnumMap<>(Location.class);

    private static final int BOMBERMEN_AMOUNT = 8;
    private enum Location { TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, TOP_LEFT_EDGE, TOP_RIGHT_EDGE, BOTTOM_LEFT_EDGE, BOTTOM_RIGHT_EDGE }
    private enum TestType {BORDER_HORIZONTAL, BORDER_VERTICAL, BORDER_DIAGONAL, INNER_HORIZONTAL, INNER_VERTICAL, INNER_DIAGONAL}
}