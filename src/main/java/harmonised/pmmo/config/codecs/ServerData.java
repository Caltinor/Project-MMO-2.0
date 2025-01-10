package harmonised.pmmo.config.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.config.readers.ConfigListener;
import harmonised.pmmo.config.scripting.Functions;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.Reference;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

import java.util.ArrayList;
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
    public ServerData() {this(General.DEFAULT, Levels.DEFAULT, Requirements.DEFAULT, XpGains.DEFAULT, Party.DEFAULT,
            MobScaling.DEFAULT, VeinMiner.DEFAULT);}
    
    @Override
    public ServerData getFromScripting(String param, Map<String, String> value) {
        return new ServerData(
                General.build(param, value, this),
                Levels.build(param, value, this),
                Requirements.build(param, value, this),
                XpGains.build(param, value, this),
                Party.build(param, value, this),
                MobScaling.build(param, value, this),
                VeinMiner.build(param, value, this));
    }

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

    //<editor-fold> General Class
    public record General(
            double creativeReach,
            ResourceLocation salvageBlock,
            boolean treasureEnabled,
            boolean brewingTracked) {
        public static final General DEFAULT = new General(50d, Reference.mc("smithing_table"),true, true);
        
        private static final String CREATIVE_REACH = "creative_reach";
        private static final String SALVAGE_BLOCK = "salvage_block";
        private static final String TREASURE = "treasure_enabled";
        private static final String BREWING = "brewing_tracked";
        //Scripting Builder
        public static General build(String param, Map<String, String> value, ServerData current) {
            double creativeReach = param.equals(CREATIVE_REACH) ? Functions.getDouble(value) : current.general().creativeReach();;
            ResourceLocation salvageBlock = param.equals(SALVAGE_BLOCK) ? Functions.getId(value) : current.general().salvageBlock();
            boolean treasureEnabled = param.equals(TREASURE) ? Functions.getBool(value) : current.general().treasureEnabled();
            boolean brewingTracked = param.equals(BREWING) ? Functions.getBool(value) : current.general().brewingTracked();
            return new General(creativeReach, salvageBlock, treasureEnabled, brewingTracked);
        }
        public static final Codec<General> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.DOUBLE.fieldOf(CREATIVE_REACH).forGetter(General::creativeReach),
                ResourceLocation.CODEC.fieldOf(SALVAGE_BLOCK).forGetter(General::salvageBlock),
                Codec.BOOL.fieldOf(TREASURE).forGetter(General::treasureEnabled),
                Codec.BOOL.fieldOf(BREWING).forGetter(General::brewingTracked)
        ).apply(instance, General::new));
    }
    //</editor-fold>
    //<editor-fold> Levels Class
    public record Levels(
            long maxLevel,
            List<Long> staticLevels,
            double lossOnDeath,
            boolean loseOnlyExcess,
            double globalModifier,
            Map<String, Double> skillModifiers,
            long xpMin,
            double xpBase,
            double perLevel
    ) {
        public static final Levels DEFAULT = new Levels(
                1523,
                List.of(-1L),
                0.05,
                true,
                1.0,
                Map.of("example_skill", 1.0),
                200,
                1.025,
                1.1);

        private static final String MAX_LEVEL = "max_level";
        private static final String STATIC_LVLS = "static_levels";
        private static final String LOSS_DEATH = "loss_on_death";
        private static final String LOSE_EXCESS = "lose_only_excess";
        private static final String GLOBAL_MODIFIER = "global_modifier";
        private static final String SKILL_MODIFIER = "skill_modifiers";
        private static final String XP_MIN = "xp_min";
        private static final String XP_BASE = "xp_base";
        private static final String PER_LEVEL = "per_level";

        //Scripting Builder
        public static Levels build(String param, Map<String, String> value, ServerData current) {
            long maxLevel = param.equals(MAX_LEVEL) ? Functions.getLong(value) : current.levels().maxLevel();
            List<Long> staticLevels = param.equals(STATIC_LVLS) ? Arrays.stream(value.getOrDefault("value", "-1").split(",")).map(Long::parseLong).toList() : current.levels().staticLevels();
            double lossOnDeath = param.equals(LOSS_DEATH) ? Functions.getDouble(value) : current.levels().lossOnDeath();
            boolean loseOnlyExcess = param.equals(LOSE_EXCESS) ? Functions.getBool(value) : current.levels().loseOnlyExcess();
            double globalModifier = param.equals(GLOBAL_MODIFIER) ? Functions.getDouble(value) : current.levels().globalModifier();
            Map<String, Double> skillModifiers = param.equals(SKILL_MODIFIER) ? Functions.doubleMap(value.getOrDefault("value", "")) : current.levels().skillModifiers();
            long xpMin = param.equals(XP_MIN) ? Functions.getLong(value) : current.levels().xpMin();
            double xpBase = param.equals(XP_BASE) ? Functions.getDouble(value) : current.levels().xpBase();;
            double perLevel = param.equals(PER_LEVEL) ? Functions.getDouble(value) : current.levels().perLevel();

            return new Levels(maxLevel, staticLevels, lossOnDeath, loseOnlyExcess, globalModifier, skillModifiers, xpMin, xpBase, perLevel);
        }
        public static final Codec<Levels> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.LONG.fieldOf(MAX_LEVEL).forGetter(Levels::maxLevel),
                Codec.LONG.listOf().fieldOf(STATIC_LVLS).forGetter(Levels::staticLevels),
                Codec.DOUBLE.fieldOf(LOSS_DEATH).forGetter(Levels::lossOnDeath),
                Codec.BOOL.fieldOf(LOSE_EXCESS).forGetter(Levels::loseOnlyExcess),
                Codec.DOUBLE.fieldOf(GLOBAL_MODIFIER).forGetter(Levels::globalModifier),
                CodecTypes.DOUBLE_CODEC.fieldOf(SKILL_MODIFIER).forGetter(Levels::skillModifiers),
                Codec.LONG.fieldOf(XP_MIN).forGetter(Levels::xpMin),
                Codec.DOUBLE.fieldOf(XP_BASE).forGetter(Levels::xpBase),
                Codec.DOUBLE.fieldOf(PER_LEVEL).forGetter(Levels::perLevel)
        ).apply(instance, Levels::new));
    }
    //</editor-fold>
    //<editor-fold> Reqs Class
    public record Requirements(Map<ReqType, Boolean> enabled) {
        public static final Requirements DEFAULT = new Requirements(Arrays.stream(ReqType.values()).collect(Collectors.toMap(a -> a, a -> true)));

        private static final String DISABLE = "disable_req";
        public static Requirements build(String param, Map<String, String> value, ServerData current) {
            Map<ReqType, Boolean> enables = new HashMap<>(current.requirements.enabled());
            if (param.equals(DISABLE)) {
                ReqType type = ReqType.byName(value.get("value").toUpperCase());
                if (type == null)
                    MsLoggy.ERROR.log(MsLoggy.LOG_CODE.DATA, "Server config requirement script provided invalid ReqType of %s.  setting skipped", value.get("value"));
                else
                    enables.put(type, false);
            }
            return new Requirements(enables);
        }
        public static final Codec<Requirements> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.simpleMap(ReqType.CODEC, Codec.BOOL, StringRepresentable.keys(ReqType.values())).codec()
                        .fieldOf("requirement_enabled").forGetter(Requirements::enabled)
        ).apply(instance, Requirements::new));
        public boolean isEnabled(ReqType type) {return enabled().getOrDefault(type, true);}
    }
    //</editor-fold>
    //<editor-fold> XpGains Class
    public record XpGains(
            double reusePenalty,
            boolean perksPlusConfig,
            Map<EventType, Map<String, Double>> playerEvents,
            Map<EventType, Map<String, Map<String, Long>>> damageXp) {
        public static final XpGains DEFAULT = new XpGains(
                0.0,
                false,
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
                ));

        private static final String REUSE = "reuse_penalty";
        private static final String PERKSPLUS = "perks_plus_config";
        private static final String PLAYERACTIONS = "player_actions";
        private static final String DAMAGE_DEALT = "damage_dealt";
        private static final String DAMAGE_RECEIVED = "damage_received";
        private static final String EVENT = "event";
        private static final String DAMAGE_TYPE = "type";

        public static XpGains build(String param, Map<String, String> value, ServerData current) {
            double reusePenalty = param.equals(REUSE) ? Functions.getDouble(value) : current.xpGains().reusePenalty();
            boolean perksPlusConfig = param.equals(PERKSPLUS) ? Functions.getBool(value) : current.xpGains().perksPlusConfig();
            Map<EventType, Map<String, Double>> playerEvents = new HashMap<>(current.xpGains().playerEvents());
            if (param.equals(PLAYERACTIONS) && value.containsKey(EVENT)) {
                EventType type = EventType.byName(value.get(EVENT).toUpperCase());
                if (type == null)
                    MsLoggy.ERROR.log(MsLoggy.LOG_CODE.DATA, "Server config xp player action script provided invalid EventType of %s.  setting skipped", value.get(EVENT));
                else
                    playerEvents.put(type, Functions.doubleMap(value.getOrDefault("value", "")));
            }
            Map<EventType, Map<String, Map<String, Long>>> damageXp = new HashMap<>(current.xpGains().damageXp());
            if ((param.equals(DAMAGE_DEALT) || param.equals(DAMAGE_RECEIVED)) && value.containsKey(DAMAGE_TYPE)) {
                EventType type = switch (param) {case DAMAGE_DEALT -> EventType.DEAL_DAMAGE; case DAMAGE_RECEIVED -> EventType.RECEIVE_DAMAGE; default -> null;};
                String damageType = value.get(DAMAGE_TYPE);
                damageXp.computeIfPresent(type, (t, map) -> new HashMap<>(map)); //make the existing map mutable, if present
                damageXp.computeIfAbsent(type, t -> new HashMap<>()).put(damageType, Functions.mapValue(value.getOrDefault("value", "")));
            }
            return new XpGains(reusePenalty, perksPlusConfig, playerEvents, damageXp);
        }

        public static final Codec<XpGains> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.DOUBLE.fieldOf(REUSE).forGetter(XpGains::reusePenalty),
                Codec.BOOL.fieldOf(PERKSPLUS).forGetter(XpGains::perksPlusConfig),
                Codec.unboundedMap(EventType.CODEC, CodecTypes.DOUBLE_CODEC).fieldOf(PLAYERACTIONS).forGetter(XpGains::playerEvents),
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
    //</editor-fold>
    //<editor-fold> Party Class
    public record Party(int range, Map<String, Double> bonus) {
        public static final Party DEFAULT = new Party(50, Map.of("combat", 1.05, "endurance", 1.1));

        private static final String PARTY_RANGE = "party_range";
        private static final String PARTY_BONUS = "party_bonus";
        public static Party build(String param, Map<String, String> value, ServerData current) {
            int range = param.equals(PARTY_RANGE) ? Functions.getInt(value) : current.party().range();
            Map<String, Double> bonus = param.equals(PARTY_BONUS)
                    ? Functions.doubleMap(value.getOrDefault("value", ""))
                    : current.party().bonus();

            return new Party(range, bonus);
        }

        public static final Codec<Party> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf(PARTY_RANGE).forGetter(Party::range),
                CodecTypes.DOUBLE_CODEC.fieldOf(PARTY_BONUS).forGetter(Party::bonus)
        ).apply(instance, Party::new));
    }
    //</editor-fold>
    //<editor-fold> MobScaling Class
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
        public static final MobScaling DEFAULT = new MobScaling(
                false,
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
                ));

        private static final String ENABLED = "enabled";
        private static final String AOE = "scaling_aoe";
        private static final String BASE_LVL = "base_level";
        private static final String BOSS_SCALE = "boss_scaling";
        private static final String USE_EXPONENT = "use_exponential_formula";
        private static final String PER_LEVEL = "per_level";
        private static final String POWER_BASE = "power_base";
        private static final String RATIOS = "ratios";
        private static final String ATTRIBUTE_ID = "attribute";

        public static MobScaling build(String param, Map<String, String> value, ServerData current) {
            boolean enabled = param.equals("mob_scaling_"+ENABLED) ? Functions.getBool(value) : current.mobScaling().enabled();
            int aoe = param.equals(AOE) ? Functions.getInt(value) : current.mobScaling().aoe();
            long base = param.equals(BASE_LVL) ? Functions.getLong(value) : current.mobScaling().baseLevel();
            double boss = param.equals(BOSS_SCALE) ? Functions.getDouble(value) : current.mobScaling().bossScaling();
            boolean exponent = param.equals(USE_EXPONENT) ? Functions.getBool(value) : current.mobScaling().useExponential();
            double perLevel = param.equals(PER_LEVEL) ? Functions.getDouble(value) : current.mobScaling().perLevel();
            double powerBase = param.equals(POWER_BASE) ? Functions.getDouble(value) : current.mobScaling().powerBase();
            Map<ResourceLocation, Map<String, Double>> ratios = new HashMap<>(current.mobScaling().ratios());
            if (param.equals(RATIOS)) {
                ResourceLocation attributeID = Reference.of(value.getOrDefault(ATTRIBUTE_ID, "mob_scale_attribute_id:missing"));
                Map<String, Double> ratio = Functions.doubleMap(value.getOrDefault("value", ""));
                ratios.put(attributeID, ratio);
            }

            return new MobScaling(enabled, aoe, base, boss, exponent, perLevel, powerBase, ratios);
        }

        public static final Codec<MobScaling> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.fieldOf(ENABLED).forGetter(MobScaling::enabled),
                Codec.INT.fieldOf(AOE).forGetter(MobScaling::aoe),
                Codec.LONG.fieldOf(BASE_LVL).forGetter(MobScaling::baseLevel),
                Codec.DOUBLE.fieldOf(BOSS_SCALE).forGetter(MobScaling::bossScaling),
                Codec.BOOL.fieldOf(USE_EXPONENT).forGetter(MobScaling::useExponential),
                Codec.DOUBLE.fieldOf(PER_LEVEL).forGetter(MobScaling::perLevel),
                Codec.DOUBLE.fieldOf(POWER_BASE).forGetter(MobScaling::powerBase),
                Codec.unboundedMap(ResourceLocation.CODEC, CodecTypes.DOUBLE_CODEC).fieldOf(RATIOS).forGetter(MobScaling::ratios)
        ).apply(instance, MobScaling::new));
    }
    //</editor-fold>
    //<editor-fold> VeinMiner Class
    public record VeinMiner(
            boolean enabled,
            boolean requireSettings,
            int defaultConsume,
            double chargeModifier,
            List<ResourceLocation> blacklist

    ) {
        public static final VeinMiner DEFAULT = new VeinMiner(
                true,
                false,
                1,
                1.0,
                List.of(Reference.of("silentgear:saw")));

        private static final String ENABLED = "enabled";
        private static final String REQUIRE = "require_settings";
        private static final String DEFAULT_CONSUME = "default_consume";
        private static final String CHARGE_MODIFIER = "charge_modifier";
        private static final String BLACKLIST = "blacklist";

        public static VeinMiner build(String param, Map<String, String> value, ServerData current) {
            boolean enabled = param.equals("vein_"+ENABLED) ? Functions.getBool(value) : current.veinMiner().enabled();
            boolean require = param.equals(REQUIRE) ? Functions.getBool(value) : current.veinMiner().requireSettings();
            int defaultConsume = param.equals(DEFAULT_CONSUME) ? Functions.getInt(value) : current.veinMiner().defaultConsume();
            double chargeMod = param.equals(CHARGE_MODIFIER) ? Functions.getDouble(value) : current.veinMiner().chargeModifier();
            List<ResourceLocation> blacklist = param.equals(BLACKLIST)
                    ? Arrays.stream(value.getOrDefault("value", "").split(",")).map(Reference::of).toList()
                    : current.veinMiner().blacklist();

            return new VeinMiner(enabled, require, defaultConsume, chargeMod, blacklist);
        }

        public static final Codec<VeinMiner> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.fieldOf(ENABLED).forGetter(VeinMiner::enabled),
                Codec.BOOL.fieldOf(REQUIRE).forGetter(VeinMiner::requireSettings),
                Codec.INT.fieldOf(DEFAULT_CONSUME).forGetter(VeinMiner::defaultConsume),
                Codec.DOUBLE.fieldOf(CHARGE_MODIFIER).forGetter(VeinMiner::chargeModifier),
                ResourceLocation.CODEC.listOf().fieldOf(BLACKLIST).forGetter(VeinMiner::blacklist)
        ).apply(instance, VeinMiner::new));

        public class VeinBuilder {
            boolean enabled = true, require = false;
            int defaultConsume = 1;
            double modifier = 1.0;
            List<ResourceLocation> blacklist = new ArrayList<>();

            public VeinBuilder() {}
            public VeinBuilder(VeinMiner from) {
                enabled = from.enabled;
                require = from.requireSettings;
                defaultConsume = from.defaultConsume;
                modifier = from.chargeModifier;
                blacklist = new ArrayList<>(from.blacklist);
            }
            public VeinBuilder disable() {enabled = false; return this;}
            public VeinBuilder require() {require = true; return this;}
            public VeinBuilder setDefaultConsume(int i) {defaultConsume = i; return this;}
            public VeinBuilder setModifier(double d) {modifier = d; return this;}
            public VeinBuilder addBlacklist(ResourceLocation...ids) {blacklist.addAll(Arrays.asList(ids)); return this;}
            public VeinMiner build() {return new VeinMiner(enabled, require, defaultConsume, modifier, blacklist);}
        }
    }
    //</editor-fold>

    @Override
    public ServerData combine(ServerData two) {return two;}

    @Override
    public boolean isUnconfigured() {return false;}
}
