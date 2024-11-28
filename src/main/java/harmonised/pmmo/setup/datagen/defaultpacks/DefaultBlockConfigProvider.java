package harmonised.pmmo.setup.datagen.defaultpacks;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.config.codecs.ObjectData;
import harmonised.pmmo.setup.datagen.PmmoDataProvider;
import harmonised.pmmo.util.RegistryUtil;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
                    "cobblestone", "cobbled_deepslate")
                .addXpValues(EventType.BLOCK_BREAK, Map.of("mining", 10L));

        tagGet(BlockTags.LOGS)
                .addXpValues(EventType.BLOCK_BREAK, Map.of("woodcutting", 80L))
                .addXpValues(EventType.BLOCK_PLACE, Map.of("building", 10L))
                .addReq(ReqType.BREAK, Map.of("woodcutting", 0L));
        tagGet(BlockTags.LEAVES)
                .addXpValues(EventType.BLOCK_BREAK, Map.of("woodcutting", 11L))
                .addReq(ReqType.BREAK, Map.of("woodcutting", 0L));
        tagGet(BlockTags.PLANKS)
                .addXpValues(EventType.BLOCK_BREAK, Map.of("woodcutting", 50L))
                .addXpValues(EventType.BLOCK_PLACE, Map.of("building", 10L))
                .addReq(ReqType.BREAK, Map.of("woodcutting", 0L))
                .addReq(ReqType.PLACE, Map.of("woodcutting", 0L));
        tagGet(BlockTags.SAPLINGS)
                .addXpValues(EventType.GROW, Map.of("farming", 300L))
                .addReq(ReqType.PLACE, Map.of("farming", 5L, "woodcutting", 10L));
    }

    private void miningBreak(Block block, long xp, TagKey<?>...tags) {
        get(block)
                .addXpValues(EventType.BLOCK_BREAK, Map.of("mining", xp))
                .addTag(tags)
                .addReq(ReqType.BREAK, Map.of("mining", 0L));
    }
    private ObjectData.Builder get(Block block) {
        return data.computeIfAbsent(RegistryUtil.getId(block), i -> ObjectData.build());}

    private ObjectData.Builder tagGet(TagKey<Block> tag) {
        return data.computeIfAbsent(tag.location(), i -> ObjectData.build()).addTag(tag);
    }

    @Override
    public String getName() {return "Project MMO Default Block Generator";}
}
