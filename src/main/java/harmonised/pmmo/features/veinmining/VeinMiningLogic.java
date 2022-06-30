package harmonised.pmmo.features.veinmining;

import java.util.ArrayList;
import java.util.List;

import harmonised.pmmo.compat.curios.CurioCompat;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class VeinMiningLogic {
	public static final String VEIN_DATA = "vein_data";
	private static final String CURRENT_CHARGE = "vein_charge";

	/**This executes the actual break logic.  This should only be called
	 * on the server.
	 * 
	 * @param player
	 * @param pos
	 */
	public static void applyVein(ServerPlayer player, BlockPos pos) {
		ServerLevel level = player.getLevel();
		int cost = Core.get(level).getVeinData().getBlockConsume(level.getBlockState(pos).getBlock());
		if (cost == -1) return; 
		int charge = getChargeFromAllItems(player);
		int consumed = 0;	
		Block block = level.getBlockState(pos).getBlock();
		int maxBlocks = charge/Core.get(level).getVeinData().getBlockConsume(block);
		VeinShapeData veinData = new VeinShapeData(level, pos, maxBlocks);
		for (BlockPos veinable : veinData.getVein()) {
			consumed += cost;
			player.gameMode.destroyAndAck(veinable, 1, "Vein Break");
		}
		MsLoggy.DEBUG.log(LOG_CODE.FEATURE, "Vein Consumed: "+consumed+" charge");
		applyChargeCostToAllItems(player, consumed);
	}
	
	public static void regenerateVein(Player player) {
		Inventory inv = player.getInventory();		
		List<ItemStack> items = new ArrayList<>();
		items.addAll(inv.armor);
		items.addAll(inv.items);
		items.addAll(inv.offhand);
		//========== CURIOS ==============
		if (CurioCompat.hasCurio) {
			items = new ArrayList<>(items);
			items.addAll(CurioCompat.getItems(player));
		}
		//================================
		Core core = Core.get(player.level);
		for (ItemStack stack : items) {
			if (!core.getVeinData().hasChargeData(stack)) continue;
			int chargeCap = core.getVeinData().getItemChargeCapSetting(stack);
			if (stack.getTag() == null || !stack.getTag().contains(CURRENT_CHARGE)) {
				stack.getOrCreateTag().putDouble(CURRENT_CHARGE, chargeCap);
			}
			else if (stack.getTag().getDouble(CURRENT_CHARGE) < chargeCap){
				double newCharge = stack.getTag().getDouble(CURRENT_CHARGE) + core.getVeinData().getItemRechargeRateSetting(stack);
				stack.getTag().putDouble(CURRENT_CHARGE, newCharge > chargeCap ? chargeCap : newCharge);
			}
		}
	}
	
	//=========================UTILITY METHODS=============================
	
	public static int getChargeFromAllItems(Player player) {
		Inventory inv = player.getInventory();		
		List<ItemStack> items = List.of(inv.getItem(36), inv.getItem(37), inv.getItem(38), inv.getItem(39), player.getMainHandItem(), player.getOffhandItem());
		//========== CURIOS ==============
		if (CurioCompat.hasCurio) {
			items = new ArrayList<>(items);
			items.addAll(CurioCompat.getItems(player));
		}
		//================================
		int totalCharge = 0;
		for (ItemStack stack : items) {
			totalCharge += getCurrentCharge(stack, player.level);
		}
		MsLoggy.DEBUG.log(LOG_CODE.FEATURE, "Vein Charge: "+totalCharge);
		return totalCharge; 
	}
	
	public static int getMaxChargeFromAllItems(Player player) {
		Inventory inv = player.getInventory();		
		List<ItemStack> items = List.of(inv.getItem(36), inv.getItem(37), inv.getItem(38), inv.getItem(39), player.getMainHandItem(), player.getOffhandItem());
		//========== CURIOS ==============
		if (CurioCompat.hasCurio) {
			items = new ArrayList<>(items);
			items.addAll(CurioCompat.getItems(player));
		}
		//================================
		int totalCapacity = 0;
		for (ItemStack stack : items) {
			totalCapacity += Core.get(player.level).getVeinData().getItemChargeCapSetting(stack);
		}
		MsLoggy.DEBUG.log(LOG_CODE.FEATURE, "Vein Capacity: "+totalCapacity);
		return totalCapacity; 
	}
	
	private static void applyChargeCostToAllItems(ServerPlayer player, int charge) {
		Inventory inv = player.getInventory();		
		List<ItemStack> items = List.of(player.getMainHandItem(), player.getOffhandItem(), inv.getItem(36), inv.getItem(37), inv.getItem(38), inv.getItem(39));
		//========== CURIOS ==============
		if (CurioCompat.hasCurio) {
			items = new ArrayList<>(items);
			items.addAll(CurioCompat.getItems(player));
		}
		//================================
		int index = 0;
		while (charge > 0 && index < items.size()) {
			ItemStack stack = items.get(index);
			int currentCharge = getCurrentCharge(stack, player.level);
			if (charge >= currentCharge) {
				charge -= currentCharge;
				stack.getTag().putDouble(CURRENT_CHARGE, 0d);
			}
			else {
				stack.getTag().putDouble(CURRENT_CHARGE, currentCharge-charge);
				charge = 0;
			}
			index++;
		}
	}
	
	public static int getCurrentCharge(ItemStack stack, Level level) {
		if (stack.getTag() == null || !stack.getTag().contains(CURRENT_CHARGE)) {
			int baseCharge = Core.get(level).getVeinData().getItemChargeCapSetting(stack);
			if (baseCharge == 0) return 0;
			stack.getOrCreateTag().putDouble(CURRENT_CHARGE, (double)baseCharge);
			return baseCharge;
		}
		else 
			return (int)Math.floor(stack.getTag().getDouble(CURRENT_CHARGE));			
	}
}
