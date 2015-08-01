package com.loera.monstersearch;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.android.vending.billing.IabHelper;
import com.android.vending.billing.IabResult;
import com.android.vending.billing.Purchase;

public class SettingsActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener{

  private static IabHelper mHelper;
  private static String base64PublicKey;
  private static final String BILLLOGTAG = "Google Play Billing";
  private static final String SKU_DONATION = "strike_search_donation";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(R.id.settingsActivity, new SettingsPage()).commit();

        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).registerOnSharedPreferenceChangeListener(this);

        //BILLING

        base64PublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAobvB+PbLGgRP3etPsLJ7FJTS8USMidTf6BIocmWHCRpli84GISCAtPdSFEiKUXB8iz1NlnQ8oWIAZXkRsSkYQB9kKzVYEttRTnDgyTfy3cqV6dK8Mb/ibe3VBxEcmps0lAXCVNbNd5y/5CTN9ya85n+qZ1rRG2PHwQwSVq6x8u1LtBjWVLcT0csgolrfl1U91hHfJ4kfgteyhjT5IElIVfmBVdqe3TVokHNH4boGclsDXtjx3eNU3lL+U+JRhD3cRSB6Qh8Gb8qU0LSFqi0+nWEW2by8DJVAC4rJUVzNDfGXHt73pamDp0qXcsPBS4HC2Uusdk6FO8e87ougY+9KfwIDAQAB";
        mHelper = new IabHelper(this, base64PublicKey);

        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            @Override
            public void onIabSetupFinished(IabResult result) {
                if(!result.isSuccess()){
                    Log.i(BILLLOGTAG,"Setup Error occurred");
                }else{

                    Log.i(BILLLOGTAG,"Setup Success");
                }
            }
        });

        //////

        setContentView(R.layout.activity_settings);
    }

    public void onDestroy(){

        super.onDestroy();
        if(mHelper != null)
            mHelper.dispose();
        mHelper = null;

    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if("pref_image".equals(key)){
            if(sharedPreferences.getString("pref_image","none").equals("box")){
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

    public static class SettingsPage extends PreferenceFragment {


        public void onCreate(Bundle onSavedInstanceState){

            super.onCreate(onSavedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            Preference donatePref = findPreference("pref_donate");

            final IabHelper.OnIabPurchaseFinishedListener finishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
                @Override
                public void onIabPurchaseFinished(IabResult result, Purchase purchase) {

                    if (result.isFailure()) {
                        Toast.makeText(getActivity().getApplicationContext(),"Failed to purchase.",Toast.LENGTH_SHORT)
                                .show();
                        return;
                    }
                    else if (purchase.getSku().equals(SKU_DONATION)) {
                        Toast.makeText(getActivity().getApplicationContext(),"Thank you for your support! ^-^",Toast.LENGTH_LONG)
                                .show();

                    }

                }
            };


            donatePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                        if(mHelper != null)mHelper.flagEndAsync();
                        mHelper.launchPurchaseFlow(getActivity(), SKU_DONATION, 10001, finishedListener, "");
                        return false;
                }
            });



        }



    }



}


