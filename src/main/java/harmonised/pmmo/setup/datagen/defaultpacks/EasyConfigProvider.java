package harmonised.pmmo.setup.datagen.defaultpacks;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.config.PerksConfig;
import harmonised.pmmo.config.codecs.ConfigData;
import harmonised.pmmo.config.codecs.ServerData;
import harmonised.pmmo.config.readers.ConfigListener;
import harmonised.pmmo.features.anticheese.AntiCheeseConfig;
import harmonised.pmmo.features.autovalues.AutoBlock;
import harmonised.pmmo.features.autovalues.AutoEntity;
import harmonised.pmmo.features.autovalues.AutoItem;
import harmonised.pmmo.features.autovalues.AutoValueConfig;
import harmonised.pmmo.setup.datagen.PmmoDataProvider;
import harmonised.pmmo.util.Reference;
import harmonised.pmmo.util.RegistryUtil;
import harmonised.pmmo.util.TagBuilder;
import net.minecraft.data.PackOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.lwjgl.system.APIUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EasyConfigProvider extends PmmoDataProvider<ConfigData<?>> {
    Map<ConfigListener.ServerConfigs, ConfigData<?>> data = new HashMap<>();
    public EasyConfigProvider(PackOutput gen) {super(gen, "easy", "config", ConfigListener.ServerConfigs.MAPPER);}

    @Override
    protected void start() {
        populateData();
        data.forEach((type, config) -> this.add(Reference.rl(type.filename), config));
    }

    private void populateData() {
        data.put(ConfigListener.ServerConfigs.SERVER, new ServerData(
                new ServerData.General(50d, Reference.mc("smithing_table"), true, true),
                new ServerData.Levels(1000000, List.of(-1L), 0.0, false,
                        true, 1.0, Map.of(), 3000, 0, 1.0),
                new ServerData.Requirements(Arrays.stream(ReqType.values()).collect(Collectors.toMap(a -> a, a -> false))),
                new ServerData.XpGains(0.0, false, serverDefaults(), Map.of(
                        EventType.DEAL_DAMAGE, Map.of(
                                "minecraft:generic_kill", Map.of("combat", 100L),
                                "minecraft:player_attack", Map.of("combat", 100L),
                                "#minecraft:is_projectile", Map.of("combat", 100L)
                        ),
                        EventType.RECEIVE_DAMAGE, Map.of(
                                "minecraft:generic_kill", Map.of("endurance", 100L),
                                "#pmmo:environment", Map.of("endurance", 100L),
                                "#pmmo:impact", Map.of("endurance", 150L),
                                "#pmmo:magic", Map.of("endurance", 150L),
                                "#minecraft:is_projectile", Map.of("endurance", 150L))
                )),
                new ServerData.Party(200, 1.3),
                new ServerData.MobScaling(false, 0, 0, 1.0, true, 0.0, 1.0, Map.of()),
                new ServerData.VeinMiner(true, false, 1, 1.5,
                        List.of(Reference.of("silentgear:saw")), 0.5, 15)
        ));
        data.put(ConfigListener.ServerConfigs.PERKS, new PerksConfig(perkDefaults()));
        AutoValueConfig defaultAutoValues = new AutoValueConfig();
        data.put(ConfigListener.ServerConfigs.AUTOVALUES, new AutoValueConfig(true,
                Arrays.stream(ReqType.values()).collect(Collectors.toMap(req -> req, r -> false)),
                defaultAutoValues.xpEnabled(),
                new AutoValueConfig.XpAwards(200d,
                        Arrays.stream(AutoItem.EVENTTYPES).collect(Collectors
                                .toMap(event -> event, event -> Map.of(event.autoValueSkill, event == EventType.SMELT || event == EventType.BREW ? 100L : 10L))),
                        Arrays.stream(AutoBlock.EVENTTYPES).collect(Collectors.toMap(event -> event, event -> Map.of(event.autoValueSkill, 10L))),
                        Map.of("woodcutting", 100L),
                        Map.of("farming", 100L),
                        Map.of("excavation", 100L),
                        Stream.of(EventType.DEATH, EventType.SHIELD_BLOCK, EventType.TAMING)
                                .collect(Collectors.toMap(event -> event, event -> Map.of(event.autoValueSkill, 10L)))),
                new AutoValueConfig.Requirements(Map.of(),Map.of(),Map.of(),Map.of(),Map.of(),Map.of(),Map.of()),
                defaultAutoValues.tweaks()
        ));
    }

    private static Map<EventType, Map<String, Double>> serverDefaults() {
        Map<EventType, Map<String, Double>> map = new HashMap<>();
        map.put(EventType.JUMP, Map.of("agility", 2.5));
        map.put(EventType.SPRINT_JUMP, Map.of("agility", 2.5));
        map.put(EventType.CROUCH_JUMP, Map.of("agility", 2.5));
        map.put(EventType.BREATH_CHANGE, Map.of("swimming", 1d));
        map.put(EventType.HEALTH_INCREASE, Map.of("endurance", 100d));
        map.put(EventType.HEALTH_DECREASE, Map.of("endurance", 100d));
        map.put(EventType.SPRINTING, Map.of("agility", 100d));
        map.put(EventType.SUBMERGED, Map.of("swimming", 1d));
        map.put(EventType.SWIMMING, Map.of("swimming", 1d));
        map.put(EventType.DIVING, Map.of("swimming", 1d));
        map.put(EventType.SURFACING, Map.of("swimming", 1d));
        map.put(EventType.SWIM_SPRINTING, Map.of("swimming", 1d));
        return map;
    }
    private static Map<EventType, List<CompoundTag>> perkDefaults() {
        Map<EventType, List<CompoundTag>> defaultSettings = new HashMap<>();
        List<CompoundTag> bodyList = new ArrayList<>();
        //====================BREAK SPEED DEFAULTS========================
        bodyList.add(TagBuilder.start().withString("perk", "pmmo:break_speed").withString(APIUtils.SKILLNAME, "mining").withDouble("pickaxe_dig", 0.005).build());
        bodyList.add(TagBuilder.start().withString("perk", "pmmo:break_speed").withString(APIUtils.SKILLNAME, "excavation").withDouble("shovel_dig", 0.005).build());
        bodyList.add(TagBuilder.start().withString("perk", "pmmo:break_speed").withString(APIUtils.SKILLNAME, "woodcutting").withDouble("axe_dig", 0.005).build());
        bodyList.add(TagBuilder.start().withString("perk", "pmmo:break_speed").withString(APIUtils.SKILLNAME, "farming").withDouble("sword_dig", 0.005).withDouble("hoe_dig", 0.005).withDouble("shears_dig", 0.005).build());
        defaultSettings.put(EventType.BREAK_SPEED, new ArrayList<>(bodyList));
        bodyList.clear();
        //====================SKILL_UP DEFAULTS==========================
        List.of("mining", "building", "excavation", "woodcutting", "farming", "agility", "endurance",
                "combat", "smithing", "swimming", "fishing", "crafting", "taming", "cooking", "alchemy")
                .forEach(skill -> bodyList.add(TagBuilder.start()
                        .withString("perk", "pmmo:fireworks")
                        .withInt(APIUtils.MODULUS, 10)
                        .withString(APIUtils.SKILLNAME, skill).build()));
        bodyList.add(TagBuilder.start().withString("perk", "pmmo:attribute").withString(APIUtils.SKILLNAME, "building")
                .withString(APIUtils.ATTRIBUTE, RegistryUtil.getAttributeId(Attributes.BLOCK_INTERACTION_RANGE).toString())
                .withDouble(APIUtils.PER_LEVEL, 0.005)
                .withDouble(APIUtils.MAX_BOOST, 10d).build());
        bodyList.add(TagBuilder.start().withString("perk", "pmmo:attribute").withString(APIUtils.SKILLNAME, "agility")
                .withString(APIUtils.ATTRIBUTE, RegistryUtil.getAttributeId(Attributes.MOVEMENT_SPEED).toString())
                .withDouble(APIUtils.PER_LEVEL, 0.0000015)
                .withDouble(APIUtils.MAX_BOOST, 1d).build());
        bodyList.add(TagBuilder.start().withString("perk", "pmmo:attribute").withString(APIUtils.SKILLNAME, "endurance")
                .withString(APIUtils.ATTRIBUTE, RegistryUtil.getAttributeId(Attributes.MAX_HEALTH).toString())
                .withDouble(APIUtils.PER_LEVEL, 0.005)
                .withDouble(APIUtils.MAX_BOOST, 20d).build());
        bodyList.add(TagBuilder.start().withString("perk", "pmmo:attribute").withString(APIUtils.SKILLNAME, "combat")
                .withString(APIUtils.ATTRIBUTE, RegistryUtil.getAttributeId(Attributes.ATTACK_DAMAGE).toString())
                .withDouble(APIUtils.PER_LEVEL, 0.0005)
                .withDouble(APIUtils.MAX_BOOST, 3d).build());
        bodyList.add(TagBuilder.start().withString("perk", "pmmo:command").withString(APIUtils.SKILLNAME, "crafting")
                .withString("command", "experience add @s 1 levels").build());
        bodyList.add(TagBuilder.start().withString("perk", "pmmo:command").withString(APIUtils.SKILLNAME, "smithing")
                .withString("command", "experience add @s 1 levels").build());
        bodyList.add(TagBuilder.start().withString("perk", "pmmo:command").withString(APIUtils.SKILLNAME, "cooking")
                .withString("command", "experience add @s 1 levels").build());

        defaultSettings.put(EventType.SKILL_UP, new ArrayList<>(bodyList));
        bodyList.clear();

        //=====================JUMP DEFAULTS=============================
        bodyList.add(TagBuilder.start().withString("perk", "pmmo:jump_boost")
                .withString(APIUtils.SKILLNAME, "agility")
                .withDouble(APIUtils.PER_LEVEL, 0.0005).build());
        defaultSettings.put(EventType.JUMP, new ArrayList<>(bodyList));
        bodyList.clear();

        //=====================JUMP DEFAULTS=============================
        bodyList.add(TagBuilder.start().withString("perk", "pmmo:jump_boost")
                .withString(APIUtils.SKILLNAME, "agility")
                .withDouble(APIUtils.PER_LEVEL, 0.001).build());
        defaultSettings.put(EventType.SPRINT_JUMP, new ArrayList<>(bodyList));
        bodyList.clear();

        //=====================JUMP DEFAULTS=============================
        bodyList.add(TagBuilder.start().withString("perk", "pmmo:jump_boost")
                .withString(APIUtils.SKILLNAME, "agility")
                .withDouble(APIUtils.PER_LEVEL, 0.002)
                .withDouble(APIUtils.MAX_BOOST, 0.5).build());
        defaultSettings.put(EventType.CROUCH_JUMP, new ArrayList<>(bodyList));
        bodyList.clear();

        //=====================SUBMERGED DEFAULTS========================
        bodyList.add(TagBuilder.start().withString("perk", "pmmo:breath").withString(APIUtils.SKILLNAME, "swimming").build());
        bodyList.add(TagBuilder.start().withString("perk", "pmmo:effect").withString(APIUtils.SKILLNAME, "swimming")
                .withString("effect", "minecraft:night_vision")
                .withInt(APIUtils.MAX_BOOST, 160)
                .withInt(APIUtils.MIN_LEVEL, 25).build());
        defaultSettings.put(EventType.SUBMERGED, new ArrayList<>(bodyList));
        bodyList.clear();

        //=====================FROM_IMPACT==============================
        bodyList.add(TagBuilder.start().withString("perk", "pmmo:damage_reduce").withString(APIUtils.SKILLNAME, "agility")
                .withString(APIUtils.DAMAGE_TYPE_IN, "minecraft:fall")
                .withDouble(APIUtils.PER_LEVEL, 0.025).build());
        bodyList.add(TagBuilder.start().withString("perk", "pmmo:damage_reduce").withString(APIUtils.SKILLNAME, "endurance")
                .withString(APIUtils.DAMAGE_TYPE_IN, "minecraft:mob_attack")
                .withDouble(APIUtils.PER_LEVEL, 0.025).build());
        defaultSettings.put(EventType.RECEIVE_DAMAGE, new ArrayList<>(bodyList));
        bodyList.clear();

        //=====================FISHING==============================
        bodyList.add(TagBuilder.start().withString("perk", "pmmo:attribute").withString(APIUtils.SKILLNAME, "fishing")
                .withString(APIUtils.ATTRIBUTE, RegistryUtil.getAttributeId(Attributes.LUCK).toString())
                .withDouble(APIUtils.CHANCE, 0.05d)
                .withDouble(APIUtils.PER_LEVEL, 0.005d)
                .withDouble(APIUtils.MAX_BOOST, 100d).build());
        defaultSettings.put(EventType.FISH, new ArrayList<>(bodyList));
        bodyList.clear();

        //=====================TAMING==============================
        bodyList.add(TagBuilder.start().withString("perk", "pmmo:tame_boost").build());
        defaultSettings.put(EventType.TAMING, new ArrayList<>(bodyList));
        bodyList.clear();

        //=====================DEAL_RANGED_DAMAGE=======================
        bodyList.add(TagBuilder.start().withString("perk", "pmmo:damage_boost").withString(APIUtils.SKILLNAME, "combat")
                .withList("applies_to", StringTag.valueOf("minecraft:bow"), StringTag.valueOf("minecraft:crossbow"), StringTag.valueOf("minecraft:trident")).build());
        defaultSettings.put(EventType.DEAL_DAMAGE, new ArrayList<>(bodyList));
        return defaultSettings;
    }

    @Override
    public String getName() {return "Project MMO Easy Config Generator";}
}
