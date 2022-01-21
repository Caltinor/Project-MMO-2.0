package harmonised.pmmo.events;

import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.XP;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.ShieldBlockEvent;

public class ShieldBlockHandler {

	public static void handleBlock(ShieldBlockEvent event) {
		if (event.getEntityLiving() instanceof Player && !event.getEntityLiving().getLevel().isClientSide) {
			ServerPlayer player = (ServerPlayer) event.getEntityLiving();
			float blockedDamage = event.getBlockedDamage();
			XP.awardXp(player, Skill.ENDURANCE.name.toLowerCase(), "Shield Block", blockedDamage * 5, false, false, false);
		}
	}
}
