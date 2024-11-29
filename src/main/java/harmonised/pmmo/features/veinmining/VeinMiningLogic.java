package harmonised.pmmo.features.veinmining;

import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.codecs.VeinData;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.features.veinmining.VeinShapeData.ShapeType;
import harmonised.pmmo.network.Networking;
import harmonised.pmmo.network.clientpackets.CP_SyncVein;
import harmonised.pmmo.setup.CommonSetup;
import harmonised.pmmo.storage.DataAttachmentTypes;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import harmonised.pmmo.util.RegistryUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class VeinMiningLogic {
	public static final String VEIN_DATA = "vein_data";
	public static final Map<UUID, Integer> maxBlocksPerPlayer = new HashMap<>();
	public static final Map<UUID, ShapeType> shapePerPlayer = new HashMap<>();

	/**This executes the actual break logic.  This should only be called
	 * on the server.
	 * 
	 * @param player
	 * @param pos
	 */
	public static void applyVein(ServerPlayer player, BlockPos pos) {
		if (!Config.server().veinMiner().enabled()) return;
		ServerLevel level = player.serverLevel();
		Block block = level.getBlockState(pos).getBlock();
		int cost = Core.get(level).getBlockConsume(block);
		if (cost <= 0) return; 
		double charge = player.getAttribute(CommonSetup.VEIN_AMOUNT).getValue();
		int consumed = 0;			
		int maxBlocks = Math.min((int)charge/cost, maxBlocksPerPlayer.computeIfAbsent(player.getUUID(), id -> 64));
		ShapeType mode = shapePerPlayer.computeIfAbsent(player.getUUID(), id -> ShapeType.AOE);
		VeinShapeData veinData = new VeinShapeData(level, pos, maxBlocks, mode, player.getDirection());
		for (BlockPos veinable : veinData.getVein()) {
			consumed += cost;
			player.gameMode.destroyAndAck(veinable, 1, "Vein Break");
		}
		MsLoggy.DEBUG.log(LOG_CODE.FEATURE, "Vein Consumed: "+consumed+" charge");
		setAmount(player, charge-consumed);
	}
	
	public static void regenerateVein(ServerPlayer player) {
		if (!Config.server().veinMiner().enabled()) return;
		Inventory inv = player.getInventory();
		List<ItemStack> items = List.of(
				inv.getItem(36),
				inv.getItem(37),
				inv.getItem(38),
				inv.getItem(39),
				player.getMainHandItem(),
				player.getOffhandItem());
		//================================
		Core core = Core.get(player.level());
		double currentCharge = veinAmount(player);
		int chargeCap = getCap(player) ;
		double chargeRate = getCharge(player);
		for (ItemStack stack : items) {
			if (!core.isActionPermitted(ReqType.WEAR, stack, player)) continue;
			VeinData data = core.getLoader().ITEM_LOADER.getData(RegistryUtil.getId(stack)).veinData();
			chargeCap += data.chargeCap.orElse(0);
			chargeRate += data.chargeRate.orElse(0d);
		}
		if (chargeRate == 0 || chargeCap == 0 || currentCharge >= chargeCap) 
			return;
	
		final int fCap = chargeCap;
		final double fRate = chargeRate * Config.server().veinMiner().chargeModifier();
		if ((currentCharge + fRate) >= fCap) {
			setAmount(player, fCap);
			MsLoggy.DEBUG.log(MsLoggy.LOG_CODE.FEATURE, "Regen at Cap: "+fCap);
		}
		else {
			setAmount(player, currentCharge + fRate);
			MsLoggy.DEBUG.log(MsLoggy.LOG_CODE.FEATURE, "Regen: "+(currentCharge+fRate));
		}
	}
	
	//=========================UTILITY METHODS=============================
	public static int getMaxChargeFromAllItems(Player player) {
		Inventory inv = player.getInventory();		
		List<ItemStack> items = List.of(
				inv.getItem(36),
				inv.getItem(37),
				inv.getItem(38),
				inv.getItem(39),
				player.getMainHandItem(),
				player.getOffhandItem());
		//================================
		int totalCapacity = getCap(player) + items.stream()
				.filter(stack -> Core.get(player.level()).isActionPermitted(ReqType.WEAR, stack, player))
				.mapToInt(stack -> Core.get(player.level()).getLoader().ITEM_LOADER.getData(RegistryUtil.getId(stack)).veinData().chargeCap.orElse(0))
				.sum();
		MsLoggy.DEBUG.log(LOG_CODE.FEATURE, "Vein Capacity: "+totalCapacity);
		return totalCapacity; 
	}

	public static double veinAmount(Player player) {
		var attribute = player.getAttribute(CommonSetup.VEIN_AMOUNT);
		return attribute == null ? 0 : attribute.getValue();
	}
	public static void setAmount(Player player, double amount) {
		var attribute = player.getAttribute(CommonSetup.VEIN_AMOUNT);
		if (attribute != null)
			attribute.setBaseValue(amount);
	}
	public static int getCap(Player player) {
		var attribute = player.getAttribute(CommonSetup.VEIN_CAPACITY);
		return  attribute == null ? 0 : (int)attribute.getValue();
	}
	public static int getCharge(Player player) {
		var attribute = player.getAttribute(CommonSetup.VEIN_RECHARGE);
		return  attribute == null ? 0 : (int)attribute.getValue();
	}
}
