package com.atsoft.thirukural.game;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class SplashScreen extends AppCompatActivity {

    SharedPreferences sharedPrefs;

    //private AdView mAdView;


    private static final int REQUEST= 112;

    Context mContext = this;
    ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        //mAdView = (AdView) findViewById(R.id.ss_adView);
        progress = new ProgressDialog(this);
        progress.setTitle("Please wait.!");
        progress.setMessage("Uploading...");
        progress.setIcon(R.drawable.dialog_process);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setCancelable(false);
        progress.setIndeterminate(false);
        /*FirebaseApp.initializeApp(this);
        MobileAds.initialize(getApplicationContext(), String.valueOf(R.string.YOUR_ADMOB_APP_ID));
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);*/
        sharedPrefs = getSharedPreferences("kural", Context.MODE_PRIVATE);
        getAllPermission();

        /*mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdOpened() {
                super.onAdOpened();
                FirebaseCrash.log("onAdOpened");
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                FirebaseCrash.log("onAdLoaded");
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                FirebaseCrash.log("onAdClicked");
            }

            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                FirebaseCrash.log("onAdFailedToLoad : "+i);
            }

            @Override
            public void onAdImpression() {
                super.onAdImpression();
                FirebaseCrash.log("onAdImpression");
            }
        });*/

    }

    private void checkDB() {
        String DB_NAME = "Thirukural.db";
        try {
            File database = getApplicationContext().getDatabasePath(DB_NAME);
            if (!database.exists()) {
                byte[] buffer = new byte[1024];
                OutputStream myOutput = null;
                int length;
                // Open your local db as the input stream
                InputStream myInput = null;
                try
                {
                    myInput =getApplicationContext().getAssets().open(DB_NAME);
                    // transfer bytes from the inputfile to the
                    // outputfile
                    myOutput =new FileOutputStream(database);
                    while((length = myInput.read(buffer)) > 0)
                    {
                        myOutput.write(buffer, 0, length);
                    }
                    myOutput.close();
                    myOutput.flush();
                    myInput.close();
                    Log.i("Syso Database",
                            "New database has been copied to device!"+getApplicationContext().getDatabasePath("Thirukural.db"));
                    //saveToSD();
                    new GetValueFromDB().execute("");

                }
                catch(IOException e)
                {
                    e.printStackTrace();
                    //FirebaseCrash.report(e);
                }
            } else {
                //saveToSD();
                new GetValueFromDB().execute("");
            }
        } catch (Exception e) {
            e.printStackTrace();
            //FirebaseCrash.report(e);
        }
    }

    private void saveToSD() {
        byte[] buffer = new byte[1024];
        OutputStream myOutput = null;
        int length;
        // Open your local db as the input stream
        InputStream myInput = null;
        try
        {
            myInput =getApplicationContext().getAssets().open("Thirukural.db");
            // transfer bytes from the inputfile to the
            // outputfile
            File dir = Environment.getExternalStorageDirectory();
            File desc = new File(dir, "Thirukural/Thirukural.db");
            myOutput =new FileOutputStream(desc);
            while((length = myInput.read(buffer)) > 0)
            {
                myOutput.write(buffer, 0, length);
            }
            myOutput.close();
            myOutput.flush();
            myInput.close();
            Log.i("Syso Database",
                    "New database has been copied to device!");
            //new GetValueFromDB().execute("");

        }
        catch(IOException e)
        {
            e.printStackTrace();
            //FirebaseCrash.report(e);
        }
    }

    private void getAllPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            Log.d("TAG","@@@ IN IF Build.VERSION.SDK_INT >= 23");
            String[] PERMISSIONS = {
                    android.Manifest.permission.INTERNET,
                    android.Manifest.permission.EXPAND_STATUS_BAR,
                    android. Manifest.permission.ACCESS_COARSE_LOCATION,
            };


            if (!hasPermissions(mContext, PERMISSIONS)) {
                Log.d("TAG","@@@ IN IF hasPermissions");
                ActivityCompat.requestPermissions((Activity) mContext, PERMISSIONS, REQUEST );
            } else {
                Log.d("TAG","@@@ IN ELSE hasPermissions");
                checkDB();
            }
        } else {
            Log.d("TAG","@@@ IN ELSE  Build.VERSION.SDK_INT >= 23");
            checkDB();
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("TAG","@@@ PERMISSIONS grant");
                    checkDB();
                } else {
                    Log.d("TAG","@@@ PERMISSIONS Denied");
                    Toast.makeText(mContext, "PERMISSIONS Denied", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private class GetValueFromDB extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... params) {
            DBController controller = new DBController(mContext);
            SQLiteDatabase db = controller.getReadableDatabase();
            String qry = "SELECT words FROM allwords";
            Cursor cursor = db.rawQuery(qry, null);
            int count = 0;
            Defs.allwords = new ArrayList<>();
            try {
                while (cursor.moveToNext()) {
                    try {
                        Defs.allwords.add(cursor.getString(cursor.getColumnIndex("words")));
                    } catch (Exception e) {
                        e.printStackTrace();
                        //FirebaseCrash.report(e);
                    }
                    count++;
                }
            } catch (Exception e) {
                e.printStackTrace();
                //FirebaseCrash.report(e);
            }
            db.close();
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            progress.dismiss();
            //saveToSD();
            Intent intent = new Intent(SplashScreen.this, MainActivity.class);
            startActivity(intent);
            SplashScreen.this.finish();
            // might want to change "executed" for the returned string passed
            // into onPostExecute() but that is upto you
        }

        @Override
        protected void onPreExecute() {
            progress.show();
        }

        @Override
        protected void onProgressUpdate(Void... values) {}
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (progress != null) {
            progress.dismiss();
            progress = null;
        }
    }
}
