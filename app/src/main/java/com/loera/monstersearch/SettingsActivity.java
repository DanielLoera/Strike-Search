package com.loera.monstersearch;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity{


  SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    public void onStart(){

        super.onStart();

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        SharedPreferences.OnSharedPreferenceChangeListener spChanged = new
                SharedPreferences.OnSharedPreferenceChangeListener() {
                    @Override
                    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                                          String key) {
                        if("pref_image".equals(key)){
                            if(prefs.getString("pref_image","none").equals("box")){
                                FavoriteFragment fav = new FavoriteFragment();
                                Bundle bundle = new Bundle();
                                bundle.putBoolean("selecting", true);
                                fav.setArguments(bundle);
                                if(!isFinishing()) {
                                    getFragmentManager().beginTransaction()
                                            .setCustomAnimations(R.anim.slideinright, R.anim.slideoutleft)
                                            .replace(R.id.settingsActivity, fav)
                                            .commit();
                                }
                            }

                        }


                    }
                };

        prefs.registerOnSharedPreferenceChangeListener(spChanged);

       getFragmentManager().beginTransaction().add(R.id.settingsActivity, new SettingsPage()).commit();

 }



    public static class SettingsPage extends PreferenceFragment {


        public void onCreate(Bundle onSavedInstanceState){

            super.onCreate(onSavedInstanceState);

            addPreferencesFromResource(R.xml.preferences);

        }



    }



}


