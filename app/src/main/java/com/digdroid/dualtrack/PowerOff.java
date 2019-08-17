package com.digdroid.dualtrack;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;


public class PowerOff extends BroadcastReceiver implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor sensor;
    private Store store;
    private static boolean received = false, stepsRead = false;


    @Override
    public void onReceive(Context context, Intent intent)
    {
        synchronized ( PowerOff.class )
        {
            if ( received ) return;
            received = true;
        }

        store = Store.getInstance( context );
        store.setShutdownSteps( -1 );

        if ( store.isEnabled() )
        {
            sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

            sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
            if (sensorManager == null || (sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)) == null)
                return;

            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

        Sync.sync( context );
    }


    @Override
    public void onSensorChanged( SensorEvent event ) {

        if (event == null) return;

        synchronized ( PowerOff.class )
        {
            if ( stepsRead ) return;
            stepsRead = true;
        }

        store.setShutdownSteps( Math.round( event.values[0] ) );
        store.setShutdownTime( Utils.currentTime() );

        sensorManager.unregisterListener( this );
    }

    @Override
    public void onAccuracyChanged( Sensor sensor, int accuracy ) {}
}

