package com.example.guilh.bluetootharduino.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class BluetoothArduinoContract {

    private BluetoothArduinoContract() {

    }

    public static final String CONTENT_AUTHORITY = "com.example.guilh.bluetootharduino";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_BLUETOOTH_ARDUINO = "bluetootharduino";

    public static final class BluetoothArduinoEntry implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_BLUETOOTH_ARDUINO);

        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BLUETOOTH_ARDUINO;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BLUETOOTH_ARDUINO;

        public final static String TABLE_NAME = "bluetootharduino";

        public final static String _ID = BaseColumns._ID;

        public final static String COLUMN_ITERATION = "iteration";
        public final static String COLUMN_WEIGHT = "weight";
        public final static String COLUMN_FORCE = "force";
        public final static String COLUMN_TIME = "time";
        public final static String COLUMN_ALERT = "alert";
    }
}
