package no.elg.infiniteBootleg;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.GL20;
import com.strongjoshua.console.LogLevel;
import no.elg.infiniteBootleg.console.ConsoleLogger;
import no.elg.infiniteBootleg.world.World;
import no.elg.infiniteBootleg.world.generator.EmptyChunkGenerator;
import org.junit.BeforeClass;
import org.mockito.Mockito;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;

public class TestGraphic {

    private static ConsoleLogger logger = new ConsoleTestLogger();

    @BeforeClass
    public static void init() {
        Main.renderGraphic = false;
        Main.loadWorldFromDisk = false; //ensure no loading from disk (to be able to duplicate tests)

        Gdx.gl20 = Mockito.mock(GL20.class);
        Gdx.gl = Gdx.gl20;

        //run postRunnable at once
        Gdx.app = Mockito.mock(Application.class);
        doAnswer(invocation -> {
            invocation.getArgument(0, Runnable.class).run();
            return null;
        }).when(Gdx.app).postRunnable(any(Runnable.class));


        Gdx.graphics = Mockito.mock(Graphics.class);
        Mockito.when(Gdx.graphics.getWidth()).thenReturn(1);
        Mockito.when(Gdx.graphics.getHeight()).thenReturn(1);

        Main.inst = Mockito.mock(Main.class);
        Mockito.when(Main.inst.getConsoleLogger()).thenReturn(logger);
        World world = new World(new EmptyChunkGenerator());
        Mockito.when(Main.inst.getWorld()).thenReturn(world);
    }

    private static class ConsoleTestLogger implements ConsoleLogger {

        @Override
        public void log(LogLevel level, String msg) {
            if (level == LogLevel.ERROR) {
                System.err.println(msg);
            }
            else {
                System.out.println(msg);
            }
        }
    }
}
