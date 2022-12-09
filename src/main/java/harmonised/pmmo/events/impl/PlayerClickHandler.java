package harmonised.pmmo.events.impl;

import java.util.List;
import java.util.Map;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.features.party.PartyUtils;
import harmonised.pmmo.util.Messenger;
import harmonised.pmmo.util.RegistryUtil;
import harmonised.pmmo.util.TagUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickItem;
import net.minecraftforge.eventbus.api.Event.Result;

public class PlayerClickHandler {

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
			if (hookOutput.getBoolean(APIUtils.DENY_BLOCK_USE))
				event.setUseBlock(Result.DENY);
			if (hookOutput.getBoolean(APIUtils.DENY_ITEM_USE))
				event.setUseItem(Result.DENY);
		}
		
		hookOutput = TagUtils.mergeTags(hookOutput, core.getPerkRegistry().executePerk(EventType.HIT_BLOCK, player, core.getSide()));
		if (serverSide) {
			Map<String, Long> xpAward = core.getBlockExperienceAwards(EventType.HIT_BLOCK, event.getPos(), player.getLevel(), event.getPlayer(), hookOutput);
			List<ServerPlayer> partyMembersInRange = PartyUtils.getPartyMembersInRange((ServerPlayer) event.getEntity());
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
		if (event.getUseBlock().equals(Result.DENY)) {
			Messenger.sendDenialMsg(ReqType.INTERACT, player, event.getItemStack());
			return;
		}
		
		CompoundTag hookOutput = new CompoundTag();
		if (serverSide) {
			hookOutput = core.getEventTriggerRegistry().executeEventListeners(EventType.ACTIVATE_BLOCK, event, hookOutput);
			if (hookOutput.getBoolean(APIUtils.IS_CANCELLED)) {
				event.setCanceled(true);
				return;
			}
			//======================SALVAGE DROP LOGIC=======================================
			if (player.isCrouching() && RegistryUtil.getId(event.getWorld().getBlockState(event.getPos()).getBlock()).equals(new ResourceLocation(Config.SALVAGE_BLOCK.get()))) {
				core.getSalvageLogic().getSalvage((ServerPlayer) player, core);
			}
			//=======================END SALVAGE============================================
		}		
		
		hookOutput = TagUtils.mergeTags(hookOutput, core.getPerkRegistry().executePerk(EventType.ACTIVATE_BLOCK, player, core.getSide()));
		if (serverSide) {
			Map<String, Long> xpAward = core.getBlockExperienceAwards(EventType.ACTIVATE_BLOCK, event.getPos(), player.getLevel(), event.getPlayer(), hookOutput);
			List<ServerPlayer> partyMembersInRange = PartyUtils.getPartyMembersInRange((ServerPlayer) event.getEntity());
			core.awardXP(partyMembersInRange, xpAward);	
		}
	}
	
	public static void rightClickItem(RightClickItem event) {
		Player player = event.getPlayer();
		Core core = Core.get(player.level);
		boolean serverSide = !player.level.isClientSide;
		
		if (!core.isActionPermitted(ReqType.USE, event.getItemStack(), player)) {
			event.setCancellationResult(InteractionResult.FAIL);
			event.setCanceled(true);
			Messenger.sendDenialMsg(ReqType.USE, player, event.getItemStack());
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
			Map<String, Long> xpAward = core.getExperienceAwards(EventType.ACTIVATE_ITEM, event.getItemStack(), event.getPlayer(), hookOutput);
			List<ServerPlayer> partyMembersInRange = PartyUtils.getPartyMembersInRange((ServerPlayer) event.getEntity());
			core.awardXP(partyMembersInRange, xpAward);	
		}
	}
}
