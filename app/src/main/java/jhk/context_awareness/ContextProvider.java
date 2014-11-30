package jhk.context_awareness;

/**
 * Created by jhk on 11/30/14.
 */
public interface ContextProvider<T> {
	public void registerContextListener(ContextListener<T> cl);
	public void unregisterContextListener(ContextListener<T> cl);
}
