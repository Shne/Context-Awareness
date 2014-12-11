package jhk.context_awareness;

import android.location.Location;

/**
 * Created by Mads on 11-12-2014.
 */
public interface LocationConsumer {
    public void consume(Location l);
}
