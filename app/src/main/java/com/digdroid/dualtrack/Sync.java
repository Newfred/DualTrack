package com.digdroid.dualtrack;

import android.content.Context;
import android.os.AsyncTask;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.SessionsClient;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.request.SessionInsertRequest;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


public class Sync {

    static void sync( Context context )
    {
        new SyncTask( context ).execute();
    }
}


class SyncTask extends AsyncTask<Void,Void,Void>
{
    Context context;
    Store store;

    SyncTask( Context context )
    {
        this.context = context;
        store = Store.getInstance( context );
    }

    @Override
    protected Void doInBackground( Void... x )
    {
        synchronized ( SyncTask.class )
        {
            ArrayList<MySession> mySessions = store.getSessions();

            for (MySession mySession : mySessions)
                insertSession(mySession);
        }

        return null;
    }


    private void insertSession( final MySession mySession )
    {
        int count = mySession.getStepCount();
        long startTime = mySession.getStartTime();
        long endTime = mySession.getEndTime();
        String activity = mySession.getActivity();

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
        dataPoint.getValue( Field.FIELD_ACTIVITY ).setActivity( activity );
        segementData.add( dataPoint );

        Session session = new Session.Builder()
                .setName( context.getString( R.string.standby_steps ) + " " + activity )
                .setDescription( context.getString( R.string.standby_steps ) + " " + activity )
                .setIdentifier( context.getPackageName() + "." + startTime )
                .setActivity( activity )
                .setStartTime( startTime, TimeUnit.MILLISECONDS )
                .setEndTime( endTime, TimeUnit.MILLISECONDS )
                .build();

        SessionInsertRequest insertRequest = new SessionInsertRequest.Builder()
                .setSession( session )
                .addDataSet( stepsData )
                .addDataSet( segementData )
                .build();

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount( context );

        SessionsClient sessionsClient = Fitness.getSessionsClient( context, account );

        Task<Void> task = sessionsClient
                .insertSession( insertRequest )
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess( Void aVoid ) {
                        store.removeSession( mySession );
                    }
                });

        try {
            Tasks.await( task, 60, TimeUnit.SECONDS );
        }
        catch( Exception e ) {
        }

    }
}
