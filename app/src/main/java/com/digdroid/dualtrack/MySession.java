package com.digdroid.dualtrack;

import com.google.android.gms.fitness.FitnessActivities;

class MySession
{
    static final int RUNNING_PACE = 160;
    static final int JOGGING_PACE = 140;
    static final int WALKING_FIT_PACE = 120;
    static final int WALKING_PACE = 80;

    private int stepCount;
    private long startTime, endTime;
    private String activity;

    MySession( int count, long startTime, long endTime )
    {
        this.stepCount = count;
        this.startTime = startTime;
        this.endTime = endTime;

        float pace = (float) count / ( (float) (endTime-startTime)*0.95f / 60000f );

        if ( pace >= RUNNING_PACE )
            activity = FitnessActivities.RUNNING;
        else if ( pace >= JOGGING_PACE )
            activity = FitnessActivities.RUNNING_JOGGING;
        else if ( pace >= WALKING_FIT_PACE )
            activity = FitnessActivities.WALKING_FITNESS;
        else if ( pace >= WALKING_PACE )
            activity = FitnessActivities.WALKING;
        else
            activity = FitnessActivities.UNKNOWN;

    }

    MySession( String session )
    {
        String[] vals = session.split(",");
        stepCount = Integer.parseInt( vals[0] );
        startTime = Long.parseLong( vals[1] );
        endTime = Long.parseLong( vals[2] );
        activity = vals[3];
    }

    @Override
    public String toString()
    {
        return "" + stepCount + "," + startTime + "," + endTime + "," + activity;
    }

    void setActivity( String activity ) { this.activity = activity; }

    int getStepCount() { return stepCount; }
    long getStartTime() { return startTime; }
    long getEndTime() { return endTime; }
    String getActivity() { return activity; }
}

