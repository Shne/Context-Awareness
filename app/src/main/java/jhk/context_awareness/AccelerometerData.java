package jhk.context_awareness;

/**
 * Created by jhk on 11/30/14.
 */
public class AccelerometerData {

	private float x;
	private float y;
	private float z;

	public AccelerometerData(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public float getZ() {
		return z;
	}
}
