package com.tech.areeb.wavelash;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RoomOnClickActivity extends Activity {

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roomonclick);
        Intent intent= getIntent();
        int roomno = intent.getIntExtra("RoomId",99);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        final Switch lights, fans;
        lights = findViewById(R.id.lights);
        fans = findViewById(R.id.fans);

        switch (roomno){
            case 0:

                databaseReference.child("bedroom").child("light").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        int curr_light_value;
                        curr_light_value = Integer.parseInt(dataSnapshot.getValue().toString());
                        if (curr_light_value == 1) {
                            lights.setChecked(true);
                        } else
                            lights.setChecked(false);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                databaseReference.child("bedroom").child("fan").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        int curr_fan_value;
                        curr_fan_value = Integer.parseInt(dataSnapshot.getValue().toString());
                        if (curr_fan_value == 1) {
                            fans.setChecked(true);
                        } else
                            fans.setChecked(false);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });



                lights.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            if (b)
                                databaseReference.child("bedroom").child("light").setValue("1");
                            else
                                databaseReference.child("bedroom").child("light").setValue("0");
                        }
                });
                fans.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        if (b)
                            databaseReference.child("bedroom").child("fan").setValue("1");
                        else
                            databaseReference.child("bedroom").child("fan").setValue("0");
                    }
                });

                break;

            case 1:

                databaseReference.child("drawingroom").child("light").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        int curr_light_value;
                        curr_light_value = Integer.parseInt(dataSnapshot.getValue().toString());
                        if (curr_light_value == 1) {
                            lights.setChecked(true);
                        } else
                            lights.setChecked(false);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                databaseReference.child("drawingroom").child("fan").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        int curr_fan_value;
                        curr_fan_value = Integer.parseInt(dataSnapshot.getValue().toString());
                        if (curr_fan_value == 1) {
                            fans.setChecked(true);
                        } else
                            fans.setChecked(false);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });





                lights.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b)
                        databaseReference.child("drawingroom").child("light").setValue("1");
                    else
                        databaseReference.child("drawingroom").child("light").setValue("0");
                }
            });
                fans.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        if (b)
                            databaseReference.child("drawingroom").child("fan").setValue("1");
                        else
                            databaseReference.child("drawingroom").child("fan").setValue("0");
                    }
                });

                break;
            default:
                break;
        }
    }
}
