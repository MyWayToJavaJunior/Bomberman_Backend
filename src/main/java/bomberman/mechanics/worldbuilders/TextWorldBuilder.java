package bomberman.mechanics.worldbuilders;

import bomberman.mechanics.TileFactory;
import bomberman.mechanics.interfaces.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javatuples.Triplet;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

public class TextWorldBuilder implements IWorldBuilder {

    public static Map<String, IWorldBuilder> getAllTextBuilders() {
        final HashMap<String, IWorldBuilder> builders = new HashMap<>();
        final File[] worldBlueprints = new File("data/worlds").listFiles();

        if (worldBlueprints == null)
            LOGGER.fatal("Cannot access world blueprints folder or it is empty!");
        else
            for (final File blueprint : worldBlueprints)
                if (!blueprint.isDirectory())
                    try {
                        final String nameWithoutExtension = blueprint.getName().substring(0, blueprint.getName().lastIndexOf('.'));
                        builders.put(nameWithoutExtension, new TextWorldBuilder(blueprint));
                    } catch (Exception ex) {
                        LOGGER.error("Cannot build world from file\"" + blueprint.getAbsolutePath() + "\". It is corrupted.");
                    }

        return builders;
    }

    @SuppressWarnings("OverlyBroadThrowsClause")
    public TextWorldBuilder(File blueprint) throws Exception {
        BufferedReader strings = null;
        //noinspection OverlyBroadCatchBlock
        try {
             strings = new BufferedReader(new FileReader(blueprint));

            if (!strings.readLine().equals(CURRENT_VERSION))
                throw new Exception();
            name = strings.readLine();
            for (int i = 0; i < WORLD_HEIGHT; ++i)
                rawTiles.add(strings.readLine());
            if (rawTiles.size() != WORLD_HEIGHT)
                throw new Exception();
        }
        catch (Exception ex)
        {
            LOGGER.error("Cannot read\"" + blueprint.getAbsolutePath() + "\" due to some weird reason! Check server's rights.");
            throw ex;
        } finally {
            if (strings != null)
                strings.close();
        }
    }

    @Override
    public WorldData getWorldData(UniqueIDManager newSupplicant, EventStashable newEventQueue) {
        supplicant = newSupplicant;
        eventQueue = newEventQueue;
        generateWorldFromText();
        return new WorldData(tileArray, getBombermenSpawns(), name);
    }

    private float[][] getBombermenSpawns() {
        final float[][] spawnArray = new float[spawnList.size()][2];  // 2 for x and y coordinates
        int i = 0;

        for(float[] onePoint : spawnList)
        {
            spawnArray[i] = onePoint;
            i++;
        }
        return spawnArray;
    }

    private void generateWorldFromText() {
        tileArray = new ITile[WORLD_HEIGHT][WORLD_WIDTH];

        int y = 0;
        for (String row : rawTiles) {
            int x = 0;
            for (char tileChar : row.toCharArray())
            {
                tileArray[y][x] = mapSymbolToTile(tileChar, x, y);
                x++;
            }
            y++;
        }
    }

    @SuppressWarnings("MagicNumber")
    @Nullable
    private ITile mapSymbolToTile(char c, int x, int y){
        switch (c)
        {
            case '.':
                return null;
            case '#':
                return TileFactory.getInstance().getNewTile(EntityType.UNDESTRUCTIBLE_WALL, supplicant.getNextID());
            case 'd':
                return TileFactory.getInstance().getNewTile(EntityType.DESTRUCTIBLE_WALL, supplicant.getNextID());
            case 'E':
                return TileFactory.getInstance().getNewTile(EntityType.BONUS_DECBOMBFUSE, eventQueue, supplicant.getNextID());
            case 'P':
                return TileFactory.getInstance().getNewTile(EntityType.BONUS_DECBOMBSPAWN, eventQueue, supplicant.getNextID());
            case 'R':
                return TileFactory.getInstance().getNewTile(EntityType.BONUS_INCMAXRANGE, eventQueue, supplicant.getNextID());
            case 'S':
                spawnList.add(new float[]{x + 0.5f, y + 0.5f});
                return null;
            default:
                LOGGER.warn("Found undocumented symbol '" + c + "'. Treating him like an empty place.");
                return null;
        }
    }


    private UniqueIDManager supplicant;
    private EventStashable eventQueue;
    private ITile[][] tileArray;
    private final Queue<float[]> spawnList = new LinkedList<>();
    private String name = "REPORT AS A BUG";
    private final ArrayList<String> rawTiles = new ArrayList<>(32);

    private static final Logger LOGGER = LogManager.getLogger(TextWorldBuilder.class);
    private static final String CURRENT_VERSION = "v1.0";
    private static final int WORLD_HEIGHT = 32;
    private static final int WORLD_WIDTH = 32;

}
