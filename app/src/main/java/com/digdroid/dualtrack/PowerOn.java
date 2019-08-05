package com.digdroid.dualtrack;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;


public class PowerOn extends BroadcastReceiver implements SensorEventListener {

    private Context context;
    private SensorManager sensorManager;
    private Sensor sensor;
    private Store store;
    private int shutdownSteps;
    private String activity;


    @Override
    public void onReceive(Context context, Intent intent)
    {
        this.context = context;

        store = Store.getInstance(context);
        store.setStandbySteps( 0 );

        shutdownSteps = store.getShutdownSteps();
        store.setShutdownSteps( -1 );

        activity = store.getNextActivity();
        store.setNextActivity( "" );

        if ( store.isEnabled() && shutdownSteps >= 0 )
        {
            sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

            if ( sensorManager == null || (sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)) == null )
                return;

            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL );
        }
    }


    @Override
    public void onSensorChanged( SensorEvent event )
    {
        if ( event != null )
        {
            int bootSteps = Math.round( event.values[0] );
            int standbySteps = bootSteps - shutdownSteps;

            if ( standbySteps > 0 )
            {
                store.setStandbySteps( standbySteps );

                MySession mySession = new MySession( standbySteps, store.getShutdownTime(), Utils.currentTime() );
                if ( !activity.equals( "" ) ) mySession.setActivity( activity );

                store.addSession( mySession );
            }
        }

        sensorManager.unregisterListener( this );

        Sync.sync( context );
    }


    @Override
    public void onAccuracyChanged( Sensor sensor, int accuracy ) {}

}

