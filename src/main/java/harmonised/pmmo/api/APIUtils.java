package harmonised.pmmo.api;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.pmmo_saved_data.PmmoSavedData;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.XP;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.resources.ResourceLocation;

public class APIUtils {

	/**the most flexible way to award people experience, made specifically for API
	 * The xp values can be configured by anyone inside xp_value_trigger.json, by the use of a 
	 * given key, example: "doomweapon.consume.invisible" inside the "trigger_xp" 
	 * entry of xp_value_trigger.json will determine how much xp, and in what skills 
	 * this action will award when the xp award is triggered from an API
	 * 
	 * @param playerID the UUID of the player being awarded xp for the trigger
	 * @param triggerKey the unique name of the xp trigger corresponding to the key in xp_value_trigger.json
	 * @param sourceName a debug string used to identify what triggered the xp award.  this may be null
	 * @param skip tells the gui and debugger to ignore this event and display nothing
	 * @param ignoreBonuses should this trigger event ignore bonuses from sources such as player, dimension, biome, etc.
	 */
	@SuppressWarnings("deprecation")
	public static void awardXpTrigger(UUID playerID, String triggerKey, @Nullable String sourceName, boolean skip, boolean ignoreBonuses) 
	{
		Preconditions.checkNotNull(playerID);
		Preconditions.checkNotNull(triggerKey);
		Preconditions.checkNotNull(skip);
		Preconditions.checkNotNull(ignoreBonuses);
		XP.awardXpTrigger(playerID, triggerKey, sourceName, skip, ignoreBonuses);
	}
	
	/**provides the provided player's current level for the skill supplied
	 * 
	 * @param skill string key for the skill desired
	 * @param player the player entity 
	 * @return the level of the skill for the supplied player
	 */
	@SuppressWarnings("deprecation")
	public static int getLevel(String skill, Player player) 
	{
		Preconditions.checkNotNull(skill);
		Preconditions.checkNotNull(player);
		return Skill.getLevel(skill, player);
	}
	
	/**provides the raw xp value for the supplied skill and player
	 * 
	 * @param skill string key for the skill desired
	 * @param player the player entity
	 * @return the raw xp value of the skill for the supplied player
	 */
	@SuppressWarnings("deprecation")
	public static double getXp(String skill, Player player) 
	{
		Preconditions.checkNotNull(skill);
		Preconditions.checkNotNull(player);
		return Skill.getXp(skill, player);
	}
	
	/**sets the player's level in the provided skill to the amount supplied
	 * 
	 * @param skill	string key for the skill desired
	 * @param player the player entity
	 * @param amount the ultimate level value to be set
	 */
	@SuppressWarnings("deprecation")
	public static void setLevel(String skill, ServerPlayer player, double amount) 
	{
		Preconditions.checkNotNull(skill);
		Preconditions.checkNotNull(player);
		Preconditions.checkNotNull(amount);
		Skill.setLevel(skill, player, amount);
	}
	
	/**sets the player's raw xp value in the provided skill to the amount supplied
	 * 
	 * @param skill string key for the skill desired
	 * @param player the player entity
	 * @param amount the ultimate xp value to be set
	 */
	@SuppressWarnings("deprecation")
	public static void setXp(String skill, ServerPlayer player, double amount) 
	{
		Preconditions.checkNotNull(skill);
		Preconditions.checkNotNull(player);
		Preconditions.checkNotNull(amount);
		Skill.setXp(skill, player, amount);
	}
	
	/**This method adds levels to the existing player level for the skill provided
	 * this method can be called on either side and will be handled internall by 
	 * pmmo to synchronize the data.
	 * 
	 * @param skill string key for the skill being added to
	 * @param playerID the player UUID for the player being added to
	 * @param amount the level amount being increased by
	 * @param sourceName a debug string used to identify what triggered the level addition.  this may be null
	 * @param skip tells the gui and debugger to ignore this event and display nothing
	 * @param ignoreBonuses should this trigger event ignore bonuses from sources such as player, dimension, biome, etc.
	 */
	@SuppressWarnings("deprecation")
	public static void addLevel(String skill, UUID playerID, double amount, String sourceName, boolean skip, boolean ignoreBonuses) 
	{
		Preconditions.checkNotNull(playerID);
		Preconditions.checkNotNull(skill);
		Preconditions.checkNotNull(amount);
		Preconditions.checkNotNull(skip);
		Preconditions.checkNotNull(ignoreBonuses);
		Skill.addLevel(skill, playerID, amount, sourceName, skip, ignoreBonuses);
	}
	
	/**This method adds levels to the existing player level for the skill provided
	 * this method can be called on either side and will be handled internall by 
	 * pmmo to synchronize the data.
	 * 
	 * @param skill string key for the skill being added to
	 * @param playerID the player UUID for the player being added to
	 * @param amount the Xp amount being increased by
	 * @param sourceName a debug string used to identify what triggered the Xp addition.  this may be null
	 * @param skip tells the gui and debugger to ignore this event and display nothing
	 * @param ignoreBonuses should this trigger event ignore bonuses from sources such as player, dimension, biome, etc.
	 */
	@SuppressWarnings("deprecation")
	public static void addXp(String skill, UUID playerID, double amount, String sourceName, boolean skip, boolean ignoreBonuses) 
	{
		Preconditions.checkNotNull(playerID);
		Preconditions.checkNotNull(skill);
		Preconditions.checkNotNull(amount);
		Preconditions.checkNotNull(skip);
		Preconditions.checkNotNull(ignoreBonuses);
		Skill.addXp(skill, playerID, amount, sourceName, skip, ignoreBonuses);
	}
	
	/** returns the skill map for a tile entity object.
	 * 
	 * @param tile the tile entity whose map is being returned
	 * @param jType the JType for which map should be returned
	 * @return a map of skills and values for the JType and TE
	 */
	public static Map<String, Double> getXp (BlockEntity tile, JType jType) 
	{
		Preconditions.checkNotNull(tile);
		Preconditions.checkNotNull(jType);
		return XP.getXp(tile, jType);
	}
	
	/** returns the skill map for an itemstack object.
	 * 
	 * @param stack the stack whose map is being returned
	 * @param jType the JType for which map should be returned
	 * @return a map of skills and values for the JType and ItemStack
	 */
	public static Map<String, Double> getXp(ItemStack stack, JType jType)
	{
		Preconditions.checkNotNull(stack);
		Preconditions.checkNotNull(jType);
		return XP.getXp(stack, jType);
	}
	
	/** returns the skill map for an entity object.
	 * 
	 * @param entity the entity whose map is being returned
	 * @param jType the JType for which map should be returned
	 * @return a map of skills and values for the JType and entity
	 */
	public static Map<String, Double> getXp(Entity entity, JType jType)
	{
		Preconditions.checkNotNull(entity);
		Preconditions.checkNotNull(jType);
		return XP.getXp(entity, jType);
	}
	
	/** USE ONLY IF OTHER {@link #getXp(ItemStack, JType) getXP} METHODS ARE NOT APPLICABLE
	 * this method searches the base pmmo configurations for skill/vlaue maps.  Note
	 * that any mods using the API registries to supply custom maps will be bypassed
	 * when this method is used.  There are certain cases where this is applicable, such
	 * as plants, however if an itemstack, entity, or tileentity is the target, the
	 * other methods should be used instead.  failure to do so will cause incompatibility
	 * with otherwise compatible pmmo mods.
	 * 
	 * @param registryName the registry resourcelocation for the object
	 * @param jType the JType for which map should be returned
	 * @return a map of skills and values for the JType and object
	 */
	public static Map<String, Double> getXp(ResourceLocation registryName, JType jType)
	{
		Preconditions.checkNotNull(registryName);
		Preconditions.checkNotNull(jType);
		return XP.getXpBypass(registryName, jType);
	}

	/**
	 * //Gets all xp boost maps
	 */
	public static Map<String, Map<String, Double>> getXpBoostsMap(Player player)
	{
		if(player.level.isClientSide())
			return Config.xpBoosts;
		else
			return PmmoSavedData.get().getPlayerXpBoostsMap(player.getUUID());
	}

	/**
	 * //Gets a specific xp boost map
	 */
	public static Map<String, Double> getXpBoostMap(Player player, String xpBoostKey)
	{
		if(player.level.isClientSide())
			return Config.xpBoosts.getOrDefault(xpBoostKey, new HashMap<>());
		else
			return PmmoSavedData.get().getPlayerXpBoostMap(player.getUUID(), xpBoostKey);
	}

	/**
	 * //Gets a specific xp boost in a specific skill
	 */
	public static double getPlayerXpBoost(Player player, String skill)
	{
		double xpBoost = 0;

		for(Map.Entry<String, Map<String, Double>> entry : getXpBoostsMap(player).entrySet())
		{
			xpBoost += entry.getValue().getOrDefault(skill, 0D);
		}

		return xpBoost;
	}

	/**
	 * //Sets a specific xp boost map
	 */
	public static void setPlayerXpBoost(ServerPlayer player, String xpBoostKey, Map<String, Double> newXpBoosts)
	{
		PmmoSavedData.get().setPlayerXpBoost(player.getUUID(), xpBoostKey, newXpBoosts);
	}

	/**
	 * //Removes a specific xp boost map
	 */
	public static void removePlayerXpBoost(ServerPlayer player, String xpBoostKey)
	{
		PmmoSavedData.get().removePlayerXpBoost(player.getUUID(), xpBoostKey);
	}

	/**
	 * //WARNING: Removes ALL Xp Boosts, INCLUDING ONES CAUSED BY OTHER MODS
	 */
	public static void removeAllPlayerXpBoosts(ServerPlayer player)
	{
		PmmoSavedData.get().removeAllPlayerXpBoosts(player.getUUID());
	}

	/**
	 * SERVER ONLY, THE ONLY TIME CLIENT IS CALLED WHEN A PACKET IS RECEIVED >FROM SERVER<
	 * Only Project MMO should use this.
	 */
	@Deprecated
	public static void setPlayerXpBoostsMaps(Player player, Map<String, Map<String, Double>> newBoosts)
	{
		if(player.level.isClientSide())
			Config.xpBoosts = newBoosts;
		else
			PmmoSavedData.get().setPlayerXpBoostsMaps(player.getUUID(), newBoosts);
	}
}
