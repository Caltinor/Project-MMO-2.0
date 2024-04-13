package harmonised.pmmo.registry;

import com.google.common.base.Preconditions;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class LevelRegistry {
	private final List<BiFunction<String, Long, Long>> providers = new ArrayList<>();
	
	public void registerLevelProvider(BiFunction<String, Long, Long> provider) {
		Preconditions.checkNotNull(provider);
		this.providers.add(provider);
		MsLoggy.INFO.log(LOG_CODE.API, "Level Provider Registered");
	}
	
	public long process(String skill, long nativeLevel) {
		long outLevel = nativeLevel;
		for (BiFunction<String, Long, Long> provider : providers) {
			outLevel = provider.apply(skill, outLevel);
		}
		return outLevel;
	}
}
