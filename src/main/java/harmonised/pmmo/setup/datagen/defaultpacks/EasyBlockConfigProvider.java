package harmonised.pmmo.setup.datagen.defaultpacks;

import com.mojang.serialization.Codec;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.config.codecs.ObjectData;
import harmonised.pmmo.setup.datagen.PmmoDataProvider;
import harmonised.pmmo.util.RegistryUtil;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.HashMap;
import java.util.Map;

public class EasyBlockConfigProvider extends PmmoDataProvider<ObjectData> {
    Map<Identifier, ObjectData.Builder> data = new HashMap<>();
    public EasyBlockConfigProvider(PackOutput gen) {
        super(gen, "easy", "pmmo/blocks", ObjectData.CODEC.codec());
    }

    @Override
    protected void start() {
        populateData();
        data.forEach((id, builder) -> this.add(id, builder.end()));
    }

    private void populateData() {
        miningBreak(Blocks.COAL_ORE, 250L);
        miningBreak(Blocks.DEEPSLATE_COAL_ORE, 500L);
        miningBreak(Blocks.COPPER_ORE, 300L);
        miningBreak(Blocks.DEEPSLATE_COPPER_ORE, 600L);
        miningBreak(Blocks.IRON_ORE, 1500L);
        miningBreak(Blocks.DEEPSLATE_IRON_ORE, 3000L);
        miningBreak(Blocks.GOLD_ORE, 2000L);
        miningBreak(Blocks.DEEPSLATE_GOLD_ORE, 4000L);
        miningBreak(Blocks.NETHER_GOLD_ORE, 4000L);
        miningBreak(Blocks.REDSTONE_ORE, 1500L);
        miningBreak(Blocks.DEEPSLATE_REDSTONE_ORE, 3000L);
        miningBreak(Blocks.DIAMOND_ORE, 7500L);
        miningBreak(Blocks.DEEPSLATE_DIAMOND_ORE, 7500L);
        miningBreak(Blocks.EMERALD_ORE, 4000L);
        miningBreak(Blocks.DEEPSLATE_EMERALD_ORE, 100000L);
        miningBreak(Blocks.LAPIS_ORE, 500L);
        miningBreak(Blocks.DEEPSLATE_LAPIS_ORE, 1000L);
        miningBreak(Blocks.NETHER_QUARTZ_ORE, 1000L);
        miningBreak(Blocks.ANCIENT_DEBRIS, 15000L);
    }

    private void miningBreak(Block block, long xp) {
        get(block).addXpValues(EventType.BLOCK_BREAK, Map.of("mining", xp));
    }
    private ObjectData.Builder get(Block block) {
        return data.computeIfAbsent(RegistryUtil.getId(block), i -> ObjectData.build());}

    @Override
    public String getName() {return "Project MMO Easy Block Generator";}
}
