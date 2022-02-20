package harmonised.pmmo.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class Functions<X, Y> {
	
	public static <X, Y> Function<X, Y> memoize(Function<X, Y> func) {
		Map<X, Y> cache = new ConcurrentHashMap<X, Y>();
		
		return (a) -> cache.computeIfAbsent(a, func);
	}
}
