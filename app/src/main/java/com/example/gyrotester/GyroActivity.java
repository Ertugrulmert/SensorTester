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
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

import static java.lang.System.out;

public class GyroActivity extends AppCompatActivity implements SensorEventListener {

    //BLUETOOTH
    boolean socketConnected = false;
    OutputStream outputStream;
    ConnectedThread connectedThread;

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
    private float lastZ,lastX,lastY = 0f;

    private SensorManager sensorManager;
    private float lastin= 0;
    private double lastTime = 0d;
    private float offset;

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

        xSeries = new LineGraphSeries<>();
        xSeries.setColor(Color.RED);

        ySeries = new LineGraphSeries<>();
        ySeries.setColor(Color.RED);

        zSeries = new LineGraphSeries<>();
        zSeries.setColor(Color.RED);

        zServo = new LineGraphSeries<>();
        zServo.setColor(Color.GREEN);
        yServo = new LineGraphSeries<>();
        yServo.setColor(Color.GREEN);
        xServo = new LineGraphSeries<>();
        xServo.setColor(Color.GREEN);

        graphX.addSeries(xSeries);
        graphY.addSeries(ySeries);
        graphZ.addSeries(zSeries);
        graphZ.addSeries(zServo);
        graphX.addSeries(xServo);
        graphY.addSeries(yServo);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                sensorManager.SENSOR_DELAY_GAME);

       sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                sensorManager.SENSOR_DELAY_GAME);

        handler = new Handler() {
            public void handleMessage(android.os.Message message) {
                switch (message.what) {
                    case 1:
                        byte[] rBuffer = (byte[]) message.obj;
                        String tempString = new String(rBuffer, 0, message.arg1);
                        lastin = ((int) tempString.charAt(0));
                        Log.d("INPUT", "INPUT: " + lastin);

                        break;
                }
            };
        };
        //RECEPTION
     /**   Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] buffer = new byte[1024];
                int bytecount;
                // Keep listening to the InputStream until an exception occurs.
                while (inputStream != null) {
                    out.println("input not null");
                    try {
                        bytecount = inputStream.read(buffer);
                        handler.obtainMessage(1, bytecount, -1, buffer).sendToTarget();
                       /** for (int i = 0; i < incomingMessage.length(); i++) {

                            // if (buffer[i]=="*".getBytes()[0]){

                            try {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.d("*", "*********************");
                                        out.println("*****");
                                    }
                                });
                            } catch (Exception e) {
                                Log.d("*", "fail *");
                            }
                            // newServo1 = buffer[i+1];
                            //  }
                        }

                        Message readMessage = handler.obtainMessage(0, bytes, -1, buffer);
                        readMessage.sendToTarget();

                    } catch (IOException e) {
                        Log.d("GYRO", "Instream distrupted", e);
                        break;
                    }
                }
            }
        });
        thread.start(); **/

        //TRANSMISSION


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sampleGyro();

                if (socketConnected) {
                    try {
                        connectedThread = new ConnectedThread(socket);
                        connectedThread.start();
                        outputStream = socket.getOutputStream();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        outputStream.write('x');
                        Toast.makeText(getApplicationContext(), "sending...", Toast.LENGTH_SHORT);
                    } catch (IOException e) {
                        Toast.makeText(getApplicationContext(), "could not send data", Toast.LENGTH_LONG);
                    }
                }
            }
        });


    }


    private void sampleGyro() {
        graphTimer = new CountDownTimer(25000, 80) {
            @Override
            public void onTick(long l) {
                SensorManager.getRotationMatrix(rotationMatrix, null,
                        accel, magnet);
                SensorManager.getOrientation(rotationMatrix, angles);
                angles[0] = (float) Math.cos(angles[0])*3;
                angles[1] = (float) Math.cos(angles[1])*3;
                angles[2] = (float) Math.sin(angles[2])*3;


                if (time != 0) {
                    lastZ = (angles[0] + lastZ)/2 ;
                    lastX = (angles[1] + lastX)/2 ;
                    lastY = (angles[2] + lastY)/2;
                } else {
                    lastZ = angles[0];
                    lastX = angles[1];
                    lastY = angles[2];
                }
                xSeries.appendData(new DataPoint(time, lastX), true, 1500);
                ySeries.appendData(new DataPoint(time, lastY), true, 1500);
                zSeries.appendData(new DataPoint(time, lastZ), true, 1500);

                lastin =(float)Math.cos( Math.toRadians(lastin))*3;
                zServo.appendData(new DataPoint(lastTime,lastin),true,1500);
                out.println(lastZ);
                lastTime += 0.08d;



                time += 0.08d;
              //  Log.d("servotest", "srv1:" + newServo1 + " , srv2:" + newServo2);
            }

            @Override
            public void onFinish() {
                if (socketConnected) {
                    try {
                        outputStream.write(0);
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

    private class ConnectBluetooth extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... devices) {

            if (socket == null || !socketConnected) {

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
                                    socket.connect();
                                    socketConnected = true;
                                    bluetoothAdapter.cancelDiscovery();


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
        Log.d("sensor report:", "onAccuracyChanged called");
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accel, 0, accel.length);
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnet, 0, magnet.length);
        }

    }


    private class ConnectedThread extends Thread {
        private final InputStream inStream;
        private byte[] buffer;

        public ConnectedThread(BluetoothSocket msocket) {
            InputStream tempInStream = null;
            try {
                tempInStream = msocket.getInputStream();
            } catch (IOException e) {
                Log.e("instream err", "Error w/ input stream", e);
            }
            inStream = tempInStream;
        }

        public void run() {
            buffer = new byte[256];  // buffer store for the stream
            int bytes; // bytes returned from read()
            while (true) {

                try {

                    bytes = inStream.read(buffer);
                    handler.obtainMessage(1, bytes, -1, buffer).sendToTarget();     // Send to Handler

                } catch (IOException e) {
                    Log.d("read err","could not receive bytes");
                    break;
                }
            }
        }
    }
}