package harmonised.pmmo.config.codecs;

public interface DataSource<T> {
	public T combine(T two);
	public boolean isUnconfigured();
}
