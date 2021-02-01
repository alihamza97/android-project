package com.fontys.practical;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.ImageView;

public class SensorActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor light;

    ImageView brightness_icon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);
        setTitle("Ambience");

        brightness_icon = findViewById(R.id.brightness_icon);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        light = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float ambientLightLevel = sensorEvent.values[0];
        System.out.println(ambientLightLevel);

        if (ambientLightLevel > 14 && ambientLightLevel <= 54){
            brightness_icon.setImageResource(R.mipmap.sun2);
        } else if (ambientLightLevel > 54 && ambientLightLevel < 244){
            brightness_icon.setImageResource(R.mipmap.sun3);
        } else if (ambientLightLevel >= 244){
            brightness_icon.setImageResource(R.mipmap.sun4);
        } else {
            brightness_icon.setImageResource(R.mipmap.sun1);
        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, light,
                SensorManager.SENSOR_DELAY_NORMAL);
    }
    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }
}
