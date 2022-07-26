package harmonised.pmmo.config.codecs;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import harmonised.pmmo.util.Functions;

public record CodecMapPlayer (
		Optional<Boolean> override,
		Optional<Boolean> ignoreReq,
		Optional<Map<String, Double>> bonuses) {
	
	public static final Codec<CodecMapPlayer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.BOOL.optionalFieldOf("override").forGetter(CodecMapPlayer::override),
			Codec.BOOL.optionalFieldOf("ignoreReq").forGetter(CodecMapPlayer::ignoreReq),
			CodecTypes.DOUBLE_CODEC.optionalFieldOf("bonuses").forGetter(CodecMapPlayer::bonuses)
			).apply(instance, CodecMapPlayer::new));

	public static record PlayerData(boolean override, boolean ignoreReq, Map<String, Double> bonus) {
		public static PlayerData getDefault() {return new PlayerData(false, false, new HashMap<>());}
		public PlayerData(CodecMapPlayer raw) {
			this(raw.override().orElse(false),
				raw.ignoreReq().orElse(false),
				raw.bonuses().orElse(new HashMap<>()));
		}
		public static PlayerData combine(PlayerData one, PlayerData two) {
			AtomicBoolean ignoreReqMerged = new AtomicBoolean(false);
			Map<String, Double> mergedBonus = new HashMap<>();
			BiConsumer<PlayerData, PlayerData> bothOrNeither = (o, t) -> {
				ignoreReqMerged.set(o.ignoreReq() | t.ignoreReq());
				mergedBonus.putAll(o.bonus());
				t.bonus().forEach((skill, mod) -> {
					mergedBonus.merge(skill, mod, (o1, t1) -> o1 > t1 ? o1 : t1);
				});
			};
			Functions.biPermutation(one, two, one.override(), two.override(), (o, t) -> {
				ignoreReqMerged.set(o.ignoreReq());
				mergedBonus.putAll(o.bonus().isEmpty() ? t.bonus() : o.bonus());
			}, 
			bothOrNeither, 
			bothOrNeither);

			return new PlayerData(one.override() || two.override(), ignoreReqMerged.get(), mergedBonus);
		}
		
		public static Codec<PlayerData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.BOOL.fieldOf("override").forGetter(PlayerData::override),
				Codec.BOOL.fieldOf("ignoreReq").forGetter(PlayerData::ignoreReq),
				CodecTypes.DOUBLE_CODEC.fieldOf("bonus").forGetter(PlayerData::bonus)
				).apply(instance, PlayerData::new));
		
		public Map<String, Double> mergeWithPlayerBonuses(Map<String, Double> map) {
			Map<String, Double> newMap = new HashMap<>(map);
			bonus.forEach((string, ratio) -> {
				newMap.merge(string, ratio, (one, two) -> one + two);
			});
			return newMap;
		}
	}
}
