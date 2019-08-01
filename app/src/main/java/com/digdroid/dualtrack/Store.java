package com.digdroid.dualtrack;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Store
{
    final private static String SHUTDOWN_STEPS = "shutdown_steps";
    final private static String SHUTDOWN_TIME = "shutdown_time";
    final private static String STANDBY_STEPS = "standby_steps";
    final private static String ENABLED = "enabled";

    private SharedPreferences preferences;
    private static Store instance = null;


    private Store( Context context ) {

        preferences = PreferenceManager.getDefaultSharedPreferences( context );
    }


    public static synchronized Store getInstance( Context context )
    {
        if ( instance == null )
            instance = new Store( context );

        return instance;
    }


    private void putInt( String name, int i )
    {
        SharedPreferences.Editor editor = preferences.edit();

        editor.putInt( name, i );
        editor.apply();
    }

    public void putLong( String name, long i )
    {
        SharedPreferences.Editor editor = preferences.edit();

        editor.putLong( name, i );
        editor.apply();
    }

    public void putBoolean( String name, boolean b )
    {
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean( name, b );
        editor.apply();
    }


    public void putString( String name, String s )
    {
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString( name, s );
        editor.apply();
    }



    public int getInt( String key, int def )
    {
        return preferences.getInt( key, def );
    }
    public long getLong( String key, long def )
    {
        return preferences.getLong( key, def );
    }
    public String getString( String key, String def ) { return preferences.getString( key, def ); }


    public void setShutdownSteps( int steps )
    {
        putInt( SHUTDOWN_STEPS, steps );
    }

    public void setShutdownTime( long t )
    {
        putLong( SHUTDOWN_TIME, t );
    }

    public int getShutdownSteps()
    {
        return getInt( SHUTDOWN_STEPS, -1 );
    }

    public long getShutdownTime()
    {
        return getLong( SHUTDOWN_TIME, -1 );
    }

    public void setStandbySteps( int steps )
    {
        putInt( STANDBY_STEPS, steps );
    }

    public int getStandbySteps()
    {
        return getInt( STANDBY_STEPS, 0 );
    }

    public boolean isEnabled() { return preferences.getBoolean( ENABLED, false ); }

    public void setEnabled( boolean enabled ) { putBoolean( ENABLED, enabled ); }

}
