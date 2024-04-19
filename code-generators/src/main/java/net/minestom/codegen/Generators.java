package net.minestom.codegen;

import net.minestom.codegen.color.DyeColorGenerator;
import net.minestom.codegen.fluid.FluidGenerator;
import net.minestom.codegen.recipe.RecipeTypeGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;

public class Generators {
    private static final Logger LOGGER = LoggerFactory.getLogger(Generators.class);

    public static void main(String[] args) {
        if (args.length != 1) {
            LOGGER.error("Usage: <target folder>");
            return;
        }
        File outputFolder = new File(args[0]);

        // Special generators
        new DyeColorGenerator(resource("dye_colors.json"), outputFolder).generate();
        new RecipeTypeGenerator(resource("recipe_types.json"), outputFolder).generate();

        // Generic protocol object
        var generator = new CodeGenerator(outputFolder);
        generator.generate(resource("blocks.json"), "net.minestom.server.instance.block", "Block", "BlockImpl", "Blocks");
        generator.generate(resource("items.json"), "net.minestom.server.item", "Material", "MaterialImpl", "Materials");
        generator.generate(resource("entities.json"), "net.minestom.server.entity", "EntityType", "EntityTypeImpl", "EntityTypes");
        generator.generate(resource("biomes.json"), "net.minestom.server.world.biomes", "Biome", "BiomeImpl", "Biomes");
        generator.generate(resource("enchantments.json"), "net.minestom.server.item.enchant", "Enchantment", "EnchantmentImpl", "Enchantments");
        generator.generate(resource("potion_effects.json"), "net.minestom.server.potion", "PotionEffect", "PotionEffectImpl", "PotionEffects");
        generator.generate(resource("potions.json"), "net.minestom.server.potion", "PotionType", "PotionTypeImpl", "PotionTypes");
        generator.generate(resource("particles.json"), "net.minestom.server.particle", "Particle", "ParticleImpl", "Particles");
        generator.generate(resource("sounds.json"), "net.minestom.server.sound", "SoundEvent", "BuiltinSoundEvent", "SoundEvents");
        generator.generate(resource("custom_statistics.json"), "net.minestom.server.statistic", "StatisticType", "StatisticTypeImpl", "StatisticTypes");
        generator.generate(resource("damage_types.json"), "net.minestom.server.entity.damage", "DamageType", "DamageTypeImpl", "DamageTypes");
        generator.generate(resource("trim_materials.json"), "net.minestom.server.item.armor", "TrimMaterial", "TrimMaterialImpl", "TrimMaterials");
        generator.generate(resource("trim_patterns.json"), "net.minestom.server.item.armor", "TrimPattern", "TrimPatternImpl", "TrimPatterns");
        generator.generate(resource("attributes.json"), "net.minestom.server.entity.attribute", "Attribute", "AttributeImpl", "Attributes");

        // Generate fluids
        new FluidGenerator(resource("fluids.json"), outputFolder).generate();

        // TODO: Generate villager professions
//        new VillagerProfessionGenerator(
//                new File(inputFolder, targetVersion + "_villager_professions.json"),
//                outputFolder
//        ).generate();
        // TODO: Generate villager types
//        new VillagerTypeGenerator(
//                new File(inputFolder, targetVersion + "_villager_types.json"),
//                outputFolder
//        ).generate();
        LOGGER.info("Finished generating code");
    }

    private static InputStream resource(String name) {
        return Generators.class.getResourceAsStream("/" + name);
    }
}
