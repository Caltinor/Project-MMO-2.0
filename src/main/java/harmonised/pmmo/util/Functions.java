package harmonised.pmmo.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;

import harmonised.pmmo.util.MsLoggy.LOG_CODE;

public class Functions<X, Y> {
	
	public static <X, Y> Function<X, Y> memoize(Function<X, Y> func) {
		Map<X, Y> cache = new ConcurrentHashMap<X, Y>();
		
		return (a) -> cache.computeIfAbsent(a, func);
	}
	
	/**<p>This function accepts two like objects to be consumed based on the
	 * relationship between them.  The supplied BiConsumers are then chosen
	 * based on the ultimate relationship.  Ideally the {@link isOneTrue} and
	 * {@link isTwoTrue} are properties of {@link one} and {@link two} 
	 * respectively without enforcing object inheritence.</p>
	 * <p>An exmaple implementation would look like:</p> 
	 * <p><code>biPermuation(objectA,
	 * objectB, objectA.booleanProperty, objectB.booleanProperty, consumerA,
	 * consumerB, consumerC)</code></p>
	 * <p>internally the {@link either} logic simply replaces which of the T
	 * parameters is passed first such that the first object in the consumer
	 * is the one with only true property
	 * 
	 * @param <T> any Object
	 * @param one an instance of T
	 * @param two an instance of T
	 * @param isOneTrue a property of one
	 * @param isTwoTrue a property of two
	 * @param either a BiConsumer for treating the first parameter as the only object with a true property
	 * @param neither a BiConsumer for if neither properties are true
	 * @param both a BiConsumer for if both properties are true
	 */
	public static <T> void biPermutation(T one, T two, boolean isOneTrue, boolean isTwoTrue, 
			BiConsumer<T,T> either,
			BiConsumer<T,T> neither,
			BiConsumer<T,T> both) {
		if (isOneTrue && !isTwoTrue) either.accept(one, two);
		else if (!isOneTrue && isTwoTrue) either.accept(two, one);
		else if (!isOneTrue && !isTwoTrue) neither.accept(one, two);
		else both.accept(one, two);
	}
	
	@SafeVarargs
	public static <K, V extends Number> Map<K, V> mergeMaps(Map<K, V>...maps) {
		Map<K, V> outMap = maps.length >= 2 
				? mergeMaps(maps[0], maps[1]) 
				: maps.length >= 1 
					? maps[0] : new HashMap<>();
		for (int i =2; i < maps.length; i++) {
			outMap = mergeMaps(outMap, maps[i]);
		}
		return outMap;
	}
	
	public static <K, V extends Number> Map<K, V> mergeMaps(Map<K, V> one, Map<K, V> two) {
		Map<K, V> outMap = new HashMap<>(one);
		two.forEach((key, value) -> {
			outMap.merge(key, value, (v1, v2) -> v1.longValue() > v2.longValue() ? v1 : v2);
		});
		 MsLoggy.DEBUG.log(LOG_CODE.EVENT, "Merged Map: {}", MsLoggy.mapToString(outMap));
		return outMap;
	}
}
