package jhk.context_awareness;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Mads on 11-12-2014.
 */
public class LocationProvider implements LocationListener {

    public static final String TAG = "CACLocationProvider";
    private ArrayList<LocationConsumer> consumers;

    /**
     *
     * @param appContext A locationManager from Context
     */
    public LocationProvider(final Context appContext) {
        consumers = new ArrayList<LocationConsumer>();

        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager)appContext.getSystemService(Context.LOCATION_SERVICE);

        // Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);

        Log.i(TAG,"Setting Gps provider");

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, location.toString());
        notifyConsumers(location);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        Log.i(TAG,"Status changed: " + s);

    }

    @Override
    public void onProviderEnabled(String s) {
        Log.i(TAG,"On provider enabled: " + s);

    }

    @Override
    public void onProviderDisabled(String s) {
        Log.i(TAG,"On provider disabled: "  + s);

    }
    public void registerConsumer(LocationConsumer c) {
        consumers.add(c);
    }

    public void unregisterConsumer(LocationConsumer c) {
        consumers.remove(c);
    }

    public void notifyConsumers(Location l) {
        for(LocationConsumer d : consumers) {
            d.consume(l);
        }
    }

}
