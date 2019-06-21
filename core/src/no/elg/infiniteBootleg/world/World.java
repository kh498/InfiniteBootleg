package no.elg.infiniteBootleg.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Disposable;
import com.strongjoshua.console.LogLevel;
import no.elg.infiniteBootleg.Main;
import no.elg.infiniteBootleg.input.WorldInputHandler;
import no.elg.infiniteBootleg.util.CoordUtil;
import no.elg.infiniteBootleg.util.ZipUtils;
import no.elg.infiniteBootleg.world.blocks.UpdatableBlock;
import no.elg.infiniteBootleg.world.generator.ChunkGenerator;
import no.elg.infiniteBootleg.world.loader.ChunkLoader;
import no.elg.infiniteBootleg.world.render.HeadlessWorldRenderer;
import no.elg.infiniteBootleg.world.render.Updatable;
import no.elg.infiniteBootleg.world.render.WorldRender;
import no.elg.infiniteBootleg.world.subgrid.Entity;
import no.elg.infiniteBootleg.world.subgrid.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static no.elg.infiniteBootleg.world.Block.BLOCK_SIZE;

/**
 * @author Elg
 */
public class World implements Disposable, Updatable {

    private final long seed;
    private final Map<Location, Chunk> chunks;
    private final WorldTicker ticker;
    private final ChunkLoader chunkLoader;
    private FileHandle worldFile;

    //only exists when graphics exits
    private WorldInputHandler input;
    private WorldRender render;

    private String name = "World";
    private final UUID uuid;
    private Set<Entity> entities;


    /**
     * Generate a world with a random seed
     *
     * @param generator
     */
    public World(@NotNull ChunkGenerator generator) {
        this(generator, new Random().nextLong());
    }

    public World(@NotNull ChunkGenerator generator, long seed) {
        this.seed = seed;
        Random random = new Random(seed);
        chunks = new ConcurrentHashMap<>();
        entities = ConcurrentHashMap.newKeySet();
        byte[] UUIDSeed = new byte[128];
        random.nextBytes(UUIDSeed);
        uuid = UUID.nameUUIDFromBytes(UUIDSeed);

        if (Main.renderGraphic) {
            render = new WorldRender(this);
            input = new WorldInputHandler(render);

            Player p = new Player(this);
            entities.add(p);
        }
        else {
            render = new HeadlessWorldRenderer(this);
        }

        chunkLoader = new ChunkLoader(this, generator);
        ticker = new WorldTicker(this);
        load();
    }

    @NotNull
    public Chunk getChunk(int chunkX, int chunkY) {
        return getChunk(new Location(chunkX, chunkY));
    }

    @NotNull
    public Chunk getChunk(@NotNull Location chunkLoc) {
        Chunk chunk = chunks.get(chunkLoc);
        if (chunk == null) {
            chunk = chunkLoader.load(chunkLoc);
            chunks.put(chunkLoc, chunk);
        }
        return chunk;
    }

    @NotNull
    public Chunk getChunkFromWorld(int worldX, int worldY) {
        int chunkX = CoordUtil.worldToChunk(worldX);
        int chunkY = CoordUtil.worldToChunk(worldY);
        return getChunk(chunkX, chunkY);
    }

    /**
     * @param worldX
     *     The x coordinate from world view
     * @param worldY
     *     The y coordinate from world view
     *
     * @return The block at the given x and y
     *
     * @see Chunk#getBlock(int, int)
     */
    public Block getBlock(int worldX, int worldY) {

        int chunkX = CoordUtil.worldToChunk(worldX);
        int chunkY = CoordUtil.worldToChunk(worldY);

        int localX = worldX - chunkX * Chunk.CHUNK_SIZE;
        int localY = worldY - chunkY * Chunk.CHUNK_SIZE;

        return getChunk(chunkX, chunkY).getBlock(localX, localY);
    }

    /**
     * Set a block at a given location and update the textures
     *
     * @param worldLoc
     *     The location in world coordinates
     * @param material
     *     The new material to at given location
     *
     * @see Chunk#setBlock(int, int, Material, boolean)
     */
    public Chunk setBlock(@NotNull Location worldLoc, @Nullable Material material) {
        return setBlock(worldLoc, material, true);
    }

    /**
     * Set a block at a given location
     *
     * @param worldLoc
     *     The location in world coordinates
     * @param material
     *     The new material to at given location
     * @param update
     *     If the texture of the corresponding chunk should be updated
     *
     * @see Chunk#setBlock(int, int, Material, boolean)
     */
    public Chunk setBlock(@NotNull Location worldLoc, @Nullable Material material, boolean update) {
        return setBlock(worldLoc.x, worldLoc.y, material, update);
    }

    /**
     * Set a block at a given location and update the textures
     *
     * @param worldX
     *     The x coordinate from world view
     * @param worldY
     *     The y coordinate from world view
     * @param material
     *     The new material to at given location
     *
     * @see Chunk#setBlock(int, int, Material, boolean)
     */
    public Chunk setBlock(int worldX, int worldY, @Nullable Material material) {
        return setBlock(worldX, worldY, material, true);
    }

    /**
     * Set a block at a given location
     *
     * @param worldX
     *     The x coordinate from world view
     * @param worldY
     *     The y coordinate from world view
     * @param material
     *     The new material to at given location
     * @param update
     *     If the texture of the corresponding chunk should be updated
     *
     * @see Chunk#setBlock(int, int, Material, boolean)
     */
    public Chunk setBlock(int worldX, int worldY, @Nullable Material material, boolean update) {
        int chunkX = CoordUtil.worldToChunk(worldX);
        int chunkY = CoordUtil.worldToChunk(worldY);

        int localX = worldX - chunkX * Chunk.CHUNK_SIZE;
        int localY = worldY - chunkY * Chunk.CHUNK_SIZE;

        Chunk chunk = getChunk(chunkX, chunkY);
        chunk.setBlock(localX, localY, material, update);
        return chunk;
    }

    /**
     * Check if a given location in the world is {@link Material#AIR} (or internally, doesn't exists) this is faster than a
     * standard {@code getBlock(worldX, worldY).getMaterial == Material.AIR} as the {@link #getBlock(int, int)} method migt create
     * and store a new air block at the given location
     *
     * @param worldX
     *     The x coordinate from world view
     * @param worldY
     *     The y coordinate from world view
     *
     * @return If the block at the given location is air.
     */
    public boolean isAir(int worldX, int worldY) {
        int chunkX = CoordUtil.worldToChunk(worldX);
        int chunkY = CoordUtil.worldToChunk(worldY);

        int localX = worldX - chunkX * Chunk.CHUNK_SIZE;
        int localY = worldY - chunkY * Chunk.CHUNK_SIZE;

        Block b = getChunk(chunkX, chunkY).getBlocks()[localX][localY];
        return b == null || b.getMaterial() == Material.AIR;
    }

    /**
     * Set all blocks around a given block to be updated
     *
     * @param worldLoc
     *     The coordinates to updates around (but not included)
     */
    public void updateAround(@NotNull Location worldLoc) {
        updateAround(worldLoc.x, worldLoc.y);
    }

    /**
     * Set all blocks around a given block to be updated
     *
     * @param worldX
     *     The x coordinate from world view
     * @param worldY
     *     The y coordinate from world view
     */
    public void updateAround(int worldX, int worldY) {
        Block center = getBlock(worldX, worldY);
        for (Direction dir : Direction.values()) {
            Block rel = center.getRelative(dir);
            if (rel instanceof UpdatableBlock) {
                ((UpdatableBlock) rel).setUpdate(true);
            }
        }
    }

    /**
     * @param chunkLoc
     *     Chunk location in chunk coordinates
     *
     * @return If the given chunk is loaded in memory
     */
    public boolean isLoadedAt(@NotNull Location chunkLoc) {
        return chunks.containsKey(chunkLoc);
    }

    /**
     * @param chunk
     *     The chunk to unload
     *
     * @return If the chunk was unloaded
     */
    public boolean unload(@Nullable Chunk chunk) {
        if (chunk == null || !chunk.isLoaded() || !isLoadedAt(chunk.getLocation())) {
            return false;
        }
        chunk.dispose();
        chunkLoader.save(chunk);
        return chunk.unload();
    }

    /**
     * @param worldLoc
     *     The world location of this chunk
     *
     * @return The chunk at the given world location
     */
    @NotNull
    public Chunk getChunkFromWorld(@NotNull Location worldLoc) {
        return getChunk(CoordUtil.worldToChunk(worldLoc));
    }

    /**
     * @return The random seed of this world
     */
    public long getSeed() {
        return seed;
    }

    /**
     * @return The name of the world
     */
    public String getName() {
        return name;
    }

    /**
     * @return Unique identification of this world
     */
    public UUID getUuid() {
        return uuid;
    }


    public void setName(String name) {
        this.name = name;
    }

    @NotNull
    public WorldRender getRender() {
        return render;
    }

    @NotNull
    public WorldInputHandler getInput() {
        return input;
    }

    @NotNull
    public WorldTicker getWorldTicker() {
        return ticker;
    }

    /**
     * @return The current world tick
     */
    public long getTick() {
        return ticker.getTickId();
    }

    @Override
    public String toString() {
        return "World{" + "name='" + name + '\'' + ", uuid=" + uuid + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        World world = (World) o;
        return Objects.equals(uuid, world.uuid);
    }

    @Override
    public int hashCode() {
        return uuid != null ? uuid.hashCode() : 0;
    }

    @Override
    public void dispose() {
        render.dispose();
        input.dispose();
        ticker.stop();
    }

    /**
     * @return The current folder of the world or {@code null} if no disk should be used
     */
    @Nullable
    public FileHandle worldFolder() {
        if (Main.renderGraphic) {
            if (worldFile == null) {
                worldFile = Gdx.files.external(Main.WORLD_FOLDER + uuid);
            }
            return worldFile;
        }
        else {
            return null;
        }
    }

    public void save() {
        FileHandle worldFolder = worldFolder();
        if (worldFolder == null) { return; }
        for (Chunk chunk : chunks.values()) {
            //FIXME only save chunks that has been changed! (doesn't do it properly now)
            chunkLoader.save(chunk);
        }
        FileHandle worldZip = worldFolder.parent().child(uuid + ".zip");
        try {
            ZipUtils.zip(worldFolder, worldZip);
            Main.inst().getConsoleLogger().log("World saved!");
        } catch (IOException e) {
            Main.inst().getConsoleLogger().log(LogLevel.ERROR, "Failed to save world due to a " + e.getClass().getSimpleName());
            e.printStackTrace();
            return;
        }

        worldFolder.deleteDirectory();
    }

    public void load() {
        FileHandle worldFolder = worldFolder();
        if (worldFolder == null) { return; }
        FileHandle worldZip = worldFolder.parent().child(uuid + ".zip");
        Main.inst.getConsoleLogger().log("Loading/saving world from '" + worldZip.file().getAbsolutePath() + '\'');
        if (!worldZip.exists()) {
            System.out.println("No world save found");
            return;
        }

        worldFolder.deleteDirectory();
        ZipUtils.unzip(worldFolder, worldZip);
    }

    @Override
    public void update() {
        long tick = getWorldTicker().getTickId();
        for (Iterator<Chunk> iterator = chunks.values().iterator(); iterator.hasNext(); ) {
            Chunk chunk = iterator.next();

            //clean up dead chunks
            if (!chunk.isLoaded()) {
                iterator.remove();
                continue;
            }

            //Unload chunks not seen for 5 seconds
            if (tick - chunk.getLastViewedTick() > Chunk.CHUNK_UNLOAD_TIME) {
                unload(chunk);
                iterator.remove();
                continue;
            }
            chunk.update();
        }
        for (Entity entity : entities) {
            entity.update();
        }
    }

    public Set<Entity> getEntities() {
        return entities;
    }

    /**
     * @param worldX
     *     The x component of the world coordinate
     * @param worldY
     *     The y component of the world coordinate
     * @param width
     *     The absolute width (ie this will be divided by {@link Block#BLOCK_SIZE})
     * @param height
     *     The absolute height (ie this will be divided by {@link Block#BLOCK_SIZE})
     *
     * @return If anything at the given coordinate with the given width will collide with any blocks in this world
     */
    public boolean willCollide(float worldX, float worldY, float width, float height) {
        int dx = (int) Math.floor(worldX), maxX = (int) Math.floor(worldX + width / BLOCK_SIZE);
        int dy0 = (int) Math.floor(worldY), maxY = (int) Math.floor(worldY + height / BLOCK_SIZE);
        for (; dx <= maxX; dx++) {
            for (int dy = dy0; dy <= maxY; dy++) {
                if (getBlock(dx, dy).getMaterial().isSolid()) {
                    return true;
                }
            }
        }
        return false;
    }
}
