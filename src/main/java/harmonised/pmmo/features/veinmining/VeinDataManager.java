package harmonised.pmmo.features.veinmining;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import harmonised.pmmo.util.RegistryUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class VeinDataManager {
	public VeinDataManager() {}
	
	private final Map<ResourceLocation, VeinData> data = new HashMap<>(); 
	private final Map<UUID, BlockPos> markers = new HashMap<>();
	
	public static record VeinData(
			Optional<Integer> chargeCap,
			Optional<Double> chargeRate,
			Optional<Integer> consumeAmount) {
		public static final Codec<VeinData> VEIN_DATA_CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.INT.optionalFieldOf("chargeCap").forGetter(VeinData::chargeCap),
				Codec.DOUBLE.optionalFieldOf("chargeRate").forGetter(VeinData::chargeRate),
				Codec.INT.optionalFieldOf("consumeAmount").forGetter(VeinData::consumeAmount)
				).apply(instance, VeinData::new));
		public static VeinData EMPTY = new VeinData(Optional.of(0), Optional.of(0d), Optional.empty());
		
		public VeinData combineWith(VeinData other) {
			return new VeinData(
					this.chargeCap().orElse(0) > other.chargeCap().orElse(0) ? this.chargeCap() : other.chargeCap(),
					this.chargeRate().orElse(0d) > other.chargeRate().orElse(0d) ? this.chargeRate() : other.chargeRate(),
					this.consumeAmount().orElse(0) > other.consumeAmount().orElse(0) ? this.consumeAmount() : other.consumeAmount());
		}
	}
	//===================DATA MANAGEMENT====================================
	public void setVeinData(ResourceLocation objectID, VeinData veinData) {
		data.put(objectID, veinData);
	}
	public void reset() {data.clear();}
	//===================UTILITY METHODS====================================
	public void setMarkedPos(UUID playerID, BlockPos pos) {
		BlockPos finalPos = pos.equals(getMarkedPos(playerID)) ? BlockPos.ZERO : pos;
		markers.put(playerID, finalPos);
		MsLoggy.DEBUG.log(LOG_CODE.FEATURE, "Player "+playerID.toString()+" Marked Pos: "+finalPos.toString());
	}
	public BlockPos getMarkedPos(UUID playerID) {
		return markers.getOrDefault(playerID, BlockPos.ZERO);
	}
	
	public boolean hasData(ItemStack stack) {
		return data.containsKey(stack.getItem().getRegistryName());
	}

	public boolean hasChargeData(ItemStack stack) {
		ResourceLocation stackID = RegistryUtil.getId(stack);
		return hasData(stack) ?
				!(data.get(stackID).chargeCap().orElseGet(() -> VeinData.EMPTY.chargeCap().get()) == VeinData.EMPTY.chargeCap().get()
				&& data.get(stackID).chargeRate().orElseGet(() -> VeinData.EMPTY.chargeRate().get()) == VeinData.EMPTY.chargeRate().get())
				: false;
	}	
	public int getBlockConsume(Block block) {
		return data.getOrDefault(block.getRegistryName(), VeinData.EMPTY).consumeAmount().orElseGet(() -> {
			return Config.REQUIRE_SETTING.get() ? -1 : Config.DEFAULT_CONSUME.get();
		});
	}
	
	public int getItemChargeCapSetting(ItemStack stack) {
		return data.getOrDefault(stack.getItem().getRegistryName(), VeinData.EMPTY).chargeCap().orElse(0);
	}
	
	public double getItemRechargeRateSetting(ItemStack stack) {
		return data.getOrDefault(stack.getItem().getRegistryName(), VeinData.EMPTY).chargeRate().orElse(0d);
	}
	public VeinData getData(ItemStack stack) {
		return data.getOrDefault(stack.getItem().getRegistryName(), VeinData.EMPTY);
	}
}
