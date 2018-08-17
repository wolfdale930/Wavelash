package com.tech.areeb.wavelash;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static android.content.Context.NOTIFICATION_SERVICE;

public class AlarmAssigner extends BroadcastReceiver {

    private Context context;
    private List<SchedulerModel> elementList = new ArrayList<>();
    private static int PENDING_INTENT_LIST_SIZE = 0;
    private static List<PendingIntent> pendingIntentList = new ArrayList<>();

    public AlarmAssigner(){}

    public AlarmAssigner(Context context){
        this.context = context;
    }

    public void addSchedulerToList(SchedulerModel element){
        elementList.add(element);
        AssignAlarm();
    }

    private void AssignAlarm(){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        for (SchedulerModel temp : elementList){
            int ampm = temp.getAmPm();
            int hr,min;
            hr = temp.getHour();
            min = temp.getMinute();
            if (ampm == 0){
                if (hr == 12){
                    hr = 0;
                }
            }
            else {
                if (hr>12){
                    hr = hr + 12;
                }
            }
            calendar.set(Calendar.HOUR_OF_DAY, hr);
            calendar.set(Calendar.MINUTE,min);
            Bundle bundle = new Bundle();
            bundle.putString("Name",temp.getSchedulerName());
            bundle.putBoolean("BF",temp.getBedroomFans());
            bundle.putBoolean("BL",temp.getBedroomLights());
            bundle.putBoolean("DF",temp.getDrawingroomFans());
            bundle.putBoolean("DL",temp.getDrawingroomLights());
            Intent intent = new Intent(context,AlarmAssigner.class);
            intent.putExtras(bundle);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context,PENDING_INTENT_LIST_SIZE, intent, 0);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            pendingIntentList.add(pendingIntent);
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),AlarmManager.INTERVAL_DAY,pendingIntent);
        }


    }


    @Override
    public void onReceive(Context context, Intent intent) {

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        Bundle bundle;
        bundle = intent.getExtras();
        if (bundle!=null){
            databaseReference.child("bedroom").child("fan").setValue(bundle.getBoolean("BF")?1:0);
            databaseReference.child("bedroom").child("light").setValue(bundle.getBoolean("BL")?1:0);
            databaseReference.child("drawingroom").child("fan").setValue(bundle.getBoolean("DF")?1:0);
            databaseReference.child("drawingroom").child("light").setValue(bundle.getBoolean("DL")?1:0);



            //NOTIFICATION

            PendingIntent pendingIntent = PendingIntent.getActivity(context,98,new Intent(context,MainActivity.class),PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context,"Wavelash_ID");

            NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
            bigTextStyle.bigText("Triggering Schedule: " + bundle.getString("Name"))
                    .setBigContentTitle("Wavelash Schedule");

            builder.setContentIntent(pendingIntent)
                    .setSmallIcon(R.drawable.logo)
                    .setContentTitle("Wavelash Scheduler")
                    .setContentText("Triggering Schedule: " + bundle.getString("Name"))
                    .setStyle(bigTextStyle);

            NotificationManager manager = (NotificationManager)context.getSystemService(NOTIFICATION_SERVICE);

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                NotificationChannel channel = new NotificationChannel("Wavelash_ID","Set Priority", NotificationManager.IMPORTANCE_DEFAULT);
                manager.createNotificationChannel(channel);
            }

            manager.notify(0 , builder.build());



        }
        else {
            Log.e("TriggerAlarm","Bundle not Found");
        }
    }

}
