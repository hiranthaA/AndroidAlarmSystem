package com.example.alarm3;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.widget.Button;

public class AlertReceiver extends BroadcastReceiver {

    public static Ringtone ringtone;
    public Button cancelAlarm;

    @TargetApi(Build.VERSION_CODES.P)
    @Override
    public void onReceive(Context context, Intent intent) {

        String alarmTitle = intent.getStringExtra("alarmTitle");
        int alarmTone = intent.getIntExtra("alarmTone",1);
        NotificationHelper notificationHelper = new NotificationHelper(context,alarmTitle);
        NotificationCompat.Builder notificationBuilder = notificationHelper.getChannelNotification();
        notificationHelper.getManager().notify(999,notificationBuilder.build());
        Uri alarmUri = RingtoneManager.getDefaultUri(alarmTone);
        if(alarmUri==null){
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        }
        ringtone = RingtoneManager.getRingtone(context, alarmUri);
        ringtone.play();
        ringtone.setLooping(true);
        MainActivity.enableCancelButton();
    }
}
