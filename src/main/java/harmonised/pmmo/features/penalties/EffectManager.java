package harmonised.pmmo.features.penalties;

import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.core.Core;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

public class EffectManager {
	/* TODO figure out how to designate negative effects for WORN/HELD
	 * It was suggested in the tracker that there be some configuration
	 * for this.  I agree and need to find a reasonable solution
	 */
	public static void applyEffects(Core core, Player player) {
		//BIOME/DIM Efects
		ResourceLocation biomeID = player.level.getBiome(player.blockPosition()).value().getRegistryName();
		boolean meetsReq = core.doesPlayerMeetReq(ReqType.BIOME, biomeID, player.getUUID());
		for (MobEffectInstance mei : core.getDataConfig().getLocationEffect(meetsReq, biomeID)) {
			player.addEffect(mei);
		}
		//WORN/HELD Effects
		
	}
}
