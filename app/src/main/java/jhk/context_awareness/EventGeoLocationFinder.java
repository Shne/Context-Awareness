package jhk.context_awareness;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jhk on 12/4/14.
 */
public class EventGeoLocationFinder implements DataConsumer<CalendarEvent>, DataProvider<CalendarEvent> {
	private List<DataConsumer<CalendarEvent>> consumers;
	private Context appContext;
	private static String TAG = "Context-Awareness";

	public EventGeoLocationFinder(Context appContext) {
		this.appContext = appContext;
		consumers = new ArrayList<DataConsumer<CalendarEvent>>();
	}

	@Override
	public void consume(CalendarEvent e) {
		Geocoder geocoder = new Geocoder(appContext);
		try {
			List<Address> addresses = geocoder.getFromLocationName(e.location, 1);
			if(addresses.size() > 0) {
				double latitude = addresses.get(0).getLatitude();
				double longitude = addresses.get(0).getLongitude();
				Location loc = new Location("YourMom");
				loc.setLatitude(latitude);
				loc.setLongitude(longitude);
				e.geoLocation = loc;
				provideConsumers(e);
			} else {
				e.geoLocation = null;
				provideConsumers(e);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	private void provideConsumers(CalendarEvent event) {
		for(DataConsumer<CalendarEvent> c : consumers) {
			c.consume(event);
		}
	}

	@Override
	public void registerConsumer(DataConsumer<CalendarEvent> c) {
		consumers.add(c);
	}

	@Override
	public void unregisterConsumer(DataConsumer<CalendarEvent> c) {
		consumers.remove(c);
	}
}
