package jhk.context_awareness;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.CalendarContract.Instances;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jhk on 12/4/14.
 */
public class CalendarLocationDataProvider implements DataProvider<CalendarEvent> {
	private List<DataConsumer<CalendarEvent>> dataConsumers;

	public CalendarLocationDataProvider(Context appContext) {
		dataConsumers = new ArrayList<DataConsumer<CalendarEvent>>();

		ContentResolver contentResolver = appContext.getContentResolver();
		long now = System.currentTimeMillis();
		String[] proj = new String[]{
				Instances.TITLE,
				Instances.EVENT_LOCATION,
				Instances.AVAILABILITY,
				Instances.BEGIN,
		        Instances.END
		};
		Cursor cursor = Instances.query(contentResolver, proj, now, now);
		if (cursor.getCount() > 0) {
			// deal with conflict
		}
		if (cursor.moveToFirst()) {
			do {
				String title = cursor.getString(0);
				String location = cursor.getString(1);
				int availability = cursor.getInt(2);
				CalendarEvent calendarEvent = new CalendarEvent(title, location, availability);
				provideConsumers(calendarEvent);
			} while (cursor.moveToNext());
		}
	}

	private void provideConsumers(CalendarEvent calendarEvent) {
		for(DataConsumer c : dataConsumers) {
			c.consume(calendarEvent);
		}
	}

	@Override
	public void registerConsumer(DataConsumer<CalendarEvent> c) {
		dataConsumers.add(c);
	}

	@Override
	public void unregisterConsumer(DataConsumer<CalendarEvent> c) {
		dataConsumers.remove(c);
	}
}
