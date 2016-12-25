package aalto.fi.thin_client_computing;

import android.*;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Created by romanvoglhuber on 17/10/2016.
 */

public class LocationTracker implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static LocationTracker mInstance = null;

    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private Location mCurrentLocation;
    private Context context;

    public static LocationTracker getInstance(Context context){
        if (mInstance != null){
            return mInstance;
        }
        else{
            mInstance = new LocationTracker(context);
        }
        return mInstance;
    }

    public LocationTracker(Context context){
        this.context = context;
        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(context)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            mGoogleApiClient.connect();
        }
    }

    /**
     * Return current location
     * @return last location, can be null if there is no location available
     */
    public Location getCurrentLocation(){
        startLocatoinUpdates();
        return mCurrentLocation;
    }

    /**
     * Get location if Google API is connected
     * @param connectionHint
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d("Location", "onConnected");
        if ( ContextCompat.checkSelfPermission( context, android.Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED ) {

            Log.d("Location", "getLastLocation");
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);

            if (mLastLocation != null) {
                mCurrentLocation = mLastLocation;
                Log.d("Location", "Lat: "+String.valueOf(mLastLocation.getLatitude())+"lon: "+String.valueOf(mLastLocation.getLongitude()));
            }
            else{
                Log.d("Location", "mLastLocationLat is null");
            }

            startLocatoinUpdates();
        }
    }

    /**
     * Start location updates
     */
    protected void startLocatoinUpdates(){
        if ( ContextCompat.checkSelfPermission( context, android.Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED ) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, createLocationRequest(), this);
        }
    }

    /**
     * Create a location request
     * @return location request
     */
    protected LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

    /**
     * Failed to connect to Google API
     * @param var1
     */
    @Override
    public void onConnectionFailed( ConnectionResult var1){
        Log.d("Location", "onConnectionFailed: "+var1);
    }

    @Override
    public void onConnectionSuspended(int var1){
        Log.d("Location", "onConnectionSuspended");
    }

    /**
     * Location changed
     * @param location
     */
    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        Log.d("Location", "Location was updated to lat:"+mCurrentLocation.getLatitude());
    }



    /**
     * Stop location updates
     */
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    /*
    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected() && ContextCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, createLocationRequest(), this);
        }
    }
     */
}
