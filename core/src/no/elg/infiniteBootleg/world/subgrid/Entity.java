package no.elg.infiniteBootleg.world.subgrid;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectSet;
import no.elg.infiniteBootleg.Main;
import no.elg.infiniteBootleg.util.Util;
import no.elg.infiniteBootleg.world.Block;
import no.elg.infiniteBootleg.world.Material;
import no.elg.infiniteBootleg.world.World;
import no.elg.infiniteBootleg.world.render.Updatable;
import no.elg.infiniteBootleg.world.render.WorldRender;
import no.elg.infiniteBootleg.world.subgrid.box2d.ContactHandler;
import no.elg.infiniteBootleg.world.subgrid.box2d.ContactType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * An entity that can move between the main world grid.
 * <p>
 * The position of each entity is recorded in world coordinates and is centered in the middle of the entity.
 */
public abstract class Entity implements Updatable, Disposable, ContactHandler {

    private static final float GROUND_CHECK_OFFSET = 0.25f;

    private final World world;
    private Body body;
    private boolean flying; //ignore world gravity
    private UUID uuid;
    private Vector2 posCache;
    private boolean onGround;

    public Entity(@NotNull World world, float worldX, float worldY) {
        uuid = UUID.randomUUID();
        this.world = world;
        flying = false;
        world.addEntity(this);
        posCache = new Vector2(worldX, worldY);

        if (!validate()) {
            Main.inst().getConsoleLogger()
                .logf("Did not spawn %s at (% 8.2f,% 8.2f) as the spawn is invalid", simpleName(), worldX, worldY);
            world.removeEntity(this);
            return;
        }

        if (Main.renderGraphic) {
            BodyDef def = createBodyDef(worldX + getHalfBox2dWidth(), worldY + getHalfBox2dHeight());
            synchronized (WorldRender.BOX2D_LOCK) {
                body = getWorld().getRender().getBox2dWorld().createBody(def);
                createFixture(body);
            }
        }

        update();
    }

    @NotNull
    protected BodyDef createBodyDef(float worldX, float worldY) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(worldX, worldY);
        bodyDef.awake = true;
        bodyDef.fixedRotation = true;
        return bodyDef;
    }

    protected void createFixture(@NotNull Body body) {
        PolygonShape box = new PolygonShape();
        box.setAsBox(getHalfBox2dWidth(), getHalfBox2dHeight());
        Fixture fix = body.createFixture(box, 1.0f);
        fix.setFilterData(World.ENTITY_FILTER);
        box.dispose();
    }

    /**
     * @return If the given location is valid
     */
    protected boolean validate() {
        //noinspection LibGDXUnsafeIterator
        for (Block block : touchingBlocks()) {
            if (block.getMaterial() != Material.AIR) {
                return false;
            }
        }
        return touchingEntities().isEmpty();
    }

    public Array<Block> touchingBlocks() {
        Array<Block> blocks = new Array<>(Block.class);
        int x = MathUtils.floor(posCache.x - getHalfBox2dWidth());
        float maxX = posCache.x + getHalfBox2dWidth();
        for (; x < maxX; x++) {
            int y = MathUtils.floor(posCache.y - getHalfBox2dHeight());
            float maxY = posCache.y + getHalfBox2dHeight();
            for (; y < maxY; y++) {
                blocks.add(world.getBlock(x, y));
            }
        }
        return blocks;
    }

    public ObjectSet<Entity> touchingEntities() {
        ObjectSet<Entity> entities = new ObjectSet<>();

        for (Entity entity : world.getEntities()) {
            if (entity == this) { continue; }
            Vector2 pos = entity.getPosition();
            boolean bx =
                Util.isBetween(pos.x - entity.getHalfBox2dWidth(), posCache.x, pos.x + entity.getHalfBox2dWidth());
            boolean by =
                Util.isBetween(pos.y - entity.getHalfBox2dHeight(), posCache.y, pos.y + entity.getHalfBox2dHeight());
            if (bx && by) {
                entities.add(entity);
            }
        }
        return entities;
    }


    @Override
    public void contact(@NotNull ContactType type, @NotNull Contact contact) {
        if (contact.getFixtureA().getFilterData().categoryBits == World.GROUND_CATEGORY) {
            if (type == ContactType.BEGIN_CONTACT) {
                //newest pos is needed to accurately check if this is on ground
                updatePos();
                //y pos - getHalfBox2dHeight is middle
                int y = MathUtils.floor(posCache.y - getHalfBox2dHeight() - GROUND_CHECK_OFFSET);
                if (world.isAir(getBlockX(), y)) { return; }
                onGround = true;
            }
            else if (type == ContactType.END_CONTACT) {
                onGround = false;
            }
        }
    }

    /**
     * Update the cached position
     */
    private void updatePos() {
        posCache = body.getPosition();
    }

    @Override
    public void update() {
        if (Main.renderGraphic) {
            updatePos();
        }
    }

    /**
     * @return The texture of this entity
     */
    @Nullable
    public abstract TextureRegion getTextureRegion();

    /**
     * One unit is {@link Block#BLOCK_SIZE}
     *
     * @return The width of this entity in world view
     */
    public abstract int getWidth();

    /**
     * One unit is {@link Block#BLOCK_SIZE}
     *
     * @return The height of this entity in world view
     */
    public abstract int getHeight();

    public float getHalfBox2dWidth() {
        return getWidth() / (Block.BLOCK_SIZE * 2f);
    }

    public float getHalfBox2dHeight() {
        return getHeight() / (Block.BLOCK_SIZE * 2f);
    }

    @NotNull
    public Vector2 getPosition() {
        return posCache;
    }

    public int getBlockX() {
        return MathUtils.floor(posCache.x - getHalfBox2dWidth());
    }

    public int getBlockY() {
        return MathUtils.floor(posCache.y - getHalfBox2dHeight());
    }

    public Body getBody() {
        return body;
    }

    public boolean isFlying() {
        return flying;
    }

    public void setFlying(boolean flying) {
        this.flying = flying;
        synchronized (WorldRender.BOX2D_LOCK) {
            if (flying) {
                body.setLinearVelocity(0, 0);
                body.setGravityScale(0);
            }
            else {
                body.setGravityScale(1);
                body.setAwake(true);
            }
        }
    }

    public World getWorld() {
        return world;
    }

    public UUID getUuid() {
        return uuid;
    }

    public boolean isOnGround() {
        return onGround;
    }

    public String simpleName() {
        return getClass().getSimpleName();
    }

    @Override
    public void dispose() {
        if (body != null) {
            synchronized (WorldRender.BOX2D_LOCK) {
                getWorld().getRender().getBox2dWorld().destroyBody(body);
            }
            body = null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (!(o instanceof Entity)) { return false; }
        Entity entity = (Entity) o;
        return uuid.equals(entity.uuid);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    @Override
    public String toString() {
        return "Entity{" + "uuid=" + uuid + '}';
    }
}
