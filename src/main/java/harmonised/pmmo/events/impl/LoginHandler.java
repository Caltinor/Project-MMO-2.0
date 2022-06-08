package harmonised.pmmo.events.impl;

import java.util.Map;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.network.Networking;
import harmonised.pmmo.network.clientpackets.CP_ResetXP;
import harmonised.pmmo.network.clientpackets.CP_UpdateExperience;
import harmonised.pmmo.network.clientpackets.CP_UpdateLevelCache;
import harmonised.pmmo.storage.PmmoSavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.LogicalSide;

public class LoginHandler {

	public static void handle(PlayerLoggedInEvent event) {		
		Player player = event.getPlayer();
		Core core = Core.get(player.level);
		//Send welcome message encouraging datapack usage
		MutableComponent link = Component.translatable("pmmo.clickMe");
		link.setStyle(Style.EMPTY
				.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.curseforge.com/minecraft/texture-packs/search?category=&search=project+mmo"))
				.withUnderlined(true)
				.withColor(ChatFormatting.BLUE));
		MutableComponent welcome = Component.translatable("pmmo.welcomeText", link);
		player.sendSystemMessage(welcome);
		
		
		core.getPerkRegistry().terminatePerk(EventType.DISABLE_PERK, player, core.getSide());
		
		if (core.getSide().equals(LogicalSide.SERVER)) {
			//===========UPDATE DATA MIRROR=======================
			Networking.sendToClient(new CP_ResetXP(), (ServerPlayer) player);
			for (Map.Entry<String, Long> skillMap : core.getData().getXpMap(player.getUUID()).entrySet()) {
				Networking.sendToClient(new CP_UpdateExperience(skillMap.getKey(), skillMap.getValue()), (ServerPlayer) player);
			}
			Networking.sendToClient(new CP_UpdateLevelCache(((PmmoSavedData)core.getData()).getLevelCache()), (ServerPlayer) player);
			
			//===========EXECUTE FEATURE LOGIC====================
			((PmmoSavedData)core.getData()).awardScheduledXP(player.getUUID());
		}
	}
}
