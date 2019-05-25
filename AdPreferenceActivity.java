// this activity is to show the settings to the user

package edu.pda.lba;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceFragment;

public class AdPreferenceActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new AdPreferenceFragment())
                .commit();
    }

    public static class AdPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences);

        }
    }
}


