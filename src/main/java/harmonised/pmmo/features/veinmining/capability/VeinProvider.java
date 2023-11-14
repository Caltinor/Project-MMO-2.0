package harmonised.pmmo.features.veinmining.capability;

import net.neoforged.neoforge.common.capabilities.Capability;
import net.neoforged.neoforge.common.capabilities.CapabilityManager;
import net.neoforged.neoforge.common.capabilities.CapabilityToken;
import net.neoforged.neoforge.common.capabilities.ICapabilitySerializable;
import net.neoforged.neoforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import harmonised.pmmo.util.Reference;
import net.minecraft.core.Direction;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.resources.ResourceLocation;

public class VeinProvider implements ICapabilitySerializable<DoubleTag> {
	public static final ResourceLocation VEIN_CAP_ID = new ResourceLocation(Reference.MOD_ID, "veind_data");
	public static final Capability<VeinHandler> VEIN_CAP = CapabilityManager.get(new CapabilityToken<VeinHandler>() {});
	
	private final VeinHandler backend = new VeinHandler();
	private LazyOptional<VeinHandler> instance = LazyOptional.of(() -> backend);

	@Override
	public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
		return cap == VEIN_CAP ? instance.cast() : LazyOptional.empty();
	}

	@Override
	public DoubleTag serializeNBT() {
		return DoubleTag.valueOf(instance.orElse(backend).getCharge());
	}

	@Override
	public void deserializeNBT(DoubleTag nbt) {
		getCapability(VEIN_CAP, null).orElse(backend).setCharge(nbt.getAsDouble());
		
	}

}
