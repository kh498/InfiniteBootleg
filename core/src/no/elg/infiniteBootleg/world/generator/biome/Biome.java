package no.elg.infiniteBootleg.world.generator.biome;

import com.badlogic.gdx.utils.Array;
import no.elg.infiniteBootleg.util.Tuple;
import no.elg.infiniteBootleg.world.Block;
import no.elg.infiniteBootleg.world.Chunk;
import no.elg.infiniteBootleg.world.Material;
import no.elg.infiniteBootleg.world.generator.PerlinChunkGenerator;
import no.elg.infiniteBootleg.world.generator.noise.PerlinNoise;
import org.jetbrains.annotations.NotNull;

/**
 * @author Elg
 */
public enum Biome {


    PLAINS(0.1f, 0.9f, 64, 0.009f, 0, Material.STONE, Material.GRASS, new Tuple<>(Material.DIRT, 10)),
    MOUNTAINS(100f, 0.9f, 356, 0.005f, 25, Material.STONE, Material.GRASS, new Tuple<>(Material.DIRT, 6)),
    DESERT(0.1f, 0.9f, 32, 0.005f, 0, Material.STONE, Material.SAND, new Tuple<>(Material.SAND, 12));

    public static final int INTERPOLATION_RADIUS = 25;

    private final float y;
    private final float z;
    private final float amplitude;
    private final float frequency;
    private final int offset;
    private final Material filler;
    private final Material topmostBlock;
    private final Material[] topBlocks;

    @SafeVarargs
    Biome(float y, float z, float amplitude, float frequency, int offset, @NotNull Material filler,
          @NotNull Material topmostBlock, @NotNull Tuple<Material, Integer>... topBlocks) {
        this.y = y;
        this.z = z;
        this.amplitude = amplitude;
        this.frequency = frequency;
        this.offset = offset;
        this.filler = filler;
        this.topmostBlock = topmostBlock;

        Array<Material> mats = new Array<>(true, 16, Material.class);
        for (Tuple<Material, Integer> tuple : topBlocks) {
            mats.ensureCapacity(tuple.value);
            for (int i = 0; i < tuple.value; i++) {
                mats.add(tuple.key);
            }
        }
        this.topBlocks = mats.toArray();
    }

    public static float rawHeightAt(@NotNull PerlinNoise noise, int worldX, float y, float z, float amplitude,
                                    float frequency, int offset) {
        return noise.octaveNoise(worldX * frequency, y * frequency, z * frequency, 6, 0.5f) * amplitude + offset;
    }

    public Material materialAt(@NotNull PerlinNoise noise, int height, int worldX, int worldY) {
        int delta = height - worldY - 1;
        if (delta == 0) { return topmostBlock; }

        delta += (int) Math.abs(Math.floor(rawHeightAt(noise, worldX, y, z, 10, 0.05f, 0)));

        if (delta >= topBlocks.length) { return filler; }
        return topBlocks[delta];
    }

    public int heightAt(@NotNull PerlinChunkGenerator pcg, int worldX) {
        int y = 0;
        for (int dx = -INTERPOLATION_RADIUS; dx <= INTERPOLATION_RADIUS; dx++) {
            if (dx != 0) {
                Biome biome = pcg.getBiome(worldX + dx);
                //NOTE it is not a bug that the worldX does not have dx added to it
                //well it was, but it looks better (ie more random) when the dx is not here
                y += biome.rawHeightAt(pcg.getNoise(), worldX);
            }
            else {
                y += rawHeightAt(pcg.getNoise(), worldX);
            }
        }
        return Math.abs(y / (INTERPOLATION_RADIUS * 2 + 1));
    }

    public double rawHeightAt(@NotNull PerlinNoise noise, int worldX) {
        return rawHeightAt(noise, worldX, y, z, amplitude, frequency, offset);
    }

    public void fillUpTo(@NotNull PerlinNoise noise, @NotNull Chunk chunk, int localX, int localY, int height) {
        Block[] blocks = chunk.getBlocks()[localX];
        for (int dy = 0; dy < localY; dy++) {
            Material mat = materialAt(noise, height, chunk.getWorldX() + localX, chunk.getWorldY() + dy);
            blocks[dy] = mat.createBlock(chunk.getWorld(), chunk, localX, dy);
        }
    }
}
