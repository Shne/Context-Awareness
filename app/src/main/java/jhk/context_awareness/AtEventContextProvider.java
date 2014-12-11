package jhk.context_awareness;

import android.location.Location;

import java.util.ArrayList;

/**
 * Created by Mads on 11-12-2014.
 */
public class AtEventContextProvider implements  DataConsumer<CalendarEvent>, LocationConsumer {

    private ArrayList<AtEventContextListener> consumers = new ArrayList<AtEventContextListener>();
    private CalendarEvent latestEvent;
    private Location latestLocation;

    public void notifyConsumers(float f) {
        for(AtEventContextListener consumer : consumers) {
            // TODO: Change stub
            AtEventType i;
            if(f  < 100)
               i = AtEventType.AT_EVENT;
            else
               i = AtEventType.NOT_AT_EVENT;

            consumer.onContextChanged(i);
        }
    }

    @Override
    public void consume(CalendarEvent d) {
        latestEvent = d;
        onChange();
    }

    @Override
    public void consume(Location l) {
        latestLocation = l;
        onChange();
    }

    private void onChange() {
        if(latestEvent != null && latestEvent.geoLocation != null && latestLocation != null) {
            float distance  = latestEvent.geoLocation.distanceTo(latestLocation);
            notifyConsumers(distance);
        }
    }

    public void registerContextListener(AtEventContextListener cl) {
        consumers.add(cl);

    }

    public void unregisterContextListener(AtEventContextListener cl) {
        consumers.remove(cl);

    }
}
