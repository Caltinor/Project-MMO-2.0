package harmonised.pmmo.features.veinmining.capability;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import harmonised.pmmo.util.Reference;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class VeinProvider implements ICapabilitySerializable<CompoundTag>{
	public static final ResourceLocation VEIN_CAP_ID = new ResourceLocation(Reference.MOD_ID, "vein_data");
	public static final Capability<IVeinCap> VEIN_CAP = CapabilityManager.get(new CapabilityToken<IVeinCap>() {});

	private final VeinHandler backend = new VeinHandler(-1d);
	private LazyOptional<IVeinCap> instance = LazyOptional.of(() -> backend);
	@Override
	public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
		return cap == VEIN_CAP ? instance.cast() : LazyOptional.empty();
	}
	private static final String CHARGE = "charge";
	
	@Override
	public CompoundTag serializeNBT() {
		CompoundTag nbt = new CompoundTag();
		nbt.putDouble(CHARGE, getCapability(VEIN_CAP).orElse(backend).getCharge());
		return nbt;
	}

	@Override
	public void deserializeNBT(CompoundTag nbt) {
		getCapability(VEIN_CAP, null).orElse(backend).setCharge(nbt.contains(CHARGE)? nbt.getDouble(CHARGE) : -1d);		
	}

}
