package harmonised.pmmo.events.impl;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.features.party.PartyUtils;
import harmonised.pmmo.util.RegistryUtil;
import harmonised.pmmo.util.TagBuilder;
import harmonised.pmmo.util.TagUtils;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.event.entity.player.PlayerEnchantItemEvent;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class EnchantHandler {

	public static void handle(PlayerEnchantItemEvent event) {
		Player player = event.getEntity();
		Core core = Core.get(player.level());
		event.getEnchantments().forEach(instance ->
				handleEach(core, player, event.getEnchantedItem(), instance.level(), instance.enchantment(), event));
	}

	private static void handleEach(Core core, Player player, ItemStack item, int level, Holder<Enchantment> enchantment, PlayerEnchantItemEvent event) {
		CompoundTag hookOutput = new CompoundTag();
		boolean serverSide = !player.level().isClientSide();
		if (serverSide) {
			CompoundTag dataIn = TagBuilder.start()
					.withString(APIUtils.STACK, TagUtils.stackTag(item, player.level()).asString().get())
					.withString(APIUtils.PLAYER_ID, player.getUUID().toString())
					.withInt(APIUtils.ENCHANT_LEVEL, level)
					.withString(APIUtils.ENCHANT_NAME, RegistryUtil.getId(enchantment).toString()).build();
			hookOutput = core.getEventTriggerRegistry().executeEventListeners(EventType.ENCHANT, event, dataIn);
		}
		hookOutput = TagUtils.mergeTags(hookOutput, core.getPerkRegistry().executePerk(EventType.ENCHANT, player, hookOutput));
		if (serverSide) {
			double proportion = (double)level / (double)enchantment.value().getMaxLevel();
			Map<String, Long> xpAward = core.getExperienceAwards(EventType.ENCHANT, item, player, hookOutput);
			Set<String> keys = xpAward.keySet();
			keys.forEach((skill) -> {
				xpAward.computeIfPresent(skill, (key, value) -> Double.valueOf((double)value * proportion).longValue());
			});
			List<ServerPlayer> partyMembersInRange = PartyUtils.getPartyMembersInRange((ServerPlayer) player);
			core.awardXP(partyMembersInRange, xpAward);
		}
	}
}
