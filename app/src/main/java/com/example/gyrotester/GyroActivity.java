package com.example.gyrotester;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.AsyncQueryHandler;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Set;
import java.util.UUID;

import static java.lang.System.out;

public class GyroActivity extends AppCompatActivity implements SensorEventListener {

    //BLUETOOTH
    boolean socketConnected = false;
    OutputStream outputStream;
    InputStream inputStream;

    private BluetoothDevice HC05;
    BluetoothSocket socket;
    final String ADRESS = "HC-05";
    final UUID myUUID = UUID.fromString("ee7349ca-3ecc-4472-8203-f7f618789b88");
    BluetoothAdapter bluetoothAdapter;




    //UI
    Button button;
    GraphView graphX, graphY, graphZ;
    private LineGraphSeries<DataPoint> xSeries, ySeries, zSeries;
    private LineGraphSeries<DataPoint> xServo, yServo, zServo;
    private int newServo1, newServo2 = 0;
    private CountDownTimer graphTimer;
    private Handler handler;
    private double time = 0d;
    private double time2 = 0d;
    private float[] accel = new float[3];
    private float[] magnet = new float[3];

    private float[] rotationMatrix = new float[9];
    private float[] angles = new float[3];

    private SensorManager sensorManager;
    private double lastX, lastY, lastZ = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gyro);
            new ConnectBluetooth().execute();

        button = findViewById(R.id.test);
        graphX = findViewById(R.id.graphX);
        graphX.getViewport().setXAxisBoundsManual(true);
        graphX.getViewport().setMinX(0);
        graphX.getViewport().setMaxX(5);

        graphX.getViewport().setYAxisBoundsManual(true);
        graphX.getViewport().setMinY(-5);
        graphX.getViewport().setMaxY(5);

        graphX.getViewport().setScrollable(true);
        graphX.getViewport().setScrollableY(true);
        graphX.getViewport().setScalable(true);
        graphX.getViewport().setScalableY(true);

        graphX.setTitle("X-Axis Data");

        graphY = findViewById(R.id.graphY);
        graphY.getViewport().setXAxisBoundsManual(true);
        graphY.getViewport().setMinX(0);
        graphY.getViewport().setMaxX(5);

        graphY.getViewport().setYAxisBoundsManual(true);
        graphY.getViewport().setMinY(-5);
        graphY.getViewport().setMaxY(5);

        graphY.getViewport().setScrollable(true);
        graphY.getViewport().setScrollableY(true);
        graphY.getViewport().setScalable(true);
        graphY.getViewport().setScalableY(true);

        graphY.setTitle("Y-Axis Data");

        graphZ = findViewById(R.id.graphZ);
        graphZ.getViewport().setXAxisBoundsManual(true);
        graphZ.getViewport().setMinX(0);
        graphZ.getViewport().setMaxX(5);

        graphZ.getViewport().setYAxisBoundsManual(true);
        graphZ.getViewport().setMinY(-5);
        graphZ.getViewport().setMaxY(5);

        graphZ.getViewport().setScrollable(true);
        graphZ.getViewport().setScrollableY(true);
        graphZ.getViewport().setScalable(true);
        graphZ.getViewport().setScalableY(true);

        graphZ.setTitle("Z-Axis Data");

        xSeries=new LineGraphSeries<>();
        xSeries.setColor(Color.RED);

        ySeries=new LineGraphSeries<>();
        ySeries.setColor(Color.RED);

        zSeries=new LineGraphSeries<>();
        zSeries.setColor(Color.RED);

        zServo = new LineGraphSeries<>();
        zServo.setColor(Color.GREEN);

        graphX.addSeries(xSeries);
        graphY.addSeries(ySeries);
        graphZ.addSeries(zSeries);
        graphZ.addSeries(zServo);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                sensorManager.SENSOR_DELAY_GAME);

        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                sensorManager.SENSOR_DELAY_GAME);

        handler = new Handler();
            //RECEPTION
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    int bytes;


                    // Keep listening to the InputStream until an exception occurs.
                    while (inputStream != null) {
                        try {
                            int bytecount = inputStream.available();
                            if (bytecount>0) {
                                byte[] buffer = new byte[bytecount];
                                bytes = inputStream.read(buffer);
                                for (int i=0; i<bytecount; i++){

                                    if (buffer[i]=="*".getBytes()[0]){

                                        try{
                                            handler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Log.d("*","*********************");
                                                }
                                            });
                                        }catch (Exception e){
                                            Log.d("*","fail *");
                                        }
                                        newServo1 = buffer[i+1];
                                    }
                                    else if (buffer[i]==",".getBytes()[0]){
                                        try{
                                            handler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Log.d(",",",,,,,,,,,,,,,,,,,,,,,");
                                                }
                                            });
                                        }catch (Exception e){
                                            Log.d(",","fail ,");
                                        }
                                        newServo2 = buffer[i+1];
                                    }
                                }

                                Message readMessage = handler.obtainMessage(0, bytes, -1, buffer);
                                readMessage.sendToTarget();
                            }
                            //DO STH HERE

                        } catch (IOException e) {
                            Log.d("GYRO", "Instream distrupted", e);
                            break;
                        }
                    }
                }
            });
            thread.start();

            //TRANSMISSION


                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        sampleGyro();

                        if (socketConnected) {
                            try {
                                socket.getOutputStream().write('x');
                                Toast.makeText(getApplicationContext(), "sending...", Toast.LENGTH_SHORT);
                            } catch (IOException e) {
                                Toast.makeText(getApplicationContext(), "could not send data", Toast.LENGTH_LONG);
                            }
                        }
                    }
                });



        }



    private void sampleGyro(){
        graphTimer = new CountDownTimer(15000,80) {
            @Override
            public void onTick(long l) {

                SensorManager.getRotationMatrix(rotationMatrix, null,
                        accel, magnet);
                SensorManager.getOrientation(rotationMatrix, angles);
                if (!(lastZ == 0 && lastX == 0 && lastY== 0)){
                    lastZ=(angles[0]+lastZ)/2;
                    lastX=(angles[1]+lastX)/2;
                    lastY=(angles[2]+lastY)/2;
                }
                else {
                    lastZ = angles[0];
                    lastX = angles[1];
                    lastY = angles[2];
                }
                xSeries.appendData(new DataPoint(time, lastX),true,1500);
                ySeries.appendData(new DataPoint(time, lastY),true,1500);
                zSeries.appendData(new DataPoint(time, lastZ),true,1500);
                time +=  0.08d;
                Log.d("servotest","srv1:"+newServo1+" , srv2:"+newServo2);            }

            @Override
            public void onFinish() {
                if (socketConnected) {
                    try {
                        socket.getOutputStream().write(0);
                        Toast.makeText(getApplicationContext(), "sending...", Toast.LENGTH_SHORT);
                    } catch (IOException e) {
                        Toast.makeText(getApplicationContext(), "could not send data", Toast.LENGTH_LONG);
                    }
                }
            }
        };

        graphTimer.start();

    }

    @Override
    public void onBackPressed() {
        if (graphTimer != null) graphTimer.cancel();
        time = 0d;
        try {
            socket.getOutputStream().write(0);
            Toast.makeText(getApplicationContext(), "sending...", Toast.LENGTH_SHORT);
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "could not send data", Toast.LENGTH_LONG);
        }
        super.onBackPressed();
    }

    private class ConnectBluetooth extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... devices){

            if (socket==null || !socketConnected) {

                bluetoothAdapter = BluetoothAdapter.
                        getDefaultAdapter();


                if (bluetoothAdapter == null) {
                    out.append("device not supported");
                } else if (!bluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, 0);
                } else {
                    Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
                    if (pairedDevices.isEmpty()) {
                        Toast.makeText(getApplicationContext(), "No paired device found.", Toast.LENGTH_SHORT);
                    } else {
                        for (BluetoothDevice device : pairedDevices) {
                            //find HC-05 adress
                            if (device.getName().equals("HC-05")) {
                                HC05 = device;
                                System.out.println("hc-05 found");
                                try {
                                    socket = HC05.createInsecureRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                                    socket.connect();
                                    socketConnected = true;

                                } catch (IOException e) {
                                    socketConnected = false;
                                    System.out.println("cound not connect");
                                    e.printStackTrace();

                                }
                            }
                        }

                    }
                }
            }




            return null;
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d("sensor report:","onAccuracyChanged called");
    }


    @Override
    public void onSensorChanged(SensorEvent event){
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accel, 0, accel.length);
        }
        else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnet, 0,magnet.length);
        }

    }


}
