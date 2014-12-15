package jhk.context_awareness;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.Queue;


public class MainActivity extends Activity implements AtEventContextListener, ContextListener<MovementType>, SpeedListener {

	private SensorManager sensorManager;
	private Sensor accelerometer;
	private AccelerometerDataProvider adp;
	private int windowSize = 128;
	private int calendarQueryIntervalMilis = 10000;
    private double currentSpeed = 0.0;

	AudioManager am;
	MovementType movementType = MovementType.STILL;
	AtEventType atEvent = AtEventType.NOT_AT_EVENT;
	int availability = CalendarContract.Events.AVAILABILITY_FREE;
    ClassifierSequence classifierSequence;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		setupMovementDetection();
		setupAtScheduledEventDetection();
        classifierSequence = new ClassifierSequence(8);

        am = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

    }

    private void setupAtScheduledEventDetection() {
        Context appContext = getApplicationContext();
        CalendarLocationDataProvider calendarLocationDataProvider = new CalendarLocationDataProvider(appContext, calendarQueryIntervalMilis);
        EventGeoLocationFinder eventGeoLocationFinder = new EventGeoLocationFinder(appContext);
        calendarLocationDataProvider.registerConsumer(eventGeoLocationFinder);

	    AtEventContextProvider atEventContextProvider = new AtEventContextProvider();
        SpeedProvider speedProvider = new SpeedProvider();
	    eventGeoLocationFinder.registerConsumer(atEventContextProvider);
	    LocationProvider locationProvider = new LocationProvider(appContext);
	    locationProvider.registerConsumer(atEventContextProvider);
        locationProvider.registerConsumer(speedProvider);
	    atEventContextProvider.registerContextListener(this);
        speedProvider.registerContextListener(this);
    }

	private void setupMovementDetection() {
		//Setup accelerometerDataProvider
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		adp = new AccelerometerDataProvider(windowSize);
		accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		sensorManager.registerListener(adp, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);

		//setup movement inferer
		MovementInferer movementInferer = new MovementInferer(getApplicationContext());
		movementInferer.registerContextListener(this); //we listen to the movement inferer
		adp.registerConsumer(movementInferer); //the movement inferer listens to the accelerometer data provider
	}

	@Override
	public void onMovementTypeChanged(final MovementType movementType) {
		this.movementType = movementType;

        //Fix Driving vs. Still based on current speed.
        //Don't know if the speeds are the right ones yet.
        //Not tested yet.
        if(currentSpeed > 0.0 && currentSpeed <= 3.0){
            if(this.movementType == MovementType.DRIVING){
                this.movementType = MovementType.STILL;
            }
        }else if(currentSpeed > 3.0){
            if(this.movementType == MovementType.STILL){
                this.movementType = MovementType.DRIVING;
            }
        }

        //Looks at previous element in classifier sequence and checks if it is probable.
        //Could possible be extended to look at last few classifiers in stead of just the last one.
        //Needs testing, and more cases could possibly be added
        if(classifierSequence.numberOfElements() > 0) {
            MovementType previous = classifierSequence.getLatestClassifiedMovement();
            if (previous == MovementType.DRIVING && this.movementType == MovementType.BIKING) {
                this.movementType = previous;
            } else if (previous == MovementType.BIKING
                    && (this.movementType == MovementType.DRIVING
                    || this.movementType == MovementType.RUNNING)) {
                this.movementType = previous;
            } else if (previous == MovementType.RUNNING && this.movementType == MovementType.DRIVING) {
                this.movementType = previous;
            }
        }
        classifierSequence.add(this.movementType);

		updateRingerMode();
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				String msg = "User is "+movementType.toString();
				TextView tv = (TextView) findViewById(R.id.MovementTextView);
				tv.setText(msg);
			}
		});
	}

	@Override
	public void onAtEventContextChanged(final AtEventType atEvent, int availability) {
		this.atEvent = atEvent;
		this.availability = availability;
		updateRingerMode();
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				String msg = "Is at event? : " +  atEvent.toString();
				TextView tv = (TextView) findViewById(R.id.AtEventTextView);
				tv.setText(msg);
			}
		});
	}

    @Override
    public void onSpeedChanged(double currentSpeed) {
        this.currentSpeed = currentSpeed*3.6;
    }

	public void updateRingerMode() {
		switch(availability) {
			case CalendarContract.Events.AVAILABILITY_FREE:
				am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
				break;
			case CalendarContract.Events.AVAILABILITY_BUSY:
				switch(atEvent) {
					case AT_EVENT:
						am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
						break;
					case NOT_AT_EVENT:
						am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
						break;
					case NO_LOCATION:
						switch(movementType) {
							case STILL:case WALKING:
								am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
								break;
							case RUNNING:case BIKING:case DRIVING:
								am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
								break;
						}
				}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
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
