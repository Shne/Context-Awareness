package jhk.context_awareness;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


public class MainActivity extends Activity implements ContextListener<MovementType>, DataConsumer<CalendarEvent> {

	private static final String TAG = "Context-Awareness";
	private SensorManager sensorManager;
	private Sensor accelerometer;
	private AccelerometerDataProvider adp;
	private int windowSize = 32;
	private int calendarQueryIntervalMilis = 10000;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		setupMovementDetection();
		setupAtScheduledEventDetection();
	}

	private void setupAtScheduledEventDetection() {
		Context appContext = getApplicationContext();
		CalendarLocationDataProvider calendarLocationDataProvider = new CalendarLocationDataProvider(appContext, calendarQueryIntervalMilis);
		EventGeoLocationFinder eventGeoLocationFinder = new EventGeoLocationFinder(appContext);
		calendarLocationDataProvider.registerConsumer(eventGeoLocationFinder);
		eventGeoLocationFinder.registerConsumer(this);
		//TODO: then some contextprovider taking a CalendarEvent and tries to figure out if the user is there
		//TODO: it should probably also use a new DataProvider providing the users current location
	}

	private void setupMovementDetection() {
		//Setup accelerometerDataProvider
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		adp = new AccelerometerDataProvider(windowSize);
		accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		sensorManager.registerListener(adp, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

		//setup movement inferer
		MovementInferer movementInferer = new MovementInferer();
		movementInferer.registerContextListener(this); //we listen to the movement inferer
		adp.registerConsumer(movementInferer); //the movement inferer listens to the accelerometer data provider
	}

	@Override
	public void consume(final CalendarEvent e) {
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				String msg = "CalendarEvent location is: "+e.geoLocation.getLatitude() +", "+ e.geoLocation.getLongitude();
				TextView tv = (TextView) findViewById(R.id.LocationTextView);
				tv.setText(msg);
			}
		});
	}

	@Override
	public void onContextChanged(final MovementType context) {
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				String msg = "User is "+context.toString();
				TextView tv = (TextView) findViewById(R.id.MovementTextView);
				tv.setText(msg);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	protected void onPause() {
		super.onPause();
		sensorManager.unregisterListener(adp);
	}

	@Override
	protected void onResume() {
		super.onResume();
		sensorManager.registerListener(adp, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}



}
