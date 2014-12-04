package jhk.context_awareness;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jhk on 12/4/14.
 */
public class EventGeoLocationFinder implements DataConsumer<CalendarEvent>, DataProvider<CalendarEvent> {
	private List<DataConsumer<CalendarEvent>> consumers;

	public EventGeoLocationFinder() {
		consumers = new ArrayList<DataConsumer<CalendarEvent>>();
	}

	@Override
	public void consume(CalendarEvent d) {
		//TODO: Lookup geo location using some API
		//TODO: save it to the event object and pass it one to consumers
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
