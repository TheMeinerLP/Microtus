package net.minestom.server.instance.batch;

import net.minestom.server.instance.block.Block;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@EnvTest
public class BatchQueryIntegrationTest {

    @Test
    public void radius(Env env) {
        var instance = env.process().instance().createInstanceContainer();
        var batch = BatchQuery.radius(5);

        instance.loadChunk(0, 0).join();

        for (int x = 3; x < 13; x++) {
            for (int y = 3; y < 13; y++) {
                for (int z = 3; z < 13; z++) {
                    instance.setBlock(x, y, z, Block.STONE);
                }
            }
        }

        var result = instance.getBlocks(8, 8, 8, batch);
        assertEquals(1331, result.count());

        for (int x = 3; x < 13; x++) {
            for (int y = 3; y < 13; y++) {
                for (int z = 3; z < 13; z++) {
                    assertEquals(instance.getBlock(x, y, z), result.getBlock(x, y, z),
                            "Block at " + x + ", " + y + ", " + z + " was not equal");
                }
            }
        }
    }

    @Test
    public void type(Env env) {
        var instance = env.process().instance().createInstanceContainer();
        var batch = BatchQuery.builder(3).type(Block.STONE).build();

        instance.loadChunk(0, 0).join();

        // Fill section with grass
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    instance.setBlock(x, y, z, Block.GRASS);
                }
            }
        }

        // Place stone in the middle
        for (int x = 7; x < 10; x++) {
            for (int y = 7; y < 10; y++) {
                for (int z = 7; z < 10; z++) {
                    instance.setBlock(x, y, z, Block.STONE);
                }
            }
        }

        var result = instance.getBlocks(8, 8, 8, batch);
        assertEquals(27, result.count());

        for (int x = 5; x < 11; x++) {
            for (int y = 5; y < 11; y++) {
                for (int z = 5; z < 11; z++) {

                    if (x >= 7 && y >= 7 && z >= 7
                            && x < 10 && y < 10 && z < 10) {
                        assertEquals(instance.getBlock(x, y, z), result.getBlock(x, y, z),
                                "Block at " + x + ", " + y + ", " + z + " was not equal");
                    } else {
                        int finalX = x;
                        int finalY = y;
                        int finalZ = z;
                        assertThrows(Exception.class, () -> result.getBlock(finalX, finalY, finalZ),
                                "Block at " + x + ", " + y + ", " + z + " was not air");
                    }
                }
            }
        }
    }
}
