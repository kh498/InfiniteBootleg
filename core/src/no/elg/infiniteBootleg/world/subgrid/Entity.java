package no.elg.infiniteBootleg.world.subgrid;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Bresenham2;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import no.elg.infiniteBootleg.world.Location;
import no.elg.infiniteBootleg.world.World;
import no.elg.infiniteBootleg.world.render.Updatable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An entity that can move between the main world grid. The position of each entity is recorded in world coordinates.
 */
public abstract class Entity implements Updatable {

    public static final float DEFAULT_GRAVITY = -0.81f;
    public static final float DEFAULT_DRAG = 0.97f;
    public static final float DEFAULT_JUMP_FORCE = 2f;
    private float gravity;
    private float drag;
    private float jumpForce;

    private Bresenham2 bresenham;
    private World world;
    private Vector2 position;
    private Vector2 velocity;
    private boolean flying = false;


    public Entity(@NotNull World world, int x, int y) {
        this(world, new Vector2(x, y));
    }

    private Entity(@NotNull World world, @NotNull Vector2 position) {
        world.getEntities().add(this);
        this.world = world;
        this.position = position;
        velocity = new Vector2(0, 0);
        gravity = DEFAULT_GRAVITY;
        drag = DEFAULT_DRAG;
        jumpForce = DEFAULT_DRAG;
        bresenham = new Bresenham2();
    }

    public abstract TextureRegion getTextureRegion();

    /**
     * @param end
     *     The wanted position of the entity
     *
     * @return The last valid location between the current and the given position, if no collision {@code null}
     */
    @Nullable
    public Location collide(Vector2 end) {
        Array<GridPoint2> grid = bresenham.line((int) position.x, (int) position.y, (int) end.x, (int) end.y);

        for (GridPoint2 point : new Array.ArrayIterable<>(grid)) {
            if (world.getBlock(point.x, point.y - 1).getMaterial().isSolid()) {
                return new Location(point.x, point.y);
            }
        }
        return null;
    }

    @Override
    public void update() {
        float delta = Gdx.graphics.getDeltaTime();
        if (!velocity.isZero()) {
            Vector2 newPos = new Vector2(position.x * delta, position.y * delta);
            Location collisionPos = collide(newPos);
            if (collisionPos != null) {
                velocity.setZero();
                position = collisionPos.toVector2();
            }
            else {
                position.add(velocity.x, velocity.y);
            }
        }
        if (!flying) {
            velocity.add(0, gravity * delta);
            velocity.x *= drag;
            velocity.y *= drag;
        }
    }


    public Vector2 getPosition() {
        return position;
    }

    public Vector2 getVelocity() {
        return velocity;
    }

    public boolean isFlying() {
        return flying;
    }

    public void setFlying(boolean flying) {
        this.flying = flying;
    }

    public float getGravity() {
        return gravity;
    }

    public void setGravity(float gravity) {
        this.gravity = gravity;
    }

    public float getDrag() {
        return drag;
    }

    public void setDrag(float drag) {
        this.drag = drag;
    }

    public float getJumpForce() {
        return jumpForce;
    }

    public void setJumpForce(float jumpForce) {
        this.jumpForce = jumpForce;
    }

    public World getWorld() {
        return world;
    }
}