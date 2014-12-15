package jhk.context_awareness;

import android.location.Location;

import java.util.ArrayList;

/**
 * Created by Roland on 14-12-2014.
 */
public class SpeedProvider implements LocationConsumer {
    private Location latestLocation;
    private ArrayList<SpeedListener> consumers = new ArrayList<SpeedListener>();

    public void notifyConsumers(double speed) {
        for(SpeedListener consumer : consumers) {
            consumer.onSpeedChanged(speed);
        }
    }

    @Override
    public void consume(Location l) {
        latestLocation = l;
        onChange();
    }

    private void onChange(){
        if(latestLocation != null){
            notifyConsumers((double)latestLocation.getSpeed());
        }
    }


    public void registerContextListener(SpeedListener cl) {
        consumers.add(cl);
    }

    public void unregisterContextListener(SpeedListener cl) {
        consumers.remove(cl);

    }


}
