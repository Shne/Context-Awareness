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

    public void notifyConsumers(AtEventType atEvent) {
        for(AtEventContextListener consumer : consumers) {
            consumer.onAtEventContextChanged(atEvent, latestEvent.availability);
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
        if(latestEvent != null && latestLocation != null) {
	        AtEventType atEvent;
	        if(latestEvent.geoLocation != null) {
		        float distance = latestEvent.geoLocation.distanceTo(latestLocation);
		        if (distance < 100)
			        atEvent = AtEventType.AT_EVENT;
		        else
			        atEvent = AtEventType.NOT_AT_EVENT;
	        } else {
		        atEvent = AtEventType.NO_LOCATION;
	        }
	        notifyConsumers(atEvent);
        }
    }

    public void registerContextListener(AtEventContextListener cl) {
        consumers.add(cl);
    }

    public void unregisterContextListener(AtEventContextListener cl) {
        consumers.remove(cl);

    }
}
