package harmonised.pmmo.features.penalties;

import java.util.ArrayList;
import java.util.List;

import harmonised.pmmo.api.enums.ObjectType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.compat.curios.CurioCompat;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.core.CoreUtils;
import harmonised.pmmo.util.RegistryUtil;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

public class EffectManager {

	public static void applyEffects(Core core, Player player) {
		//BIOME/DIM Efects
		Biome biome = player.level.getBiome(player.blockPosition()).value();
		ResourceKey<Level> dimension = player.level.dimension();
		List<MobEffectInstance> effects = core.isActionPermitted(ReqType.TRAVEL, biome, player)
				? CoreUtils.getEffects(core.getLoader().getLoader(ObjectType.BIOME).getData(RegistryUtil.getId(biome)).getPositiveEffect(), false)
				: CoreUtils.getEffects(core.getLoader().getLoader(ObjectType.BIOME).getData(RegistryUtil.getId(biome)).getNegativeEffect(), true);
		if (core.isActionPermitted(ReqType.TRAVEL, dimension, player))
			effects.addAll(CoreUtils.getEffects(core.getLoader().getLoader(ObjectType.DIMENSION).getData(dimension.location()).getPositiveEffect(), false));
		
		for (MobEffectInstance mei : effects) {
			if (!player.hasEffect(mei.getEffect()) || player.getEffect(mei.getEffect()).getDuration() < 10)
				player.addEffect(mei);
		}
		//WORN/HELD Effects
		Inventory inv = player.getInventory();
		
		List<ItemStack> items = List.of(inv.getItem(36), inv.getItem(37), inv.getItem(38), inv.getItem(39), player.getMainHandItem(), player.getOffhandItem());
		//========== CURIOS ==============
		if (CurioCompat.hasCurio) {
			items = new ArrayList<>(items);
			items.addAll(CurioCompat.getItems(player));
		}
		//================================
		for (ItemStack stack : items) {
			if (!stack.isEmpty() && !core.isActionPermitted(ReqType.WEAR, stack, player)) {
				for (MobEffectInstance mei : CoreUtils.getEffects(core.getLoader().getLoader(ObjectType.ITEM).getData(RegistryUtil.getId(stack)).getNegativeEffect(), true)) {
					if (!player.hasEffect(mei.getEffect()) || player.getEffect(mei.getEffect()).getDuration() < 10)
						player.addEffect(mei);
				}
			}
		}
	}
}
