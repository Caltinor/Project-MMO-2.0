package harmonised.pmmo.config.codecs;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class VeinData  implements DataSource<VeinData>{
		public Optional<Integer> chargeCap;
		public Optional<Double> chargeRate;
		public Optional<Integer> consumeAmount;
		
	public VeinData(Optional<Integer> chargeCap, Optional<Double> chargeRate, Optional<Integer> consumeAmount) {
		this.chargeCap = chargeCap;
		this.chargeRate = chargeRate;
		this.consumeAmount = consumeAmount;
	}
		
	public static final Codec<VeinData> VEIN_DATA_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.optionalFieldOf("chargeCap").forGetter(vd -> vd.chargeCap),
			Codec.DOUBLE.optionalFieldOf("chargeRate").forGetter(vd -> vd.chargeRate),
			Codec.INT.optionalFieldOf("consumeAmount").forGetter(vd -> vd.consumeAmount)
			).apply(instance, VeinData::new));
	public static VeinData EMPTY = new VeinData(Optional.of(0), Optional.of(0d), Optional.empty());
	
	public void replaceWith(VeinData other) {
		this.chargeCap = other.chargeCap;
		this.chargeRate = other.chargeRate;
		this.consumeAmount = other.consumeAmount;
	}
	
	@Override
	public VeinData combine(VeinData other) {
		return new VeinData(
				this.chargeCap.orElse(0) > other.chargeCap.orElse(0) ? this.chargeCap : other.chargeCap,
				this.chargeRate.orElse(0d) > other.chargeRate.orElse(0d) ? this.chargeRate : other.chargeRate,
				this.consumeAmount.orElse(0) > other.consumeAmount.orElse(0) ? this.consumeAmount : other.consumeAmount);
	}
	
	@Override
	public boolean isUnconfigured() {
		return (chargeCap.isEmpty() || chargeCap.get() == 0)
				&& (chargeRate.isEmpty() || chargeCap.get() == 0d)
				&& (consumeAmount.isEmpty() || consumeAmount.get() == 0);
	}
}
