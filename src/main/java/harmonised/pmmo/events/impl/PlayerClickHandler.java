package harmonised.pmmo.events.impl;

import java.util.List;
import java.util.Map;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.features.party.PartyUtils;
import harmonised.pmmo.util.TagUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickItem;
import net.minecraftforge.eventbus.api.Event.Result;

public class PlayerClickHandler {
//TODO add in how to handle event trigger cancellations
	public static void leftClickBlock(LeftClickBlock event ) {
		Player player = event.getPlayer();
		Core core = Core.get(player.level);
		boolean serverSide = !player.level.isClientSide;
		
		if (!core.isBlockActionPermitted(ReqType.INTERACT, event.getPos(), player)) {
			event.setUseBlock(Result.DENY);
		}
		if (!core.isActionPermitted(ReqType.INTERACT, event.getItemStack(), player)) {
			event.setUseItem(Result.DENY);
		}
		if (event.getUseBlock().equals(Result.DENY)) return;
		
		CompoundTag hookOutput = new CompoundTag();
		if (serverSide) {
			hookOutput = core.getEventTriggerRegistry().executeEventListeners(EventType.HIT_BLOCK, event, hookOutput);
			if (hookOutput.getBoolean(APIUtils.IS_CANCELLED)) {
				event.setCanceled(true);
				return;
			}
		}
		
		hookOutput = TagUtils.mergeTags(hookOutput, core.getPerkRegistry().executePerk(EventType.HIT_BLOCK, player, core.getSide()));
		if (serverSide) {
			Map<String, Long> xpAward = core.getBlockExperienceAwards(EventType.HIT_BLOCK, event.getPos(), player.getLevel(), event.getPlayer(), hookOutput);
			List<ServerPlayer> partyMembersInRange = PartyUtils.getPartyMembersInRange((ServerPlayer) event.getPlayer());
			core.awardXP(partyMembersInRange, xpAward);	
		}
	}
	
	public static void rightClickBlock(RightClickBlock event) {
		Player player = event.getPlayer();
		Core core = Core.get(player.level);
		boolean serverSide = !player.level.isClientSide;
		
		if (!core.isBlockActionPermitted(ReqType.INTERACT, event.getPos(), player)) {
			event.setUseBlock(Result.DENY);
		}
		if (!core.isActionPermitted(ReqType.INTERACT, event.getItemStack(), player)) {
			event.setUseItem(Result.DENY);
		}
		if (event.getUseBlock().equals(Result.DENY)) return;
		
		CompoundTag hookOutput = new CompoundTag();
		if (serverSide) {
			hookOutput = core.getEventTriggerRegistry().executeEventListeners(EventType.ACTIVATE_BLOCK, event, hookOutput);
			if (hookOutput.getBoolean(APIUtils.IS_CANCELLED)) {
				event.setCanceled(true);
				return;
			}
		}
		
		hookOutput = TagUtils.mergeTags(hookOutput, core.getPerkRegistry().executePerk(EventType.ACTIVATE_BLOCK, player, core.getSide()));
		if (serverSide) {
			Map<String, Long> xpAward = core.getBlockExperienceAwards(EventType.ACTIVATE_BLOCK, event.getPos(), player.getLevel(), event.getPlayer(), hookOutput);
			List<ServerPlayer> partyMembersInRange = PartyUtils.getPartyMembersInRange((ServerPlayer) event.getPlayer());
			core.awardXP(partyMembersInRange, xpAward);	
		}
	}
	
	public static void rightClickItem(RightClickItem event) {
		Player player = event.getPlayer();
		Core core = Core.get(player.level);
		boolean serverSide = !player.level.isClientSide;
		
		if (!core.isActionPermitted(ReqType.USE, event.getItemStack(), player)) {
			event.setResult(Result.DENY);
			return;
		}
		CompoundTag hookOutput = new CompoundTag();
		if (serverSide) {
			hookOutput = core.getEventTriggerRegistry().executeEventListeners(EventType.ACTIVATE_ITEM, event, hookOutput);
			if (hookOutput.getBoolean(APIUtils.IS_CANCELLED)) {
				event.setCanceled(true);
				return;
			}
		}
		
		hookOutput = TagUtils.mergeTags(hookOutput, core.getPerkRegistry().executePerk(EventType.ACTIVATE_ITEM, player, core.getSide()));
		if (serverSide) {
			Map<String, Long> xpAward = core.getBlockExperienceAwards(EventType.ACTIVATE_ITEM, event.getPos(), player.getLevel(), event.getPlayer(), hookOutput);
			List<ServerPlayer> partyMembersInRange = PartyUtils.getPartyMembersInRange((ServerPlayer) event.getPlayer());
			core.awardXP(partyMembersInRange, xpAward);	
		}
	}
}
