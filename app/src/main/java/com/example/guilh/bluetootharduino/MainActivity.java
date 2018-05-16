package com.example.guilh.bluetootharduino;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.guilh.bluetootharduino.data.BluetoothArduinoContract;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int REQUEST_ACTIVATION = 0;
    private static final int REQUEST_CONNECTION = 1;
    private static final int ID_BLUETOOTHARDUINO_LOADER = 44;
    private static final int MY_PERMISSION_EXTERNAL_STORAGE_REQUEST_CODE = 88;

    private final SimpleDateFormat dataFormater = new SimpleDateFormat("yyyyMMddHHmmssSSS");

    private BluetoothArduinoAdapter mBluetoothArduinoAdapter;
    private RecyclerView mRecyclerView;
    private int mPosision = RecyclerView.NO_POSITION;

    private Button connectionButton;
    private EditText etSentData;
    private TextView tvDataSent;

    private TextView iterationTextViewLabel;
    private TextView weightTextViewLabel;
    private TextView forceTextViewLabel;
    private TextView timeTextViewLabel;
    private TextView alertTextViewLabel;

    private boolean connection = false;
    private boolean isReceiverRegistered;
    private LocalService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button sendButton = (Button) findViewById(R.id.buttonSend);
        final TextView tvDataSentLabel = (TextView) findViewById(R.id.tvSentDataLabel);
        connectionButton = (Button) findViewById(R.id.buttonConnection);
        etSentData = (EditText) findViewById(R.id.etSentData);
        tvDataSent = (TextView) findViewById(R.id.tvDataSent);

        iterationTextViewLabel = (TextView) findViewById(R.id.textViewIterationLabel);

        weightTextViewLabel = (TextView) findViewById(R.id.textViewWeightLabel);

        forceTextViewLabel = (TextView) findViewById(R.id.textViewForceLabel);

        timeTextViewLabel = (TextView) findViewById(R.id.textViewTimeLabel);

        alertTextViewLabel = (TextView) findViewById(R.id.textViewAlertLabel);

        mRecyclerView = (RecyclerView) findViewById(R.id.rvDataReceived);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        mBluetoothArduinoAdapter = new BluetoothArduinoAdapter(this);

        mRecyclerView.setAdapter(mBluetoothArduinoAdapter);

        getSupportLoaderManager().initLoader(ID_BLUETOOTHARDUINO_LOADER, null, this);

        BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBtAdapter == null) {
            Toast.makeText(this, getString(R.string.no_bluetooth_adapter), Toast.LENGTH_SHORT).show();
        } else if (!mBtAdapter.isEnabled()) {
            Intent activateBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(activateBluetooth, REQUEST_ACTIVATION);
        }


        connectionButton.setText(getString(R.string.connection_button_to_connect));
        connectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connection = mService.isConnected();
                if (connection) {
                    tvDataSent.setText(R.string.disconnect_device);
                    mService.disconnectDevice();
                    connectionButton.setText(R.string.connection_button_to_connect);
                } else {
                    Intent intent = new Intent(MainActivity.this, DeviceList.class);
                    startActivityForResult(intent, REQUEST_CONNECTION);
                    tvDataSent.setText("");
                    //TODO (1): dado recebido
                }
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connection = mService.isConnected();
                if (connection) {
                    tvDataSentLabel.setText(R.string.data_sent_label);
                    tvDataSent.setText(etSentData.getText().toString());
                    mService.sendMessage(etSentData.getText().toString());
                    etSentData.setText("");
                } else {
                    mService.sendMessage(etSentData.getText().toString());
                    etSentData.setText("");
                }
                //TODO (2): dado recebido
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ACTIVATION:
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, getString(R.string.bluetooth_activated), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, getString(R.string.bluetooth_not_activated), Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;

            case REQUEST_CONNECTION:
                if (resultCode == Activity.RESULT_OK) {
                    mService.connectDevice(data, false);

                    registerReceiver(mHandleMessageReceiver, new IntentFilter(LocalService.ACTION_BLUETOOTH_BROADCAST));
                    isReceiverRegistered = true;
                    connection = mService.isConnected();
                    if (connection) {
                        connectionButton.setText(R.string.connection_button_to_disconnect);
                    }
                }
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, LocalService.class);
        startService(intent);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isReceiverRegistered) {
            try {
                unregisterReceiver(mHandleMessageReceiver);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
            isReceiverRegistered = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isReceiverRegistered) {
            registerReceiver(mHandleMessageReceiver, new IntentFilter(LocalService.ACTION_BLUETOOTH_BROADCAST));
            isReceiverRegistered = true;
        }
        Intent intent = new Intent(this, LocalService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mConnection != null) {
            unbindService(mConnection);
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocalService.LocalBinder binder = (LocalService.LocalBinder) service;

            mService = binder.getService();
            connection = mService.isConnected();
            mService.sendData();
            if (connection) {
                connectionButton.setText(R.string.connection_button_to_disconnect);
            } else {
                connectionButton.setText(R.string.connection_button_to_connect);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            /*String message = intent.getExtras().getString(LocalService.DATA);
            if (message.contains(getString(R.string.device_connected))) {
                connectionButton.setText(R.string.connection_button_to_disconnect);
                //TODO (3): dado recebido
            } else {
                //connectionButton.setText(R.string.connection_button_to_connect);
                //TODO (4): dado recebido
            }*/
            if (mService.isConnected()) {
                connectionButton.setText(R.string.connection_button_to_disconnect);
            } else {
                connectionButton.setText(R.string.connection_button_to_connect);
            }
        }
    };

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        switch (id) {
            case ID_BLUETOOTHARDUINO_LOADER:
                Uri blueoothArduinoUri = BluetoothArduinoContract.BluetoothArduinoEntry.CONTENT_URI;
                String sortOrder = BluetoothArduinoContract.BluetoothArduinoEntry._ID + " ASC";

                return new CursorLoader(this,
                        blueoothArduinoUri,
                        null,
                        null,
                        null,
                        sortOrder);

                default:
                    throw new RuntimeException("Loader Not Implemented: " + id);
        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        mBluetoothArduinoAdapter.swapCursor(data);
        iterationTextViewLabel.setText(R.string.iteration_label);
        weightTextViewLabel.setText(R.string.weight_label);
        forceTextViewLabel.setText(R.string.force_label);
        timeTextViewLabel.setText(R.string.time_label);
        alertTextViewLabel.setText(R.string.alert_label);

        if (mPosision == RecyclerView.NO_POSITION) {
            mPosision = 0;
        }
        mRecyclerView.smoothScrollToPosition(mPosision);
        if (data.getCount() != 0) {
            mRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mBluetoothArduinoAdapter.swapCursor(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_export) {
            setupPermission();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void exportDB() {
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String currentDBPath = "//data//" +
                        "com.example.guilh.bluetootharduino" +
                        "//databases//" +
                        "bluetoothdata.db";

                String backupDBPath = String.format(Constants.DATABASE_NAME, dataFormater.format(new Date()));

                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                MediaScannerConnection.scanFile(this, new String[] { backupDB.getPath()}, null, null);


                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.delete_data_in_database_msg);
                builder.setPositiveButton(R.string.delete_data_in_database, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getContentResolver().delete(BluetoothArduinoContract.BluetoothArduinoEntry.CONTENT_URI, null, null);
                        Toast.makeText(MainActivity.this, getString(R.string.save_backup_successful_msg), Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton(R.string.keep_data_in_database, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (dialogInterface != null) {
                            dialogInterface.dismiss();
                            Toast.makeText(MainActivity.this, getString(R.string.save_backup_successful_msg), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();


            }
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.save_backup_fail_msg), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void setupPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                String[] permissionsWeNeed = new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE };
                requestPermissions(permissionsWeNeed, MY_PERMISSION_EXTERNAL_STORAGE_REQUEST_CODE);
            }
        } else {
            exportDB();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_EXTERNAL_STORAGE_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    exportDB();
                } else {
                    Toast.makeText(this, getString(R.string.permission_not_granted_message), Toast.LENGTH_SHORT).show();
                }
        }
    }
}
