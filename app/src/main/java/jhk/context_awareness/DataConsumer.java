package jhk.context_awareness;

/**
 * Created by jhk on 11/30/14.
 */
public interface DataConsumer<DataType> {
	public void consume(DataType d);
}
