package com.tech.areeb.wavelash;


import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.speech.RecognizerIntent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;

public class DeskFragment extends Fragment{

    private String Description;
    private String Temperature;
    private TextView description;
    private TextView temperature;
    private DatabaseReference databaseReference;
    private TextView homeTemp;
    private TextView homeHumid;
    private ImageView weatherIcon;
    private ImageView btnSpeak;
    private int Id;

    public DeskFragment() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragment = inflater.inflate(R.layout.fragment_desk, container, false);

        weatherIcon = fragment.findViewById(R.id.weather_icon);
        description = fragment.findViewById(R.id.details_field);
        temperature = fragment.findViewById(R.id.current_temperature_field);
        homeTemp = fragment.findViewById(R.id.tempValue);
        homeHumid = fragment.findViewById(R.id.humidValue);
        btnSpeak = fragment.findViewById(R.id.btnSpeak);

        Glide.with(getActivity()).load(R.drawable.newweather).into(weatherIcon);

        new GetWeather().execute(getActivity());

        databaseReference.child("sensors").child("temperature").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                homeTemp.setText(dataSnapshot.getValue().toString() + "°C");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        databaseReference.child("sensors").child("humidity").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                homeHumid.setText(dataSnapshot.getValue().toString() + "%");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });


        btnSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                promptSpeechInput();
            }
        });

        return fragment;
    }


    private void promptSpeechInput(){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Command!");
        try {
            startActivityForResult(intent,99);
        }
        catch (ActivityNotFoundException e){
            Toast.makeText(getActivity(), "Your device does not support speech",Toast.LENGTH_SHORT).show();
        }
    }

    public void onActivityResult(int reqCode, int resCode, Intent data) {
        super.onActivityResult(reqCode, resCode, data);
        switch (reqCode) {
            case 99:
                if (resCode == RESULT_OK && data != null) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if ((result.get(0)).compareToIgnoreCase("turn on bedroom lights") == 0) {
                        databaseReference.child("bedroom").child("light").setValue(1);
                    } else if (result.get(0).compareToIgnoreCase("turn off bedroom lights") == 0) {
                        databaseReference.child("bedroom").child("light").setValue(0);
                    } else if (result.get(0).compareToIgnoreCase("turn on bedroom fans") == 0) {
                        databaseReference.child("bedroom").child("fan").setValue(1);
                    } else if (result.get(0).compareToIgnoreCase("turn off bedroom fans") == 0) {
                        databaseReference.child("bedroom").child("fan").setValue(0);
                    } else if (result.get(0).compareToIgnoreCase("turn on drawing room lights") == 0) {
                        databaseReference.child("drawingroom").child("light").setValue(1);
                    } else if (result.get(0).compareToIgnoreCase("turn off drawing room lights") == 0) {
                        databaseReference.child("drawingroom").child("light").setValue(0);
                    } else if (result.get(0).compareToIgnoreCase("turn on drawing room fans") == 0) {
                        databaseReference.child("drawingroom").child("fan").setValue(1);
                    } else if (result.get(0).compareToIgnoreCase("turn off drawing room fans") == 0) {
                        databaseReference.child("drawingroom").child("fan").setValue(0);
                    } else if (result.get(0).compareToIgnoreCase("turn off everything") == 0) {
                        databaseReference.child("bedroom").child("light").setValue(0);
                        databaseReference.child("bedroom").child("fan").setValue(0);
                        databaseReference.child("drawingroom").child("light").setValue(0);
                        databaseReference.child("drawingroom").child("fan").setValue(0);
                    } else if (result.get(0).compareToIgnoreCase("turn on everything") == 0) {
                        databaseReference.child("bedroom").child("light").setValue(1);
                        databaseReference.child("bedroom").child("fan").setValue(1);
                        databaseReference.child("drawingroom").child("light").setValue(1);
                        databaseReference.child("drawingroom").child("fan").setValue(1);
                    }
                }
                break;
            default:
                Toast.makeText(getActivity(), "Not Processes", Toast.LENGTH_SHORT).show();
                break;
        }
    }






    public class GetWeather extends AsyncTask<Context,Void,Context> {

        private static final String OPEN_WEATHER_MAP_API ="http://api.openweathermap.org/data/2.5/weather?q=Delhi&units=metric &APPID=f3049f63825d6ffe01d0a18d162304d2";
        JSONObject data;

        @Override
        protected Context doInBackground(Context...context){
            try {
                URL url = new URL(OPEN_WEATHER_MAP_API);
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();

                BufferedReader reader = new BufferedReader( new InputStreamReader(connection.getInputStream() ) );

                StringBuffer json = new StringBuffer(1024);
                String tmp;
                while((tmp=reader.readLine())!=null)
                    json.append(tmp).append("\n");
                reader.close();

                data = new JSONObject(json.toString());

                if(data.getInt("cod") != 200){
                    description.setText("--");
                    temperature.setText("-℃");
                    return null;
                }
                return context[0];

            }
            catch(Exception e){
                description.setText("--");
                temperature.setText("-℃");
                return null;
            }
        }

        @Override
        protected void onPostExecute(Context context){
            if(context==null){
                return;
            }
            try {

                JSONObject details = data.getJSONArray("weather").getJSONObject(0);
                JSONObject main = data.getJSONObject("main");


                Id = details.getInt("id");
                Description = details.getString("description").toUpperCase(Locale.US);
                Double res = main.getDouble("temp");
                res = res - 273.15;
                Temperature = String.format("%.1f",res)+"℃";


                description.setText(Description);
                temperature.setText(Temperature);
                setWeatherIcon(Id);

            }
            catch (Exception e){
                Log.e("MyApp",e.toString());
                //Error reading JSON
            }
        }


        public void setWeatherIcon(int id){
            if (id>=200 && id<=232){
                Glide.with(getActivity()).load(R.drawable.group2xx).into(weatherIcon);
            }
            else if (id>=300 && id<=321){
                Glide.with(getActivity()).load(R.drawable.group3xx).into(weatherIcon);
            }
            else if (id>=500 && id<=531){
                Glide.with(getActivity()).load(R.drawable.group5xx).into(weatherIcon);
            }
            else if (id>=600 && id<=622){
                Glide.with(getActivity()).load(R.drawable.group6xx).into(weatherIcon);
            }
            else if (id>=701 && id<=781){
                Glide.with(getActivity()).load(R.drawable.group7xx).into(weatherIcon);
            }
            else if (id==800){
                Glide.with(getActivity()).load(R.drawable.group800).into(weatherIcon);
            }
            else if (id==801){
                Glide.with(getActivity()).load(R.drawable.group801).into(weatherIcon);
            }
            else if (id==802){
                Glide.with(getActivity()).load(R.drawable.group802).into(weatherIcon);
            }
            else if (id==803||id==804){
                Glide.with(getActivity()).load(R.drawable.group803_4).into(weatherIcon);
            }
            else {
                Glide.with(getActivity()).load(R.drawable.newweather).into(weatherIcon);
            }
        }



    }


}
