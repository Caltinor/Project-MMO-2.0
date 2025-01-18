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
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;

@EventBusSubscriber(modid=Reference.MOD_ID, bus=EventBusSubscriber.Bus.GAME)
public class MobAttributeHandler {
	private static final ResourceLocation ADDITION_MODIFIER_ID = Reference.rl("mob_scaling_modifier");
	private static final ResourceLocation MULTIPLY_BASE_MODIFIER_ID = Reference.rl("mob_scaling_modifier_m");
	private static final ResourceLocation MULTIPLY_TOTAL_MODIFIER_ID = Reference.rl("mob_scaling_modifier_mt");
	/**Used for balancing purposes to ensure configurations do not exceed known limits.*/
	private static final Map<ResourceLocation, Float> CAPS = Map.of(
			Attributes.MAX_HEALTH.unwrapKey().get().location(), 1024f,
			Attributes.MOVEMENT_SPEED.unwrapKey().get().location(), 1.5f,
			Attributes.ATTACK_DAMAGE.unwrapKey().get().location(), 2048f,
			Attributes.SPAWN_REINFORCEMENTS_CHANCE.unwrapKey().get().location(), 1f
	);

	@SubscribeEvent
	public static void onBossAdd(MobSpawnEvent.PositionCheck event) {
		if (!Config.server().mobScaling().enabled())
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
	public static void onMobSpawn(MobSpawnEvent.PositionCheck event) {
		if (!Config.server().mobScaling().enabled())
			return;
		if (event.getEntity().getType().is(Reference.MOB_TAG)) {
			handle(event.getEntity(), event.getLevel().getLevel()
					, new Vec3(event.getX(), event.getY(), event.getZ())
					, event.getLevel().getDifficulty().getId());
		}
	}

	private static void handle(LivingEntity entity, ServerLevel level, Vec3 spawnPos, int diffScale) {
		int range = Config.server().mobScaling().aoe();
		TargetingConditions targetCondition = TargetingConditions.forNonCombat().ignoreInvisibilityTesting().ignoreLineOfSight().range(Math.pow(range, 2)*3);
		List<Player> nearbyPlayers = level.getNearbyPlayers(targetCondition, entity, AABB.ofSize(spawnPos, range, range, range));
		MsLoggy.DEBUG.log(LOG_CODE.FEATURE, "NearbyPlayers on Spawn: "+MsLoggy.listToString(nearbyPlayers));

		//get values for biome and dimension scaling
		Core core = Core.get(level.getLevel());
		LocationData dimData = core.getLoader().DIMENSION_LOADER.getData(level.getLevel().dimension().location());
		LocationData bioData = core.getLoader().BIOME_LOADER.getData(RegistryUtil.getId(level.getBiome(entity.getOnPos())));

		var dimMods = dimData.mobModifiers().getOrDefault(RegistryUtil.getId(entity), new ArrayList<>(0));
		var bioMods = bioData.mobModifiers().getOrDefault(RegistryUtil.getId(entity), new ArrayList<>(0));
		var globalDimMods = dimData.globalModifiers();
		var globalBioMods = bioData.globalModifiers();
		var mergedModifiers = mergeModifiers(Stream.of(dimMods, bioMods, globalDimMods, globalBioMods).collect(Collectors.toList()));

		final float bossMultiplier = entity.getType().is(Tags.EntityTypes.BOSSES) ? Config.server().mobScaling().bossScaling().floatValue() : 1f;

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
				case ADD_VALUE -> additionModifiers.add(mod);
				case ADD_MULTIPLIED_BASE -> multiplyBaseModifiers.add(mod);
				case ADD_MULTIPLIED_TOTAL -> multiplyTotalModifiers.add(mod);
			}
		});
		modifiers.clear();

		var collapsedAdditionModifiers = collapseModifiers(additionModifiers);
		var collapsedMultiplyBaseModifiers = collapseModifiers(multiplyBaseModifiers);
		var collapsedMultiplyTotalModifiers = collapseModifiers(multiplyTotalModifiers);

		var mobScalingMultipliers = Config.server().mobScaling().ratios();
		applyMobScaling(collapsedAdditionModifiers, entity, mobScalingMultipliers, nearbyPlayers, diffScale);
		applyBossMultiplier(collapsedAdditionModifiers, bossMultiplier);

		applyModifiers(entity, ADDITION_MODIFIER_ID, AttributeModifier.Operation.ADD_VALUE, collapsedAdditionModifiers);
		applyModifiers(entity, MULTIPLY_BASE_MODIFIER_ID, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, collapsedMultiplyBaseModifiers);
		applyModifiers(entity, MULTIPLY_TOTAL_MODIFIER_ID, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, collapsedMultiplyTotalModifiers);
	}

	/**Applies the modifiers to the entity.
	 *
	 * @param entity the entity to apply the modifiers to
	 * @param modifierId the id of the modifier to apply
	 * @param operation the operation to apply
	 * @param collapsedModifiers the map of modifiers to apply
	 */
	private static void applyModifiers(LivingEntity entity, ResourceLocation modifierId, AttributeModifier.Operation operation, Map<ResourceLocation, Double> collapsedModifiers) {
		collapsedModifiers.forEach((attributeID, amount) -> {
			if (Math.abs(amount) < 0.0001f) return;
			var attribute = entity.level().registryAccess().registryOrThrow(Registries.ATTRIBUTE).getHolder(attributeID);
			if (attribute.isEmpty()) return;
			var attributeInstance = entity.getAttribute(attribute.get());
			if (attributeInstance == null) return;
			var modifier = new AttributeModifier(modifierId, amount, operation);
			attributeInstance.removeModifier(modifierId);
			attributeInstance.addPermanentModifier(modifier);
			MsLoggy.DEBUG.log(LOG_CODE.FEATURE, "Entity={} Attribute={} value={} operation={}", entity.getDisplayName().getString(), attributeID, amount, operation);
		});
	}

	/**Collapses the list of modifiers into a single map
	 * with the attribute as the key and the sum of the
	 * modifiers as the value.
	 *
	 * @param modifiers the list of modifiers to collapse
	 * @return the collapsed map of modifiers
	 */
	private static HashMap<ResourceLocation, Double> collapseModifiers(List<MobModifier> modifiers) {
		var modifierMap = new HashMap<ResourceLocation, Double>();
		for (MobModifier mod : modifiers) {
			if (modifierMap.containsKey(mod.attribute())) {
				modifierMap.put(mod.attribute(), modifierMap.get(mod.attribute()) + mod.amount());
			} else {
				modifierMap.put(mod.attribute(), mod.amount());
			}
		}
		return modifierMap;
	}

	private static void applyMobScaling(HashMap<ResourceLocation, Double> collapsedModifiers, LivingEntity entity, Map<ResourceLocation, Map<String, Double>> config, List<Player> nearbyPlayers, int difficultyScale) {
		config.forEach((attributeID, configMap) -> {
			var attributeScalingConfig = config.getOrDefault(attributeID, new HashMap<>());
			if (attributeScalingConfig.isEmpty()) return;
			var attribute = entity.level().registryAccess().registryOrThrow(Registries.ATTRIBUTE).getHolder(attributeID);
			if (attribute.isEmpty()) return;
			var attributeInstance = entity.getAttribute(attribute.get());
			if (attributeInstance == null) return;

			var baseValue = baseValue(entity, attributeID, attributeInstance);
			var cap = CAPS.getOrDefault(attributeID, Float.MAX_VALUE);
			var bonus = getBonus(nearbyPlayers, attributeScalingConfig, difficultyScale, baseValue, cap);
			if (Math.abs(bonus) < 0.0001f) return;

			if (collapsedModifiers.containsKey(attributeID)) {
				collapsedModifiers.put(attributeID, collapsedModifiers.get(attributeID) + bonus);
			} else {
				collapsedModifiers.put(attributeID, (double) bonus);
			}
		});
	}

	private static void applyBossMultiplier(HashMap<ResourceLocation, Double> collapsedModifiers, float multiplier) {
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
		Map<String, Long> totalLevel = new HashMap<>();
		//pass through case for dim/biome bonuses to still apply.
		if (nearbyPlayers.isEmpty()) return 0f;
		nearbyPlayers.forEach(player -> {
			config.keySet().stream().collect(Collectors.toMap(str -> str, str -> Core.get(player.level()).getData().getLevel(str, player.getUUID())))
					.forEach((skill, level) -> {
				totalLevel.merge(skill, level, Long::sum);
			});
		});
		//get the average level for each skill and calculate its modifier from the configuration formula
		float outValue = 0f;
		for (Map.Entry<String, Double> configEntry : config.entrySet()) {
			long averageLevel = totalLevel.getOrDefault(configEntry.getKey(), 0L)/nearbyPlayers.size();
			if (averageLevel < Config.server().mobScaling().baseLevel()) continue;
			outValue += Config.server().mobScaling().useExponential()
					? Math.pow(Config.server().mobScaling().powerBase(), (Config.server().mobScaling().perLevel() * (averageLevel - Config.server().mobScaling().baseLevel())))
					: (averageLevel - Config.server().mobScaling().baseLevel()) * Config.server().mobScaling().perLevel();
			outValue *= configEntry.getValue();
		}
		MsLoggy.DEBUG.log(LOG_CODE.FEATURE, "Modifier Value: "+outValue * scale);
		outValue *= scale;
		return outValue + ogValue > cap ? cap : outValue;
	}	
}
