package no.elg.infiniteBootleg.world.box2d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Disposable;
import no.elg.infiniteBootleg.Main;
import no.elg.infiniteBootleg.util.CoordUtil;
import no.elg.infiniteBootleg.util.Tuple;
import no.elg.infiniteBootleg.world.*;
import no.elg.infiniteBootleg.world.blocks.StaticLightBlock;
import no.elg.infiniteBootleg.world.render.WorldRender;
import org.jetbrains.annotations.NotNull;

import static no.elg.infiniteBootleg.world.Chunk.CHUNK_SIZE;

/**
 * @author Elg
 */
public class ChunkBody implements Disposable {

    public static final long INITIAL_UNSURE_FIXTURE_RELOAD_DELAY = 10L;
    public static final long UNSURE_FIXTURE_RELOAD_DELAY = 100L;

    private final Chunk chunk;
    private Body box2dBody;
    //make there is only one delayed check for this chunk
    private boolean unsureFixture;

    private final static Tuple<Direction, byte[]>[] EDGE_DEF;

    static {
        //represent the direction to look and if no solid block there how to create a fixture at that location (ie
        // two relative vectors)
        // the value of the tuple is as follows dxStart, dyStart, dxEnd, dyEnd
        // this can be visually represented with a cube:
        //
        // (0,1)---(1,1)
        //   |       |
        //   |       |
        //   |       |
        // (0,0)---(1,0)
        //
        // where 'd' stands for delta
        // x/y is if this is the x or component of the coordinate
        // end/start is if this is the start or end vector
        //noinspection unchecked
        EDGE_DEF = new Tuple[4];
        EDGE_DEF[0] = new Tuple<>(Direction.NORTH, new byte[] {0, 1, 1, 1});
        EDGE_DEF[1] = new Tuple<>(Direction.EAST, new byte[] {1, 0, 1, 1});
        EDGE_DEF[2] = new Tuple<>(Direction.SOUTH, new byte[] {0, 0, 1, 0});
        EDGE_DEF[3] = new Tuple<>(Direction.WEST, new byte[] {0, 0, 0, 1});
    }

    public ChunkBody(@NotNull Chunk chunk) {
        this.chunk = chunk;
        unsureFixture = false;
    }

    /**
     * Update the box2d fixture of this block
     *
     * @param recalculateNeighbors
     *     If the neighbors also should be updated
     * @param lightsOnly
     */
    public synchronized void update(boolean recalculateNeighbors, boolean lightsOnly) {
        synchronized (WorldRender.BOX2D_LOCK) {
            if (lightsOnly) {
                updateLights();
                return;
            }
            if (chunk.isAllAir()) {
                chunk.getWorld().getWorldBody().destroyBody(box2dBody);
                box2dBody = null;

                return;
            }

            //recalculate the shape of the chunk (box2d)

            BodyDef bodyDef = new BodyDef();
            bodyDef.position.set(chunk.getChunkX() * CHUNK_SIZE, chunk.getChunkY() * CHUNK_SIZE);
            bodyDef.fixedRotation = true;
            bodyDef.awake = false;
            bodyDef.type = BodyDef.BodyType.StaticBody;

            Body tmpBody = chunk.getWorld().getWorldBody().createBody(bodyDef);

            EdgeShape edgeShape = new EdgeShape();

            for (byte localX = 0; localX < CHUNK_SIZE; localX++) {
                for (byte localY = 0; localY < CHUNK_SIZE; localY++) {
                    Block b = chunk.getRawBlock(localX, localY);

                    if (b == null || !b.getMaterial().isSolid()) {
                        continue;
                    }

                    int worldX = CoordUtil.chunkToWorld(chunk.getChunkX(), localX);
                    int worldY = CoordUtil.chunkToWorld(chunk.getChunkY(), localY);

                    for (Tuple<Direction, byte[]> tuple : EDGE_DEF) {
                        Direction dir = tuple.key;

                        //FIXME only check the chunk if the local coordinates are outside this chunk
                        if (!CoordUtil.isInsideChunk(localX + dir.dx, localY + dir.dy) && //
                            !chunk.getWorld().isChunkLoaded(CoordUtil.worldToChunk(worldX + dir.dx),
                                                            CoordUtil.worldToChunk(worldY + dir.dy))) {
                            continue;
                        }

                        Block rel;
                        if (CoordUtil.isInsideChunk(localX + dir.dx, localY + dir.dy)) {
                            rel = chunk.getRawBlock(localX + dir.dx, localY + dir.dy);
                        }
                        else {
                            Chunk relChunk = chunk.getWorld().getChunkFromWorld(worldX + dir.dx, worldY + dir.dy);
                            int relOffsetX = CoordUtil.chunkOffset(worldX + dir.dx);
                            int relOffsetY = CoordUtil.chunkOffset(worldY + dir.dy);
                            rel = relChunk.getBlocks()[relOffsetX][relOffsetY];
                        }
                        if (rel == null || !rel.getMaterial().isSolid() ||
                            (dir == Direction.NORTH && localY == CHUNK_SIZE - 1)//always render top of chunk
                            || (dir == Direction.EAST && localX == CHUNK_SIZE - 1) //and the sides
                            || (dir == Direction.WEST && localX == 0)) {
                            byte[] ds = tuple.value;
                            edgeShape.set(localX + ds[0], localY + ds[1], localX + ds[2], localY + ds[3]);

                            synchronized (WorldRender.BOX2D_LOCK) {
                                Fixture fix = tmpBody.createFixture(edgeShape, 0);
                                if (!b.getMaterial().blocksLight()) {
                                    fix.setFilterData(World.SOLID_TRANSPARENT_FILTER);
                                }
                            }
                        }
                    }
                }
            }
            edgeShape.dispose();


            chunk.getWorld().getWorldBody().destroyBody(box2dBody);
            box2dBody = tmpBody;


            Gdx.app.postRunnable(() -> chunk.getWorld().getRender().update());


            boolean potentiallyDirty = false;

            //TODO Try to optimize this (ie select what directions to recalculate)
            for (Direction direction : Direction.values()) {
                Location relChunk = Location.relative(chunk.getChunkX(), chunk.getChunkY(), direction);
                if (chunk.getWorld().isChunkLoaded(relChunk)) {
                    if (recalculateNeighbors) {
                        Main.inst().getScheduler().executeAsync(() -> {
                            chunk.getWorld().getChunk(relChunk).getChunkBody().update(false, !direction.isCardinal());
                        });
                    }
                }
                else {
                    potentiallyDirty = true;
                }
            }

            if (potentiallyDirty) {
                scheduleFixtureReload(true);
            }
            updateLights();
        }
    }

    private synchronized void scheduleFixtureReload(boolean initial) {
        if (unsureFixture) {
            return;
        }
        unsureFixture = true;
        Main.inst().getScheduler().scheduleAsync(() -> {
            synchronized (this) {
                unsureFixture = false;
                if (chunk.isNeighborsLoaded()) {
                    update(false, false);
                }
                else {
                    scheduleFixtureReload(false);
                }
            }
        }, initial ? INITIAL_UNSURE_FIXTURE_RELOAD_DELAY : UNSURE_FIXTURE_RELOAD_DELAY);
    }

    private void updateLights() {
        synchronized (chunk) {
            for (int localX = 0; localX < CHUNK_SIZE; localX++) {
                for (int localY = 0; localY < CHUNK_SIZE; localY++) {
                    Block block = chunk.getRawBlock(localX, localY);
                    if (block instanceof StaticLightBlock) {
                        ((StaticLightBlock) block).updateLight();
                    }
                }
            }
        }

    }

    @Override
    public synchronized void dispose() {
        chunk.getWorld().getWorldBody().destroyBody(box2dBody);
        box2dBody = null;
    }
}
