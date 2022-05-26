package harmonised.pmmo.config.codecs;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record CodecMapPlayer (
		Optional<Boolean> ignoreReq,
		Optional<Map<String, Double>> bonuses) {
	
	public static final Codec<CodecMapPlayer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.BOOL.optionalFieldOf("ignoreReq").forGetter(CodecMapPlayer::ignoreReq),
			CodecTypes.DOUBLE_CODEC.optionalFieldOf("bonuses").forGetter(CodecMapPlayer::bonuses)
			).apply(instance, CodecMapPlayer::new));

	public static record PlayerData(boolean ignoreReq, Map<String, Double> bonus) {
		public static PlayerData getDefault() {return new PlayerData(false, new HashMap<>());}
		public PlayerData(CodecMapPlayer raw) {
			this(raw.ignoreReq().orElseGet(() -> false),
				raw.bonuses().orElseGet(() -> new HashMap<>()));
		}
		public static PlayerData combine(PlayerData one, PlayerData two) {
			boolean ignoreReqMerged = one.ignoreReq || two.ignoreReq;
			Map<String, Double> mergedBonus = new HashMap<>(one.bonus());
			mergedBonus.putAll(two.bonus());
			return new PlayerData(ignoreReqMerged, mergedBonus);
		}
		
		public static Codec<PlayerData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
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
