package com.tech.areeb.wavelash;


import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class SchedulerFragment extends Fragment{

    ListView SchedulerList;
    List<SchedulerModel> Schedules = new ArrayList<>();
    FloatingActionButton AddSchedules;
    private ArrayAdapter<SchedulerModel> adapter;
    private DatabaseReference databaseReference;
    private int REQ_CODE = 99;

    public SchedulerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragment = inflater.inflate(R.layout.fragment_scheduler, container, false);
        SchedulerList = fragment.findViewById(R.id.scheduler_list);
        AddSchedules = fragment.findViewById(R.id.fab);

        adapter = new ArrayAdapter<SchedulerModel>(getActivity(),R.layout.scheduler_list_item_layout,Schedules){
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                if (convertView==null){
                    convertView = getLayoutInflater().inflate(R.layout.scheduler_list_item_layout,null);
                }
                TextView name = convertView.findViewById(R.id.scheduler_name);
                TextView eventDescription = convertView.findViewById(R.id.event_description);
                name.setText(Schedules.get(position).getSchedulerName());

                //ETA TYPE
                if(Schedules.get(position).getEventType().compareToIgnoreCase("eta")==0){
                    int hr, min, eta;
                    eta = Schedules.get(position).getETA();
                    hr = eta/60;
                    min = eta%60;
                    if (hr == 0){
                        eventDescription.setText(min + " min");
                    }
                    else if(min==0 & hr!=0){
                        eventDescription.setText(hr + " hr");
                    }
                    else {
                        eventDescription.setText(hr + " hr " + min + " min");
                    }

                }
                //ALARM TYPE
                else {
                    int hr, min, ampm;
                    String time;
                    hr = Schedules.get(position).getHour();
                    min = Schedules.get(position).getMinute();
                    ampm = Schedules.get(position).getAmPm();
                    if (min<=10){
                        time = hr + ":0" + min + (ampm==0? " am" : " pm");
                        eventDescription.setText(time);
                    }
                    else {
                        time = hr + ":" + min + (ampm==0? " am" : " pm");
                        eventDescription.setText(time);
                    }
                }

                return convertView;
            }
        };

        SchedulerList.setAdapter(adapter);


        fetchSchedulesFromFirebase();
        adapter.notifyDataSetChanged();

        AddSchedules.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(),AddSchedulerActivity.class);
                startActivityForResult(intent,REQ_CODE);
            }
        });

        SchedulerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                viewSchedule(i);
            }
        });
        return fragment;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_CODE && resultCode == RESULT_OK){
            SchedulerModel intent = (SchedulerModel) data.getExtras().getSerializable("Schedule");
            Schedules.add(intent);
            adapter.notifyDataSetChanged();

        }
        if (requestCode == REQ_CODE && resultCode == RESULT_CANCELED){
            fetchSchedulesFromFirebase();
            adapter.notifyDataSetChanged();
        }
    }

    private void viewSchedule(int position){

        Intent intent = new Intent(getContext(),AddSchedulerActivity.class);
        intent.putExtra("Schedule",Schedules.get(position));
        startActivity(intent);

    }


    private void fetchSchedulesFromFirebase(){
        Schedules.clear();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Schedules");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot temp : dataSnapshot.getChildren()){
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
                        adapter.notifyDataSetChanged();
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


}
