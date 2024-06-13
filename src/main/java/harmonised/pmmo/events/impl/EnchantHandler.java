package harmonised.pmmo.events.impl;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.events.EnchantEvent;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.features.party.PartyUtils;
import harmonised.pmmo.util.TagBuilder;
import harmonised.pmmo.util.TagUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class EnchantHandler {

	@SuppressWarnings("resource")
	public static void handle(EnchantEvent event) {
		Core core = Core.get(event.getEntity().level());
		CompoundTag hookOutput = new CompoundTag();
		boolean serverSide = !event.getEntity().level().isClientSide; 
		if (serverSide) {
			CompoundTag dataIn = TagBuilder.start()
					.withString(APIUtils.STACK, TagUtils.stackTag(event.getItem(), event.getEntity().level()).getAsString())
					.withString(APIUtils.PLAYER_ID, event.getEntity().getUUID().toString())
					.withInt(APIUtils.ENCHANT_LEVEL, event.getEnchantment().level)
					.withString(APIUtils.ENCHANT_NAME, event.getEnchantment().enchantment.unwrapKey().get().location().toString()).build();
			hookOutput = core.getEventTriggerRegistry().executeEventListeners(EventType.ENCHANT, event, dataIn);
		}
		hookOutput = TagUtils.mergeTags(hookOutput, core.getPerkRegistry().executePerk(EventType.ENCHANT, event.getEntity(), hookOutput));
		if (serverSide) {
			double proportion = (double)event.getEnchantment().level / (double)event.getEnchantment().enchantment.value().getMaxLevel();
			Map<String, Long> xpAward = core.getExperienceAwards(EventType.ENCHANT, event.getItem(), event.getEntity(), hookOutput);
			Set<String> keys = xpAward.keySet();
			keys.forEach((skill) -> {
				xpAward.computeIfPresent(skill, (key, value) -> Double.valueOf((double)value * proportion).longValue());
			});
			List<ServerPlayer> partyMembersInRange = PartyUtils.getPartyMembersInRange((ServerPlayer) event.getEntity());
			core.awardXP(partyMembersInRange, xpAward);	
		}
		
		
	}
}
