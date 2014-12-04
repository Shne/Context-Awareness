package jhk.context_awareness;

/**
 * Created by jhk on 12/4/14.
 */
public class CalendarEvent {
	public String title;
	public String location;
	public android.location.Location geoLocation;

	public CalendarEvent() {}

	public CalendarEvent(String title, String location) {
		this.title = title;
		this.location = location;
	}
}
