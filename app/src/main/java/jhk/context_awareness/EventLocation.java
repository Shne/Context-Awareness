package jhk.context_awareness;

/**
 * Created by jhk on 12/4/14.
 */
public class EventLocation {
	public String fromEvent;
	public android.location.Location geoLocation;

	public EventLocation(String fromEvent) {
		this.fromEvent = fromEvent;
	}
}
