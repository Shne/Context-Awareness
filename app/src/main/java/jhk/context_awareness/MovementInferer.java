package jhk.context_awareness;

import java.util.ArrayList;

/**
 * Created by jhk on 11/30/14.
 */
public class MovementInferer implements Consumer<AccelerometerData[]>, ContextProvider<MovementType> {
	private ArrayList<ContextListener<MovementType>> contextListeners;
	private double min;
	private double max;
	private double stdev;
	private MovementType context = MovementType.STILL;


	@Override
	public void consume(AccelerometerData[] window) {
		double[] eucNorms = convertToEuclideanNorm(window);
		calculateValues(eucNorms);
		inferContext();
		for(ContextListener<MovementType> cl : contextListeners) {
			cl.onContextChanged(context);
		}
	}

	private double[] convertToEuclideanNorm(AccelerometerData[] window) {
		double[] eucNorm = new double[window.length];
		for(int i=0; i<window.length; i++) {
			double x2 = window[i].getX() * window[i].getX();
			double y2 = window[i].getY() * window[i].getY();
			double z2 = window[i].getZ() * window[i].getZ();
			eucNorm[i] = Math.sqrt(x2 + y2 + z2);
		}
		return eucNorm;
	}

	private void calculateValues(double[] data) {
		min = Double.POSITIVE_INFINITY;
		max = Double.NEGATIVE_INFINITY;
		double sum = 0;
		for(double value : data) {
			sum += value;
			if(value < min) min = value;
			if(value > max) max = value;
		}
		double mean = sum / data.length;
		double ssd = 0;
		for(double value : data) {
			ssd += (value - mean) * (value - mean);
		}
		stdev = Math.sqrt(ssd / data.length);
	}

	private void inferContext() {
		if(max > 25.0) {
			context = MovementType.RUNNING;
			return;
		} else if(max > 15.0) {
			context = MovementType.WALKING;
			return;
		} else {
			context = MovementType.STILL;
		}
	}

	public MovementInferer() {
		contextListeners = new ArrayList<ContextListener<MovementType>>();
	}

	@Override
	public void registerContextListener(ContextListener<MovementType> cl) {
		contextListeners.add(cl);
	}

	public void unregisterContextListener(ContextListener<MovementType> cl) {
		contextListeners.remove(cl);
	}
}
