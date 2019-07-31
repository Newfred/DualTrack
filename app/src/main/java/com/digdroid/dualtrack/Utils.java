package com.digdroid.dualtrack;

import java.util.Calendar;
import java.util.Date;


public class Utils
{
    static long currentTime()
    {
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime( now );
        return cal.getTimeInMillis();
    }
}
