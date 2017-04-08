package com.stephen.wikilocation.Activity;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;

import com.stephen.wikilocation.R;

/**
 * Created by Stephen on 4/6/2017.
 */
public class SettingsActivity extends AppCompatActivity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // Display the fragment as the main content
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }


    public static class SettingsFragment extends PreferenceFragment{


        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            //Load preferences from XML file
            addPreferencesFromResource(R.xml.app_preferences);
        }


    }

}


