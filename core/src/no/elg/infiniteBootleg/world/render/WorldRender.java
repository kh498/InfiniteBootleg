package no.elg.infiniteBootleg.world.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Disposable;
import no.elg.infiniteBootleg.Main;
import no.elg.infiniteBootleg.world.Chunk;
import no.elg.infiniteBootleg.world.Location;
import no.elg.infiniteBootleg.world.World;
import org.jetbrains.annotations.NotNull;

import static no.elg.infiniteBootleg.world.World.BLOCK_SIZE;

/**
 * @author Elg
 */
public class WorldRender implements Renderer, Disposable {


    public static final int VERT_START = 0;
    public static final int VERT_END = 1;
    public static final int HOR_START = 2;
    public static final int HOR_END = 3;

    public final static int CHUNK_TEXT_WIDTH = Chunk.CHUNK_WIDTH * BLOCK_SIZE;
    public final static int CHUNK_TEXT_HEIGHT = Chunk.CHUNK_HEIGHT * BLOCK_SIZE;

    private final World world;
    private final SpriteBatch batch;

    private final OrthographicCamera camera;
    private final Rectangle viewBounds;
    private final int[] chunksInView;
    private final ChunkRenderer chunkRenderer;

    public WorldRender(@NotNull World world) {
        if (!Main.RENDER_GRAPHIC) {
            throw new IllegalStateException("Cannot render world as graphics are not enabled");
        }

        this.world = world;

        batch = new SpriteBatch();
        chunkRenderer = new ChunkRenderer(this, batch);

        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.viewBounds = new Rectangle();
        chunksInView = new int[4];
        update();
    }


    @Override
    public void update() {
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        float width = camera.viewportWidth * camera.zoom;
        float height = camera.viewportHeight * camera.zoom;
        float w = width * Math.abs(camera.up.y) + height * Math.abs(camera.up.x);
        float h = height * Math.abs(camera.up.y) + width * Math.abs(camera.up.x);
        viewBounds.set(camera.position.x - w / 2, camera.position.y - h / 2, w, h);

        chunksInView[HOR_START] = (int) Math.floor(viewBounds.x / CHUNK_TEXT_WIDTH);
        chunksInView[HOR_END] = (int) Math.floor((viewBounds.x + viewBounds.width + CHUNK_TEXT_WIDTH) / CHUNK_TEXT_WIDTH);

        chunksInView[VERT_START] = (int) Math.floor(viewBounds.y / CHUNK_TEXT_HEIGHT);
        chunksInView[VERT_END] = (int) Math.floor((viewBounds.y + viewBounds.height + CHUNK_TEXT_HEIGHT) / CHUNK_TEXT_HEIGHT);
    }

    @Override
    public void render() {
        if (Gdx.graphics.getFrameId() % 5 == 0) {
            chunkRenderer.render();
        }
        final int colEnd = chunksInView[HOR_END];
        final int colStart = chunksInView[HOR_START];
        final int rowEnd = chunksInView[VERT_END];
        final int rowStart = chunksInView[VERT_START];

        final int debug = 0;

        batch.begin();
        for (int y = rowStart + debug; y < rowEnd - debug; y++) {
            for (int x = colStart + debug; x < colEnd - debug; x++) {
                Chunk chunk = world.getChunk(x, y);
                if (chunk.isAllAir()) {
                    continue;
                }
                if (chunk.getTexture() == null) {
                    chunkRenderer.queueRendering(chunk);
                    continue;
                }
                float dx = chunk.getLocation().x * CHUNK_TEXT_WIDTH;
                float dy = chunk.getLocation().y * CHUNK_TEXT_HEIGHT;
                batch.draw(chunk.getTexture(), dx, dy, CHUNK_TEXT_WIDTH, CHUNK_TEXT_HEIGHT);

//                for (Block block : chunk) {
//                    if (block == null || block.getMaterial() == Material.AIR) { continue; }
//                    Location blkLoc = block.getLocation();
//                    float x = (blkLoc.x + (chunk.getLocation().x * Chunk.CHUNK_WIDTH)) * World.BLOCK_SIZE;
//                    float y = (blkLoc.y + chunk.getLocation().y * Chunk.CHUNK_HEIGHT) * World.BLOCK_SIZE;
//                    //noinspection ConstantConditions
//                    batch.draw(block.getTexture(), x, y, World.BLOCK_SIZE, World.BLOCK_SIZE);
//                }
            }
        }
        batch.end();
    }

    public boolean inInView(@NotNull Chunk chunk) {
        Location pos = chunk.getLocation();
        return pos.x >= chunksInView[HOR_START] && pos.x < chunksInView[HOR_END] && pos.y >= chunksInView[VERT_START] &&
               pos.y < chunksInView[VERT_END];
    }

    public int[] getChunksInView() {
        return chunksInView;
    }

    public Rectangle getViewBounds() {
        return viewBounds;
    }

    public OrthographicCamera getCamera() {
        return camera;
    }

    public World getWorld() {
        return world;
    }

    public ChunkRenderer getChunkRenderer() {
        return chunkRenderer;
    }

    @Override
    public void dispose() {
        batch.dispose();
    }
}
