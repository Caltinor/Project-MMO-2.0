package harmonised.pmmo.features.veinmining;

import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.compat.curios.CurioCompat;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.codecs.VeinData;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.features.veinmining.VeinShapeData.ShapeType;
import harmonised.pmmo.network.Networking;
import harmonised.pmmo.network.clientpackets.CP_SyncVein;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class VeinMiningLogic {
	public static final String VEIN_DATA = "vein_data";
	public static final String CURRENT_CHARGE = "vein_charge";
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
		double charge = player.getData(DataAttachmentTypes.VEIN_CHARGE.get());
		int consumed = 0;			
		int maxBlocks = Math.min((int)charge/cost, maxBlocksPerPlayer.computeIfAbsent(player.getUUID(), id -> 64));
		ShapeType mode = shapePerPlayer.computeIfAbsent(player.getUUID(), id -> ShapeType.AOE);
		VeinShapeData veinData = new VeinShapeData(level, pos, maxBlocks, mode, player.getDirection());
		for (BlockPos veinable : veinData.getVein()) {
			consumed += cost;
			player.gameMode.destroyAndAck(veinable, 1, "Vein Break");
		}
		MsLoggy.DEBUG.log(LOG_CODE.FEATURE, "Vein Consumed: "+consumed+" charge");
		applyChargeCost(player, consumed, charge);
	}
	
	public static void regenerateVein(ServerPlayer player) {
		if (!Config.server().veinMiner().enabled()) return;
		Inventory inv = player.getInventory();	
		List<ItemStack> items = new ArrayList<>();
		items.addAll(inv.armor);
		items.add(inv.getSelected());
		items.addAll(inv.offhand);
		//========== CURIOS ==============
		if (CurioCompat.hasCurio) {
			items = new ArrayList<>(items);
			items.addAll(CurioCompat.getItems(player));
		}
		//================================
		Core core = Core.get(player.level());
		double currentCharge = player.getData(DataAttachmentTypes.VEIN_CHARGE.get());
		int chargeCap = Config.server().veinMiner().baseVeinCapacity();
		double chargeRate = Config.server().veinMiner().baseChargeRate();
		for (ItemStack stack : items) {
			VeinData data;
			if (!core.isActionPermitted(ReqType.WEAR, stack, player)) continue;
			if ((data = core.getLoader().ITEM_LOADER.getData(RegistryUtil.getId(stack)).veinData()).chargeRate.isPresent()) {
				chargeCap += data.chargeCap.orElse(0);
				chargeRate += data.chargeRate.orElse(0d);
			}
		}
		if (chargeRate == 0 || chargeCap == 0 || currentCharge >= chargeCap) 
			return;
	
		final int fCap = chargeCap;
		final double fRate = chargeRate * Config.server().veinMiner().chargeModifier();
		if ((currentCharge + fRate) >= fCap) {
			player.setData(DataAttachmentTypes.VEIN_CHARGE.get(), (double)fCap);
			MsLoggy.DEBUG.log(MsLoggy.LOG_CODE.FEATURE, "Regen at Cap: "+fCap);
		}
		else {
			player.setData(DataAttachmentTypes.VEIN_CHARGE.get(), currentCharge + fRate);
			MsLoggy.DEBUG.log(MsLoggy.LOG_CODE.FEATURE, "Regen: "+(currentCharge+fRate));
		}
		Networking.sendToClient(new CP_SyncVein(player.getData(DataAttachmentTypes.VEIN_CHARGE.get())), player);
	}
	
	//=========================UTILITY METHODS=============================
	
	@SuppressWarnings("resource")
	public static int getMaxChargeFromAllItems(Player player) {
		Inventory inv = player.getInventory();		
		List<ItemStack> items = List.of(
				inv.getItem(36),
				inv.getItem(37),
				inv.getItem(38),
				inv.getItem(39),
				player.getMainHandItem(),
				player.getOffhandItem());
		//========== CURIOS ==============
		if (CurioCompat.hasCurio) {
			items = new ArrayList<>(items);
			items.addAll(CurioCompat.getItems(player));
		}
		//================================
		int totalCapacity = Config.server().veinMiner().baseVeinCapacity();
		for (ItemStack stack : items) {
			if (!Core.get(player.level()).isActionPermitted(ReqType.WEAR, stack, player)) continue;
			totalCapacity += Core.get(player.level()).getLoader().ITEM_LOADER.getData(RegistryUtil.getId(stack)).veinData().chargeCap.orElse(0);
		}
		MsLoggy.DEBUG.log(LOG_CODE.FEATURE, "Vein Capacity: "+totalCapacity);
		return totalCapacity; 
	}
	
	private static void applyChargeCost(ServerPlayer player, int cost, double currentCharge) {
		player.setData(DataAttachmentTypes.VEIN_CHARGE.get(), currentCharge-cost);
		Networking.sendToClient(new CP_SyncVein(currentCharge-cost), player);
	}
}
