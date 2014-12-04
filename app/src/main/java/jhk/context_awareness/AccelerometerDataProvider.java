package jhk.context_awareness;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

import java.util.ArrayList;

/**
 * Created by jhk on 11/30/14.
 */
public class AccelerometerDataProvider implements SensorEventListener, DataProvider<AccelerometerData[]> {

	private int windowSize;
	private AccelerometerData[] window1;
	private AccelerometerData[] window2;
	private int window1Index;
	private int window2Index;
	private ArrayList<DataConsumer<AccelerometerData[]>> dataConsumers;

	public AccelerometerDataProvider(int windowSize) {
		this.windowSize = windowSize;
		window1 = new AccelerometerData[windowSize];
		window2 = new AccelerometerData[windowSize];
		window1Index = (int) Math.floor(windowSize / 2);
		for(int i=0; i<window1Index; i++) {
			window1[i] = new AccelerometerData(0,0,0);
		}
		window2Index = 0;

		dataConsumers = new ArrayList<DataConsumer<AccelerometerData[]>>();
	}

	@Override
	public void onSensorChanged(SensorEvent sensorEvent) {
		Sensor sensor = sensorEvent.sensor;
		if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			float x = sensorEvent.values[0];
			float y = sensorEvent.values[1];
			float z = sensorEvent.values[2];
			AccelerometerData accData = new AccelerometerData(x, y, z);

			window1[window1Index++] = accData;
			window2[window2Index++] = accData;

			if (window1Index == windowSize) {
				this.provideConsumers(window1);
				window1Index = 0;
			}
			if(window2Index == windowSize) {
				this.provideConsumers(window2);
				window2Index = 0;
			}
		}
	}

	@Override
	public void registerConsumer(DataConsumer<AccelerometerData[]> c) {
		dataConsumers.add(c);
	}

	@Override
	public void unregisterConsumer(DataConsumer<AccelerometerData[]> c) {
		dataConsumers.remove(c);
	}

	private void provideConsumers(AccelerometerData[] window) {
		for(DataConsumer c : dataConsumers) {
			c.consume(window);
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int i) {

	}
}
