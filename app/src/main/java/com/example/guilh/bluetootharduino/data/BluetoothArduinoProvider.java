package com.example.guilh.bluetootharduino.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

public class BluetoothArduinoProvider extends ContentProvider {

    private static final int BLUETOOTHARDUINO = 100;
    private static final int BLUETOOTHARDUINO_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private BluetoothArduinoDbHelper mDbHelper;

    static {
        sUriMatcher.addURI(BluetoothArduinoContract.CONTENT_AUTHORITY, BluetoothArduinoContract.PATH_BLUETOOTH_ARDUINO, BLUETOOTHARDUINO);
        sUriMatcher.addURI(BluetoothArduinoContract.CONTENT_AUTHORITY, BluetoothArduinoContract.PATH_BLUETOOTH_ARDUINO + "/#", BLUETOOTHARDUINO_ID);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new BluetoothArduinoDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case BLUETOOTHARDUINO:
                cursor = database.query(
                        BluetoothArduinoContract.BluetoothArduinoEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            case BLUETOOTHARDUINO_ID:
                selection = BluetoothArduinoContract.BluetoothArduinoEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(
                        BluetoothArduinoContract.BluetoothArduinoEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

                default:
                    throw new IllegalArgumentException("Cannot query unkown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BLUETOOTHARDUINO:
                return insertBluetoothArduino(uri, values);

                default:
                    throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertBluetoothArduino(Uri uri, ContentValues values) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        long id = database.insert(BluetoothArduinoContract.BluetoothArduinoEntry.TABLE_NAME, null, values);

        if (id == -1) {
            Log.e("Provider", "Failed to insert row for " + uri);
            return null;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowsDeleted;
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BLUETOOTHARDUINO:
                rowsDeleted = database.delete(
                        BluetoothArduinoContract.BluetoothArduinoEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;

                default:
                    throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BLUETOOTHARDUINO:
                return BluetoothArduinoContract.BluetoothArduinoEntry.CONTENT_LIST_TYPE;

            case BLUETOOTHARDUINO_ID:
                return BluetoothArduinoContract.BluetoothArduinoEntry.CONTENT_ITEM_TYPE;

                default:
                    throw new IllegalArgumentException("Unknown URI " + uri + " with match " + match);
        }
    }
}
