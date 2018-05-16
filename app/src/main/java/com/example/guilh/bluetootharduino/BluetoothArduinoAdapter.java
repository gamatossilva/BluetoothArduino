package com.example.guilh.bluetootharduino;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.guilh.bluetootharduino.data.BluetoothArduinoContract;

public class BluetoothArduinoAdapter extends RecyclerView.Adapter<BluetoothArduinoAdapter.BluetoothArduinoAdapterViewHolder> {

    private final Context mContext;

    private Cursor mCursor;

    public BluetoothArduinoAdapter(Context context) {
        mContext = context;
    }


    @NonNull
    @Override
    public BluetoothArduinoAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_item_bluetooth_data, parent, false);
        view.setFocusable(true);

        return new BluetoothArduinoAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BluetoothArduinoAdapterViewHolder holder, int position) {
        mCursor.moveToPosition(position);

        //int iteration = mCursor.getInt(MainActivity.INDEX_BLUETOOTHARDUINO_ITERATION);
        int iterationColumnIndex = mCursor.getColumnIndex(BluetoothArduinoContract.BluetoothArduinoEntry.COLUMN_ITERATION);
        int iteration = mCursor.getInt(iterationColumnIndex);
        holder.iterationTextView.setText(String.valueOf(iteration));

        //long weight = mCursor.getLong(MainActivity.INDEX_BLUETOOTHARDUINO_WEIGHT);
        int weightColumnIndex = mCursor.getColumnIndex(BluetoothArduinoContract.BluetoothArduinoEntry.COLUMN_WEIGHT);
        float weight = mCursor.getFloat(weightColumnIndex);
        holder.weightTextView.setText(String.valueOf(weight));

        //long force = mCursor.getLong(MainActivity.INDEX_BLUETOOTHARDUINO_FORCE);
        int forceColumnIndex = mCursor.getColumnIndex(BluetoothArduinoContract.BluetoothArduinoEntry.COLUMN_FORCE);
        float force = mCursor.getFloat(forceColumnIndex);
        holder.forceTextView.setText(String.valueOf(force));

        //int time = mCursor.getInt(MainActivity.INDEX_BLUETOOTHARDUINO_TIME);
        int timeColumnIndex = mCursor.getColumnIndex(BluetoothArduinoContract.BluetoothArduinoEntry.COLUMN_TIME);
        int time = mCursor.getInt(timeColumnIndex);
        holder.timeTextView.setText(String.valueOf(time));

        //int alert = mCursor.getInt(MainActivity.INDEX_BLUETOOTHARDUINO_ALERT);
        int alertColumnIndex = mCursor.getColumnIndex(BluetoothArduinoContract.BluetoothArduinoEntry.COLUMN_ALERT);
        int alert = mCursor.getInt(alertColumnIndex);

        if (alert == 1) {
            holder.alertTextView.setText(R.string.alert_message_on);
        } else {
            holder.alertTextView.setText(R.string.alert_message_off);
        }

    }

    @Override
    public int getItemCount() {
        if (null == mCursor) {
            return 0;
        }
        return mCursor.getCount();
    }

    void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }

    class BluetoothArduinoAdapterViewHolder extends RecyclerView.ViewHolder {
        final TextView iterationTextView;

        final TextView weightTextView;

        final TextView forceTextView;

        final TextView timeTextView;

        final TextView alertTextView;

        BluetoothArduinoAdapterViewHolder(View view) {
            super(view);

            iterationTextView = (TextView) view.findViewById(R.id.textViewIteration);

            weightTextView = (TextView) view.findViewById(R.id.textViewWeight);

            forceTextView = (TextView) view.findViewById(R.id.textViewForce);

            timeTextView = (TextView) view.findViewById(R.id.textViewTime);

            alertTextView = (TextView) view.findViewById(R.id.textViewAlert);
        }
    }
}
