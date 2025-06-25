package harmonised.pmmo.setup.datagen.defaultpack;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.config.codecs.ObjectData;
import harmonised.pmmo.setup.datagen.PmmoDataProvider;
import harmonised.pmmo.util.Reference;
import harmonised.pmmo.util.RegistryUtil;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class DefaultBlockConfigProvider extends PmmoDataProvider<ObjectData> {
    Map<ResourceLocation, ObjectData.Builder> data = new HashMap<>();
    public DefaultBlockConfigProvider(PackOutput gen) {
        super(gen, "default", "pmmo/blocks", ObjectData.CODEC.codec());
    }

    @Override
    protected void start() {
        populateData();
        data.forEach((id, builder) -> this.add(id, builder.end()));
    }

    private void populateData() {
        miningBreak(Blocks.COAL_ORE, 25L, Tags.Blocks.ORES_COAL);
        miningBreak(Blocks.DEEPSLATE_COAL_ORE, 50L);
        miningBreak(Blocks.COPPER_ORE, 30L, Tags.Blocks.ORES_COPPER);
        miningBreak(Blocks.DEEPSLATE_COPPER_ORE, 60L);
        miningBreak(Blocks.IRON_ORE, 150L, Tags.Blocks.ORES_IRON);
        miningBreak(Blocks.DEEPSLATE_IRON_ORE, 300L);
        miningBreak(Blocks.GOLD_ORE, 200L, Tags.Blocks.ORES_GOLD);
        miningBreak(Blocks.DEEPSLATE_GOLD_ORE, 400L);
        miningBreak(Blocks.NETHER_GOLD_ORE, 400L);
        miningBreak(Blocks.REDSTONE_ORE, 150L, Tags.Blocks.ORES_REDSTONE);
        miningBreak(Blocks.DEEPSLATE_REDSTONE_ORE, 300L);
        miningBreak(Blocks.DIAMOND_ORE, 750L, Tags.Blocks.ORES_DIAMOND);
        miningBreak(Blocks.DEEPSLATE_DIAMOND_ORE, 750L);
        miningBreak(Blocks.EMERALD_ORE, 400L, Tags.Blocks.ORES_EMERALD);
        miningBreak(Blocks.DEEPSLATE_EMERALD_ORE, 10000L);
        miningBreak(Blocks.LAPIS_ORE, 50L, Tags.Blocks.ORES_LAPIS);
        miningBreak(Blocks.DEEPSLATE_LAPIS_ORE, 100L);
        miningBreak(Blocks.NETHER_QUARTZ_ORE, 100L, Tags.Blocks.ORES_QUARTZ);
        miningBreak(Blocks.ANCIENT_DEBRIS, 1500L);

        tagGet(BlockTags.BASE_STONE_OVERWORLD)
            .addTag(BlockTags.BASE_STONE_NETHER)
            .addTag("polished_andesite", "polished_deepslate","polished_diorite","polished_granite",
                    "cobblestone", "cobbled_deepslate", "calcite", "infested_deepslate", "infested_stone",
                    "nether_bricks", "magma_block")
                .addXpValues(EventType.BLOCK_BREAK, Map.of("mining", 10L));
        tagGet(Tags.Blocks.OBSIDIANS)
                .addXpValues(EventType.BLOCK_BREAK, Map.of("mining", 2000L))
                .addReq(ReqType.BREAK, Map.of("mining", 30L))
                .setVeinConsume(25);

        excavationBreak(Blocks.DIRT, 5, BlockTags.DIRT);
        for (Block block : List.of(Blocks.BONE_BLOCK, Blocks.CRIMSON_NYLIUM, Blocks.DIRT_PATH, Blocks.FARMLAND,
                Blocks.GRAVEL, Blocks.RED_SAND, Blocks.SAND, Blocks.SOUL_SAND, Blocks.SOUL_SOIL, Blocks.WARPED_NYLIUM)) {
            excavationBreak(block, 10);
        }

        tagGet(BlockTags.LOGS)
                .addXpValues(EventType.BLOCK_BREAK, Map.of("woodcutting", 20L))
                .addXpValues(EventType.BLOCK_PLACE, Map.of("building", 10L))
                .addReq(ReqType.BREAK, Map.of("woodcutting", 0L));
        tagGet(BlockTags.LEAVES)
                .addXpValues(EventType.BLOCK_BREAK, Map.of("woodcutting", 1L))
                .addReq(ReqType.BREAK, Map.of("woodcutting", 0L));
        tagGet(BlockTags.PLANKS)
                .addXpValues(EventType.BLOCK_BREAK, Map.of("woodcutting", 10L))
                .addXpValues(EventType.BLOCK_PLACE, Map.of("building", 10L))
                .addReq(ReqType.BREAK, Map.of("woodcutting", 0L))
                .addReq(ReqType.PLACE, Map.of("woodcutting", 0L));
        tagGet(BlockTags.SAPLINGS)
                .addXpValues(EventType.GROW, Map.of("farming", 300L))
                .addReq(ReqType.PLACE, Map.of("farming", 5L, "woodcutting", 10L));
        tagGet(Tags.Blocks.BOOKSHELVES).addXpValues(EventType.BLOCK_BREAK, Map.of("woodcutting", 500L));

        doFor(builder -> builder
                .addXpValues(EventType.BLOCK_BREAK, Map.of("farming", 20L, "woodcutting", 40L))
                .addXpValues(EventType.GROW, Map.of("farming", 12L)),
        Blocks.BAMBOO, Blocks.BAMBOO_SAPLING);
        farmingXp(150, 120, Blocks.BEETROOTS);
        farmingXp(100, 0, Blocks.BROWN_MUSHROOM, Blocks.BROWN_MUSHROOM_BLOCK);
        farmingXp(20, 60, Blocks.CACTUS);
        farmingXp(10, 80, Blocks.CARROTS);
        farmingXp(350, 80, Blocks.CHORUS_FLOWER);
        farmingXp(25, 80, Blocks.CHORUS_PLANT);
        farmingXp(10, 170, Blocks.COCOA);
        farmingXp(100, 300, Blocks.CRIMSON_FUNGUS);
        farmingXp(0, 20, Blocks.KELP, Blocks.KELP_PLANT);
        farmingXp(100, 0, Blocks.MUSHROOM_STEM);
        farmingXp(0, 110, Blocks.NETHER_WART);
        farmingXp(66, 66, Blocks.POTATOES);
        farmingXp(100, 0, Blocks.RED_MUSHROOM, Blocks.RED_MUSHROOM_BLOCK);
        farmingXp(0, 40, Blocks.SEA_PICKLE);
        farmingXp(1, 1, Blocks.SEAGRASS, Blocks.TALL_SEAGRASS);
        farmingXp(10, 50, Blocks.SUGAR_CANE);
        farmingXp(100, 300, Blocks.WARPED_FUNGUS);
        farmingXp(10, 90, Blocks.WHEAT);

        get(Blocks.BLAST_FURNACE).addReq(ReqType.PLACE, Map.of("building", 10L));
        get(Blocks.ENCHANTING_TABLE).addReq(ReqType.PLACE, Map.of("building", 25L));
        get(Blocks.SCAFFOLDING).addReq(ReqType.PLACE, Map.of("building", 10L));
        get(Blocks.SMOKER).addReq(ReqType.PLACE, Map.of("building", 10L));
        get(Blocks.STONECUTTER).addReq(ReqType.PLACE, Map.of("building", 10L));

        //because Jesus XP meme must persist.
        get(Blocks.FROSTED_ICE).addXpValues(EventType.BLOCK_PLACE, Map.of("magic", 1L));

        //===TINKERS SECTION=======
        for (String first : List.of("blood", "earth", "ender", "ichor", "sky")) {
            for (String second : List.of("earth", "ender", "ichor", "sky", "vanilla")) {
                get("tconstruct:"+first+"_"+second+"_slime_grass")
                        .addXpValues(EventType.BLOCK_BREAK, Map.of("excavation", 50L))
                        .addXpValues(EventType.BLOCK_PLACE, Map.of("building", 5L))
                        .setVeinConsume(1);
            }
            get("tconstruct:"+first+"_slime_tall_grass")
                    .addXpValues(EventType.BLOCK_BREAK, Map.of("excavation", 50L))
                    .addXpValues(EventType.BLOCK_PLACE, Map.of("building", 5L))
                    .setVeinConsume(1);
        }
        get("tconstruct:cobalt_ore").addXpValues(EventType.BLOCK_BREAK, Map.of("mining", 250L));
        for (String id : List.of("tconstruct:bloodshroom_button",
                "tconstruct:bloodshroom_door",
                "tconstruct:bloodshroom_fence",
                "tconstruct:bloodshroom_fence_gate",
                "tconstruct:bloodshroom_planks",
                "tconstruct:bloodshroom_pressure_plate",
                "tconstruct:bloodshroom_sign",
                "tconstruct:bloodshroom_planks_slab",
                "tconstruct:bloodshroom_planks_stairs",
                "tconstruct:bloodshroom_trapdoor",
                "tconstruct:bloodshroom_wall_sign",
                "tconstruct:bloodshroom_wood",
                "tconstruct:bloodshroom_sapling",
                "tconstruct:greenheart_sapling",
                "tconstruct:greenheart_button",
                "tconstruct:greenheart_door",
                "tconstruct:greenheart_fence",
                "tconstruct:greenheart_fence_gate",
                "tconstruct:greenheart_planks",
                "tconstruct:greenheart_pressure_plate",
                "tconstruct:greenheart_sign",
                "tconstruct:greenheart_planks_slab",
                "tconstruct:greenheart_planks_stairs",
                "tconstruct:greenheart_trapdoor",
                "tconstruct:greenheart_wall_sign",
                "tconstruct:greenheart_wood",
                "tconstruct:skyroot_button",
                "tconstruct:skyroot_door",
                "tconstruct:skyroot_fence",
                "tconstruct:skyroot_fence_gate",
                "tconstruct:skyroot_planks",
                "tconstruct:skyroot_pressure_plate",
                "tconstruct:skyroot_sign",
                "tconstruct:skyroot_planks_slab",
                "tconstruct:skyroot_planks_stairs",
                "tconstruct:skyroot_trapdoor",
                "tconstruct:skyroot_wall_sign",
                "tconstruct:skyroot_wood",
                "tconstruct:bloodshroom_log",
                "tconstruct:stripped_bloodshroom_wood",
                "tconstruct:skyroot_log",
                "tconstruct:stripped_skyroot_wood",
                "tconstruct:greenheart_log",
                "tconstruct:stripped_greenheart_wood")) {
            get(id).addXpValues(EventType.BLOCK_BREAK, Map.of("woodcutting", 90L))
                    .addXpValues(EventType.BLOCK_PLACE, Map.of("building", 10L))
                    .addReq(ReqType.BREAK, Map.of("woodcutting", 0L))
                    .addReq(ReqType.PLACE, Map.of("woodcutting", 0L))
                    .setVeinConsume(1);
        }
    }

    private void miningBreak(Block block, long xp, TagKey<?>...tags) {
        breakBlock(block, xp, "mining", tags);
    }

    private void excavationBreak(Block block, long xp, TagKey<?>...tags) {
        breakBlock(block, xp, "excavation", tags);
    }

    private void farmingXp(long mine, long grow, Block...blocks) {
        doFor(builder -> builder.addXpValues(EventType.BLOCK_BREAK, Map.of("farming", mine))
                .addXpValues(EventType.GROW, Map.of("farming", grow)), blocks);
    }

    private void breakBlock(Block block, long xp, String skill, TagKey<?>...tags) {
        get(block)
                .addXpValues(EventType.BLOCK_BREAK, Map.of(skill, xp))
                .addTag(tags)
                .addReq(ReqType.BREAK, Map.of(skill, 0L));
    }
    private ObjectData.Builder get(String id) {
        return data.computeIfAbsent(Reference.of(id), i -> ObjectData.build());}

    private ObjectData.Builder get(Block block) {
        return data.computeIfAbsent(RegistryUtil.getId(block), i -> ObjectData.build());}

    private ObjectData.Builder tagGet(TagKey<Block> tag) {
        return data.computeIfAbsent(tag.location(), i -> ObjectData.build()).addTag(tag);
    }

    private void doFor(Consumer<ObjectData.Builder> builder, Block...blocks) {
        for (Block block : blocks) {
            builder.accept(get(block));
        }
    }

    @Override
    public String getName() {return "Project MMO Default Block Generator";}
}
