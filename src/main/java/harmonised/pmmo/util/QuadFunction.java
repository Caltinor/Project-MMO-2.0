package harmonised.pmmo.util;

@FunctionalInterface
public interface QuadFunction<A, B, C, D, E> {
    E apply(A a, B b, C c, D d);
}
