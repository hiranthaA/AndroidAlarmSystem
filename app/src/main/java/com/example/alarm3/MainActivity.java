package com.example.alarm3;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener {

    private TextView txtalarmTime;
    private ListView alarmListView;
    private EditText alarmName;
    private Calendar selectedCalendar;
    private DatabaseHelper db;
    final Context context = this;
    private String answer;
    private String corrAns;
    private static Button buttonCancelAlarm;
    private Button buttonTimePicker;
    private Button buttonSetAlarm;

    public static Context staticcontext;


    @TargetApi(Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        staticcontext = this;

        //setIcon
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_stat_name);

        //initializing elements
        buttonTimePicker = findViewById(R.id.select_time_picker);
        buttonSetAlarm = findViewById(R.id.btn_set_alarm);
        buttonCancelAlarm = findViewById(R.id.btn_cancel_alarm);
        txtalarmTime = findViewById(R.id.text_alarm_time);
        alarmListView = findViewById(R.id.alarmList);
        alarmName = findViewById(R.id.txt_set_alarm_name);
        //---------------------------------------------------------------------

        buttonTimePicker.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                DialogFragment timePicker = new TimePickerFragment();
                timePicker.show(getSupportFragmentManager(),"time picker");
            }
        }
        );

        buttonCancelAlarm.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                //Generate Random number
                int min = 0;
                int max = 20;
                int val1 = new Random().nextInt((max - min) + 1) + min;
                int val2 = new Random().nextInt((max - min) + 1) + min;
                corrAns = Integer.toString(val1+val2);
                //--------------------------

                //AskQuestion
                LayoutInflater li = LayoutInflater.from(context);
                View promptsView = li.inflate(R.layout.askquestion, null);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                alertDialogBuilder.setView(promptsView);

                final EditText userInput = (EditText) promptsView.findViewById(R.id.editTextDialogUserInput);

                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        // get user input and set it to result
                                        // edit text
                                        answer = userInput.getText().toString();
                                        if(answer!=null && answer.equals(corrAns)){
                                            cancelAlarm();
                                        }
                                        else {
                                            showToastMessage("Wrong Answer. Try Again!");
                                        }
                                    }
                                })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        dialog.cancel();
                                    }
                                }).setMessage("What is "+val1+" + "+val2+" ?");

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();
                // show it
                alertDialog.show();
                //------------------------------------
            }
        });

        buttonSetAlarm.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                startAlarm(selectedCalendar);
            }
        });

        //Get Data from Database
        db = new DatabaseHelper(this);
        updateAlarmList();

        if(AlertReceiver.ringtone==null){
            buttonCancelAlarm.setEnabled(false);
            buttonCancelAlarm.setBackgroundColor(getApplication().getColor(R.color.colorButtonGrey));
        }
        else{
            if(AlertReceiver.ringtone.isPlaying()){
                buttonCancelAlarm.setEnabled(true);
                buttonCancelAlarm.setBackgroundColor(getApplication().getColor(R.color.colorCancelButtonEnabled));
            }
            else{
                buttonCancelAlarm.setEnabled(false);
                buttonCancelAlarm.setBackgroundColor(getApplication().getColor(R.color.colorButtonGrey));
            }
        }
    }

    public static Context getContext(){
        return staticcontext;
    }

    public void updateAlarmList(){
        ArrayList<String> alarmlist = new ArrayList<>();
        Cursor data = db.getAlarmList();

        if(data!=null){
            String listString;

            if(data.getCount()>0){
                while (data.moveToNext()){
                    String alarmtime = data.getString(1);
                    String alarmname = data.getString(2);

                    listString = alarmname + " : " + alarmtime;

                    alarmlist.add(listString);
                    ListAdapter listAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,alarmlist);
                    alarmListView.setAdapter(listAdapter);
                }
            }
        }
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY,hourOfDay);
        calendar.set(Calendar.MINUTE,minute);
        calendar.set(Calendar.SECOND,0);
        selectedCalendar = calendar;
        updateTimeText(selectedCalendar);
        //startAlarm(calendar);
    }

    private void updateTimeText(Calendar calendar){
        String timeText = "Set For: ";
        timeText += DateFormat.getTimeInstance(DateFormat.SHORT).format(calendar.getTime());

        txtalarmTime.setText(timeText);
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void startAlarm(Calendar calendar){

        if(alarmName.getText().toString().equals("")){
            showToastMessage("Invalid Data. Cannot Set Alarm!");
        }
        else if(selectedCalendar==null){
            showToastMessage("Invalid Data. Cannot Set Alarm!");
        }
        else{
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(this,AlertReceiver.class);
            intent.putExtra("alarmTitle", alarmName.getText().toString());
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this,1,intent,0);

            if(calendar.before(Calendar.getInstance())){
                calendar.add(Calendar.DATE,1);
            }

            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),pendingIntent);
            String timeText = DateFormat.getTimeInstance(DateFormat.SHORT).format(calendar.getTime());

            //--Adding to database
            db.addAlarm(timeText,alarmName.getText().toString());
            //--------------------

            //clear previous alarm data
            alarmName.setText("");
            txtalarmTime.setText("");
            selectedCalendar=null;
            showToastMessage("Alarm Created!");
            updateAlarmList();
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void cancelAlarm(){
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this,AlertReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,1,intent,0);

        alarmManager.cancel(pendingIntent);
        showToastMessage("Alarm Canceled!");

        if(AlertReceiver.ringtone.isPlaying()){
            AlertReceiver.ringtone.stop();
            buttonCancelAlarm.setEnabled(false);
            buttonCancelAlarm.setBackgroundColor(getApplication().getColor(R.color.colorButtonGrey));

            NotificationHelper notificationHelper = new NotificationHelper(context,"Alarm");
            notificationHelper.getManager().cancel(999);
        }

    }

    private void showToastMessage(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @TargetApi(Build.VERSION_CODES.O)
    public static void enableCancelButton(){
        buttonCancelAlarm.setEnabled(true);
        buttonCancelAlarm.setBackgroundColor(getContext().getColor(R.color.colorCancelButtonEnabled));
    }
}

