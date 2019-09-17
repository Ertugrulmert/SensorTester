package com.example.gyrotester;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import static java.lang.System.out;

public class MainActivity extends AppCompatActivity {

private RadioGroup radioGroup;
private RadioButton gyroButton, otherButton;
private Button beginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        gyroButton = (RadioButton) findViewById(R.id.gyroButton);
        //other button may be instantiated for new testing options

        beginButton = (Button) findViewById(R.id.button);
        beginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //navigate to gyro test activity
                Intent intent = new Intent(getApplicationContext(), GyroActivity.class);
                startActivity(intent);
            }


        });


    }
}
