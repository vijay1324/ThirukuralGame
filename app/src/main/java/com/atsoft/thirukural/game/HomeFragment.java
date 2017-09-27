package com.atsoft.thirukural.game;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.images.ImageManager;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.BaseGameUtils;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;


public class HomeFragment extends Fragment implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    Button play, achivement, leaderboard, google, help, about, settings, share;
    ImageButton  img_play, img_achivement, img_leaderboard, img_google, img_help, img_about, img_settings, img_share;
    LinearLayout ll_play, ll_achivement, ll_leaderboard, ll_google, ll_help, ll_about, ll_settings, ll_share;
    static View currentViewButton, currentViewLayout;
    Animation anim_enter, anim_exit;
    TextView gamerName, highscoretv;
    ImageView dp;
    private MediaPlayer mp;
    private GoogleApiClient mGoogleApiClient;
    private static int RC_SIGN_IN = 9001;

    private boolean mResolvingConnectionFailure = false;
    private boolean mAutoStartSignInFlow = true;
    private boolean mSignInClicked = false;
    View rootView;
    boolean mExplicitSignOut = false;
    boolean mInSignInFlow = false;
    int highscore = 0;

    SharedPreferences sharedPrefs;
    private FirebaseAnalytics mFirebaseAnalytics;
    private AdView mAdView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_home, container, false);
        init(rootView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        showButton(play, ll_play);
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());
        // Create the Google Api Client with access to the Play Games services
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
//                .addApi(Drive.API).addScope(Drive.SCOPE_APPFOLDER) // Drive API
                // add other APIs and scopes here as needed
                .build();

        sharedPrefs = getActivity().getSharedPreferences(Defs.sharedPreferenceName, Context.MODE_PRIVATE);
        highscore = sharedPrefs.getInt("HighScore", 0);
        Defs.playSound = sharedPrefs.getBoolean("playSound", true);
        highscoretv.setText(""+highscore);
        if (highscore < Defs.HighScore) {
            highscoretv.setText(""+Defs.HighScore);
            try {
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putInt("HighScore", Defs.HighScore);
                editor.commit();
            } catch (NumberFormatException e) {
                FirebaseCrash.report(e);
                e.printStackTrace();
            }
        }
        setAchievements();

        /*try {
            String name = Games.Players.getCurrentPlayer(mGoogleApiClient).getDisplayName();
            Uri photo = Games.Players.getCurrentPlayer(mGoogleApiClient).getIconImageUri();

            if (!name.equals(null) && !name.isEmpty() && !name.equalsIgnoreCase("")) {
                gamerName.setText(name);
                ImageManager mgr = ImageManager.create(getActivity());
                mgr.loadImage(dp, photo);
            } else {
                gamerName.setText("Guest");
                dp.setImageDrawable(getResources().getDrawable(R.drawable.dp));
            }
        } catch (Exception e) {
            gamerName.setText("Guest");
            dp.setImageDrawable(getResources().getDrawable(R.drawable.dp));
            e.printStackTrace();
        }*/

        return rootView;
    }

    private void setAchievements() {
        try {
            if (mGoogleApiClient.isConnected()) {
                if (highscore < Defs.HighScore)
                    Games.Leaderboards.submitScore(mGoogleApiClient, String.valueOf(R.string.app_id), Defs.HighScore);
                if (Defs.HighScore > 10)
                    Games.Achievements.unlock(mGoogleApiClient, String.valueOf(R.string.achievement_high_score_10));
                else if (Defs.HighScore > 50)
                    Games.Achievements.unlock(mGoogleApiClient, String.valueOf(R.string.achievement_high_score_50));
                else if (Defs.HighScore > 100)
                    Games.Achievements.unlock(mGoogleApiClient, String.valueOf(R.string.achievement_high_score_100));
            }
        } catch (Exception e) {
            FirebaseCrash.report(e);
            e.printStackTrace();
        }
    }

    private void init(View rootView) {
        play = rootView.findViewById(R.id.btn_play);
        gamerName = rootView.findViewById(R.id.gamer_name);
        mAdView = rootView.findViewById(R.id.adView);
        highscoretv = rootView.findViewById(R.id.high_score_tv);
        dp = rootView.findViewById(R.id.profile_img);
        achivement = rootView.findViewById(R.id.btn_achievement);
        leaderboard = rootView.findViewById(R.id.btn_leaderboard);
        settings = rootView.findViewById(R.id.btn_settings);
        help = rootView.findViewById(R.id.btn_help);
        share = rootView.findViewById(R.id.btn_share);
        about = rootView.findViewById(R.id.btn_aboutus);
        google = rootView.findViewById(R.id.btn_google);

        img_play = rootView.findViewById(R.id.img_btn_play);
        img_settings = rootView.findViewById(R.id.img_btn_settings);
        img_help = rootView.findViewById(R.id.img_btn_help);
        img_share = rootView.findViewById(R.id.img_btn_share);
        img_about = rootView.findViewById(R.id.img_btn_aboutus);
        img_achivement = rootView.findViewById(R.id.img_btn_achievement);
        img_leaderboard = rootView.findViewById(R.id.img_btn_leaderboard);
        img_google = rootView.findViewById(R.id.img_btn_google);

        ll_play = rootView.findViewById(R.id.ll_play);
        ll_settings = rootView.findViewById(R.id.ll_settings);
        ll_help = rootView.findViewById(R.id.ll_help);
        ll_share = rootView.findViewById(R.id.ll_share);
        ll_about = rootView.findViewById(R.id.ll_aboutus);
        ll_achivement = rootView.findViewById(R.id.ll_achievement);
        ll_leaderboard = rootView.findViewById(R.id.ll_leaderboard);
        ll_google = rootView.findViewById(R.id.ll_google);

        img_play.setOnClickListener(this);
        img_settings.setOnClickListener(this);
        img_help.setOnClickListener(this);
        img_share.setOnClickListener(this);
        img_about.setOnClickListener(this);
        img_achivement.setOnClickListener(this);
        img_leaderboard.setOnClickListener(this);
        img_google.setOnClickListener(this);

        play.setOnClickListener(this);
        settings.setOnClickListener(this);
        help.setOnClickListener(this);
        share.setOnClickListener(this);
        about.setOnClickListener(this);
        achivement.setOnClickListener(this);
        leaderboard.setOnClickListener(this);
        google.setOnClickListener(this);
    }

    private void showButton(View btn, View ll) {
        playSound();
        if (btn.getId() == R.id.btn_aboutus || btn.getId() == R.id.btn_leaderboard || btn.getId() == R.id.btn_achievement || btn.getId() == R.id.btn_google)
            anim_enter = AnimationUtils.loadAnimation(getActivity(), android.R.anim.slide_in_left);
        else
            anim_enter = AnimationUtils.loadAnimation(getActivity(), R.anim.animation_right_enter);
        if (currentViewButton != null) {
            if (currentViewButton.getId() == R.id.btn_aboutus || currentViewButton.getId() == R.id.btn_leaderboard || currentViewButton.getId() == R.id.btn_achievement || currentViewButton.getId() == R.id.btn_google)
                anim_exit = AnimationUtils.loadAnimation(getActivity(), R.anim.animation_left_exit);
            else
                anim_exit = AnimationUtils.loadAnimation(getActivity(), R.anim.animation_right_exit);
            currentViewLayout.setAnimation(anim_exit);
            currentViewButton.setVisibility(View.GONE);
        }
        ll.setAnimation(anim_enter);
        btn.setVisibility(View.VISIBLE);
        currentViewButton = btn;
        currentViewLayout = ll;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // show sign-out button, hide the sign-in button
        google.setText("Sign Out");

        // (your code here: update UI, enable functionality that depends on sign in, etc)
        Games.Achievements.unlockImmediate(mGoogleApiClient, String.valueOf(R.string.achievement_signin));
        setAchievements();
        String name = Games.Players.getCurrentPlayer(mGoogleApiClient).getDisplayName();
        Uri photo = Games.Players.getCurrentPlayer(mGoogleApiClient).getIconImageUri();

        if (!name.equals(null) && !name.isEmpty() && !name.equalsIgnoreCase("")) {
            gamerName.setText(name);
            ImageManager mgr = ImageManager.create(getActivity());
            mgr.loadImage(dp, photo);
            Defs.gamerName = name;
        } else {
            gamerName.setText("Guest");
            dp.setImageDrawable(getResources().getDrawable(R.drawable.dp));
        }
        Bundle fbundle = new Bundle();
        fbundle.putString(FirebaseAnalytics.Param.ITEM_ID, photo.toString());
        fbundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name);
        fbundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "image");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, fbundle);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!mInSignInFlow && !mExplicitSignOut) {
            // auto sign in
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        // Attempt to reconnect
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        System.out.println("Syso : connect error : "+connectionResult.getErrorMessage());
        if (mResolvingConnectionFailure) {
            // already resolving
            return;
        }

        // if the sign-in button was clicked or if auto sign-in is enabled,
        // launch the sign-in flow
        if (mSignInClicked || mAutoStartSignInFlow) {
            mAutoStartSignInFlow = false;
            mSignInClicked = false;
            mResolvingConnectionFailure = true;

            // Attempt to resolve the connection failure using BaseGameUtils.
            // The R.string.signin_other_error value should reference a generic
            // error string in your strings.xml file, such as "There was
            // an issue with sign-in, please try again later."
            if (!BaseGameUtils.resolveConnectionFailure(getActivity(),
                    mGoogleApiClient, connectionResult,
                    RC_SIGN_IN, R.string.signin_other_error)) {
                mResolvingConnectionFailure = false;
            }
        }

        // Put code here to display the sign-in button
    }

    public void onActivityResult(int requestCode, int resultCode,
                                 Intent intent) {
        if (requestCode == RC_SIGN_IN) {
            mSignInClicked = false;
            mResolvingConnectionFailure = false;
            if (resultCode == 1) {
                mGoogleApiClient.connect();
            } else {
                // Bring up an error dialog to alert the user that sign-in
                // failed. The R.string.signin_failure should reference an error
                // string in your strings.xml file that tells the user they
                // could not be signed in, such as "Unable to sign in."
               BaseGameUtils.showActivityResultError(getActivity(),
                        requestCode, resultCode, R.string.signin_failure);
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_btn_play:
                showButton(play, ll_play);
                break;
            case R.id.img_btn_settings:
                showButton(settings, ll_settings);
                break;
            case R.id.img_btn_help:
                showButton(help, ll_help);
                break;
            case R.id.img_btn_share:
                showButton(share, ll_share);
                break;
            case R.id.img_btn_aboutus:
                showButton(about, ll_about);
                break;
            case R.id.img_btn_achievement:
                showButton(achivement, ll_achivement);
                break;
            case R.id.img_btn_leaderboard:
                showButton(leaderboard, ll_leaderboard);
                break;
            case R.id.img_btn_google:
                showButton(google, ll_google);
                break;
            case R.id.btn_play:
                Defs.currentFragment = "game";
                FragmentManager manager = getActivity().getSupportFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                manager.popBackStackImmediate();
                GameFragment gameFragment = new GameFragment();
                transaction.replace(R.id.container, gameFragment, "game");
                transaction.addToBackStack(null);
                transaction.commit();
                break;
            case R.id.btn_settings:
                final Switch sw = new Switch(getActivity());
                sw.setChecked(true);
                sw.setTextOn("Sound On");
                sw.setTextOff("Sound Off");


                LinearLayout linearLayout = new LinearLayout(getActivity());
                linearLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));
                linearLayout.setGravity(Gravity.CENTER_HORIZONTAL);
                linearLayout.addView(sw);


                AlertDialog.Builder myDialog = new    AlertDialog.Builder(getActivity());
                myDialog.setTitle("Settings");
                myDialog.setMessage("Sound On/Off");
                myDialog.setView(linearLayout);
                myDialog.setNeutralButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Defs.playSound = sw.isChecked();
                        SharedPreferences.Editor editor = sharedPrefs.edit();
                        try {
                            editor.putBoolean("playSound", sw.isChecked());
                            editor.commit();
                        } catch (Exception e) {
                            FirebaseCrash.report(e);
                            e.printStackTrace();
                        }
                       if (sw.isChecked()) {
                           System.out.println("Syso sound on");
                       } else {
                           System.out.println("Syso sound off");
                       }
                    }
                });

                myDialog.show();
                break;
            case R.id.btn_help:
                launchNewActivity(getActivity().getApplicationContext(), "com.atsoft.dhinamorukural");
                break;
            case R.id.btn_share:
                try {
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("text/plain");
                    i.putExtra(Intent.EXTRA_SUBJECT, R.string.app_name);
                    String sAux = "\nI recommend this App\n";
                    sAux = sAux + "https://play.google.com/store/apps/details?id=com.atsoft.thirukural.game \n";
                    i.putExtra(Intent.EXTRA_TEXT, sAux);
                    startActivity(Intent.createChooser(i, "Share this App"));
                } catch(Exception e) {
                    FirebaseCrash.report(e);
                    e.printStackTrace();
                }
                break;
            case R.id.btn_aboutus:
                Defs.currentFragment = "about";
                FragmentManager ab_manager = getActivity().getSupportFragmentManager();
                FragmentTransaction ab_transaction = ab_manager.beginTransaction();
                ab_manager.popBackStackImmediate();
                AboutUsFragment aboutUsFragment = new AboutUsFragment();
                ab_transaction.replace(R.id.container, aboutUsFragment, "about");
                ab_transaction.addToBackStack(null);
                ab_transaction.commit();
                break;
            case R.id.btn_achievement:
                if (google.getText().toString().equalsIgnoreCase("Sign In"))
                    Toast.makeText(getActivity().getApplicationContext(), "Please Sign in with Google first.", Toast.LENGTH_LONG).show();
                else
                    startActivityForResult(Games.Achievements.getAchievementsIntent(mGoogleApiClient), 100);
                break;
            case R.id.btn_leaderboard:
                if (google.getText().toString().equalsIgnoreCase("Sign In"))
                    Toast.makeText(getActivity().getApplicationContext(), "Please Sign in with Google first.", Toast.LENGTH_LONG).show();
                else
                    startActivityForResult(Games.Leaderboards.getLeaderboardIntent(mGoogleApiClient, String.valueOf(R.string.app_id)), 1);
                break;
            case R.id.btn_google:
                signInSignOut();
                break;
            default:
                Toast.makeText(getActivity().getApplicationContext(), "Under Process...", Toast.LENGTH_LONG).show();
                break;
        }
    }

    private void signInSignOut() {
        if (google.getText().toString().equalsIgnoreCase("Sign In")) {
            // start the asynchronous sign in flow
            mSignInClicked = true;
            mGoogleApiClient.connect();
        } else {
            // sign out.
            mSignInClicked = false;
            Games.signOut(mGoogleApiClient);

            google.setText("Sign In");

            // user explicitly signed out, so turn off auto sign in
            mExplicitSignOut = true;
            if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                try {
                    Games.signOut(mGoogleApiClient);
                    mGoogleApiClient.disconnect();
                } catch (Exception e) {
                    FirebaseCrash.report(e);
                    e.printStackTrace();
                }
            }
        }
    }

    public void launchNewActivity(Context context, String packageName) {
        Intent intent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.CUPCAKE) {
            intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        }
        if (intent == null) {
            try {
                intent = new Intent(Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setData(Uri.parse("market://details?id=" + packageName));
                context.startActivity(intent);
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packageName)));
            }
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    private void stopPlaying() {
        if (mp != null) {
            mp.stop();
            mp.release();
            mp = null;
        }
    }

    private void playSound() {
        stopPlaying();
        if (Defs.playSound) {
            mp = MediaPlayer.create(getActivity(), R.raw.button_click);
            mp.start();
        }
    }
}