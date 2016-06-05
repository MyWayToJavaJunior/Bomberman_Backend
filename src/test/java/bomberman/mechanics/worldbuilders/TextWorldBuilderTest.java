package bomberman.mechanics.worldbuilders;

import bomberman.mechanics.interfaces.ITile;
import bomberman.mechanics.tiles.DestructibleWall;
import bomberman.mechanics.tiles.UndestructibleWall;
import constants.Constants;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class TextWorldBuilderTest {

    @BeforeClass
    public static void setup() throws Exception {
        testWorldData = new TextWorldBuilder(new File("data/unit-test-world-do-not-alter.txt")).getWorldData(Constants.GameMechanicsMocks.getMockedWorld());
    }

    @Test
    public void testGetITileArrayAndSpawnsArray() throws Exception {
        final ITile[][] actualTileArray = testWorldData.getTileArray();
        final ITile[][] expectedTileArray = new ITile[32][32];

        assertEquals(expectedTileArray.length, actualTileArray.length);

        for (int j = 0; j < expectedTileArray.length; ++j)
            for (int i = 0; i < expectedTileArray[j].length; ++i)
                expectedTileArray[j][i] = null;
        expectedTileArray[0][0] = new UndestructibleWall(0);
        expectedTileArray[0][1] = new UndestructibleWall(1);
        expectedTileArray[1][0] = new UndestructibleWall(2);
        expectedTileArray[2][2] = new DestructibleWall(3);

        for (int j = 0; j < expectedTileArray.length; ++j)
            for (int i = 0; i < expectedTileArray.length; ++i)
                assertEquals(expectedTileArray[j][i], actualTileArray[j][i]);
    }

    @Test
    public void testGetBombermenSpawns() throws Exception {
        final float[][] actualSpawns = testWorldData.getSpawnList();
        final float[][] expectedSpawns = new float[][]{{31.5f, 31.5f}};

        assertEquals(expectedSpawns.length, actualSpawns.length);
        for (int i = 0; i < expectedSpawns.length; ++i) {
            assertEquals(expectedSpawns[i][0], actualSpawns[i][0], Constants.SOME_ERROR_DELTA);
            assertEquals(expectedSpawns[i][1], actualSpawns[i][1], Constants.SOME_ERROR_DELTA);
        }
    }

    @Test
    public void testGetName() throws Exception {
        final String worldName = testWorldData.getName();
        assertEquals("Unit test", worldName);
    }

    private static WorldData testWorldData;

}