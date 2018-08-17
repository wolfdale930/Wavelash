package com.tech.areeb.wavelash;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ScheduleHandler {

    private Context context;
    private  List<SchedulerModel> Schedules = new ArrayList<>();
    private DatabaseReference databaseReference;
    private long SCHEDULE_LIST_SIZE;

    public ScheduleHandler(Context context){
        this.context = context;
        fetchFormFirebase();
    }

    private void fetchFormFirebase(){
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Schedules");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot temp : dataSnapshot.getChildren()){
                        SCHEDULE_LIST_SIZE = dataSnapshot.getChildrenCount();

                        String name, type;
                        int hr, min, ampm, eta;
                        int bf, bl, df, dl;
                        SchedulerModel model;
                        name = temp.getKey();
                        bf = ((Long)temp.child("BedroomFans").getValue()).intValue();
                        bl = ((Long)temp.child("BedroomLights").getValue()).intValue();
                        df = ((Long)temp.child("DrawingRoomFans").getValue()).intValue();
                        dl = ((Long)temp.child("DrawingRoomLights").getValue()).intValue();

                        type = (String) temp.child("Type").getValue();
                        if (type.equalsIgnoreCase("alarm")){
                            hr = ((Long) temp.child("Trigger").child("Hour").getValue()).intValue();
                            min = ((Long) temp.child("Trigger").child("Minute").getValue()).intValue();
                            ampm = ((Long) temp.child("Trigger").child("AmPm").getValue()).intValue();

                            model = new SchedulerModel(name,hr,min,ampm);
                            model.setBedroomFans(bf==1);
                            model.setBedroomLights(bl==1);
                            model.setDrawingroomFans(df==1);
                            model.setDrawingroomLights(dl==1);
                        }
                        else {
                            eta = ((Long) temp.child("Trigger").getValue()).intValue();
                            model = new SchedulerModel(name,eta);
                            model.setBedroomFans(bf==1);
                            model.setBedroomLights(bl==1);
                            model.setDrawingroomFans(df==1);
                            model.setDrawingroomLights(dl==1);
                        }

                        Schedules.add(model);
                    }

                    assignhandler();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void assignhandler(){
        LocationTrackerService locationTrackerService = new LocationTrackerService(context);
        AlarmAssigner alarmAssigner = new AlarmAssigner(context);
        if (Schedules.size()<SCHEDULE_LIST_SIZE){
            assignhandler();
        }
        else {
            for (SchedulerModel temp : Schedules) {
                if (temp.getEventType().equalsIgnoreCase("alarm")) {
                    alarmAssigner.addSchedulerToList(temp);
                }
                if (temp.getEventType().equalsIgnoreCase("eta")) {
                    locationTrackerService.addScheduleToList(temp);
                    if (!locationTrackerService.isCanGetLocation()) {
                        locationTrackerService.showSettingsAlert();
                    }

                }
            }
        }
    }








}
