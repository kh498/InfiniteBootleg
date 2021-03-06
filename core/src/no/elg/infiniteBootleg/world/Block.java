package no.elg.infiniteBootleg.world;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import no.elg.infiniteBootleg.util.Binembly;
import no.elg.infiniteBootleg.util.CoordUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A block in the world each block is a part of a chunk which is a part of a world. Each block know its world location
 * and its
 * location within the parent chunk.
 *
 * @author Elg
 */
public class Block implements Binembly, Disposable {

    public final static int BLOCK_SIZE = 16;

    private final Material material;
    private final World world;
    private final Chunk chunk;

    private final int localX;
    private final int localY;

    public Block(@NotNull World world, @NotNull Chunk chunk, int localX, int localY, @NotNull Material material) {
        this.localX = localX;
        this.localY = localY;

        this.material = material;
        this.world = world;
        this.chunk = chunk;
    }

    @Nullable
    public TextureRegion getTexture() {
        return getMaterial().getTextureRegion();
    }

    @NotNull
    public Material getMaterial() {
        return material;
    }

    @NotNull
    public Chunk getChunk() {
        return chunk;
    }

    /**
     * @return World this block exists in
     */
    public World getWorld() {
        return world;
    }


    /**
     * @return World location of this block
     */
    public int getWorldX() {
        return chunk.getWorldX(localX);
    }

    /**
     * @return World location of this block
     */
    public int getWorldY() {
        return chunk.getWorldY(localY);
    }

    /**
     * @return The offset/local position of this block within its chunk
     */
    public int getLocalX() {
        return localX;
    }

    /**
     * @return The offset/local position of this block within its chunk
     */
    public int getLocalY() {
        return localY;
    }

    /**
     * @param dir
     *     The relative direction
     *
     * @return The relative block in the given location
     *
     * @see World#getBlock(int, int, boolean)
     */
    @NotNull
    public Block getRelative(@NotNull Direction dir) {
        return world.getBlock(getWorldX() + dir.dx, getWorldY() + dir.dy, false);
    }

    /**
     * @param dir
     *     The relative direction
     *
     * @return The relative raw block in the given location
     *
     * @see World#getBlock(int, int, boolean)
     */
    @Nullable
    public Block getRawRelative(@NotNull Direction dir) {
        int newWorldX = getWorldX() + dir.dx;
        int newWorldY = getWorldY() + dir.dy;
        if (CoordUtil.worldToChunk(newWorldX) == chunk.getChunkX() && //
            CoordUtil.worldToChunk(newWorldY) == chunk.getChunkY()) {
            return chunk.getBlocks()[localX + dir.dx][localY + dir.dy];
        }
        return world.getBlock(newWorldX, newWorldY, true);
    }

    public Block setBlock(@NotNull Material material) {
        return setBlock(material, true);
    }

    public Block setBlock(@NotNull Material material, boolean update) {
        return chunk.setBlock(localX, localY, material, update);
    }

    /**
     * Remove this block from the world
     */
    public void destroy() {
        chunk.setBlock(localX, localY, (Block) null, true);
    }

    @NotNull
    @Override
    public byte[] disassemble() {
        return new byte[] {(byte) material.ordinal()};
    }

    @Override
    public void assemble(@NotNull byte[] bytes) {
        throw new UnsupportedOperationException("Cannot assemble blocks directly. Blocks must be assembled by a chunk");
    }

    @Override
    public void dispose() {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        Block block = (Block) o;

        if (localX != block.localX) { return false; }
        if (localY != block.localY) { return false; }
        if (material != block.material) { return false; }
        if (!world.equals(block.world)) { return false; }
        return chunk.equals(block.chunk);
    }

    @Override
    public int hashCode() {
        int result = material.hashCode();
        result = 31 * result + world.hashCode();
        result = 31 * result + chunk.hashCode();
        result = 31 * result + localX;
        result = 31 * result + localY;
        return result;
    }

    @Override
    public String toString() {
        return "Block{" + "material=" + material + ", chunk=" + chunk + ", worldX=" + getWorldX() + ", worldY=" +
               getWorldY() + '}';
    }
}
