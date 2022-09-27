package harmonised.pmmo.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import com.google.common.base.Preconditions;

import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;

public class LevelRegistry {
	private final List<BiFunction<String, Integer, Integer>> providers = new ArrayList<>();
	
	public void registerLevelProvider(BiFunction<String, Integer, Integer> provider) {
		Preconditions.checkNotNull(provider);
		this.providers.add(provider);
		MsLoggy.INFO.log(LOG_CODE.API, "Level Provider Registered");
	}
	
	public int process(String skill, int nativeLevel) {
		int outLevel = nativeLevel;
		for (BiFunction<String, Integer, Integer> provider : providers) {
			outLevel = provider.apply(skill, outLevel);
		}
		return outLevel;
	}
}
