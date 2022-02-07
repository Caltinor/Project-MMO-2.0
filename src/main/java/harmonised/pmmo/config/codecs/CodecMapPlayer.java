package harmonised.pmmo.config.codecs;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record CodecMapPlayer (Optional<Boolean> ignoreReq) {
	
	public static final Codec<CodecMapPlayer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.BOOL.optionalFieldOf("ignoreReq").forGetter(CodecMapPlayer::ignoreReq)
			).apply(instance, CodecMapPlayer::new));

	public static record PlayerData(boolean ignoreReq) {
		public static PlayerData getDefault() {return new PlayerData(false);}
		public PlayerData(CodecMapPlayer raw) {
			this(raw.ignoreReq.orElseGet(() -> false));
		}
		public static PlayerData combine(PlayerData one, PlayerData two) {
			boolean ignoreReqMerged = one.ignoreReq || two.ignoreReq;
			return new PlayerData(ignoreReqMerged);
		}
	}
}
