package com.example.guilh.bluetootharduino.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.guilh.bluetootharduino.data.BluetoothArduinoContract.BluetoothArduinoEntry;

public class BluetoothArduinoDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "bluetoothdata.db";

    private static final int DATABASE_VERSION = 1;

    public BluetoothArduinoDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_BLUETOOTHDATA_TABLE = "CREATE TABLE " + BluetoothArduinoEntry.TABLE_NAME + " (" +
                BluetoothArduinoEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                BluetoothArduinoEntry.COLUMN_ITERATION + " INTEGER, " +
                BluetoothArduinoEntry.COLUMN_WEIGHT + " REAL, " +
                BluetoothArduinoEntry.COLUMN_FORCE + " REAL, " +
                BluetoothArduinoEntry.COLUMN_TIME + " INTEGER," +
                BluetoothArduinoEntry.COLUMN_ALERT + " INTEGER);";
        db.execSQL(SQL_CREATE_BLUETOOTHDATA_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
