package jhk.context_awareness;

/**
 * Created by Mads on 11-12-2014.
 */
public interface AtEventContextListener{
    public void onContextChanged(AtEventType t, int availability);
}
