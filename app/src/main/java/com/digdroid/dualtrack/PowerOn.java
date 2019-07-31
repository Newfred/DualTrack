package com.digdroid.dualtrack;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessActivities;
import com.google.android.gms.fitness.HistoryClient;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.request.SessionInsertRequest;

import java.util.concurrent.TimeUnit;


public class PowerOn extends BroadcastReceiver implements SensorEventListener {

    private Context context;
    private SensorManager sensorManager;
    private Sensor sensor;
    private Store store;
    private int shutdownSteps;


    @Override
    public void onReceive(Context context, Intent intent)
    {
        this.context = context;

        store = Store.getInstance(context);
        store.setStandbySteps( 0 );

        shutdownSteps = store.getShutdownSteps();
        store.setShutdownSteps( -1 );

        if ( store.isEnabled() && shutdownSteps >= 0 )
        {
            sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

            if (sensorManager == null || (sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)) == null)
                return;

            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }


    @Override
    public void onSensorChanged( SensorEvent event )
    {
        if (event != null)
        {
            int bootSteps = Math.round( event.values[0] );
            int standbySteps = bootSteps - shutdownSteps;

            if ( standbySteps > 0 )
            {
                store.setStandbySteps( standbySteps );

                recordSteps( standbySteps, store.getShutdownTime(), Utils.currentTime() );
            }
        }

        sensorManager.unregisterListener(this);
    }


    @Override
    public void onAccuracyChanged( Sensor sensor, int accuracy ) {}



    private void recordSteps( int count, long startTime, long endTime )
    {
        DataSource dataSource = new DataSource.Builder()
                .setAppPackageName( context.getApplicationContext() )
                .setDataType( DataType.TYPE_STEP_COUNT_DELTA )
                .setType( DataSource.TYPE_RAW )
                .build();

        DataSet stepsData = DataSet.create( dataSource );
        DataPoint dataPoint = stepsData.createDataPoint();
        dataPoint.setTimeInterval( startTime, endTime, TimeUnit.MILLISECONDS);
        dataPoint.getValue( Field.FIELD_STEPS ).setInt( count );
        stepsData.add( dataPoint );


        dataSource = new DataSource.Builder()
                .setAppPackageName( context.getApplicationContext() )
                .setDataType( DataType.TYPE_ACTIVITY_SEGMENT )
                .setName( "Long standby activity segments")
                .setType( DataSource.TYPE_RAW )
                .build();

        DataSet segementData = DataSet.create( dataSource );
        dataPoint = segementData.createDataPoint();
        dataPoint.setTimeInterval( startTime, endTime, TimeUnit.MILLISECONDS);
        dataPoint.getValue( Field.FIELD_ACTIVITY ).setActivity( FitnessActivities.WALKING );
        segementData.add( dataPoint );

        Session session = new Session.Builder()
                .setName( context.getString( R.string.standby_steps ) )
                .setDescription( context.getString( R.string.standby_steps ) )
                .setIdentifier( context.getPackageName() + "." + startTime )
                .setActivity( FitnessActivities.WALKING )
                .setStartTime( startTime, TimeUnit.MILLISECONDS )
                .setEndTime( endTime, TimeUnit.MILLISECONDS )
                .build();

        SessionInsertRequest insertRequest = new SessionInsertRequest.Builder()
                .setSession( session )
                .addDataSet( stepsData )
                .addDataSet( segementData )
                .build();

        Fitness.getSessionsClient( context, GoogleSignIn.getLastSignedInAccount( context ))
                .insertSession( insertRequest );
    }

}

