package com.example.guilh.bluetootharduino;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.guilh.bluetootharduino.data.BluetoothArduinoContract;

import java.util.ArrayList;

public class LocalService extends Service {
    private final IBinder mBinder = new LocalBinder();

    private BluetoothAdapter mBtAdapter;
    private BluetoothChatService mChatService;

    private boolean connection = false;

    private String allData;
    StringBuilder bluetoothData = new StringBuilder();
    StringBuilder serviceData = new StringBuilder();

    public static final String ACTION_BLUETOOTH_BROADCAST = "Service";
    public static final String DATA = "data";

    public class LocalBinder extends Binder {
        LocalService getService() {
            return LocalService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            setupChat();
            mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        return START_NOT_STICKY;
    }

    public void connectDevice(Intent intentm, boolean secure) {
        String address = intentm.getExtras().getString(DeviceList.EXTRA_DEVICE_ADDRESS);
        BluetoothDevice device = mBtAdapter.getRemoteDevice(address);
        mChatService.connect(device, secure);
    }

    public void disconnectDevice() {
        mChatService.write(getString(R.string.disconnect_device).getBytes());
        mChatService.stop();
        connection = false;
        stopSelf();
    }

    public boolean isConnected() {
        return connection;
    }

    public void sendMessage(String message) {
        serviceData.delete(0, serviceData.length());

        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, getString(R.string.not_connected_device), Toast.LENGTH_SHORT).show();
            return;
        }

        if (message.trim().length() > 0) {
            mChatService.write(message.getBytes());
        }
    }

    public void sendData() {
        if (connection) {
            //sendBroadcastMessage(allData);
        }
    }

    public void setupChat() {
        if (mChatService == null) {
            mChatService = new BluetoothChatService(this, mHandler);
        }
    }

    /*private void sendBroadcastMessage(String string) {
        if (string != null) {
            Intent intent = new Intent(ACTION_BLUETOOTH_BROADCAST);
            intent.putExtra(DATA, string);
            sendBroadcast(intent);
        }
    }*/


    private void sendBroadcastMessage() {
            Intent intent = new Intent(ACTION_BLUETOOTH_BROADCAST);
            sendBroadcast(intent);

    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            Toast.makeText(LocalService.this, getString(R.string.device_connected), Toast.LENGTH_SHORT).show();
                            connection = true;

                            serviceData.delete(0, serviceData.length());
                            //sendBroadcastMessage(getString(R.string.device_connected));
                            //sendBroadcastMessage("");
                            sendBroadcastMessage();
                            break;

                        case BluetoothChatService.STATE_CONNECTING:
                            Toast.makeText(LocalService.this, getString(R.string.device_connecting), Toast.LENGTH_SHORT).show();
                            break;

                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            Toast.makeText(LocalService.this, getString(R.string.device_not_connected), Toast.LENGTH_SHORT).show();
                            connection = false;
                            sendBroadcastMessage();
                            break;
                    }
                    break;

                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;

                    String writeMessage = new String(writeBuf);
                    break;

                case Constants.MESSAGE_READ:
                    String received = (String) msg.obj;
                    ContentValues values = new ContentValues();

                    String[] splited = received.split("\n");
                    ArrayList<String> dataArrayList = new ArrayList<>();

                    for (String string : splited) {
                        if (!string.isEmpty() && !string.equals("{") && !string.equals("}")) {
                            String[] splitedAgain = string.split(": ");
                            dataArrayList.add(splitedAgain[1]);
                        }
                    }

                    values.put(BluetoothArduinoContract.BluetoothArduinoEntry.COLUMN_ITERATION, Integer.parseInt(dataArrayList.get(0)));
                    values.put(BluetoothArduinoContract.BluetoothArduinoEntry.COLUMN_WEIGHT, Float.parseFloat(dataArrayList.get(1)));
                    values.put(BluetoothArduinoContract.BluetoothArduinoEntry.COLUMN_FORCE, Float.parseFloat(dataArrayList.get(2)));
                    values.put(BluetoothArduinoContract.BluetoothArduinoEntry.COLUMN_TIME, Integer.parseInt(dataArrayList.get(3)));
                    values.put(BluetoothArduinoContract.BluetoothArduinoEntry.COLUMN_ALERT, Integer.parseInt(dataArrayList.get(4)));


                    Uri newUri = getContentResolver().insert(BluetoothArduinoContract.BluetoothArduinoEntry.CONTENT_URI, values);

                    if (dataArrayList.get(4).equals("1")) {
                        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getBaseContext())
                                .setColor(ContextCompat.getColor(LocalService.this, R.color.colorPrimary))
                                .setSmallIcon(R.drawable.ic_stat_name)
                                .setVibrate(new long[]{0, 500})
                                .setContentTitle(getString(R.string.app_name))
                                .setContentText("Alerta")
                                .setAutoCancel(true);

                        Intent intent = new Intent(LocalService.this, MainActivity.class);
                        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(LocalService.this);
                        taskStackBuilder.addNextIntentWithParentStack(intent);

                        PendingIntent pendingIntent = taskStackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

                        notificationBuilder.setContentIntent(pendingIntent);

                        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                        notificationManager.notify(1, notificationBuilder.build());
                        try {
                            Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                            Ringtone ringtone = RingtoneManager.getRingtone(LocalService.this, sound);
                            ringtone.play();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }


                    /*int endInformation = bluetoothData.indexOf("}");

                    if (endInformation > 0) {
                        String completeData = bluetoothData.substring(0, endInformation);
                        int informationLenght = completeData.length();

                        if (completeData.length() > 2 && (completeData.charAt(0) == '{' || completeData.charAt(1) == '{' || completeData.charAt(2) == '{')) {
                            String finalData = bluetoothData.substring(1, informationLenght);

                            serviceData.append(finalData);
                            allData = serviceData.substring(0, serviceData.length());

                            String[] data = finalData.split("\n");
                            boolean status = (data != null);

                            ArrayList<String> dataArrayList = new ArrayList<>();
                            //Log.d("RECEBIDO", "recebido: " + data);
                            for (int i = 0; i < data.length; i++) {
                                //Log.d("RECEBIDO", "recebido: " + data[i]);
                                String[] dataInDatabase = data[i].split(": ");
                                dataArrayList.add(dataInDatabase[1]);

                                //Log.d("RECEBIDO", "recebido dataInDatabase: " + dataInDatabase[0]);

                            }
                            //for (int i = 0; i < dataArrayList.size(); i++) {
                                //Log.d("RECEBIDO", dataArrayList.get(i));
                            //}
                            //Log.d("RECEBIDO", dataArrayList.get(0));
                            /*String[] iteration = data[0].split(": ");
                            String[] weight = data[1].split(": ");
                            String[] force = data[2].split(": ");
                            String[] time = data[3].split(": ");

                            /*values.put(BluetoothArduinoContract.BluetoothArduinoEntry.COLUMN_ITERATION, Integer.parseInt(iteration[1]));
                            values.put(BluetoothArduinoContract.BluetoothArduinoEntry.COLUMN_WEIGHT, Float.parseFloat(weight[1]));
                            values.put(BluetoothArduinoContract.BluetoothArduinoEntry.COLUMN_FORCE, Float.parseFloat(force[1]));
                            values.put(BluetoothArduinoContract.BluetoothArduinoEntry.COLUMN_TIME, Integer.parseInt(time[1]));

                        }
                        bluetoothData.delete(0, bluetoothData.length());
                    }*/
                    break;

                case Constants.MESSAGE_DEVICE_NAME:
                    break;
            }
        }
    };
}
