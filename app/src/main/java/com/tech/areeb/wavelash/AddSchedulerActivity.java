package com.tech.areeb.wavelash;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class AddSchedulerActivity extends Activity {

    private RadioButton eta,alarm;
    private EditText schedulerTitle;
    private CheckBox bedroomLights,bedroomFans,drawingroomLights,drawingroomFans;
    private TimePicker alarmInput, etaInput;
    private Button saveButton;
    private Button deleteButton;
    private DatabaseReference databaseReference;
    private SchedulerModel intentModel;
    private SchedulerModel intentSchedule;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_scheduler);
        eta = findViewById(R.id.type_eta);
        alarm = findViewById(R.id.type_alarm);
        etaInput = findViewById(R.id.eta_input);
        alarmInput = findViewById(R.id.alarm_input);
        schedulerTitle = findViewById(R.id.scheduler_title);
        bedroomLights = findViewById(R.id.bedroom_lights_check);
        bedroomFans = findViewById(R.id.bedroom_fans_check);
        drawingroomLights = findViewById(R.id.drawingroom_lights_check);
        drawingroomFans = findViewById(R.id.drawingroom_fans_check);
        saveButton = findViewById(R.id.save_scheduler);
        deleteButton = findViewById(R.id.delete_scheduler);
        alarmInput.setVisibility(View.GONE);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Schedules");


        eta.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    etaInput.setVisibility(View.VISIBLE);
                    alarmInput.setVisibility(View.GONE);
                    alarm.setChecked(false);
                }
            }
        });
        etaInput.setIs24HourView(true);
        alarm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    alarmInput.setVisibility(View.VISIBLE);
                    etaInput.setVisibility(View.GONE);
                    eta.setChecked(false);
                }
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postToFirebase();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteFromFirebase();
            }
        });


        try {
            intentSchedule = (SchedulerModel) getIntent().getExtras().getSerializable("Schedule");
            setView();
        }
        catch (Exception e){
            //Skip
        }


    }

    private void setView(){
        if (intentSchedule.getEventType().compareToIgnoreCase("eta")==0){

            eta.setChecked(true);

            int hr=0,min=0,eta;
            schedulerTitle.setText(intentSchedule.getSchedulerName());
            eta = intentSchedule.getETA();
            hr = eta/60;
            min = eta%60;
            if (Build.VERSION.SDK_INT>=23) {
                etaInput.setHour(hr);
                etaInput.setMinute(min);
            }
            else {
                etaInput.setCurrentHour(hr);
                etaInput.setCurrentMinute(min);
            }
            bedroomFans.setChecked(intentSchedule.getBedroomFans());
            bedroomLights.setChecked(intentSchedule.getBedroomLights());
            drawingroomFans.setChecked(intentSchedule.getDrawingroomFans());
            drawingroomLights.setChecked(intentSchedule.getDrawingroomLights());
        }
        if (intentSchedule.getEventType().compareToIgnoreCase("alarm")==0){
            alarm.setChecked(true);
            schedulerTitle.setText(intentSchedule.getSchedulerName());
            int hr, min;
            if (Build.VERSION.SDK_INT>=23) {
                hr = intentSchedule.getHour();
                min = intentSchedule.getMinute();
            }
            else {
                hr = intentSchedule.getHour();
                min = intentSchedule.getMinute();
            }

            if (hr==12 && intentSchedule.getAmPm()==0){
                hr=0;
            }
            if (hr<12 && intentSchedule.getAmPm()==1){
                hr = hr + 12;
            }

            if (Build.VERSION.SDK_INT>=23) {
                alarmInput.setHour(hr);
                alarmInput.setMinute(min);
            }
            else {
                alarmInput.setCurrentHour(hr);
                alarmInput.setCurrentMinute(min);
            }
            bedroomFans.setChecked(intentSchedule.getBedroomFans());
            bedroomLights.setChecked(intentSchedule.getBedroomLights());
            drawingroomFans.setChecked(intentSchedule.getDrawingroomFans());
            drawingroomLights.setChecked(intentSchedule.getDrawingroomLights());
        }

    }

    private void deleteFromFirebase(){
        if (schedulerTitle.getText().toString().isEmpty()){
            //do nothing
        }
        try {
            databaseReference.child(schedulerTitle.getText().toString()).removeValue();
            setResult(RESULT_CANCELED);
            finish();
        }catch (Exception e){
            Log.e("deleteFromFirebase",e.toString());
        }
    }


    private void postToFirebase(){

        if(eta.isChecked()) {


            int hr =0, min=0, eta;
            if(Build.VERSION.SDK_INT>=23){
                hr = etaInput.getHour();
                min = etaInput.getMinute();
                eta = (hr*60) + min;
                intentModel = new SchedulerModel(schedulerTitle.getText().toString(),eta);
            }
            else {
                hr = alarmInput.getCurrentHour();
                min = alarmInput.getCurrentMinute();
                eta = (hr*60) + min;
                intentModel = new SchedulerModel(schedulerTitle.getText().toString(),eta);
            }

            intentModel.setBedroomFans(bedroomFans.isChecked());
            intentModel.setBedroomLights(bedroomLights.isChecked());
            intentModel.setDrawingroomFans(drawingroomFans.isChecked());
            intentModel.setDrawingroomLights(drawingroomLights.isChecked());


            if (schedulerTitle.getText().toString().isEmpty()){
                Toast.makeText(getApplicationContext(),"Scheduler Name cannot be blank",Toast.LENGTH_LONG).show();
                return;
            }
            databaseReference.child(schedulerTitle.getText().toString()).child("Type").setValue("ETA");
            databaseReference.child(schedulerTitle.getText().toString()).child("Trigger").setValue(eta);
            databaseReference.child(schedulerTitle.getText().toString()).child("BedroomFans").setValue(bedroomFans.isChecked()?1:0);
            databaseReference.child(schedulerTitle.getText().toString()).child("BedroomLights").setValue(bedroomLights.isChecked()?1:0);
            databaseReference.child(schedulerTitle.getText().toString()).child("DrawingRoomFans").setValue(drawingroomFans.isChecked()?1:0);
            databaseReference.child(schedulerTitle.getText().toString()).child("DrawingRoomLights").setValue(drawingroomLights.isChecked()?1:0);
        }
        if (alarm.isChecked()){
            int hr =0 , min =0, ampm =0;
            if(Build.VERSION.SDK_INT>=23){
                hr = alarmInput.getHour();
                min = alarmInput.getMinute();
                //intentModel = new SchedulerModel(schedulerTitle.getText().toString(),hr,min);
            }
            else {
                hr = alarmInput.getCurrentHour();
                min = alarmInput.getCurrentMinute();
                //intentModel = new SchedulerModel(schedulerTitle.getText().toString(),hr,min);
            }

            if (hr>=12){
                ampm = 1;
                hr = ( hr==12? 12 : hr-12);
            }
            if (hr == 0){
                hr = 12;
                ampm = 0;
            }

            intentModel = new SchedulerModel(schedulerTitle.getText().toString(), hr, min, ampm);
            intentModel.setBedroomFans(bedroomFans.isChecked());
            intentModel.setBedroomLights(bedroomLights.isChecked());
            intentModel.setDrawingroomFans(drawingroomFans.isChecked());
            intentModel.setDrawingroomLights(drawingroomLights.isChecked());

            if (schedulerTitle.getText().toString().isEmpty()){
                Toast.makeText(getApplicationContext(),"Scheduler Name cannot be blank",Toast.LENGTH_LONG).show();
                return;
            }
            databaseReference.child(schedulerTitle.getText().toString()).child("Type").setValue("Alarm");
            databaseReference.child(schedulerTitle.getText().toString()).child("Trigger").child("Hour").setValue(hr);
            databaseReference.child(schedulerTitle.getText().toString()).child("Trigger").child("Minute").setValue(min);
            databaseReference.child(schedulerTitle.getText().toString()).child("Trigger").child("AmPm").setValue(ampm);
            databaseReference.child(schedulerTitle.getText().toString()).child("BedroomFans").setValue(bedroomFans.isChecked()?1:0);
            databaseReference.child(schedulerTitle.getText().toString()).child("BedroomLights").setValue(bedroomLights.isChecked()?1:0);
            databaseReference.child(schedulerTitle.getText().toString()).child("DrawingRoomFans").setValue(drawingroomFans.isChecked()?1:0);
            databaseReference.child(schedulerTitle.getText().toString()).child("DrawingRoomLights").setValue(drawingroomLights.isChecked()?1:0);
        }

        Intent intent = new Intent();
        intent.putExtra("Schedule",intentModel);
        setResult(RESULT_OK,intent);
        finish();
    }



    @Override
    public void finish() {
        super.finish();

    }

}
