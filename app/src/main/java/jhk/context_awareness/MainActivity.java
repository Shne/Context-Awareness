package jhk.context_awareness;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.speech.tts.TextToSpeech;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;


public class MainActivity extends Activity implements AtEventContextListener, ContextListener<MovementType>, SpeedListener, TextToSpeech.OnInitListener {

	private SensorManager sensorManager;
	private Sensor accelerometer;
	private AccelerometerDataProvider adp;
	private int windowSize = 128;
	private int calendarQueryIntervalMilis = 10000;
    private double currentSpeed = 0.0;
    private int MY_DATA_CHECK_CODE = 0;

	AudioManager am;
	MovementType movementType = MovementType.STILL;
    MovementType previousMovementType;
	AtEventType atEvent = AtEventType.NOT_AT_EVENT;
	int availability = CalendarContract.Events.AVAILABILITY_FREE;
    ClassifierSequence classifierSequence;
    private TextToSpeech tts;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		setupMovementDetection();
		setupAtScheduledEventDetection();
        classifierSequence = new ClassifierSequence(8);

        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);

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
	public void onMovementTypeChanged(final MovementType movement) {
		movementType = movement;

        //Fix Driving vs. Still based on current speed.
        //Don't know if the speeds are the right ones yet.s
        //Not tested yet.
        if(currentSpeed <= 3.0 && movementType == MovementType.DRIVING) {
            movementType = MovementType.STILL;
        } else if(currentSpeed > 3.0 && movementType == MovementType.STILL){
            movementType = MovementType.DRIVING;
        }

        //Looks at previousMovementType element in classifier sequence and checks if it is probable.
        //Could possible be extended to look at last few classifiers in stead of just the last one.
        //Needs testing, and more cases could possibly be added
        if(classifierSequence.numberOfElements() > 0) {
            previousMovementType = classifierSequence.getLatestClassifiedMovement();
            if (previousMovementType == MovementType.DRIVING && movementType == MovementType.BIKING) {
                movementType = previousMovementType;
            } else if (previousMovementType == MovementType.BIKING
                    && (movementType == MovementType.DRIVING
                    || movementType == MovementType.RUNNING)) {
                movementType = previousMovementType;
            } else if (previousMovementType == MovementType.RUNNING && movementType == MovementType.DRIVING) {
                movementType = previousMovementType;
            }
        }
        classifierSequence.add(movementType);

		updateRingerMode();
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				String msg = "User is "+movementType.toString();

                if(previousMovementType != null){
                    if(classifierSequence.numberOfElements() == 0){
                        speak(movementType.toString());
                    }else if(!previousMovementType.toString().equals(movementType.toString())){
                        speak(movementType.toString());
                    }
                }

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

    //act on result of TTS data check
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                //the user has the necessary data - create the TTS
                tts = new TextToSpeech(this, this);
            }
            else {
                //no data - install it now
                Intent installTTSIntent = new Intent();
                installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installTTSIntent);
            }
        }
    }

    private void speak(String text){
        tts.speak(text, TextToSpeech.QUEUE_ADD, null);
    }


    @Override
    public void onInit(int status) {
        //check for successful instantiation
        if (status == TextToSpeech.SUCCESS) {
            if(tts.isLanguageAvailable(Locale.US)==TextToSpeech.LANG_AVAILABLE)
                tts.setLanguage(Locale.US);
        }
        else if (status == TextToSpeech.ERROR) {
            Toast.makeText(this, "Sorry! Text To Speech failed...", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDestroy() {
        // Don't forget to shutdown tts!
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}
