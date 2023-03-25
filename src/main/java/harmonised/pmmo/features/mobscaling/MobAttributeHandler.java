package harmonised.pmmo.features.mobscaling;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.Reference;
import harmonised.pmmo.util.RegistryUtil;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.MobSpawnEvent.FinalizeSpawn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid=Reference.MOD_ID, bus=Mod.EventBusSubscriber.Bus.FORGE)
public class MobAttributeHandler {
	private static final UUID MODIFIER_ID = UUID.fromString("c95a6e8c-a1c3-4177-9118-1e2cf49b7fcb");
	/**Used for balancing purposes to ensure configurations do not exceen known limits.*/
	private static final Map<ResourceLocation, Float> CAPS = Map.of(
		new ResourceLocation("generic.max_health"), 1024f,
		new ResourceLocation("generic.movement_speed"), 1.5f,
		new ResourceLocation("generic.attack_damage"), 2048f,
		new ResourceLocation("zombie.spawn_reinforcements"), 1f
	);
	
	@SubscribeEvent
	public static void onMobSpawn(FinalizeSpawn event) {
	    if (!Config.MOB_SCALING_ENABLED.get())
	        return;
		if (event.getEntity().getType().is(Reference.MOB_TAG)) {
			LivingEntity entity = event.getEntity();
			int diffScale = event.getLevel().getDifficulty().getId();
			Vec3 spawnPos = new Vec3(event.getX(), event.getY(), event.getZ());
			int range = Config.MOB_SCALING_AOE.get();
			TargetingConditions targetCondition = TargetingConditions.forNonCombat().ignoreInvisibilityTesting().ignoreLineOfSight().range(Math.pow(range, 2)*3);
			List<Player> nearbyPlayers = event.getLevel().getNearbyPlayers(targetCondition, entity, AABB.ofSize(spawnPos, range, range, range));
			MsLoggy.DEBUG.log(LOG_CODE.FEATURE, "NearbyPlayers on Spawn: "+MsLoggy.listToString(nearbyPlayers));
			if (nearbyPlayers.isEmpty()) return;
			//Set each Modifier type
			Config.MOB_SCALING.get().forEach((id, config) -> {
				ResourceLocation attributeID = new ResourceLocation(id);
				Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(attributeID);
				if (attribute == null) return;
				AttributeInstance attributeInstance = entity.getAttribute(attribute);				
				if (attributeInstance != null) {
					double base = baseValue(entity, id, attributeInstance);
					float cap = CAPS.getOrDefault(attributeID, 0f);
					float bonus = getBonus(nearbyPlayers, config, diffScale, base, cap);
					bonus *= Core.get(entity.level).getLoader().DIMENSION_LOADER.getData(entity.level.dimension().location()).mobModifiers()
								.getOrDefault(RegistryUtil.getId(entity), new HashMap<>())
									.getOrDefault(id.toString(), 1d);
					bonus *= Core.get(entity.level).getLoader().BIOME_LOADER.getData(RegistryUtil.getId(entity.level.getBiome(entity.blockPosition()).get())).mobModifiers()
							 	.getOrDefault(RegistryUtil.getId(entity), new HashMap<>())
							 		.getOrDefault(id.toString(), 1d);
					AttributeModifier modifier = new AttributeModifier(MODIFIER_ID, "Boost to Mob Scaling", bonus, AttributeModifier.Operation.ADDITION);
					attributeInstance.removeModifier(MODIFIER_ID);
					attributeInstance.addPermanentModifier(modifier);
					MsLoggy.DEBUG.log(LOG_CODE.FEATURE, "Entity={} Attribute={} value={}", entity.getDisplayName().getString(), id.toString(), bonus);
				}
			});
			//sets health to max if max HP was modified.  This is a case catch.
			entity.setHealth(entity.getMaxHealth());
		}
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
	private static double baseValue(LivingEntity entity, String id, AttributeInstance ai) {
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
		nearbyPlayers.forEach(player -> {
			config.keySet().stream().collect(Collectors.toMap(str -> str, str -> Core.get(player.level).getData().getPlayerSkillLevel(str, player.getUUID()))).forEach((skill, level) -> {
				totalLevel.merge(skill, level, (o, n) -> o + n);
			});
		});
		//get the average level for each skill and calculate its modifier from the configuration formula
		float outValue = 0f;
		for (Map.Entry<String, Double> configEntry : config.entrySet()) {
			int averageLevel = totalLevel.getOrDefault(configEntry.getKey(), 0)/nearbyPlayers.size();
			if (averageLevel < Config.MOB_SCALING_BASE_LEVEL.get()) continue;
			outValue += Config.MOB_USE_EXPONENTIAL_FORUMULA.get() 
					? Math.pow(Config.MOB_EXPONENTIAL_POWER_BASE.get(), (Config.MOB_EXPONENTIAL_LEVEL_MOD.get() * (averageLevel - Config.MOB_SCALING_BASE_LEVEL.get())))
					: (averageLevel - Config.MOB_SCALING_BASE_LEVEL.get()) * Config.MOB_LINEAR_PER_LEVEL.get();
			outValue *= configEntry.getValue();
		}
		MsLoggy.DEBUG.log(LOG_CODE.FEATURE, "Modifier Value: "+outValue * scale);
		outValue *= scale;
		return outValue + ogValue > cap ? cap : outValue;
	}	
}
