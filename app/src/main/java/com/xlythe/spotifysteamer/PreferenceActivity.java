package com.xlythe.spotifysteamer;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.Locale;

public class PreferenceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FragmentManager mFragmentManager = getFragmentManager();
        FragmentTransaction mFragmentTransaction = mFragmentManager
                .beginTransaction();
        PrefsFragment mPrefsFragment = new PrefsFragment();
        mFragmentTransaction.replace(android.R.id.content, mPrefsFragment);
        mFragmentTransaction.commit();

    }

    public static class PrefsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);

            String[] isoCountryCodes = Locale.getISOCountries();

            ListPreference listPref = (ListPreference) findPreference("list_preference");
            listPref.setKey("keyName");
            listPref.setEntries(isoCountryCodes);
            listPref.setEntryValues(isoCountryCodes);
        }
    }
}
