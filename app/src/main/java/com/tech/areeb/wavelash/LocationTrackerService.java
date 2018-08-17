package com.tech.areeb.wavelash;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class LocationTrackerService extends Service implements LocationListener {

    private final Context context;
    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    boolean canGetLocation = false;

    Location location;
    double latitude;
    double longitude;

    private List<SchedulerModel> elementsList = new ArrayList<>();

    private DatabaseReference databaseReference;

    private static final long MIN_DISTANCE_FOR_UPDATE = 100;
    private static final long MIN_TIME_FOR_UPDATE = 1000;
    private static final List<SchedulerModel> Schedules = new ArrayList<>();

    protected LocationManager locationManager;

    public LocationTrackerService(){
        this.context = getApplicationContext();
    }

    public LocationTrackerService(Context context){
        this.context = context;
        getLocation();
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    public void addScheduleToList(SchedulerModel element){
        elementsList.add(element);
    }

    public void getLocation(){
        try {


            locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);

            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if(!isGPSEnabled && !isNetworkEnabled){
                //Not Enabled
            }
            else {
                this.canGetLocation = true;

                if (isNetworkEnabled){
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,MIN_TIME_FOR_UPDATE,MIN_DISTANCE_FOR_UPDATE, this);
                    Log.d("LocationTrackerService", " Network");
                    if(locationManager!=null){
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location!=null){
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }

                }

                if (isGPSEnabled){
                    if(location==null){
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,MIN_TIME_FOR_UPDATE,MIN_DISTANCE_FOR_UPDATE,this);
                        Log.d("LocationTrackerService","GPS");
                        if (locationManager!=null){
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location!=null){
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }

            }

        }
        catch (SecurityException e){
            //
        }
        catch (Exception e){
            //
        }
    }

    public void stopTracking(){
        if (locationManager!=null){
            locationManager.removeUpdates(this);
        }
    }

    public double getLatitude(){
        if(location!=null){
            latitude = location.getLatitude();
        }
        return latitude;
    }

    public double getLongitude() {
        if (location!=null){
            longitude = location.getLongitude();
        }
        return longitude;
    }


    public boolean isCanGetLocation(){
        return canGetLocation;
    }

    public void showSettingsAlert(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enable GPS");
        builder.setMessage("GPS is not enabled. Enable it?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });

        builder.setNegativeButton("No!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        builder.show();

    }

    @Override
    public void onLocationChanged(Location location) {
        new GetMapsAPI().execute(context);
        Log.e("onLocationChaneged", latitude + ":" + longitude);
        //for (SchedulerModel traverse : elementsList){
            //Find ETA
        //}

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    public class GetMapsAPI extends AsyncTask<Context,Void, Void>{

        private static final String MAPS_API_ORIGIN =
                "https://maps.googleapis.com/maps/api/directions/json?origin=";
        private static final String MAPS_API_DESTINATION_KEY =
                "&destination=28.713312,77.174387&key=AIzaSyC62FTIiY8ACqJuDD9v4hkbsKzcZ73Su0Q";

        private JSONObject data;


        @Override
        protected Void doInBackground(Context... contexts) {


            try {

                if  (!isCanGetLocation()){
                    return null;
                }

                Double lat,lon;
                lat = getLatitude();
                lon = getLongitude();

                String origin = lat + "," + lon;

                String REQUEST_URL = MAPS_API_ORIGIN + origin + MAPS_API_DESTINATION_KEY;

                URL url = new URL(REQUEST_URL);
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();

                BufferedReader reader = new BufferedReader( new InputStreamReader(connection.getInputStream() ) );

                StringBuffer json = new StringBuffer(1024);
                String tmp;
                while((tmp=reader.readLine())!=null)
                    json.append(tmp).append("\n");
                reader.close();

                data = new JSONObject(json.toString());

                return null;

            }
            catch(Exception e){
                Log.e("LocationTrackerService",e.toString());
                return null;
            }

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            try {

                JSONObject routes = data.getJSONArray("routes").getJSONObject(0);
                JSONObject legs = routes.getJSONArray("legs").getJSONObject(0);
                JSONObject duration = legs.getJSONObject("duration");

                double eta = duration.getDouble("value");

                for (SchedulerModel traverse : elementsList){
                    if (traverse.getETA() <= (int) eta){
                        databaseReference.child("bedroom").child("fan").setValue(  (traverse.getBedroomFans()?1:0)  );
                        databaseReference.child("bedroom").child("light").setValue(  (traverse.getBedroomLights()?1:0)  );
                        databaseReference.child("drawingroom").child("fan").setValue(  (traverse.getDrawingroomFans()?1:0)  );
                        databaseReference.child("drawingroom").child("light").setValue(  (traverse.getDrawingroomLights()?1:0)  );
                    }

                }

            }
            catch (Exception e){
                Log.e("OnPostExecute",e.toString());
                //Error reading JSON
            }
        }
    }


}
