package harmonised.pmmo.setup.datagen.defaultpacks;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ModifierDataType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.config.codecs.ObjectData;
import harmonised.pmmo.core.nbt.BehaviorToPrevious;
import harmonised.pmmo.core.nbt.LogicEntry;
import harmonised.pmmo.core.nbt.Operator;
import harmonised.pmmo.setup.datagen.PmmoDataProvider;
import harmonised.pmmo.util.QuadFunction;
import harmonised.pmmo.util.Reference;
import harmonised.pmmo.util.RegistryUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import org.apache.commons.lang3.function.TriFunction;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DefaultItemConfigProvider extends PmmoDataProvider<ObjectData> {
    Map<ResourceLocation, ObjectData.Builder> data = new HashMap<>();
    public DefaultItemConfigProvider(PackOutput gen) {
        super(gen, "default", "pmmo/items", ObjectData.CODEC.codec());
    }

    @Override
    protected void start() {
        populateData();
        data.forEach((id, builder) -> this.add(id, builder.end()));
    }

    private void populateData() {
        //ORE SMELTING
        get(Items.ANCIENT_DEBRIS).addTag("#c:ores/netherite_scrap").addXpValues(EventType.SMELT, Map.of("smithing", 2500L));
        get(Items.RAW_COPPER).addTag("#c:raw_materials/copper", "#c:/ores/copper").addXpValues(EventType.SMELT, Map.of("smithing", 33L));
        get(Items.RAW_IRON).addTag("#c:raw_materials/iron", "#c:ores/iron").addXpValues(EventType.SMELT, Map.of("smithing", 100L));
        get(Items.RAW_GOLD).addTag("#c:raw_materials/gold", "#c:ores/gold").addXpValues(EventType.SMELT, Map.of("smithing", 200L));
        get(Items.DIAMOND_ORE).addTag("#c:ores/diamond").addXpValues(EventType.SMELT, Map.of("smithing", 1000L));
        get(Items.EMERALD_ORE).addTag("#c:ores/emerald").addXpValues(EventType.SMELT, Map.of("smithing", 500L));
        get(Items.LAPIS_ORE).addTag("#c:ores/lapis").addXpValues(EventType.SMELT, Map.of("smithing", 100L));
        get(Items.NETHER_QUARTZ_ORE).addTag("#c:ores/quartz").addXpValues(EventType.SMELT, Map.of("smithing", 100L));
        get(Items.REDSTONE_ORE).addTag("#c:ores/redstone").addXpValues(EventType.SMELT, Map.of("smithing", 200L));

        //BLOCK TOOLTIP PARITY SETTINGS
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

        //====FOOD============
        BuiltInRegistries.ITEM.stream()
                .filter(item -> new ItemStack(item).getFoodProperties(null) != null)
                .map(ItemStack::new).forEach(item -> {
                    FoodProperties props = item.getFoodProperties(null);
                    long xp = (props.nutrition() * 5L) +
                            (long)(props.saturation() * 50f) +
                            ((long)props.effects().size() * 50L);
                    this.get(item.getItem()).addXpValues(EventType.CONSUME, Map.of("endurance", xp));
                    this.get(item.getItem()).addXpValues(EventType.CRAFT, Map.of("cooking", xp));
                    this.get(item.getItem()).addXpValues(EventType.SMELT, Map.of("cooking", xp));
                    this.get(item.getItem()).addXpValues(EventType.FISH, Map.of("fishing", xp*2));
                });
        get(Items.ENCHANTED_GOLDEN_APPLE).addBonus(ModifierDataType.HELD, Map.of("magic", 2.0));
        get(Items.GOLDEN_APPLE).addBonus(ModifierDataType.HELD, Map.of("magic", 1.5));
        get(Items.GOLDEN_CARROT).addBonus(ModifierDataType.HELD, Map.of("magic", 1.5));

        //====REPAIRING======
        BuiltInRegistries.ITEM.stream()
                .filter(item -> new ItemStack(item).isDamageableItem())
                .map(ItemStack::new).forEach(item -> {
                    long xp = (item.getMaxDamage() / 4) * 10L;
                    this.get(item.getItem()).addXpValues(EventType.ANVIL_REPAIR, Map.of("smithing", xp));
                });
        
        //POTIONS
        var logic = new LogicEntry(BehaviorToPrevious.ADD_TO, false, List.of(
                new LogicEntry.Case(List.of("components{}.minecraft:potion_contents{}.potion"),
                        BuiltInRegistries.POTION.entrySet().stream().map(entry ->
                                equalsCriteria("magic",
                                        entry.getValue().getEffects().stream().mapToInt(MobEffectInstance::getDuration).max().orElseGet(() -> 1),
                                        entry.getKey().location())).toList())
        ));
        get(Items.POTION).addNBTXp(EventType.CONSUME, List.of(logic));
        logic = new LogicEntry(BehaviorToPrevious.ADD_TO, false, List.of(
                new LogicEntry.Case(List.of("components{}.minecraft:potion_contents{}.potion"),
                        BuiltInRegistries.POTION.entrySet().stream().map(entry ->
                                equalsCriteria("magic",
                                        entry.getValue().getEffects().stream().mapToInt(MobEffectInstance::getDuration).max().orElseGet(() -> 1),
                                        entry.getKey().location())).toList())
        ));
        get(Items.POTION).addNBTXp(EventType.BREW, List.of(logic));
        get(Items.LINGERING_POTION).addNBTXp(EventType.ACTIVATE_ITEM, List.of(logic));
        get(Items.SPLASH_POTION).addNBTXp(EventType.ACTIVATE_ITEM, List.of(logic));
        
        //ARMOR STATS
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
                        new LogicEntry.Criteria(Operator.EQUALS, Optional.of(List.of("1908001")), Map.of("combat", 1.25, "archery", 1.25)),
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
        
        doFor(List.of(Items.CHAINMAIL_HELMET, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_BOOTS), builder -> {
            builder.addXpValues(EventType.CRAFT, Map.of("crafting", 2000L, "smithing", 300L))
                    .addReq(ReqType.WEAR, Map.of("endurance", 30L))
                    .addNegativeEffect(RegistryUtil.getId(MobEffects.MOVEMENT_SLOWDOWN), 1)
                    .addNegativeEffect(RegistryUtil.getId(MobEffects.WEAKNESS), 1)
                    .addSalvage(getId(Items.IRON_INGOT), APIUtils.SalvageBuilder.start()
                            .setSalvageMax(4)
                            .setMaxChance(0.9)
                            .setChancePerLevel(Map.of("smithing", 0.005))
                            .setLevelReq(Map.of("smithing", 5L))
                            .setXpAward(Map.of("smithing", 30L)).build())
                    .setVeinCap(10)
                    .setVeinRate(0.01);
        });
        doFor(List.of(Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS), builder -> {
            builder.addXpValues(EventType.CRAFT, Map.of("crafting", 4000L, "smithing", 700L))
                    .addXpValues(EventType.ENCHANT, Map.of("magic", 80L))
                    .addReq(ReqType.WEAR, Map.of("endurance", 60L))
                    .addNegativeEffect(RegistryUtil.getId(MobEffects.MOVEMENT_SLOWDOWN), 2)
                    .addNegativeEffect(RegistryUtil.getId(MobEffects.WEAKNESS), 2)
                    .addSalvage(getId(Items.DIAMOND), APIUtils.SalvageBuilder.start()
                            .setSalvageMax(4)
                            .setMaxChance(0.9)
                            .setChancePerLevel(Map.of("smithing", 0.005))
                            .setLevelReq(Map.of("smithing", 5L))
                            .setXpAward(Map.of("smithing", 30L)).build())
                    .setVeinCap(5)
                    .setVeinRate(0.1);
        });
        doFor(List.of(Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS), builder -> {
            builder.addXpValues(EventType.CRAFT, Map.of("crafting", 2000L, "smithing", 300L))
                    .addXpValues(EventType.ENCHANT, Map.of("magic", 60L))
                    .addReq(ReqType.WEAR, Map.of("endurance", 10L))
                    .addBonus(ModifierDataType.WORN, Map.of("mining", 1.25))
                    .addNegativeEffect(RegistryUtil.getId(MobEffects.MOVEMENT_SLOWDOWN), 1)
                    .addNegativeEffect(RegistryUtil.getId(MobEffects.WEAKNESS), 1)
                    .addSalvage(getId(Items.GOLD_INGOT), APIUtils.SalvageBuilder.start()
                            .setSalvageMax(4)
                            .setMaxChance(0.9)
                            .setChancePerLevel(Map.of("smithing", 0.005))
                            .setLevelReq(Map.of("smithing", 5L))
                            .setXpAward(Map.of("smithing", 30L)).build())
                    .setVeinCap(15)
                    .setVeinRate(0.01);
        });
        doFor(List.of(Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS), builder -> {
            builder.addXpValues(EventType.CRAFT, Map.of("crafting", 2000L, "smithing", 300L))
                    .addXpValues(EventType.ENCHANT, Map.of("magic", 60L))
                    .addReq(ReqType.WEAR, Map.of("endurance", 30L))
                    .addNegativeEffect(RegistryUtil.getId(MobEffects.MOVEMENT_SLOWDOWN), 1)
                    .addNegativeEffect(RegistryUtil.getId(MobEffects.WEAKNESS), 1)
                    .addSalvage(getId(Items.IRON_INGOT), APIUtils.SalvageBuilder.start()
                            .setSalvageMax(4)
                            .setMaxChance(0.9)
                            .setChancePerLevel(Map.of("smithing", 0.005))
                            .setLevelReq(Map.of("smithing", 5L))
                            .setXpAward(Map.of("smithing", 30L)).build())
                    .setVeinCap(10)
                    .setVeinRate(0.01);
        });
        doFor(List.of(Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS), builder -> {
            builder.addXpValues(EventType.CRAFT, Map.of("crafting", 6000L, "smithing", 1000L))
                    .addXpValues(EventType.ENCHANT, Map.of("magic", 120L))
                    .addReq(ReqType.WEAR, Map.of("endurance", 90L))
                    .addNegativeEffect(RegistryUtil.getId(MobEffects.MOVEMENT_SLOWDOWN), 3)
                    .addNegativeEffect(RegistryUtil.getId(MobEffects.WEAKNESS), 3)
                    .addSalvage(getId(Items.DIAMOND), APIUtils.SalvageBuilder.start()
                            .setSalvageMax(4)
                            .setMaxChance(0.9)
                            .setChancePerLevel(Map.of("smithing", 0.005))
                            .setLevelReq(Map.of("smithing", 5L))
                            .setXpAward(Map.of("smithing", 30L)).build())
                    .addSalvage(getId(Items.NETHERITE_INGOT), APIUtils.SalvageBuilder.start()
                            .setSalvageMax(1)
                            .setMaxChance(0.3)
                            .setChancePerLevel(Map.of("smithing", 0.0005))
                            .setLevelReq(Map.of("smithing", 50L))
                            .setXpAward(Map.of("smithing", 3000L)).build())
                    .setVeinCap(30)
                    .setVeinRate(0.5);
        });
        
        //TOOLS
        doFor(List.of(Items.WOODEN_AXE, Items.WOODEN_PICKAXE, Items.WOODEN_HOE, Items.WOODEN_SHOVEL, Items.WOODEN_SWORD), builder -> builder
                .addXpValues(EventType.CRAFT, Map.of("crafting",4000L, "smithing",700L))
                .addXpValues(EventType.ENCHANT, Map.of("magic", 120L))
                .addReq(ReqType.WEAPON, Map.of("combat", 0L))
                .addReq(ReqType.WEAR, Map.of("combat", 0L,"mining", 0L,"woodcutting", 0L,"farming", 0L,"excavation", 0L))
                .addReq(ReqType.TOOL, Map.of("mining", 0L,"woodcutting", 0L,"farming", 0L,"excavation", 0L))
                .setVeinCap(30)
                .setVeinRate(0.1)
        );
        doFor(List.of(Items.STONE_AXE, Items.STONE_PICKAXE, Items.STONE_HOE, Items.STONE_SHOVEL, Items.STONE_SWORD), builder -> builder
                .addXpValues(EventType.CRAFT, Map.of("crafting",1500L, "smithing",200L))
                .addXpValues(EventType.ENCHANT, Map.of("magic", 30L))
                .addNegativeEffect(RegistryUtil.getId(MobEffects.DIG_SLOWDOWN), 2)
                .addNegativeEffect(RegistryUtil.getId(MobEffects.WEAKNESS), 2)
                .setVeinCap(10)
                .setVeinRate(0.01)
        );
        toolReq(Items.STONE_AXE, 10, 10, "woodcutting").addBonus(ModifierDataType.HELD, Map.of("woodcutting", 1.05));
        toolReq(Items.STONE_PICKAXE, 10, 5, "mining").addBonus(ModifierDataType.HELD, Map.of("mining", 1.05));
        toolReq(Items.STONE_HOE, 10, 5, "farming").addBonus(ModifierDataType.HELD, Map.of("farming", 1.05));
        toolReq(Items.STONE_SHOVEL, 10, 5, "excavation").addBonus(ModifierDataType.HELD, Map.of("excavation", 1.05));
        toolReq(Items.STONE_SWORD, 10, 10, "combat").addBonus(ModifierDataType.HELD, Map.of("combat", 1.05));
        
        doFor(List.of(Items.DIAMOND_AXE, Items.DIAMOND_PICKAXE, Items.DIAMOND_HOE, Items.DIAMOND_SHOVEL, Items.DIAMOND_SWORD), builder -> builder
                .addXpValues(EventType.CRAFT, Map.of("crafting",4000L, "smithing",700L))
                .addXpValues(EventType.ENCHANT, Map.of("magic", 120L))
                .addNegativeEffect(RegistryUtil.getId(MobEffects.DIG_SLOWDOWN), 2)
                .addNegativeEffect(RegistryUtil.getId(MobEffects.WEAKNESS), 2)
                .setVeinCap(30)
                .setVeinRate(0.1)
        );
        toolReq(Items.DIAMOND_AXE, 60, 60, "woodcutting");
        toolReq(Items.DIAMOND_PICKAXE, 60, 30, "mining");
        toolReq(Items.DIAMOND_HOE, 60, 30, "farming");
        toolReq(Items.DIAMOND_SHOVEL, 60, 30, "excavation");
        toolReq(Items.DIAMOND_SWORD, 60, 60, "combat");

        doFor(List.of(Items.IRON_AXE, Items.IRON_PICKAXE, Items.IRON_HOE, Items.IRON_SHOVEL, Items.IRON_SWORD), builder -> builder
                .addXpValues(EventType.CRAFT, Map.of("crafting",2000L, "smithing",300L))
                .addXpValues(EventType.ENCHANT, Map.of("magic", 60L))
                .addNegativeEffect(RegistryUtil.getId(MobEffects.DIG_SLOWDOWN), 2)
                .addNegativeEffect(RegistryUtil.getId(MobEffects.WEAKNESS), 2)
                .setVeinCap(30)
                .setVeinRate(0.01)
        );
        toolReq(Items.IRON_AXE, 30, 30, "woodcutting");
        toolReq(Items.IRON_PICKAXE, 30, 15, "mining");
        toolReq(Items.IRON_HOE, 30, 15, "farming");
        toolReq(Items.IRON_SHOVEL, 30, 15, "excavation");
        toolReq(Items.IRON_SWORD, 30, 30, "combat");

        doFor(List.of(Items.GOLDEN_AXE, Items.GOLDEN_PICKAXE, Items.GOLDEN_HOE, Items.GOLDEN_SHOVEL,Items.GOLDEN_SWORD), builder -> builder
                .addXpValues(EventType.CRAFT, Map.of("crafting",1500L, "smithing",200L))
                .addXpValues(EventType.ENCHANT, Map.of("magic", 30L))
                .addNegativeEffect(RegistryUtil.getId(MobEffects.DIG_SLOWDOWN), 2)
                .addNegativeEffect(RegistryUtil.getId(MobEffects.WEAKNESS), 2)
                .setVeinCap(30)
                .setVeinRate(0.1)
        );
        toolReq(Items.GOLDEN_AXE, 10, 10, "woodcutting").addBonus(ModifierDataType.HELD, Map.of("woodcutting", 1.5));
        toolReq(Items.GOLDEN_PICKAXE, 10, 5, "mining").addBonus(ModifierDataType.HELD, Map.of("mining", 1.5));
        toolReq(Items.GOLDEN_HOE, 10, 5, "farming").addBonus(ModifierDataType.HELD, Map.of("farming", 1.5));
        toolReq(Items.GOLDEN_SHOVEL, 10, 5, "excavation").addBonus(ModifierDataType.HELD, Map.of("excavation", 1.5));
        toolReq(Items.GOLDEN_SWORD, 10, 10, "combat").addBonus(ModifierDataType.HELD, Map.of("combat", 1.5));

        doFor(List.of(Items.NETHERITE_AXE, Items.NETHERITE_PICKAXE, Items.NETHERITE_HOE, Items.NETHERITE_SHOVEL, Items.NETHERITE_SWORD), builder -> builder
                .addXpValues(EventType.CRAFT, Map.of("crafting",6000L, "smithing",1000L))
                .addXpValues(EventType.ENCHANT, Map.of("magic", 250L))
                .addNegativeEffect(RegistryUtil.getId(MobEffects.DIG_SLOWDOWN), 2)
                .addNegativeEffect(RegistryUtil.getId(MobEffects.WEAKNESS), 2)
                .setVeinCap(45)
                .setVeinRate(0.01)
        );
        toolReq(Items.NETHERITE_AXE, 90, 90, "woodcutting");
        toolReq(Items.NETHERITE_PICKAXE, 90, 45, "mining");
        toolReq(Items.NETHERITE_HOE, 90, 45, "farming");
        toolReq(Items.NETHERITE_SHOVEL, 90, 45, "excavation");
        toolReq(Items.NETHERITE_SWORD, 90, 90, "combat");

        //MISC
        get(Items.HEART_OF_THE_SEA).addBonus(ModifierDataType.HELD, Map.of("swimming",1.25, "fishing",1.1));
        
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
                        .setXpAward(Map.of("crafting", 10L, "smithing", 100L)).build())
        );
        List.of(Items.WOODEN_SWORD, Items.STONE_SWORD, Items.GOLDEN_SWORD, Items.IRON_SWORD, Items.DIAMOND_SWORD,
                Items.NETHERITE_SWORD).forEach(tool -> recoverTool(tool, Items.STICK, 1, 10L, 0.005));

        recoverTool(Items.WOODEN_PICKAXE, Items.OAK_PLANKS, 3, 10L, 0.005 );
        recoverTool(Items.WOODEN_AXE, Items.OAK_PLANKS, 3, 10L, 0.005);
        recoverTool(Items.WOODEN_HOE, Items.OAK_PLANKS, 2, 10L, 0.005);
        recoverTool(Items.WOODEN_SHOVEL, Items.OAK_PLANKS, 1, 10L, 0.005);

        recoverTool(Items.STONE_PICKAXE, Items.COBBLESTONE, 3, 15L, 0.005);
        recoverTool(Items.STONE_AXE, Items.COBBLESTONE, 3, 15L, 0.005);
        recoverTool(Items.STONE_HOE, Items.COBBLESTONE, 2, 15L, 0.005);
        recoverTool(Items.STONE_SHOVEL, Items.COBBLESTONE, 1, 15L, 0.005);

        recoverTool(Items.GOLDEN_PICKAXE, Items.GOLD_INGOT, 3, 50L, 0.005);
        recoverTool(Items.GOLDEN_AXE, Items.GOLD_INGOT, 3, 50L, 0.005);
        recoverTool(Items.GOLDEN_HOE, Items.GOLD_INGOT, 2, 50L, 0.005);
        recoverTool(Items.GOLDEN_SHOVEL, Items.GOLD_INGOT, 1, 50L, 0.005);

        recoverTool(Items.IRON_PICKAXE, Items.IRON_INGOT, 3, 150L, 0.0025);
        recoverTool(Items.IRON_AXE, Items.IRON_INGOT, 3, 150L, 0.0025);
        recoverTool(Items.IRON_HOE, Items.IRON_INGOT, 2, 150L, 0.0025);
        recoverTool(Items.IRON_SHOVEL, Items.IRON_INGOT, 1, 150L, 0.0025);

        recoverTool(Items.DIAMOND_PICKAXE, Items.DIAMOND, 3, 500L, 0.00125);
        recoverTool(Items.DIAMOND_AXE, Items.DIAMOND, 3, 500L, 0.00125);
        recoverTool(Items.DIAMOND_HOE, Items.DIAMOND, 2, 500L, 0.00125);
        recoverTool(Items.DIAMOND_SHOVEL, Items.DIAMOND, 1, 500L, 0.00125);

        recoverTool(Items.NETHERITE_PICKAXE, Items.NETHERITE_INGOT, 3, 1000L, 0.00125);
        recoverTool(Items.NETHERITE_AXE, Items.NETHERITE_INGOT, 3, 1000L, 0.00125);
        recoverTool(Items.NETHERITE_HOE, Items.NETHERITE_INGOT, 2, 1000L, 0.00125);
        recoverTool(Items.NETHERITE_SHOVEL, Items.NETHERITE_INGOT, 1, 1000L, 0.00125);
        
        //CONSTRUCTION WAND DEFAULTS
        get("constructionwand:core_angel").setOverride(true)
                .addXpValues(EventType.CRAFT, Map.of("crafting",2000L,"smithing",300L))
                .addSalvage(getId(Items.GOLD_INGOT), APIUtils.SalvageBuilder.start()
                        .setSalvageMax(2)
                        .setMaxChance(0.9)
                        .setChancePerLevel(Map.of("smithing",0.005))
                        .setLevelReq(Map.of("smithing",5L))
                        .setXpAward(Map.of("smithing",30L)).build());
        get("constructionwand:core_destruction").setOverride(true)
                .addXpValues(EventType.CRAFT, Map.of("crafting",10000L,"smithing",7000L))
                .addSalvage(getId(Items.DIAMOND), APIUtils.SalvageBuilder.start()
                        .setSalvageMax(6)
                        .setMaxChance(0.9)
                        .setChancePerLevel(Map.of("smithing",0.005))
                        .setLevelReq(Map.of("smithing",5L))
                        .setXpAward(Map.of("smithing",30L)).build())
                .addSalvage(getId(Items.DIAMOND_BLOCK), APIUtils.SalvageBuilder.start()
                        .setMaxChance(0.9)
                        .setChancePerLevel(Map.of("smithing",0.005))
                        .setLevelReq(Map.of("smithing",5L))
                        .setXpAward(Map.of("smithing",180L)).build());
        var stick = APIUtils.SalvageBuilder.start()
                .setSalvageMax(2)
                .setBaseChance(0.15)
                .setChancePerLevel(Map.of("smithing",0.05))
                .setXpAward(Map.of("smithing",10L)).build();
        get("constructionwand:iron_wand").setOverride(true)
                .addXpValues(EventType.ANVIL_REPAIR, Map.of("smithing", 1500L))
                .addXpValues(EventType.CRAFT, Map.of("crafting", 2000L, "smithing", 300L))
                .addXpValues(EventType.ACTIVATE_ITEM, Map.of("building", 200L))
                .addXpValues(EventType.ENCHANT, Map.of("magic", 60L))
                .addReq(ReqType.INTERACT, Map.of("building", 30L))
                .addReq(ReqType.USE, Map.of("building", 30L))
                .addBonus(ModifierDataType.HELD, Map.of("bulding", 1.1))
                .addSalvage(getId(Items.IRON_INGOT), APIUtils.SalvageBuilder.start()
                        .setMaxChance(0.9)
                        .setLevelReq(Map.of("smithing", 5L))
                        .setChancePerLevel(Map.of("smithing",0.005))
                        .setXpAward(Map.of("smithing",30L)).build())
                .addSalvage(getId(Items.STICK), stick);
        get("constructionwand:diamond_wand").setOverride(true)
                .addXpValues(EventType.ANVIL_REPAIR, Map.of("smithing", 4000L))
                .addXpValues(EventType.CRAFT, Map.of("crafting", 4000L, "smithing", 700L))
                .addXpValues(EventType.ACTIVATE_ITEM, Map.of("building", 500L))
                .addXpValues(EventType.ENCHANT, Map.of("magic", 120L))
                .addReq(ReqType.INTERACT, Map.of("building", 60L))
                .addReq(ReqType.USE, Map.of("building", 60L))
                .addBonus(ModifierDataType.HELD, Map.of("bulding", 1.5))
                .addSalvage(getId(Items.DIAMOND), APIUtils.SalvageBuilder.start()
                        .setMaxChance(0.9)
                        .setLevelReq(Map.of("smithing", 5L))
                        .setChancePerLevel(Map.of("smithing",0.005))
                        .setXpAward(Map.of("smithing",30L)).build())
                .addSalvage(getId(Items.STICK), stick);
        get("constructionwand:infinity_wand").setOverride(true)
                .addXpValues(EventType.ANVIL_REPAIR, Map.of("smithing", 7000L))
                .addXpValues(EventType.CRAFT, Map.of("crafting", 7000L, "smithing", 1000L))
                .addXpValues(EventType.ACTIVATE_ITEM, Map.of("building", 1000L))
                .addXpValues(EventType.ENCHANT, Map.of("magic", 180L))
                .addReq(ReqType.INTERACT, Map.of("building", 90L))
                .addReq(ReqType.USE, Map.of("building", 90L))
                .addBonus(ModifierDataType.HELD, Map.of("bulding", 2.0))
                .addSalvage(getId(Items.NETHER_STAR), APIUtils.SalvageBuilder.start()
                        .setMaxChance(0.9)
                        .setLevelReq(Map.of("smithing", 10L))
                        .setChancePerLevel(Map.of("smithing",0.002))
                        .setXpAward(Map.of("smithing",30L)).build())
                .addSalvage(getId(Items.STICK), stick);

        //GOBLINS AND DUNGEONS DEFAULTS
        doForRaw(List.of(
                "goblinsanddungeons:ring_of_experience",
                "goblinsanddungeons:ring_of_glory",
                "goblinsanddungeons:ring_of_health",
                "goblinsanddungeons:ring_of_stealth"),
            builder -> builder
                .addXpValues(EventType.CRAFT, Map.of("magic",1000L,"crafting",1000L))
                .addXpValues(EventType.FISH, Map.of("fishing", 1500L))
                .addXpValues(EventType.ANVIL_REPAIR, Map.of("smithing",30L))
                .addXpValues(EventType.ENCHANT, Map.of("magic", 1000L))
                .addReq(ReqType.WEAR, Map.of("crafting", 30L))
                .addReq(ReqType.USE, Map.of("magic",15L))
                .addNegativeEffect(RegistryUtil.getId(MobEffects.MOVEMENT_SLOWDOWN), 3)
                .addNegativeEffect(RegistryUtil.getId(MobEffects.WEAKNESS), 3));

        //TINKERS CONSTRUCT DEFAULTS
        Function<List<String>, List<LogicEntry>> tinkerReqs = str -> {
            Function<Double, Map<String, Double>> getMap = d -> str.stream().collect(Collectors.toMap(s -> s, s -> d));
            return List.of(new LogicEntry(BehaviorToPrevious.ADD_TO, false, List.of(
              new LogicEntry.Case(List.of("tic_materials[]"), List.of(
                      new LogicEntry.Criteria(Operator.EQUALS, Optional.of(List.of("tconstruct:necrotic_bone","tconstruct:bone","tconstruct:flint","tconstruct:stone","tconstruct:wood","tconstruct:rock")), getMap.apply(5.0)),
                      new LogicEntry.Criteria(Operator.EQUALS, Optional.of(List.of("tconstruct:iron","tconstruct:plated_slimewood","tconstruct:seared_stone","tconstruct:scorched_stone","tconstruct:copper","tconstruct:slimewood","tconstruct:lead","tconstruct:silver","tconstruct:electrum","tconstruct:chorus","tconstruct:bloodbone")), getMap.apply(10.0)),
                      new LogicEntry.Criteria(Operator.EQUALS, Optional.of(List.of("twilightforest:steeleaf","twilightforest:knightmetal","twilightforest:fiery","tconstruct:slimesteel","tconstruct:whitestone","tconstruct:amethyst_bronze","tconstruct:steel","tconstruct:bronze","tconstruct:tinkers_bronze","tconstruct:nahuatl","tconstruct:pig_iron","tconstruct:rose_gold","tconstruct:constantan","tconstruct:cobalt", "tconstruct:necronium")), getMap.apply(20.0)),
                      new LogicEntry.Criteria(Operator.EQUALS, Optional.of(List.of("tconstruct:queens_slime","tconstruct:blazing_bone","tconstruct:bloodbone","tconstruct:hepatizon","tconstruct:manyullyn")), getMap.apply(30.0))
              ))
        )));};
        doForRaw(List.of("tconstruct:axe", "tconstruct:broad_axe", "tconstruct:hand_axe"), builder -> builder
                .addNBTReq(ReqType.TOOL, tinkerReqs.apply(List.of("woodcutting")))
                .addNBTReq(ReqType.WEAPON, tinkerReqs.apply(List.of("combat"))));
        doForRaw(List.of("tconstruct:broad_sword", "tconstruct:cleaver", "tconstruct:dagger", "tconstruct:kama", "tconstruct:scythe", "tconstruct:sword"), builder -> builder
                .addNBTReq(ReqType.TOOL, tinkerReqs.apply(List.of("combat")))
                .addNBTReq(ReqType.WEAPON, tinkerReqs.apply(List.of("combat"))));
        doForRaw(List.of("tconstruct:excavator"), builder -> builder
                .addNBTReq(ReqType.TOOL, tinkerReqs.apply(List.of("excavation")))
                .addNBTReq(ReqType.WEAPON, tinkerReqs.apply(List.of("combat"))));
        doForRaw(List.of("tconstruct:mattock"), builder -> builder
                .addNBTReq(ReqType.TOOL, tinkerReqs.apply(List.of("excavation", "woodcutting", "farming")))
                .addNBTReq(ReqType.WEAPON, tinkerReqs.apply(List.of("combat"))));
        doForRaw(List.of("tconstruct:pickadze", "tconstruct:pickaxe", "tconstruct:sledge_hammer", "tconstruct:vein_hammer"), builder -> builder
                .addNBTReq(ReqType.TOOL, tinkerReqs.apply(List.of("mining")))
                .addNBTReq(ReqType.WEAPON, tinkerReqs.apply(List.of("combat"))));

        //TETRA DEFAULTS
        doForRaw(List.of("tetra:modular_bow", "tetra:modular_crossbow", "tetra:modular_double", "tetra:modular_shield", "tetra:modular_single", "tetra:modular_sword"),
                builder -> builder
                        .addXpValues(EventType.ENCHANT, Map.of("magic", 0L))
                        .addXpValues(EventType.CRAFT, Map.of("crafting", 0L))
                        .addXpValues(EventType.FISH, Map.of("fishing", 0L)));
        
        QuadFunction<String, String, String, Map<Double, List<String>>, LogicEntry> tetraEntry = (item, component, skill, values) -> {
            List<LogicEntry.Criteria> criteria = values.entrySet().stream().map(entry -> new LogicEntry.Criteria(Operator.EQUALS, Optional.of(entry.getValue().stream().map(str -> component+"/"+str).toList()), Map.of(skill, entry.getKey()))).toList();
            LogicEntry.Case cases = new LogicEntry.Case(List.of(item+"/"+component+"_material"), criteria);
            return new LogicEntry(BehaviorToPrevious.HIGHEST, false, List.of(cases));
        };
        QuadFunction<String, String, String, Map<Double, List<String>>, LogicEntry> tetraEntryLR = (item, component, skill, values) -> {
            List<LogicEntry.Criteria> criteria = values.entrySet().stream().map(entry -> new LogicEntry.Criteria(Operator.EQUALS, Optional.of(entry.getValue().stream().map(str -> component+"/"+str).toList()), Map.of(skill, entry.getKey()))).toList();
            LogicEntry.Case cases = new LogicEntry.Case(List.of(item+"/"+component+"_left_material", item+"/"+component+"_right_material"), criteria);
            return new LogicEntry(BehaviorToPrevious.HIGHEST, false, List.of(cases));
        };
        
        List<LogicEntry> bowList = List.of(
                tetraEntry.apply("bow", "basic_string", "archery", Map.of(
                        1.0, List.of("string","weeping_vine","vine","twisting_vine","leather","hide"),
                        2.0, List.of("phantom_membrane","dragon_sinew"))),
                tetraEntry.apply("bow", "straight_stave","archery", Map.of(
                        0.0, List.of("oak","birch","acacia","jungle","spruce","dark_oak", "stick"),
                        15.0, List.of("copper"),
                        30.0, List.of("iron"),
                        10.0, List.of("gold"),
                        90.0, List.of("netherite")
                )),
                tetraEntry.apply("bow", "extended_rest","archery", Map.of(
                        0.0, List.of("bone","oak","birch","acacia","jungle","spruce","dark_oak","stick"),
                        7.0, List.of("copper"),
                        15.0, List.of("iron"),
                        5.0, List.of("gold"),
                        45.0, List.of("netherite")
                )),
                tetraEntry.apply("bow", "long_stave","archery", Map.of(
                        0.0, List.of("oak","birch","acacia","jungle","spruce","dark_oak", "stick"),
                        15.0, List.of("copper"),
                        30.0, List.of("iron"),
                        10.0, List.of("gold"),
                        90.0, List.of("netherite")
                )),
                tetraEntry.apply("bow", "recurve_stave","archery", Map.of(
                        0.0, List.of("oak","birch","acacia","jungle","spruce","dark_oak", "stick"),
                        15.0, List.of("copper"),
                        30.0, List.of("iron"),
                        10.0, List.of("gold"),
                        90.0, List.of("netherite")
                )),
                tetraEntry.apply("bow", "sights","archery", Map.of(
                        0.0, List.of("bone","oak","birch","acacia","jungle","spruce","dark_oak"),
                        7.0, List.of("copper"),
                        15.0, List.of("iron"),
                        5.0, List.of("gold"),
                        45.0, List.of("netherite")
                )),
                tetraEntry.apply("bow", "stabilizer","archery", Map.of(
                        0.0, List.of("bone","oak","birch","acacia","jungle","spruce","dark_oak"),
                        7.0, List.of("copper"),
                        15.0, List.of("iron"),
                        5.0, List.of("gold"),
                        45.0, List.of("netherite")
                ))
        );
        get("tetra:modular_bow")
                .addNBTReq(ReqType.WEAR, bowList)
                .addNBTReq(ReqType.WEAPON, bowList);
        
        List<LogicEntry> crossbowList = List.of(
                tetraEntry.apply("crossbow", "basic_string", "archery", Map.of(
                        1.0, List.of("string","weeping_vine","vine","twisting_vine","leather","hide"),
                        2.0, List.of("phantom_membrane","dragon_sinew")
                )),
                tetraEntry.apply("crossbow", "basic_stave","archery", Map.of(
                        0.0, List.of("oak","birch","acacia","jungle","spruce","dark_oak", "stick"),
                        15.0, List.of("copper"),
                        30.0, List.of("iron"),
                        10.0, List.of("gold"),
                        90.0, List.of("netherite")
                )),
                tetraEntry.apply("crossbow", "basic_stock","archery", Map.of(
                        0.0, List.of("bone","oak","birch","acacia","jungle","spruce","dark_oak","stick"),
                        7.0, List.of("copper"),
                        15.0, List.of("iron"),
                        5.0, List.of("gold"),
                        45.0, List.of("netherite")
                )),
                tetraEntry.apply("crossbow", "stirrup","archery", Map.of(
                        7.0, List.of("copper"),
                        15.0, List.of("iron"),
                        5.0, List.of("gold"),
                        45.0, List.of("netherite")
                ))
        );
        get("tetra:modular_crossbow")
                .addNBTReq(ReqType.WEAR, crossbowList)
                .addNBTReq(ReqType.WEAPON, crossbowList);
        
        Map<Double, List<String>> criteria = Map.of(
                0.0, List.of("oak","birch","acacia","jungle","spruce","dark_oak"),
                10.0, List.of("stone", "flint", "gold"),
                15.0, List.of("andesite","diorite","granite","copper"),
                30.0, List.of("iron"),
                25.0, List.of("blackstone","obsidian"),
                90.0, List.of("netherite"),
                40.0, List.of("emerald"),
                60.0, List.of("diamond")
        );
        Function<String, List<LogicEntry>> pickaxeList = str -> List.of(
                tetraEntryLR.apply("double", "basic_pickaxe", str, criteria),
                tetraEntryLR.apply("double", "basic_axe", str, criteria),
                tetraEntryLR.apply("double", "basic_hammer", str, criteria),
                tetraEntryLR.apply("double", "basic_claw", str, criteria),
                tetraEntryLR.apply("double", "basic_adze", str, criteria),
                tetraEntryLR.apply("double", "basic_butt", str, criteria),
                tetraEntryLR.apply("double", "basic_hoe", str, criteria)
        );
        doForRaw(List.of("tetra:modular_double", "tetra:modular_single"), builder -> builder
                .addNBTReq(ReqType.WEAR, pickaxeList.apply("mining"))
                .addNBTReq(ReqType.TOOL, pickaxeList.apply("mining"))
                .addNBTReq(ReqType.WEAPON, pickaxeList.apply("combat"))
                .setVeinCap(30)
                .setVeinRate(0.1)
        );
        
        get("tetra:modular_shield").addNBTReq(ReqType.WEAR, List.of(
                tetraEntry.apply("shield", "basic_grip", "endurance", Map.of(
                        0.0, List.of("bone", "oak","birch","acacia","jungle","spruce","dark_oak", "stick"),
                        5.0, List.of("gold"),
                        7.0, List.of("copper"),
                        15d, List.of("iron", "blaze_rod", "end_rod"),
                        45d, List.of("netherite", "forged_beam")
                )),
                tetraEntry.apply("shield", "buckler", "endurance", Map.of(
                        0.0, List.of("bone", "oak","birch","acacia","jungle","spruce","dark_oak"),
                        10d, List.of("gold"),
                        15d, List.of("copper"),
                        30d, List.of("iron"),
                        90d, List.of("netherite", "vent_plate")
                )),
                tetraEntry.apply("shield", "heater", "endurance", Map.of(
                        0.0, List.of("bone", "oak","birch","acacia","jungle","spruce","dark_oak"),
                        10d, List.of("gold"),
                        15d, List.of("copper"),
                        30d, List.of("iron"),
                        90d, List.of("netherite", "vent_plate")
                )),
                tetraEntry.apply("shield", "tower", "endurance", Map.of(
                        0.0, List.of("bone", "oak","birch","acacia","jungle","spruce","dark_oak"),
                        10d, List.of("gold"),
                        15d, List.of("copper"),
                        30d, List.of("iron"),
                        90d, List.of("netherite", "vent_plate")
                )),
                tetraEntry.apply("shield", "spike", "endurance", Map.of(
                        0.0, List.of("bone"),
                        5.0, List.of("gold"),
                        7.0, List.of("copper"),
                        15d, List.of("iron"),
                        45d, List.of("netherite")
                )),
                tetraEntry.apply("shield", "straps", "endurance", Map.of(
                        1.0, List.of("string", "weeping_vine","vine","twisting_vine","leather","hide"),
                        2.0, List.of("phantom_membrane", "dragon_sinew")
                ))
        ));

        Function<String, List<LogicEntry>> swordList = str -> List.of(
                tetraEntry.apply("sword", "basic_blade", str, criteria),
                tetraEntry.apply("sword", "basic_hilt", str, criteria),
                tetraEntry.apply("sword", "binding", str, criteria),
                tetraEntry.apply("sword", "counterweight", str, criteria),
                tetraEntry.apply("sword", "decorative_pommel", str, criteria),
                tetraEntry.apply("sword", "forefinger_ring", str, criteria),
                tetraEntry.apply("sword", "grip_loop", str, criteria),
                tetraEntry.apply("sword", "heavy_blade", str, criteria),
                tetraEntry.apply("sword", "howling", str, criteria),
                tetraEntry.apply("sword", "machete", str, criteria),
                tetraEntry.apply("sword", "makeshift_guard", str, criteria),
                tetraEntry.apply("sword", "reinforced_fuller", str, criteria),
                tetraEntry.apply("sword", "short_blade", str, criteria),
                tetraEntry.apply("sword", "sturdy_guard", str, criteria),
                tetraEntry.apply("sword", "wide_guard", str, criteria)
        );

        get("tetra:modular_sword")
                .addNBTReq(ReqType.WEAR, swordList.apply("combat"))
                .addNBTReq(ReqType.WEAPON, swordList.apply("combat"))
                .addNBTReq(ReqType.TOOL, swordList.apply("farming"));
     }

    protected void miningBreak(Block block, long amount, TagKey<Block>...tags) {
        var tagStrings = Arrays.stream(tags).map(key -> "#"+key.location().toString()).toArray(String[]::new);
        get(block.asItem()).addXpValues(EventType.BLOCK_BREAK, Map.of("mining", amount)).addTag(tagStrings);
    }

    protected LogicEntry.Criteria equalsCriteria(String skill, double value, ResourceLocation key) {
        return new LogicEntry.Criteria(Operator.EQUALS, Optional.of(List.of(key.toString())), Map.of(skill, value));
    }

    protected ObjectData.Builder get(Item item) {
        return data.computeIfAbsent(getId(item), i -> ObjectData.build());
    }
    protected ObjectData.Builder get(String id) {
        return data.computeIfAbsent(Reference.of(id), i -> ObjectData.build());
    }
    
    protected void doFor(List<Item> items, Consumer<ObjectData.Builder> process) {
        items.forEach(item -> process.accept(get(item)));
    }
    protected void doForRaw(List<String> items, Consumer<ObjectData.Builder> process) {
        items.forEach(item -> process.accept(get(item)));
    }

    protected void recoverTool(Item tool, Item material, int count, long xp, double skillChance) {
        get(tool).addSalvage(getId(material), APIUtils.SalvageBuilder.start()
                .setBaseChance(0.8)
                .setChancePerLevel(Map.of("crafting", skillChance, "smithing", skillChance))
                .setSalvageMax(count)
                .setXpAward(Map.of("crafting", xp, "smithing", xp)).build());
    }
    
    protected ObjectData.Builder toolReq(Item tool, long asTool, long asWeapon, String skill) {
        return get(tool).addReq(ReqType.TOOL, Map.of(skill, asTool)).addReq(ReqType.WEAR, Map.of(skill, asTool)).addReq(ReqType.WEAPON, Map.of("combat", asWeapon));
    }

    @Override
    public String getName() {return "Project MMO Default Item Generator";}
}
