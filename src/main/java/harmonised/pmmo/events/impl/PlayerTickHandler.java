package harmonised.pmmo.events.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.features.party.PartyUtils;
import harmonised.pmmo.util.TagUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.LogicalSide;

public class PlayerTickHandler {
	private static int airLast = 0;
	private static float healthLast = 0;

	public static void handle(PlayerTickEvent event) {
		Player player = event.player;
		Core core = Core.get(event.side);
		
		if (player.getAirSupply() != airLast)
			processEvent(EventType.BREATH_CHANGE, core, event);
		if (player.getHealth() != healthLast)
			processEvent(EventType.HEALTH_CHANGE, core, event);
		if (player.isPassenger())
			processEvent(EventType.RIDING, core, event);
		
		//update tracker variables
		airLast = player.getAirSupply();
		healthLast = player.getHealth();
	}
	
	private static void processEvent(EventType type, Core core, PlayerTickEvent event) {
		CompoundTag eventHookOutput = new CompoundTag();
		boolean serverSide = core.getSide().equals(LogicalSide.SERVER); 
		if (serverSide){			
			eventHookOutput = core.getEventTriggerRegistry().executeEventListeners(type, event, new CompoundTag());
		}
		//Process perks
		CompoundTag perkOutput = TagUtils.mergeTags(eventHookOutput, core.getPerkRegistry().executePerk(type, event.player, eventHookOutput, core.getSide()));
		if (serverSide) {
			Map<String, Long> xpAward = new HashMap<>();
			switch (type) {
			case BREATH_CHANGE: {
				break;
			}
			case HEALTH_CHANGE: {
				
			}
			case RIDING: {
				xpAward = core.getExperienceAwards(type, event.player.getVehicle(), event.player, perkOutput);
				break;
			}
			case SPRINTING: {
				break;
			}
			case SUBMERGED: {
				break;
			}
			case SWIMMING: {
				break;
			}
			case DIVING: {
				break;
			}
			case SURFACING: {
				break;
			}
			case SWIM_SPRINTING: {
				break;
			}
			default:{}
			}
			List<ServerPlayer> partyMembersInRange = PartyUtils.getPartyMembersInRange((ServerPlayer) event.player);
			core.awardXP(partyMembersInRange, xpAward);	
		}
	}
}
