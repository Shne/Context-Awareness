package jhk.context_awareness;

/**
 * Created by jhk on 11/30/14.
 */
public interface WindowProvider<T> {
	public void registerConsumer(WindowConsumer<T> c);
	public void unregisterConsumer(WindowConsumer<T> c);
}
