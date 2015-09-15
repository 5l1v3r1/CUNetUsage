package com.aqnichol.cunetusage;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * SessionSaver saves and restores sessions across application launches.
 */
public class SessionSaver {

    private Context context;

    /**
     * Create a SessionSaver which uses a given Context to save its data.
     * @param c the context to use for preference storage.
     */
    public SessionSaver(Context c) {
        context = c;
    }

    /**
     * Get the last NubbClient that was saved. This NubbClient will need to be re-authenticated.
     * @return a NubbClient, or null if no client was saved.
     */
    public NubbClient loadClient() {
        SharedPreferences prefs = getPreferences();
        if (prefs.contains("netid") && prefs.contains("password")) {
            NubbClient c = new NubbClient();
            c.setUsername(prefs.getString("netid", ""));
            c.setPassword(prefs.getString("password", ""));
            return c;
        }
        return null;
    }

    /**
     * Save a NubbClient for the current session.
     * @param c the client to save.
     */
    public void saveClient(NubbClient c) {
        SharedPreferences prefs = getPreferences();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("netid", c.getUsername());
        editor.putString("password", c.getPassword());
        editor.apply();
    }

    /**
     * Remove any records of the last saved NubbClient.
     */
    public void clear() {
        SharedPreferences prefs = getPreferences();
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("netid");
        editor.remove("password");
        editor.apply();
    }

    private SharedPreferences getPreferences() {
        String prefsName = context.getString(R.string.session_preferences);
        return context.getSharedPreferences(prefsName, Context.MODE_PRIVATE);
    }

}
