package harmonised.pmmo.setup.datagen.easypack;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ModifierDataType;
import harmonised.pmmo.config.codecs.ObjectData;
import harmonised.pmmo.core.nbt.BehaviorToPrevious;
import harmonised.pmmo.core.nbt.LogicEntry;
import harmonised.pmmo.core.nbt.Operator;
import harmonised.pmmo.setup.datagen.PmmoDataProvider;
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
import java.util.function.Consumer;

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
        logic = new LogicEntry(BehaviorToPrevious.ADD_TO, false, List.of(
                new LogicEntry.Case(List.of("components{}.minecraft:potion_contents{}.potion"),
                        BuiltInRegistries.POTION.entrySet().stream().map(entry ->
                                equalsCriteria("crafting",
                                        entry.getValue().getEffects().stream().mapToInt(MobEffectInstance::getDuration).max().orElseGet(() -> 1),
                                        entry.getKey().location())).toList())
        ));
        get(Items.POTION).addNBTXp(EventType.BREW, List.of(logic));

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

        //============BONUSES==================
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

        //=============VEIN DATA=============================
        get(Items.SCULK_VEIN).setVeinCap(2000).setVeinRate(100.0);
        doFor(List.of(Items.STONE_PICKAXE, Items.STONE_AXE, Items.STONE_SHOVEL, Items.STONE_HOE), builder ->
                builder.setVeinRate(1.0).setVeinCap(10));
        doFor(List.of(Items.GOLDEN_PICKAXE, Items.GOLDEN_AXE, Items.GOLDEN_SHOVEL, Items.GOLDEN_HOE), builder ->
                builder.setVeinRate(1.0).setVeinCap(50));
        doFor(List.of(Items.IRON_PICKAXE, Items.IRON_AXE, Items.IRON_SHOVEL, Items.IRON_HOE), builder ->
                builder.setVeinRate(2.0).setVeinCap(15));
        doFor(List.of(Items.DIAMOND_PICKAXE, Items.DIAMOND_AXE, Items.DIAMOND_SHOVEL, Items.DIAMOND_HOE), builder ->
                builder.setVeinRate(3.0).setVeinCap(20));
        doFor(List.of(Items.NETHERITE_PICKAXE, Items.NETHERITE_AXE, Items.NETHERITE_SHOVEL, Items.NETHERITE_HOE), builder ->
                builder.setVeinRate(4.0).setVeinCap(25));
        
        doFor(List.of(Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS), builder ->
                builder.setVeinCap(10));
        doFor(List.of(Items.CHAINMAIL_HELMET, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_BOOTS), builder ->
                builder.setVeinCap(20));
        doFor(List.of(Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS), builder ->
                builder.setVeinCap(30));
        doFor(List.of(Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS), builder ->
                builder.setVeinCap(15));
        doFor(List.of(Items.NETHERITE_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS), builder ->
                builder.setVeinCap(20));

        //================SALVAGE================
        doFor(List.of(Items.WOODEN_PICKAXE, Items.WOODEN_SHOVEL, Items.WOODEN_AXE, Items.WOODEN_HOE,
                Items.STONE_PICKAXE, Items.STONE_SHOVEL, Items.STONE_AXE, Items.STONE_HOE,
                Items.GOLDEN_PICKAXE, Items.GOLDEN_SHOVEL, Items.GOLDEN_AXE, Items.GOLDEN_HOE,
                Items.IRON_PICKAXE, Items.IRON_SHOVEL, Items.IRON_AXE, Items.IRON_HOE,
                Items.DIAMOND_PICKAXE, Items.DIAMOND_SHOVEL, Items.DIAMOND_AXE, Items.DIAMOND_HOE,
                Items.NETHERITE_PICKAXE, Items.NETHERITE_SHOVEL, Items.NETHERITE_AXE, Items.NETHERITE_HOE),
                builder -> builder.addSalvage(getId(Items.STICK), APIUtils.SalvageBuilder.start()
                        .setBaseChance(0.8)
                        .setChancePerLevel(Map.of("crafting", 0.005, "smithing", 0.005))
                        .setSalvageMax(2)
                        .setXpAward(Map.of("crafting", 100L, "smithing", 100L)).build())
        );
        List.of(Items.WOODEN_SWORD, Items.STONE_SWORD, Items.GOLDEN_SWORD, Items.IRON_SWORD, Items.DIAMOND_SWORD,
                Items.NETHERITE_SWORD).forEach(tool -> recoverTool(tool, Items.STICK, 1, 100L, 0.005));

        recoverTool(Items.WOODEN_PICKAXE, Items.OAK_PLANKS, 3, 100L, 0.005 );
        recoverTool(Items.WOODEN_AXE, Items.OAK_PLANKS, 3, 100L, 0.005);
        recoverTool(Items.WOODEN_HOE, Items.OAK_PLANKS, 2, 100L, 0.005);
        recoverTool(Items.WOODEN_SHOVEL, Items.OAK_PLANKS, 1, 100L, 0.005);

        recoverTool(Items.STONE_PICKAXE, Items.COBBLESTONE, 3, 150L, 0.005);
        recoverTool(Items.STONE_AXE, Items.COBBLESTONE, 3, 150L, 0.005);
        recoverTool(Items.STONE_HOE, Items.COBBLESTONE, 2, 150L, 0.005);
        recoverTool(Items.STONE_SHOVEL, Items.COBBLESTONE, 1, 150L, 0.005);

        recoverTool(Items.GOLDEN_PICKAXE, Items.GOLD_INGOT, 3, 500L, 0.005);
        recoverTool(Items.GOLDEN_AXE, Items.GOLD_INGOT, 3, 500L, 0.005);
        recoverTool(Items.GOLDEN_HOE, Items.GOLD_INGOT, 2, 500L, 0.005);
        recoverTool(Items.GOLDEN_SHOVEL, Items.GOLD_INGOT, 1, 500L, 0.005);

        recoverTool(Items.IRON_PICKAXE, Items.IRON_INGOT, 3, 1500L, 0.0025);
        recoverTool(Items.IRON_AXE, Items.IRON_INGOT, 3, 1500L, 0.0025);
        recoverTool(Items.IRON_HOE, Items.IRON_INGOT, 2, 1500L, 0.0025);
        recoverTool(Items.IRON_SHOVEL, Items.IRON_INGOT, 1, 1500L, 0.0025);

        recoverTool(Items.DIAMOND_PICKAXE, Items.DIAMOND, 3, 5000L, 0.00125);
        recoverTool(Items.DIAMOND_AXE, Items.DIAMOND, 3, 5000L, 0.00125);
        recoverTool(Items.DIAMOND_HOE, Items.DIAMOND, 2, 5000L, 0.00125);
        recoverTool(Items.DIAMOND_SHOVEL, Items.DIAMOND, 1, 5000L, 0.00125);

        recoverTool(Items.NETHERITE_PICKAXE, Items.NETHERITE_INGOT, 3, 10000L, 0.00125);
        recoverTool(Items.NETHERITE_AXE, Items.NETHERITE_INGOT, 3, 10000L, 0.00125);
        recoverTool(Items.NETHERITE_HOE, Items.NETHERITE_INGOT, 2, 10000L, 0.00125);
        recoverTool(Items.NETHERITE_SHOVEL, Items.NETHERITE_INGOT, 1, 10000L, 0.00125);

    }

    protected void miningBreak(Block block, long amount) {
        get(block.asItem()).addXpValues(EventType.BLOCK_BREAK, Map.of("mining", amount));
    }

    protected LogicEntry.Criteria equalsCriteria(String skill, double value, ResourceLocation key) {
        return new LogicEntry.Criteria(Operator.EQUALS, Optional.of(List.of(key.toString())), Map.of(skill, value));
    }

    protected ObjectData.Builder get(Item item) {
        return data.computeIfAbsent(getId(item), i -> ObjectData.build());
    }
    
    protected void doFor(List<Item> items, Consumer<ObjectData.Builder> process) {
        items.forEach(item -> process.accept(get(item)));
    }

    protected void recoverTool(Item tool, Item material, int count, long xp, double skillChance) {
        get(tool).addSalvage(getId(material), APIUtils.SalvageBuilder.start()
                .setBaseChance(0.8)
                .setChancePerLevel(Map.of("crafting", skillChance, "smithing", skillChance))
                .setSalvageMax(count)
                .setXpAward(Map.of("crafting", xp, "smithing", xp)).build());
    }

    @Override
    public String getName() {return "Project MMO Easy Item Generator";}
}
