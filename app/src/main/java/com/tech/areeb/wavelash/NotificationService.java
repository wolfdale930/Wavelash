package com.tech.areeb.wavelash;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NotificationService extends IntentService {

    public NotificationService() {
        super("NotificationService");
    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        databaseReference.child("pir").child("pir_out").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int val = dataSnapshot.getValue(Integer.class);
                if (val == 1){
                    Toast.makeText(getApplicationContext(), "Intruder Detected", Toast.LENGTH_LONG).show();

                    PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),98,new Intent(getApplicationContext(),MainActivity.class),0);

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(),"Wavelash_ID");

                    NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
                    bigTextStyle.bigText("Wavelash")
                            .setBigContentTitle("Intruder Detected");

                    builder.setContentIntent(pendingIntent)
                            .setSmallIcon(R.drawable.logo)
                            .setContentTitle("Wavelash Security")
                            .setContentText("Intruder Detected")
                            .setStyle(bigTextStyle);

                    NotificationManager manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                        NotificationChannel channel = new NotificationChannel("Wavelash_ID","Dont know", NotificationManager.IMPORTANCE_DEFAULT);
                        manager.createNotificationChannel(channel);
                    }

                    manager.notify(0 , builder.build());



                    //Trigger Notification tone
                    try {
                        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), uri);
                        r.play();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
