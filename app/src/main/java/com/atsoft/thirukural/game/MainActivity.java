package com.atsoft.thirukural.game;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

public class MainActivity extends AppCompatActivity {

    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getResources().getString(R.string.interstitial_ad_unit_id));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        if(savedInstanceState == null) {
            goToHome();
        }
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                Log.i("Ads", "onAdLoaded");
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
                Log.i("Ads", "onAdFailedToLoad");
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when the ad is displayed.
                Log.i("Ads", "onAdOpened");
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
                Log.i("Ads", "onAdLeftApplication");
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when when the interstitial ad is closed.
                Log.i("Ads", "onAdClosed");
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (Defs.currentFragment.equalsIgnoreCase("home")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setCancelable(false);
            builder.setTitle(R.string.app_name);
            builder.setIcon(R.drawable.ico_exit);
            builder.setMessage("Do you want exit?");
            builder.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    if (mInterstitialAd.isLoaded()) {
                        mInterstitialAd.show();
                    }
                    MainActivity.this.finish();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });

            // Create the AlertDialog object and return it
            builder.create().show();
        } else if (Defs.currentFragment.equalsIgnoreCase("game"))  {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setCancelable(false);
            builder.setTitle(R.string.app_name);
            builder.setIcon(R.drawable.ico_exit);
            builder.setMessage("Do you want exit? \nIf Yes your data will not saved.");
            builder.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    goToHome();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });

            // Create the AlertDialog object and return it
            builder.create().show();
        } else if (Defs.currentFragment.equalsIgnoreCase("about")) {
            goToHome();
        } else
            super.onBackPressed();
    }

     private void goToHome() {
         Defs.currentFragment = "home";
         FragmentManager manager = getSupportFragmentManager();
         FragmentTransaction transaction = manager.beginTransaction();
         manager.popBackStackImmediate();
         HomeFragment homeFragment = new HomeFragment();
         transaction.replace(R.id.container, homeFragment, "Home");
         transaction.addToBackStack(null);
         transaction.commit();
     }
}
