package com.example.alarm3;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String COLUMN_TIME="time";
    public static final String COLUMN_NAME="name";

    private static final String DATABASE_NAME = "alarmdb";
    private static final String TABLE_NAME = "alarminfo";
    private static final int DATABASE_VERSION = 1;

    private static final String CREATE_TABLE_SQL = "CREATE TABLE IF NOT EXISTS "+ TABLE_NAME + "(ID INTEGER PRIMARY KEY AUTOINCREMENT,"+COLUMN_TIME + " VARCHAR,"+COLUMN_NAME+ " VARCHAR)";
    private static final String DROP_TABLE_SQL = "DROP TABLE "+TABLE_NAME;

    private Context context;

    private SQLiteDatabase sqLiteDatabase;

    public DatabaseHelper(Context context){
        super(context, DATABASE_NAME,null,DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //sqLiteDatabase.execSQL(DROP_TABLE_SQL);
        sqLiteDatabase = db;
        sqLiteDatabase.execSQL(CREATE_TABLE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_TABLE_SQL);
    }


    public boolean addAlarm(String time, String name){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TIME,time);
        values.put(COLUMN_NAME,name);

        long result;
        try {
            db.execSQL(CREATE_TABLE_SQL);
            result = db.insert(TABLE_NAME,null, values);

        }catch (SQLException sqle){
            Log.e("SQLException in insert","Data error");
            result = -1;

        }

        if(result == -1){
            return false;

        }else{
            return true;
        }
    }

    public Cursor getAlarmList(){
        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL(CREATE_TABLE_SQL);
        Cursor list = db.rawQuery("SELECT * FROM "+ TABLE_NAME,null);
        return list;
    }

}
