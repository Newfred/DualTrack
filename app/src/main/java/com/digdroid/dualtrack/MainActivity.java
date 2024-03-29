package com.digdroid.dualtrack;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessActivities;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.tasks.Task;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private boolean isSensorPresent = false;

    int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 111;
    int SIGN_IN_REQUEST_CODE = 333;


    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        isSensorPresent = ( sensorManager != null && sensorManager.getDefaultSensor( Sensor.TYPE_STEP_COUNTER ) != null );
    }


    @Override
    protected void onResume()
    {
        super.onResume();

        if ( !isSensorPresent ) {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.no_sensor)
                    .setCancelable(false)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    })
                    .create()
                    .show();

            return;
        }

        final Store store = Store.getInstance( this );

        if ( store.isEnabled() && !getPermissions() ) return;

        final Switch enabled = findViewById( R.id.enabled );
        enabled.setChecked( store.isEnabled() );
        enabled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                store.setEnabled( enabled.isChecked() );
                if ( store.isEnabled() )
                    getPermissions();
            }
        });

        int standbySteps = store.getStandbySteps();

        TextView stepCount = findViewById( R.id.step_count );
        stepCount.setText( getString( R.string.during_standby ) +": " + standbySteps + " " + getString( R.string.steps ) +
                ", " + store.getStandbyPace() + getString( R.string.spm ) );


        String nextActivity = store.getNextActivity();

        final ArrayList<String> activities = new ArrayList<>();
        activities.add( "" );

        ArrayList<String> activityNames = new ArrayList<>();
        activityNames.add( getString( R.string.auto ) );

        int selection = 0;

        Field[] fields = FitnessActivities.class.getFields();
        for ( Field field : fields )
            try {
                String activity = (String) field.get( null );
                if ( !Character.isUpperCase( activity.charAt(0) ) )
                {
                    activities.add( activity );
                    String activityName = activity.substring(0,1).toUpperCase() + activity.substring(1).replaceAll( "[\\_\\.]", " ");
                    activityNames.add( activityName );

                    if ( activity.equals( nextActivity ) )
                        selection = activityNames.size() - 1;
                }
            }
            catch( Exception e ){}



        Spinner spinner = findViewById( R.id.next_activity );
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>( this, android.R.layout.simple_spinner_item, activityNames );
        dataAdapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
        spinner.setAdapter( dataAdapter );

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                store.setNextActivity( activities.get( i ) );
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spinner.setSelection( selection );


        Sync.sync( this );
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        boolean signinFailed = false;

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount (this );

        if ( requestCode == SIGN_IN_REQUEST_CODE )
        {
            signinFailed = ( account == null );
        }
        else if ( requestCode == GOOGLE_FIT_PERMISSIONS_REQUEST_CODE )
        {
            signinFailed = ( account == null || !GoogleSignIn.hasPermissions( account, getFitnessOptions() ) );
        }

        Store.getInstance( this ).setEnabled( !signinFailed );
    }


    boolean getPermissions()
    {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount (this );

        if ( account == null )
        {
            GoogleSignInClient signInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN );
            Intent intent = signInClient.getSignInIntent();
            startActivityForResult( intent, SIGN_IN_REQUEST_CODE );
        }
        else
        {
            FitnessOptions fitnessOptions = getFitnessOptions();

            if ( GoogleSignIn.hasPermissions( account, fitnessOptions ) )
                return true;

            GoogleSignIn.requestPermissions (
                    this,
                    GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
                    account,
                    fitnessOptions);
        }

        return false;
    }


    FitnessOptions getFitnessOptions()
    {
         return FitnessOptions.builder()
                 .addDataType( DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_WRITE )
                 .addDataType( DataType.TYPE_ACTIVITY_SEGMENT, FitnessOptions.ACCESS_WRITE )
                 .addDataType( DataType.AGGREGATE_ACTIVITY_SUMMARY, FitnessOptions.ACCESS_WRITE )
                 .build();
    }
}






