package no.elg.infiniteBootleg.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;
import no.elg.infiniteBootleg.Main;
import no.elg.infiniteBootleg.util.Resizable;

import static no.elg.infiniteBootleg.Main.SCALE;

public class ScreenRenderer implements Disposable, Resizable {

    public static final int FONT_SIZE = 20;

    private final int spacing;
    private final BitmapFont font;
    private final SpriteBatch batch;

    public ScreenRenderer() {

        final FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
            Gdx.files.internal(Main.FONTS_FOLDER + "UbuntuMono-R.ttf"));
        final FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = FONT_SIZE * SCALE;

        parameter.minFilter = Texture.TextureFilter.Linear;
        font = generator.generateFont(parameter);

        spacing = (FONT_SIZE * SCALE) / 2;

        batch = new SpriteBatch();
        batch.setProjectionMatrix(new Matrix4().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
    }

    public void drawTop(String text, int line) {
        font.draw(batch, text, spacing, Gdx.graphics.getHeight() - spacing * line);
    }

    public void drawBottom(String text, int line) {
        font.draw(batch, text, spacing, spacing * (line + 1));
    }

    public void begin() {
        batch.begin();
    }

    public void end() {
        batch.end();
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    public BitmapFont getFont() {
        return font;
    }

    public void resetFontColor() {
        font.setColor(Color.WHITE);
    }

    public int getSpacing() {
        return spacing;
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
    }

    @Override
    public void resize(int width, int height) {
        batch.setProjectionMatrix(new Matrix4().setToOrtho2D(0, 0, width, height));
    }
}
