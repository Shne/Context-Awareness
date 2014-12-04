package jhk.context_awareness;

/**
 * Created by jhk on 11/30/14.
 */
public interface DataProvider<T> {
	public void registerConsumer(DataConsumer<T> c);
	public void unregisterConsumer(DataConsumer<T> c);
}
