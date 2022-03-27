package harmonised.pmmo.events.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.features.party.PartyUtils;
import harmonised.pmmo.util.TagBuilder;
import harmonised.pmmo.util.TagUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

public class EnchantHandler {

	public static void handle(Player player, ItemStack stack, EnchantmentInstance enchantment) {
		Core core = Core.get(player.level);
		CompoundTag hookOutput = new CompoundTag();
		boolean serverSide = !player.level.isClientSide; 
		if (serverSide) {
			CompoundTag dataIn = TagBuilder.start()
					.withString(APIUtils.STACK, stack.serializeNBT().getAsString())
					.withString(APIUtils.PLAYER_ID, player.getUUID().toString())
					.withInt(APIUtils.ENCHANT_LEVEL, enchantment.level)
					.withString(APIUtils.ENCHANT_NAME, enchantment.enchantment.getDescriptionId()).build();
			hookOutput = core.getEventTriggerRegistry().executeEventListeners(EventType.ENCHANT, null, dataIn);
		}
		hookOutput = TagUtils.mergeTags(hookOutput, core.getPerkRegistry().executePerk(EventType.ENCHANT, player, hookOutput, core.getSide()));
		if (serverSide) {
			double proportion = (double)enchantment.level / (double)enchantment.enchantment.getMaxLevel();
			Map<String, Long> xpAward = core.getExperienceAwards(EventType.ENCHANT, stack, player, hookOutput);
			Set<String> keys = xpAward.keySet();
			keys.forEach((skill) -> {
				xpAward.computeIfPresent(skill, (key, value) -> Double.valueOf((double)value * proportion).longValue());
			});
			List<ServerPlayer> partyMembersInRange = PartyUtils.getPartyMembersInRange((ServerPlayer) player);
			core.awardXP(partyMembersInRange, xpAward);	
		}
		
		
	}
}
