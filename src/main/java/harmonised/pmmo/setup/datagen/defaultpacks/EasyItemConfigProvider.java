package harmonised.pmmo.setup.datagen.defaultpacks;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ModifierDataType;
import harmonised.pmmo.config.codecs.ObjectData;
import harmonised.pmmo.core.nbt.BehaviorToPrevious;
import harmonised.pmmo.core.nbt.LogicEntry;
import harmonised.pmmo.core.nbt.Operator;
import harmonised.pmmo.setup.datagen.PmmoDataProvider;
import harmonised.pmmo.util.RegistryUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class EasyItemConfigProvider extends PmmoDataProvider<ObjectData> {
    Map<ResourceLocation, ObjectData.Builder> data = new HashMap<>();
    public EasyItemConfigProvider(PackOutput gen) {
        super(gen, "easy", "pmmo/items", ObjectData.CODEC.codec());
    }

    @Override
    protected void start() {
        populateData();
        data.forEach((id, builder) -> this.add(id, builder.end()));
    }

    private void populateData() {
        //====FOOD============
        BuiltInRegistries.ITEM.stream()
                .filter(item -> new ItemStack(item).getFoodProperties(null) != null)
                .map(ItemStack::new).forEach(item -> {
            FoodProperties props = item.getFoodProperties(null);
            long xp = (props.nutrition() * 10L) +
                    (long)(props.saturation() * 100f) +
                    ((long)props.effects().size() * 50L);
            this.get(item.getItem()).addXpValues(EventType.CONSUME, Map.of("endurance", xp));
            this.get(item.getItem()).addXpValues(EventType.CRAFT, Map.of("cooking", xp));
            this.get(item.getItem()).addXpValues(EventType.SMELT, Map.of("cooking", xp));
        });
        //====REPAIRING======
        BuiltInRegistries.ITEM.stream()
                .filter(item -> new ItemStack(item).isDamageableItem())
                .map(ItemStack::new).forEach(item -> {
            long xp = (item.getMaxDamage() / 4) * 100L;
            this.get(item.getItem()).addXpValues(EventType.ANVIL_REPAIR, Map.of("smithing", xp));
        });

        var logic = new LogicEntry(BehaviorToPrevious.ADD_TO, false, List.of(
                new LogicEntry.Case(List.of("components{}.minecraft:potion_contents{}.potion"),
                        BuiltInRegistries.POTION.entrySet().stream().map(entry ->
                        equalsCriteria("endurance",
                                entry.getValue().getEffects().stream().mapToInt(MobEffectInstance::getDuration).max().orElseGet(() -> 1),
                                entry.getKey().location())).toList())
        ));
        get(Items.POTION).addNBTXp(EventType.CONSUME, List.of(logic));

        var logicBase = new LogicEntry(BehaviorToPrevious.ADD_TO, false, List.of(new LogicEntry.Case(
                List.of("components{}.minecraft:damage"),
                List.of(new LogicEntry.Criteria(Operator.EXISTS, Optional.empty(), Map.of(
                        "woodcutting", 1.025,
                        "farming", 1.025,
                        "agility", 1.025,
                        "fishing", 1.025))))));
        var logicSub = new LogicEntry(BehaviorToPrevious.SUB_FROM, false, List.of(new LogicEntry.Case(
                List.of("components{}.minecraft:dyed_color{}.rgb"),
                List.of(new LogicEntry.Criteria(Operator.EXISTS, Optional.empty(), Map.of(
                        "woodcutting", 1.025,
                        "farming", 1.025,
                        "agility", 1.025,
                        "fishing", 1.025))))));
        var logicBonus = new LogicEntry(BehaviorToPrevious.REPLACE, false, List.of(new LogicEntry.Case(
                List.of("components{}.minecraft:dyed_color{}.rgb"),
                List.of(
                        new LogicEntry.Criteria(Operator.EQUALS, Optional.of(List.of("16701501")), Map.of("woodcutting", 1.25)),
                        new LogicEntry.Criteria(Operator.EQUALS, Optional.of(List.of("8439583")), Map.of("agility", 1.25)),
                        new LogicEntry.Criteria(Operator.EQUALS, Optional.of(List.of("6192150")), Map.of("farming", 1.25)),
                        new LogicEntry.Criteria(Operator.EQUALS, Optional.of(List.of("3949738")), Map.of("fishing", 1.25)),
                        new LogicEntry.Criteria(Operator.EQUALS, Optional.of(List.of("1908001")), Map.of("combat", 1.25)),
                        new LogicEntry.Criteria(Operator.EQUALS, Optional.of(List.of("11546150")), Map.of("endurance", 1.25)),
                        new LogicEntry.Criteria(Operator.EQUALS, Optional.of(List.of("16383998")), Map.of("smithing", 1.25)),
                        new LogicEntry.Criteria(Operator.EQUALS, Optional.of(List.of("16351261")), Map.of("crafting", 1.25)),
                        new LogicEntry.Criteria(Operator.EQUALS, Optional.of(List.of("13061821")), Map.of("taming", 1.25)),
                        new LogicEntry.Criteria(Operator.EQUALS, Optional.of(List.of("3847130")), Map.of("building", 1.25)),
                        new LogicEntry.Criteria(Operator.EQUALS, Optional.of(List.of("15961002")), Map.of("cooking", 1.25)),
                        new LogicEntry.Criteria(Operator.EQUALS, Optional.of(List.of("4673362")), Map.of("alchemy", 1.25)),
                        new LogicEntry.Criteria(Operator.EQUALS, Optional.of(List.of("10329495")), Map.of("mining", 1.25)),
                        new LogicEntry.Criteria(Operator.EQUALS, Optional.of(List.of("1481884")), Map.of("swimming", 1.25)),
                        new LogicEntry.Criteria(Operator.EQUALS, Optional.of(List.of("8991416")), Map.of("excavation", 1.25))
                ))));
        get(Items.LEATHER_HELMET).addNBTBonus(ModifierDataType.WORN, List.of(logicBase, logicSub, logicBonus));
        get(Items.LEATHER_CHESTPLATE).addNBTBonus(ModifierDataType.WORN, List.of(logicBase, logicSub, logicBonus));
        get(Items.LEATHER_LEGGINGS).addNBTBonus(ModifierDataType.WORN, List.of(logicBase, logicSub, logicBonus));
        get(Items.LEATHER_BOOTS).addNBTBonus(ModifierDataType.WORN, List.of(logicBase, logicSub, logicBonus));

        get(Items.EMERALD)
                .addXpValues(EventType.RECEIVED_AS_TRADE, Map.of("smithing", 100L))
                .addXpValues(EventType.GIVEN_AS_TRADE, Map.of("smithing", 100L));

        //Block clones for tooltips
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

        get(Items.TORCH).addBonus(ModifierDataType.HELD, Map.of("mining", 2.0, "excavation", 2.0));
        get(Items.SOUL_TORCH).addBonus(ModifierDataType.HELD, Map.of("mining", 5.0, "excavation", 5.0));
        get(Items.SHIELD).addBonus(ModifierDataType.HELD, Map.of("combat", 2.0, "endurance", 2.0));
        get(Items.SPECTRAL_ARROW).addBonus(ModifierDataType.HELD, Map.of("combat", 5.0, "endurance", 5.0));
        get(Items.PUFFERFISH).addBonus(ModifierDataType.HELD, Map.of("swimming", 2.0));
        get(Items.NAUTILUS_SHELL).addBonus(ModifierDataType.HELD, Map.of("swimming", 5.0));
        get(Items.HEART_OF_THE_SEA).addBonus(ModifierDataType.HELD, Map.of("swimming", 10.0));
        get(Items.TORCHFLOWER).addBonus(ModifierDataType.HELD, Map.of("farming", 10.0));
        get(Items.PITCHER_POD).addBonus(ModifierDataType.HELD, Map.of("farming", 10.0));
        get(Items.SCAFFOLDING).addBonus(ModifierDataType.HELD, Map.of("building", 5.0));
        get(Items.CHAIN).addBonus(ModifierDataType.HELD, Map.of("woodcutting", 2.0));

        get(Items.SCULK_VEIN).setVeinCap(2000).setVeinRate(100.0);
    }

    private void miningBreak(Block block, long amount) {
        get(block.asItem()).addXpValues(EventType.BLOCK_BREAK, Map.of("mining", amount));
    }

    private LogicEntry.Criteria equalsCriteria(String skill, double value, ResourceLocation key) {
        return new LogicEntry.Criteria(Operator.EQUALS, Optional.of(List.of(key.toString())), Map.of(skill, value));
    }

    private ObjectData.Builder get(Item item) {
        return data.computeIfAbsent(RegistryUtil.getId(item), i -> ObjectData.build());}

    @Override
    public String getName() {return "Project MMO Easy Item Generator";}
}
