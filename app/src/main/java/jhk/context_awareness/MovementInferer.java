package jhk.context_awareness;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.util.ArrayList;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Created by jhk on 11/30/14.
 */
public class MovementInferer implements DataConsumer<AccelerometerData[]>, ContextProvider<MovementType> {
	private ArrayList<ContextListener<MovementType>> contextListeners;
	private double min;
	private double max;
	private double stdev;
	private MovementType context = MovementType.STILL;

	private Instances data;
	private Classifier cls_co;
	private Attribute minAttr;
	private Attribute maxAttr;
	private Attribute stdevAttr;

	int MIN = 0;
	int MAX = 1;
	int STDEV = 2;

	public MovementInferer(Context appContext) {
		contextListeners = new ArrayList<ContextListener<MovementType>>();

		ArrayList<Attribute> attributeList = new ArrayList<Attribute>();

		minAttr = new Attribute("minimum");
		maxAttr = new Attribute("maximum");
		stdevAttr = new Attribute("stddeviation");

		ArrayList<String> classVal = new ArrayList<String>();
		classVal.add("Still");
		classVal.add("Walking");
		classVal.add("Running");
		classVal.add("Biking");
		classVal.add("Driving");

		attributeList.add(minAttr);
		attributeList.add(maxAttr);
		attributeList.add(stdevAttr);

		Attribute classAttr = new Attribute("@@class@@",classVal);
		attributeList.add(classAttr);
		data = new Instances("TestInstances",attributeList,0);
		data.setClass(classAttr);

		// load classifier from file
		try {
			AssetManager am = appContext.getAssets();
			cls_co = (Classifier) weka.core.SerializationHelper.read(am.open("J48.model"));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

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
		// Create instance
		Instance inst_co = new DenseInstance(data.numAttributes());
		inst_co.setDataset(data);
		data.add(inst_co);

		// Set instance's values
		inst_co.setValue(minAttr, min);
		inst_co.setValue(maxAttr, max);
		inst_co.setValue(stdevAttr, stdev);

		try {
			double result = cls_co.classifyInstance(inst_co);
			switch((int) result) {
				case 0:
					context = MovementType.STILL;
					break;
				case 1:
					context = MovementType.WALKING;
					break;
				case 2:
					context = MovementType.RUNNING;
					break;
				case 3:
					context = MovementType.BIKING;
					break;
				case 4:
					context = MovementType.DRIVING;
					break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void registerContextListener(ContextListener<MovementType> cl) {
		contextListeners.add(cl);
	}

	public void unregisterContextListener(ContextListener<MovementType> cl) {
		contextListeners.remove(cl);
	}
}
