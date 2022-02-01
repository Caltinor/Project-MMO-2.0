package harmonised.pmmo.config.datapack.codecs;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class CodecMapPlayer {
	private final Optional<Boolean> ignoreReq;
	
	public static final Codec<CodecMapPlayer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.BOOL.optionalFieldOf("ignoreReq").forGetter(CodecMapPlayer::getIgnoreReq)
			).apply(instance, CodecMapPlayer::new));
	
	public CodecMapPlayer(Optional<Boolean> ignoreReq) {
		this.ignoreReq = ignoreReq;
	}
	public Optional<Boolean> getIgnoreReq() {return ignoreReq;}

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
