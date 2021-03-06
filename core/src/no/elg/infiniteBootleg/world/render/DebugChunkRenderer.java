package no.elg.infiniteBootleg.world.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import no.elg.infiniteBootleg.Main;
import no.elg.infiniteBootleg.Renderer;
import no.elg.infiniteBootleg.screen.ScreenRenderer;
import no.elg.infiniteBootleg.world.Block;
import no.elg.infiniteBootleg.world.Chunk;

public class DebugChunkRenderer implements Renderer {

    public static final Color WITHIN_CAMERA_COLOR = Color.TEAL;
    public static final Color OUTSIDE_CAMERA_COLOR = Color.FIREBRICK;

    private final WorldRender worldRender;
    private final ShapeRenderer lr;
    private final OrthographicCamera camera;

    public DebugChunkRenderer(WorldRender worldRender) {
        this.worldRender = worldRender;
        camera = worldRender.getCamera();
        lr = new ShapeRenderer(1000);
    }

    @Override
    public void render() {

        WorldRender.ChunkViewed chunksInView = worldRender.getChunksInView();

        int yEnd = chunksInView.vertical_end;
        int xEnd = chunksInView.horizontal_end;

        lr.begin(ShapeRenderer.ShapeType.Line);
        lr.setProjectionMatrix(camera.combined);

        float offset = Chunk.CHUNK_SIZE * Block.BLOCK_SIZE;
        for (float y = chunksInView.vertical_start; y < yEnd; y++) {
            for (float x = chunksInView.horizontal_start; x < xEnd; x++) {
                Color c;
                if (y == chunksInView.vertical_end - 1 || x == chunksInView.horizontal_start ||
                    x == chunksInView.horizontal_end - 1) {
                    c = OUTSIDE_CAMERA_COLOR;
                }
                else {
                    c = WITHIN_CAMERA_COLOR;
                }
                lr.setColor(c);
                lr.rect(x * offset + 0.5f, y * offset + 0.5f, offset - 1, offset - 1);
            }
        }

        lr.end();
        ScreenRenderer sr = Main.inst().getScreenRenderer();
        sr.begin();
        sr.drawBottom("Debug Chunk outline legend", 5);
        sr.getFont().setColor(WITHIN_CAMERA_COLOR);
        sr.drawBottom("  Chunks within the camera boarders", 3);
        sr.getFont().setColor(OUTSIDE_CAMERA_COLOR);
        sr.drawBottom("  Chunks outside camera boarders, only physics active", 1);
        sr.end();
        sr.resetFontColor();
    }
}
