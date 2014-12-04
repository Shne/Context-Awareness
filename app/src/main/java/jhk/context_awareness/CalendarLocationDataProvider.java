package jhk.context_awareness;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.CalendarContract.Instances;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jhk on 12/4/14.
 */
public class CalendarLocationDataProvider implements DataProvider<EventLocation> {
	private List<DataConsumer<EventLocation>> dataConsumers;
	private String TAG = "Context-Awareness";

	public CalendarLocationDataProvider(Context appContext) {
		dataConsumers = new ArrayList<DataConsumer<EventLocation>>();

		Log.i(TAG, "CalendarLocationDataProvider Called");
		ContentResolver contentResolver = appContext.getContentResolver();
		long now = System.currentTimeMillis();
				String[] proj =
				new String[]{
						Instances.TITLE,
						Instances.EVENT_LOCATION};
		Cursor cursor = Instances.query(contentResolver, proj, now, now);
		if (cursor.getCount() > 0) {
			// deal with conflict
		}
		if (cursor.moveToFirst()) {
			do {
				String title = cursor.getString(0);
				EventLocation eventLocation = new EventLocation(cursor.getString(1));
				provideConsumers(eventLocation);
				Log.i(TAG, title);
				Log.i(TAG, eventLocation.fromEvent);
			} while (cursor.moveToNext());
		}
	}

	private void provideConsumers(EventLocation eventLocation) {
		for(DataConsumer c : dataConsumers) {
			c.consume(eventLocation);
		}
	}

	@Override
	public void registerConsumer(DataConsumer<EventLocation> c) {
		dataConsumers.add(c);
	}

	@Override
	public void unregisterConsumer(DataConsumer<EventLocation> c) {
		dataConsumers.remove(c);
	}
}
