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
import net.minecraft.world.entity.ai.attributes.Attribute;
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
	private static final UUID MODIFIER_ID = UUID.fromString("c95a6e8c-a1c3-4177-9118-1e2cf49b7fcb");
	private static final UUID MODIFIER_ID2 = UUID.fromString("c95a6e8c-a1c3-4177-9118-1e2cf49b7fcc");
	private static final UUID CUSTOM_MOD_ID_ADDITION = UUID.fromString("c95a6e8c-a1c3-4177-9118-1e2cf49b7fcd");
	private static final UUID CUSTOM_MOD_ID_MULTIPLY_BASE = UUID.fromString("c95a6e8c-a1c3-4177-9118-1e2cf49b7fce");
	private static final UUID CUSTOM_MOD_ID_MULTIPLY_TOTAL = UUID.fromString("c95a6e8c-a1c3-4177-9118-1e2cf49b7fcf");
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
		handle(event.getEntity(), event.getLevel().getLevel()
				, new Vec3(event.getX(), event.getY(), event.getZ())
				, event.getLevel().getDifficulty().getId());
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

		var dimMods = dimData.mobModifiers().getOrDefault(RegistryUtil.getId(entity), new HashMap<>());
		var bioMods = bioData.mobModifiers().getOrDefault(RegistryUtil.getId(entity), new HashMap<>());
		var dimModsCustom = dimData.dimensionalMobModifiers();
		var multipliers = Config.MOB_SCALING.get();
		final float bossMultiplier = entity.getType().is(Tags.EntityTypes.BOSSES) ? Config.BOSS_SCALING_RATIO.get().floatValue() : 1f;

		Set<ResourceLocation> attributeKeys = Stream.of(dimMods.keySet(), bioMods.keySet(), multipliers.keySet())
				.flatMap(Set::stream)
				.map(ResourceLocation::new)
				.collect(Collectors.toSet());

		for (MobModifier att : dimModsCustom) {
			var attributeLocation = new ResourceLocation(att.attribute());
			var attribute = ForgeRegistries.ATTRIBUTES.getValue(attributeLocation);
			if (attribute == null) continue;

			AttributeInstance attributeInstance = entity.getAttribute(attribute);
			if (attributeInstance == null) continue;

			double bonus = att.amount();
			AttributeModifier.Operation operation = switch (att.operation()) {
				case "addition" -> AttributeModifier.Operation.ADDITION;
				case "multiply_base" -> AttributeModifier.Operation.MULTIPLY_BASE;
				case "multiply_total" -> AttributeModifier.Operation.MULTIPLY_TOTAL;
				default -> AttributeModifier.Operation.ADDITION;
			};
			UUID modifierID = switch (att.operation()) {
				case "addition" -> CUSTOM_MOD_ID_ADDITION;
				case "multiply_base" -> CUSTOM_MOD_ID_MULTIPLY_BASE;
				case "multiply_total" -> CUSTOM_MOD_ID_MULTIPLY_TOTAL;
				default -> CUSTOM_MOD_ID_ADDITION;
			};
			attributeInstance.removeModifier(modifierID);
			attributeInstance.addPermanentModifier(new AttributeModifier(modifierID, "Boost to By Dimension Scaling", bonus, operation));
		}

		//Set each Modifier type
		attributeKeys.forEach(attributeID -> {
			Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(attributeID);
			if (attribute == null) return;

			Map<String, Double> config = multipliers.getOrDefault(attributeID.toString(), new HashMap<>());
			AttributeInstance attributeInstance = entity.getAttribute(attribute);
			if (attributeInstance != null) {
				double base = baseValue(entity, attributeID, attributeInstance);
				float cap = CAPS.getOrDefault(attributeID, Float.MAX_VALUE);
				float bonus = getBonus(nearbyPlayers, config, diffScale, base, cap);
				float dimBonus = dimMods.getOrDefault(attributeID.toString(), 0d).floatValue();
				float bioBonus = bioMods.getOrDefault(attributeID.toString(), 0d).floatValue();
				boolean hasDimBonus = dimBonus >= 10000;
				if (hasDimBonus) {
					dimBonus /= 10000;
					AttributeModifier modifier = new AttributeModifier(MODIFIER_ID2, "Boost to Mob Scaling", dimBonus, AttributeModifier.Operation.MULTIPLY_BASE);
					attributeInstance.removeModifier(MODIFIER_ID2);
					attributeInstance.addPermanentModifier(modifier);
				}
				bonus += !hasDimBonus ? dimMods.getOrDefault(attributeID.toString(), 0d).floatValue() : 0f;
				bonus += bioMods.getOrDefault(attributeID.toString(), 0d).floatValue();
				bonus *= bossMultiplier;
				AttributeModifier modifier = new AttributeModifier(MODIFIER_ID, "Boost to Mob Scaling", bonus, AttributeModifier.Operation.ADDITION);
				attributeInstance.removeModifier(MODIFIER_ID);
				attributeInstance.addPermanentModifier(modifier);
				MsLoggy.DEBUG.log(LOG_CODE.FEATURE, "Entity={} Attribute={} value={}", entity.getDisplayName().getString(), attributeID.toString(), bonus);
			}
		});
		//sets health to max if max HP was modified.  This is a case catch.
		entity.setHealth(entity.getMaxHealth());
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
			float currValue = 0f;
			int averageLevel = totalLevel.getOrDefault(configEntry.getKey(), 0)/nearbyPlayers.size();
			if (averageLevel < Config.MOB_SCALING_BASE_LEVEL.get()) continue;
			currValue += Config.MOB_USE_EXPONENTIAL_FORMULA.get()
					? Math.pow(Config.MOB_EXPONENTIAL_POWER_BASE.get(), (Config.MOB_EXPONENTIAL_LEVEL_MOD.get() * (averageLevel - Config.MOB_SCALING_BASE_LEVEL.get())))
					: (averageLevel - Config.MOB_SCALING_BASE_LEVEL.get()) * Config.MOB_LINEAR_PER_LEVEL.get();
			currValue *= configEntry.getValue();
			currValue -= configEntry.getValue();
			outValue += currValue;
		}
		MsLoggy.DEBUG.log(LOG_CODE.FEATURE, "Modifier Value: "+outValue * scale);
		outValue *= scale;
		return outValue + ogValue > cap ? cap : outValue;
	}	
}
