package jhk.context_awareness;

/**
 * Created by jhk on 11/30/14.
 */
public interface ContextListener<T> {
	public void onContextChanged(T context);
}
