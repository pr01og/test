package com.probojnik.terminal;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.probojnik.terminal.data.synchronization.SyncService;
import com.probojnik.terminal.util.Const;
import com.probojnik.terminal.util.UserPreferences;

/**
 * @author Stanislav Shamji
 */
public class TerminalApplication extends Application {
    private PendingIntent pintent;
    private AlarmManager alarm;

    @Override
    public void onCreate() {
        super.onCreate();
        //init constants if not set
        UserPreferences preferences = new UserPreferences(this);
        String id = preferences.getStringPreference(Const.PREF_TERMINAL_ID);
        String date = preferences.getStringPreference(Const.PREF_DATE_SYNC);
        if (id.equals("")) {
            preferences.setStringPreferences(Const.PREF_TERMINAL_ID, Const.DEFAULT_TERMINAL_ID);
        }
        if (date.equals("")) {
            preferences.setStringPreferences(Const.PREF_DATE_SYNC, "01.01.1970 01:01:01");
        }
        //creating intent and launching SyncService
        Intent intent = new Intent(this, SyncService.class);
        pintent = PendingIntent.getService(this, 0, intent, 0);
        alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Log.e("#####", "Starting alarm...");
        //alarm.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), Const.SYNC_DELAY, pintent);
        startService(intent);
    }

    @Override
    public void onTerminate() {
        clear();
        super.onTerminate();
    }

    public void clear() {
        alarm.cancel(pintent);
        Log.e("#####", "Alarm cancelled");
    }

}
