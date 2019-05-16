package no.elg.infiniteBootleg;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector3;
import com.kotcrab.vis.ui.VisUI;
import com.strongjoshua.console.Console;
import no.elg.infiniteBootleg.console.ConsoleHandler;
import no.elg.infiniteBootleg.world.World;
import no.elg.infiniteBootleg.world.generator.GaussianChunkGenerator;
import no.elg.infiniteBootleg.world.render.WorldRender;

import java.io.File;

import static no.elg.infiniteBootleg.ProgramArgs.executeArgs;

public class Main extends ApplicationAdapter {

    public static final String WORLD_FOLDER = "worlds" + File.separatorChar;
    public static final String TEXTURES_FOLDER = "textures" + File.separatorChar;
    public static final String TEXTURES_BLOCK_FILE = TEXTURES_FOLDER + "blocks.atlas";
    public static final String VERSION_FILE = "version";

    private SpriteBatch batch;
    private OrthographicCamera camera;
    private static InputMultiplexer inputMultiplexer;
    private Console console;
    private BitmapFont font;
    private static TextureAtlas textureAtlas;

    public static boolean RENDER_GRAPHIC = true;

    public static World world;

    public Main(String[] args) {
        executeArgs(args);
    }

    @Override
    public void create() {
        camera = new OrthographicCamera();
        camera.setToOrtho(true);

        batch = new SpriteBatch();
        inputMultiplexer = new InputMultiplexer();
        VisUI.load();
        console = new ConsoleHandler();
        Gdx.input.setInputProcessor(inputMultiplexer);

        batch.setProjectionMatrix(camera.combined);
        textureAtlas = new TextureAtlas(Gdx.files.internal(TEXTURES_BLOCK_FILE));

        world = new World(new GaussianChunkGenerator());
        font = new BitmapFont(true);


    }

    @Override
    public void render() {
        if (!Main.RENDER_GRAPHIC) {
            return;
        }
        Gdx.gl.glClearColor(0.2f, 0.3f, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();

        world.getRender().render();


        final Vector3 unproject = world.getRender().getCamera().unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));

        final int blockX = (int) (unproject.x / World.BLOCK_SIZE);
        final int blockY = (int) (unproject.y / World.BLOCK_SIZE);

        int[] vChunks = world.getRender().getChunksInView();
//        int getViewingChunks = (colEnd - colStart) + (rowEnd - vChunks[VERT_START]);
        int chunksInView = Math.abs(vChunks[WorldRender.HOR_END] - vChunks[WorldRender.HOR_START]) *
                           Math.abs(vChunks[WorldRender.VERT_END] - vChunks[WorldRender.VERT_START]);
        batch.begin();
        font.draw(batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 10, 10);
        font.draw(batch, "Delta time: " + Gdx.graphics.getDeltaTime(), 10, 25);
        font.draw(batch, "Pointing at block (" + blockX + ", " + blockY + ") in chunk " +
                         world.getChunkFromWorld(blockX, blockY).getLocation(), 10, 40);
        font.draw(batch, "Viewing " + chunksInView + " chunks", 10, 55);
        batch.end();

        console.draw();
    }

    @Override
    public void dispose() {
        batch.dispose();
        VisUI.dispose();
    }

    public static TextureAtlas getTextureAtlas() {
        return textureAtlas;
    }

    public static InputMultiplexer getInputMultiplexer() {
        return inputMultiplexer;
    }
}
