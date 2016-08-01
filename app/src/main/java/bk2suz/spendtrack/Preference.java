package bk2suz.spendtrack;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by sujoy on 1/8/16.
 */
public class Preference {
    private static final String PREFS_NAME = "spendtrack_prefs";
    private static final String PREF_LAST_TAG = "last_tag";
    public static final String PREF_LIST_START_DATE = "list_start_date";
    public static final String PREF_SUM_START_DATE = "sum_start_date";

    public static String getLastTag(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getString(PREF_LAST_TAG, "");
    }

    public static void saveLastTag(Context context, String tagName) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(PREF_LAST_TAG, tagName);
        editor.commit();
    }

    public static Date getDate(Context context, String dateName, Date defaultDate) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        long timeInMillis = settings.getLong(dateName, 0);
        if (timeInMillis == 0) {
            return defaultDate;
        }
        Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis(timeInMillis);
        return cal.getTime();
    }

    public static void saveDate(Context context, String dateName, Date date) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        editor.putLong(dateName, cal.getTimeInMillis());
        editor.commit();
    }
}
