package harmonised.pmmo.config.codecs;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import harmonised.pmmo.util.Functions;

public record PlayerData(
		boolean override,
		boolean ignoreReq,
		Map<String, Double> bonuses) implements DataSource<PlayerData>{
	
	public static final Codec<PlayerData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.BOOL.optionalFieldOf("override").forGetter(pd -> Optional.of(pd.override())),
			Codec.BOOL.optionalFieldOf("ignoreReq").forGetter(pd -> Optional.of(pd.ignoreReq())),
			CodecTypes.DOUBLE_CODEC.optionalFieldOf("bonuses").forGetter(pd -> Optional.of(pd.bonuses()))
			).apply(instance, (override, reqIgnore, bonus) -> 
				new PlayerData(
						override.orElse(false),
						reqIgnore.orElse(false),
						bonus.orElse(new HashMap<>()))
			));

	@Override
	public PlayerData combine(PlayerData two) {
		AtomicBoolean ignoreReqMerged = new AtomicBoolean(false);
		Map<String, Double> mergedBonus = new HashMap<>();
		
		BiConsumer<PlayerData, PlayerData> bothOrNeither = (o, t) -> {
			ignoreReqMerged.set(o.ignoreReq() | t.ignoreReq());
			mergedBonus.putAll(o.bonuses());
			t.bonuses().forEach((skill, mod) -> {
				mergedBonus.merge(skill, mod, (o1, t1) -> o1 > t1 ? o1 : t1);
			});
		};
		Functions.biPermutation(this, two, this.override(), two.override(), (o, t) -> {
			ignoreReqMerged.set(o.ignoreReq());
			mergedBonus.putAll(o.bonuses().isEmpty() ? t.bonuses() : o.bonuses());
		}, 
		bothOrNeither, 
		bothOrNeither);

		return new PlayerData(this.override() || two.override(), ignoreReqMerged.get(), mergedBonus);
	}

	@Override
	public boolean isUnconfigured() {
		return ignoreReq() != false && bonuses().isEmpty();
	}
	
	public Map<String, Double> mergeWithPlayerBonuses(Map<String, Double> map) {
		Map<String, Double> newMap = new HashMap<>(map);
		bonuses().forEach((string, ratio) -> {
			newMap.merge(string, ratio, (one, two) -> one + two);
		});
		return newMap;
	}
}
