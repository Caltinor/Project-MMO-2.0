package harmonised.pmmo.config.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.config.readers.ConfigListener;
import harmonised.pmmo.util.Reference;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record ServerData(
        General general,
        Levels levels,
        Requirements requirements,
        XpGains xpGains,
        Party party,
        MobScaling mobScaling,
        VeinMiner veinMiner
) implements ConfigData<ServerData> {
    public ServerData() {this(new General(), new Levels(), new Requirements(), new XpGains(), new Party(),
            new MobScaling(), new VeinMiner());}

    public static final MapCodec<ServerData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            General.CODEC.fieldOf("general").forGetter(ServerData::general),
            Levels.CODEC.fieldOf("levels").forGetter(ServerData::levels),
            Requirements.CODEC.fieldOf("requirements").forGetter(ServerData::requirements),
            XpGains.CODEC.fieldOf("xp_gains").forGetter(ServerData::xpGains),
            Party.CODEC.fieldOf("party").forGetter(ServerData::party),
            MobScaling.CODEC.fieldOf("mob_scaling").forGetter(ServerData::mobScaling),
            VeinMiner.CODEC.fieldOf("vein_miner").forGetter(ServerData::veinMiner)
    ).apply(instance, ServerData::new));

    @Override
    public MapCodec<ServerData> getCodec() {return CODEC;}

    @Override
    public ConfigListener.ServerConfigs getType() {return ConfigListener.ServerConfigs.SERVER;}

    public record General(
            double creativeReach,
            ResourceLocation salvageBlock,
            boolean treasureEnabled,
            boolean brewingTracked) {
        public General() {this(
                50d,
                Reference.mc("smithing_table"),
                true,
                true);}
        public static final Codec<General> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.DOUBLE.fieldOf("creative_reach").forGetter(General::creativeReach),
                ResourceLocation.CODEC.fieldOf("salvage_block").forGetter(General::salvageBlock),
                Codec.BOOL.fieldOf("treasure_enabled").forGetter(General::treasureEnabled),
                Codec.BOOL.fieldOf("brewing_tracked").forGetter(General::brewingTracked)
        ).apply(instance, General::new));
    }
    public record Levels(
            long maxLevel,
            List<Long> staticLevels,
            double lossOnDeath,
            boolean loseOnDeath,
            boolean loseOnlyExcess,
            double globalModifier,
            Map<String, Double> skillModifiers,
            long xpMin,
            double xpBase,
            double perLevel
    ) {
        public Levels() {this(
                1523,
                List.of(-1L),
                0.05,
                false,
                true,
                1.0,
                Map.of("example_skill", 1.0),
                200,
                1.025,
                1.1);}
        public static final Codec<Levels> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.LONG.fieldOf("max_level").forGetter(Levels::maxLevel),
                Codec.LONG.listOf().fieldOf("static_levels").forGetter(Levels::staticLevels),
                Codec.DOUBLE.fieldOf("loss_on_death").forGetter(Levels::lossOnDeath),
                Codec.BOOL.fieldOf("lose_on_death").forGetter(Levels::loseOnDeath),
                Codec.BOOL.fieldOf("lose_only_excess").forGetter(Levels::loseOnlyExcess),
                Codec.DOUBLE.fieldOf("global_modifier").forGetter(Levels::globalModifier),
                CodecTypes.DOUBLE_CODEC.fieldOf("skill_modifiers").forGetter(Levels::skillModifiers),
                Codec.LONG.fieldOf("xp_min").forGetter(Levels::xpMin),
                Codec.DOUBLE.fieldOf("xp_base").forGetter(Levels::xpBase),
                Codec.DOUBLE.fieldOf("per_level").forGetter(Levels::perLevel)
        ).apply(instance, Levels::new));
    }
    public record Requirements(Map<ReqType, Boolean> enabled) {
        public Requirements() {this(Arrays.stream(ReqType.values()).collect(Collectors.toMap(a -> a, a -> true)));}
        public static final Codec<Requirements> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.simpleMap(ReqType.CODEC, Codec.BOOL, StringRepresentable.keys(ReqType.values())).codec()
                        .fieldOf("requirement_enabled").forGetter(Requirements::enabled)
        ).apply(instance, Requirements::new));
        public boolean isEnabled(ReqType type) {return enabled().getOrDefault(type, true);}
    }
    public record XpGains(
            double reusePenalty,
            boolean perksPlusConfig,
            Map<EventType, Map<String, Double>> playerEvents,
            Map<EventType, Map<String, Map<String, Long>>> damageXp) {
        public XpGains() {this(0.0, false,
                defaults(), Map.of(
                        EventType.DEAL_DAMAGE, Map.of(
                                "minecraft:generic_kill", Map.of("combat", 1L),
                                "minecraft:player_attack", Map.of("combat", 1L),
                                "#minecraft:is_projectile", Map.of("archery", 1L),
                                "#pmmo:magic", Map.of("magic", 15L),
                                "#pmmo:gun", Map.of("gunslinging", 1L)
                        ),
                        EventType.RECEIVE_DAMAGE, Map.of(
                                "minecraft:generic_kill", Map.of("endurance", 1L),
                                "#pmmo:environment", Map.of("endurance", 10L),
                                "#pmmo:impact", Map.of("endurance", 15L),
                                "#pmmo:magic", Map.of("magic", 15L),
                                "#minecraft:is_projectile", Map.of("endurance", 15L))
                ));}
        public static final Codec<XpGains> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.DOUBLE.fieldOf("reuse_penalty").forGetter(XpGains::reusePenalty),
                Codec.BOOL.fieldOf("perks_plus_config").forGetter(XpGains::perksPlusConfig),
                Codec.unboundedMap(EventType.CODEC, CodecTypes.DOUBLE_CODEC).fieldOf("player_actions").forGetter(XpGains::playerEvents),
                Codec.unboundedMap(EventType.CODEC, Codec.unboundedMap(Codec.STRING, CodecTypes.LONG_CODEC)).fieldOf("damage").forGetter(XpGains::damageXp)
        ).apply(instance, XpGains::new));

        private static Map<EventType, Map<String, Double>> defaults() {
            Map<EventType, Map<String, Double>> map = new HashMap<>();
            map.put(EventType.JUMP, Map.of("agility", 2.5));
            map.put(EventType.SPRINT_JUMP, Map.of("agility", 2.5));
            map.put(EventType.CROUCH_JUMP, Map.of("agility", 2.5));
            map.put(EventType.BREATH_CHANGE, Map.of("endurance", 1d));
            map.put(EventType.HEALTH_INCREASE, Map.of("endurance", 1d));
            map.put(EventType.HEALTH_DECREASE, Map.of("endurance", 1d));
            map.put(EventType.SPRINTING, Map.of("agility", 2d));
            map.put(EventType.SUBMERGED, Map.of("swimming", 1d));
            map.put(EventType.SWIMMING, Map.of("swimming", 1d));
            map.put(EventType.DIVING, Map.of("swimming", 1d));
            map.put(EventType.SURFACING, Map.of("swimming", 1d));
            map.put(EventType.SWIM_SPRINTING, Map.of("swimming", 1d));
            return map;
        }

        public Map<String, Map<String, Long>> receivedDamage() {return this.damageXp().getOrDefault(EventType.RECEIVE_DAMAGE, new HashMap<>());}
        public Map<String, Map<String, Long>> dealtDamage() {return this.damageXp().getOrDefault(EventType.DEAL_DAMAGE, new HashMap<>());}
        public Map<String, Double> playerXp(EventType type) {return this.playerEvents().getOrDefault(type, new HashMap<>());}
    }
    public record Party(int range, Map<String, Double> bonus) {
        public Party() {this(50, Map.of("combat", 1.05, "endurance", 1.1));}
        public static final Codec<Party> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("party_range").forGetter(Party::range),
                CodecTypes.DOUBLE_CODEC.fieldOf("party_bonus").forGetter(Party::bonus)
        ).apply(instance, Party::new));
    }
    public record MobScaling(
            boolean enabled,
            int aoe,
            long baseLevel,
            Double bossScaling,
            boolean useExponential,
            double perLevel,
            double powerBase,
            Map<ResourceLocation, Map<String, Double>> ratios
    ) {
        public MobScaling() {this(
                true,
                150,
                0,
                1.1,
                true,
                1.0,
                1.104088404342588d,
                Map.of(
                        Reference.mc("generic.max_health"), Map.of("combat", 0.001),
                        Reference.mc("generic.attack_damage"), Map.of("combat", 0.0001),
                        Reference.mc("generic.movement_speed"), Map.of("combat", 0.000001)
                ));}

        public static final Codec<MobScaling> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.fieldOf("enabled").forGetter(MobScaling::enabled),
                Codec.INT.fieldOf("scaling_aoe").forGetter(MobScaling::aoe),
                Codec.LONG.fieldOf("base_level").forGetter(MobScaling::baseLevel),
                Codec.DOUBLE.fieldOf("boss_scaling").forGetter(MobScaling::bossScaling),
                Codec.BOOL.fieldOf("use_exponential_formula").forGetter(MobScaling::useExponential),
                Codec.DOUBLE.fieldOf("per_level").forGetter(MobScaling::perLevel),
                Codec.DOUBLE.fieldOf("power_base").forGetter(MobScaling::powerBase),
                Codec.unboundedMap(ResourceLocation.CODEC, CodecTypes.DOUBLE_CODEC).fieldOf("ratios").forGetter(MobScaling::ratios)
        ).apply(instance, MobScaling::new));
    }
    public record VeinMiner(
            boolean enabled,
            boolean requireSettings,
            int defaultConsume,
            double chargeModifier,
            List<ResourceLocation> blacklist

    ) {
        public VeinMiner() {this(
                true,
                false,
                1,
                1.0,
                List.of(Reference.of("silentgear:saw")));}

        public static final Codec<VeinMiner> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.fieldOf("vein_enabled").forGetter(VeinMiner::enabled),
                Codec.BOOL.fieldOf("require_settings").forGetter(VeinMiner::requireSettings),
                Codec.INT.fieldOf("default_consume").forGetter(VeinMiner::defaultConsume),
                Codec.DOUBLE.fieldOf("charge_modifier").forGetter(VeinMiner::chargeModifier),
                ResourceLocation.CODEC.listOf().fieldOf("blacklist").forGetter(VeinMiner::blacklist)
        ).apply(instance, VeinMiner::new));
    }

    @Override
    public ServerData combine(ServerData two) {return two;}

    @Override
    public boolean isUnconfigured() {return false;}
}
