package harmonised.pmmo.features.penalties;

import java.util.List;

import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.core.Core;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class EffectManager {

	public static void applyEffects(Core core, Player player) {
		//BIOME/DIM Efects
		ResourceLocation biomeID = player.level.getBiome(player.blockPosition()).value().getRegistryName();
		boolean meetsReq = core.doesPlayerMeetReq(ReqType.TRAVEL, biomeID, player.getUUID());
		for (MobEffectInstance mei : core.getDataConfig().getLocationEffect(meetsReq, biomeID)) {
			if (!player.hasEffect(mei.getEffect()) || player.getEffect(mei.getEffect()).getDuration() < 10)
				player.addEffect(mei);
		}
		//WORN/HELD Effects
		Inventory inv = player.getInventory();
		
		List<ItemStack> items = List.of(inv.getItem(36), inv.getItem(37), inv.getItem(38), inv.getItem(39), player.getMainHandItem(), player.getOffhandItem());
		//=====PLACEHOLDER FOR CURIOS=====
		// reinstantiate list with curio
		// slots if mod is present TODO Curio
		//================================
		for (ItemStack stack : items) {
			if (!stack.isEmpty() && (!core.isActionPermitted(ReqType.WEAR, stack, player) || !core.doesPlayerMeetEnchantmentReq(stack, player.getUUID()))) {
				for (MobEffectInstance mei : core.getDataConfig().getItemEffect(stack.getItem().getRegistryName())) {
					player.addEffect(mei);
				}
			}
		}
	}
}
