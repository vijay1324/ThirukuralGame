package com.atsoft.thirukural.game;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(savedInstanceState == null) {
            goToHome();
        }
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
