package harmonised.pmmo.features.mobscaling;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.codecs.LocationData;
import harmonised.pmmo.config.codecs.MobModifier;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.Reference;
import harmonised.pmmo.util.RegistryUtil;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent.FinalizeSpawn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid=Reference.MOD_ID, bus=Mod.EventBusSubscriber.Bus.FORGE)
public class MobAttributeHandler {
	private static final UUID ADDITION_MODIFIER_ID = UUID.fromString("c95a6e8c-a1c3-4177-9118-1e2cf49b7fcd");
	private static final UUID MULTIPLY_BASE_MODIFIER_ID = UUID.fromString("c95a6e8c-a1c3-4177-9118-1e2cf49b7fce");
	private static final UUID MULTIPLY_TOTAL_MODIFIER_ID = UUID.fromString("c95a6e8c-a1c3-4177-9118-1e2cf49b7fcf");
	/**Used for balancing purposes to ensure configurations do not exceed known limits.*/
	private static final Map<ResourceLocation, Float> CAPS = Map.of(
		new ResourceLocation("generic.max_health"), 1024f,
		new ResourceLocation("generic.movement_speed"), 1.5f,
		new ResourceLocation("generic.attack_damage"), 2048f,
		new ResourceLocation("zombie.spawn_reinforcements"), 1f
	);

	@SubscribeEvent
	public static void onBossAdd(EntityJoinLevelEvent event) {
		if (!Config.MOB_SCALING_ENABLED.get())
			return;
		if (event.getEntity().getType().is(Tags.EntityTypes.BOSSES)
				&& event.getEntity() instanceof LivingEntity entity
				&& event.getLevel() instanceof ServerLevel level) {
			handle(entity, level
					, new Vec3(entity.getX(), entity.getY(), entity.getZ())
					, level.getDifficulty().getId());
		}
	}
	@SuppressWarnings("resource")
	@SubscribeEvent
	public static void onMobSpawn(FinalizeSpawn event) {
	    if (!Config.MOB_SCALING_ENABLED.get())
	        return;
		if (event.getEntity().getType().is(Reference.MOB_TAG)) {
			handle(event.getEntity(), event.getLevel().getLevel()
					, new Vec3(event.getX(), event.getY(), event.getZ())
					, event.getLevel().getDifficulty().getId());
		}
	}

	private static void handle(LivingEntity entity, ServerLevel level, Vec3 spawnPos, int diffScale) {
		int range = Config.MOB_SCALING_AOE.get();
		TargetingConditions targetCondition = TargetingConditions.forNonCombat().ignoreInvisibilityTesting().ignoreLineOfSight().range(Math.pow(range, 2)*3);
		List<Player> nearbyPlayers = level.getNearbyPlayers(targetCondition, entity, AABB.ofSize(spawnPos, range, range, range));
		MsLoggy.DEBUG.log(LOG_CODE.FEATURE, "NearbyPlayers on Spawn: "+MsLoggy.listToString(nearbyPlayers));

		//get values for biome and dimension scaling
		Core core = Core.get(level.getLevel());
		LocationData dimData = core.getLoader().DIMENSION_LOADER.getData(level.getLevel().dimension().location());
		LocationData bioData = core.getLoader().BIOME_LOADER.getData(RegistryUtil.getId(level.getBiome(entity.getOnPos())));

		var dimMods = dimData.mobModifiers().getOrDefault(RegistryUtil.getId(entity), new ArrayList<>(0));
		var bioMods = bioData.mobModifiers().getOrDefault(RegistryUtil.getId(entity), new ArrayList<>(0));
		var globalDimMods = dimData.globalMobModifiers();
		var globalBioMods = bioData.globalMobModifiers();
		var mergedModifiers = mergeModifiers(Stream.of(dimMods, bioMods, globalDimMods, globalBioMods).collect(Collectors.toList()));

		final float bossMultiplier = entity.getType().is(Tags.EntityTypes.BOSSES) ? Config.BOSS_SCALING_RATIO.get().floatValue() : 1f;

		computeAndApplyModifiers(entity, mergedModifiers, nearbyPlayers, diffScale, bossMultiplier);

		//reset health to max after applying modifiers
		entity.setHealth(entity.getMaxHealth());
	}

	private static List<MobModifier> mergeModifiers(List<List<MobModifier>> modifiers) {
		return modifiers.stream().flatMap(List::stream).collect(Collectors.toList());
	}

	/**Computes & applies all the modifiers to the entity.
	 *
	 * @param entity the entity to apply the modifiers to
	 * @param modifiers the list of modifiers to apply
	 */
	private static void computeAndApplyModifiers(LivingEntity entity, List<MobModifier> modifiers, List<Player> nearbyPlayers, int diffScale , float bossMultiplier) {
		var additionModifiers = new ArrayList<MobModifier>();
		var multiplyBaseModifiers = new ArrayList<MobModifier>();
		var multiplyTotalModifiers = new ArrayList<MobModifier>();
		modifiers.forEach(mod -> {
			switch (mod.operation()) {
			case ADDITION -> additionModifiers.add(mod);
			case MULTIPLY_BASE -> multiplyBaseModifiers.add(mod);
			case MULTIPLY_TOTAL -> multiplyTotalModifiers.add(mod);
			}
		});
		modifiers.clear();

		var collapsedAdditionModifiers = collapseModifiers(additionModifiers);
		var collapsedMultiplyBaseModifiers = collapseModifiers(multiplyBaseModifiers);
		var collapsedMultiplyTotalModifiers = collapseModifiers(multiplyTotalModifiers);

		var mobScalingMultipliers = Config.MOB_SCALING.get();
		applyMobScaling(collapsedAdditionModifiers, entity, mobScalingMultipliers, nearbyPlayers, diffScale);
		applyBossMultiplier(collapsedAdditionModifiers, bossMultiplier);

		applyModifiers(entity, ADDITION_MODIFIER_ID, AttributeModifier.Operation.ADDITION, collapsedAdditionModifiers);
		applyModifiers(entity, MULTIPLY_BASE_MODIFIER_ID, AttributeModifier.Operation.MULTIPLY_BASE, collapsedMultiplyBaseModifiers);
		applyModifiers(entity, MULTIPLY_TOTAL_MODIFIER_ID, AttributeModifier.Operation.MULTIPLY_TOTAL, collapsedMultiplyTotalModifiers);
	}

	/**Applies the modifiers to the entity.
	 *
	 * @param entity the entity to apply the modifiers to
	 * @param modifierId the id of the modifier to apply
	 * @param operation the operation to apply
	 * @param collapsedModifiers the map of modifiers to apply
	 */
	private static void applyModifiers(LivingEntity entity, UUID modifierId, AttributeModifier.Operation operation, Map<String, Double> collapsedModifiers) {
		collapsedModifiers.forEach((attributeStr, amount) -> {
			if (Math.abs(amount) < 0.0001f) return;
			var attributeLocation = new ResourceLocation(attributeStr);
			var attribute = ForgeRegistries.ATTRIBUTES.getValue(attributeLocation);
			if (attribute == null) return;
			var attributeInstance = entity.getAttribute(attribute);
			if (attributeInstance == null) return;
			var modifier = new AttributeModifier(modifierId, "Boost to Mob Scaling", amount, operation);
			attributeInstance.removeModifier(modifierId);
			attributeInstance.addPermanentModifier(modifier);
			MsLoggy.DEBUG.log(LOG_CODE.FEATURE, "Entity={} Attribute={} value={} operation={}", entity.getDisplayName().getString(), attributeStr, amount, operation);
		});
	}

	/**Collapses the list of modifiers into a single map
	 * with the attribute as the key and the sum of the
	 * modifiers as the value.
	 *
	 * @param modifiers the list of modifiers to collapse
	 * @return the collapsed map of modifiers
	 */
	private static HashMap<String, Double> collapseModifiers(List<MobModifier> modifiers) {
		var modifierMap = new HashMap<String, Double>();
		for (MobModifier mod : modifiers) {
			if (modifierMap.containsKey(mod.attribute())) {
				modifierMap.put(mod.attribute(), modifierMap.get(mod.attribute()) + mod.amount());
			} else {
				modifierMap.put(mod.attribute(), mod.amount());
			}
		}
		return modifierMap;
	}

	private static void applyMobScaling(HashMap<String, Double> collapsedModifiers, LivingEntity entity, Map<String, Map<String, Double>> config, List<Player> nearbyPlayers, int difficultyScale) {
		config.forEach((attributeName, configMap) -> {
			var attributeScalingConfig = config.getOrDefault(attributeName, new HashMap<>());
			if (attributeScalingConfig.isEmpty()) return;
			var resourceLocation = new ResourceLocation(attributeName);
			var attribute = ForgeRegistries.ATTRIBUTES.getValue(resourceLocation);
			if (attribute == null) return;
			var attributeInstance = entity.getAttribute(attribute);
			if (attributeInstance == null) return;

			var baseValue = baseValue(entity, resourceLocation, attributeInstance);
			var cap = CAPS.getOrDefault(resourceLocation, Float.MAX_VALUE);
			var bonus = getBonus(nearbyPlayers, attributeScalingConfig, difficultyScale, baseValue, cap);
			if (Math.abs(bonus) < 0.0001f) return;

			if (collapsedModifiers.containsKey(attributeName)) {
				collapsedModifiers.put(attributeName, collapsedModifiers.get(attributeName) + bonus);
			} else {
				collapsedModifiers.put(attributeName, (double) bonus);
			}
		});
	}

	private static void applyBossMultiplier(HashMap<String, Double> collapsedModifiers, float multiplier) {
		if (Math.abs(multiplier - 1f) < 0.0001f) return;
		collapsedModifiers.replaceAll((attribute, value) -> value * multiplier);
	}

	
	/**This function serves as an intermediary for balance
	 * purposes.  It seeks to intercept certain values which
	 * for one reason or another do not provide a balanced
	 * value from the standard methods.
	 * 
	 * @param entity
	 * @param id
	 * @param ai
	 * @return
	 */
	private static double baseValue(LivingEntity entity, ResourceLocation id, AttributeInstance ai) {
		return switch (id.toString()) {
		case "minecraft:generic.attack_damage" -> 1f;
		case "minecraft:generic.movement_speed" -> entity.getSpeed();
		default -> ai.getBaseValue();};
	}
	
	/**Calculates the bonus amount to be applied to this
	 * attribute based on configuration settings from
	 * toml configs.  This precedes settings applied by
	 * biomes or dimensions.
	 * 
	 * @param nearbyPlayers all players in range to affect scaling
	 * @param config the setting for this attribute scaling
	 * @param scale the gamemode difficulty
	 * @param ogValue the original value of this attribute
	 * @param cap the max value for this attribute
	 * @return the value to modify the attribute by
	 */
	private static float getBonus(List<Player> nearbyPlayers, Map<String, Double> config, int scale, double ogValue, float cap) {
		//summate all levels from the configured skills for each nearby player
		Map<String, Integer> totalLevel = new HashMap<>();
		//pass through case for dim/biome bonuses to still apply.
		if (nearbyPlayers.isEmpty()) return 0f;
		nearbyPlayers.forEach(player -> {
			config.keySet().stream().collect(Collectors.toMap(str -> str, str -> Core.get(player.level()).getData().getPlayerSkillLevel(str, player.getUUID())))
					.forEach((skill, level) -> {
				totalLevel.merge(skill, level, Integer::sum);
			});
		});
		//get the average level for each skill and calculate its modifier from the configuration formula
		float outValue = 0f;
		for (Map.Entry<String, Double> configEntry : config.entrySet()) {
			int averageLevel = totalLevel.getOrDefault(configEntry.getKey(), 0)/nearbyPlayers.size();
			if (averageLevel < Config.MOB_SCALING_BASE_LEVEL.get()) continue;
			outValue += Config.MOB_USE_EXPONENTIAL_FORMULA.get()
					? Math.pow(Config.MOB_EXPONENTIAL_POWER_BASE.get(), (Config.MOB_EXPONENTIAL_LEVEL_MOD.get() * (averageLevel - Config.MOB_SCALING_BASE_LEVEL.get())))
					: (averageLevel - Config.MOB_SCALING_BASE_LEVEL.get()) * Config.MOB_LINEAR_PER_LEVEL.get();
			outValue *= configEntry.getValue();
		}
		MsLoggy.DEBUG.log(LOG_CODE.FEATURE, "Modifier Value: "+outValue * scale);
		outValue *= scale;
		return outValue + ogValue > cap ? cap : outValue;
	}	
}
