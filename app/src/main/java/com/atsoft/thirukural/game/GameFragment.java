package com.atsoft.thirukural.game;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.firebase.crash.FirebaseCrash;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;


/**
 * A simple {@link Fragment} subclass.
 */
public class GameFragment extends Fragment implements View.OnClickListener, RewardedVideoAdListener {

    TextView first,second,third,fourth,fifth,sixth,seventh,a1,a2,a3,a4,a5,a6,a7,a8,a9;
    ArrayList<String> ans_word, kuralnoarr, correct_ans;
    String dash;
    SharedPreferences sharedPrefs;
    Button next;
    static String thirukural = "";
    static int lifec = 3;
    TextView timmer, score;
    ImageView life_img;
    CountDownTimer countDownTimer;
    static int tempTime = 0, adLoadFor = 0, mpseek = 0;
    static boolean afterPause = false;
    TextView hintCount;
    RelativeLayout hintll;
    private MediaPlayer mp;
    private RewardedVideoAd mAd;
    private AdView mAdView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_game, container, false);

        MobileAds.initialize(getActivity(), getResources().getString(R.string.appId));
        sharedPrefs = getActivity().getSharedPreferences(Defs.sharedPreferenceName, Context.MODE_PRIVATE);
        lifec = 3;
        View qus_view = rootView.findViewById(R.id.qus_ll);
        Bitmap bitmap = ImageHelper.getRoundedCornerBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.darkbg), 50);
        BitmapDrawable background = new BitmapDrawable(bitmap);
        qus_view.setBackground(background);
        Drawable qus_bg = qus_view.getBackground();
        qus_bg.setAlpha(40);
        View ans_view = rootView.findViewById(R.id.ans_ll);
        ans_view.setBackground(background);
        Drawable ans_bg = ans_view.getBackground();
        ans_bg.setAlpha(80);
        init(rootView);
        int cc_hint_count = sharedPrefs.getInt("hintCount", 15);
        hintCount.setText(String.valueOf(cc_hint_count));
        setQuestion();
        setTimmer(60000);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        mAd = MobileAds.getRewardedVideoAdInstance(getActivity());
        mAd.setRewardedVideoAdListener(this);
        loadRewardedVideoAd();
        return rootView;
    }

    private void loadRewardedVideoAd() {
        if (!mAd.isLoaded()) {
            mAd.loadAd(getResources().getString(R.string.adUnitIdVideo), new AdRequest.Builder().build());
//            mAd.loadAd("ca-app-pub-3940256099942544/5224354917", new AdRequest.Builder().build());
        }
    }

    private void setTimmer(int timertime) {
        playSound(1);
        countDownTimer = new CountDownTimer(timertime, 1000) {

            public void onTick(long millisUntilFinished) {
                long time = millisUntilFinished / 1000;
                timmer.setText(""+time);
                if (time < 11)
                    timmer.setTextColor(Color.RED);
                else
                    timmer.setTextColor(Color.BLACK);
                //here you can have your logic to set text to edittext
            }

            public void onFinish() {
                playSound(2);
                timmer.setText("0");
                if (isOnline()) {
                    AlertDialog.Builder mt_builder = new AlertDialog.Builder(getActivity());
                    mt_builder.setCancelable(false);
                    mt_builder.setTitle("Timeout");
                    mt_builder.setIcon(R.drawable.life_heart_broken);
                    mt_builder.setMessage("Do you want more time watch the video.");
                    mt_builder.setPositiveButton("Watch Video", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            if (mAd.isLoaded()) {
                                adLoadFor = 1;
                                mAd.show();
                            } else {
                                wrongAnswerDialog();
                            }
                        }
                    })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    wrongAnswerDialog();
                                }
                            });

                    // Create the AlertDialog object and return it
                    mt_builder.create().show();
                } else
                    wrongAnswerDialog();
            }

        }.start();
    }

    private void wrongAns() {
        switch (lifec) {
            case 3:
                life_img.setImageDrawable(getResources().getDrawable(R.drawable.life_2));
                countDownTimer.cancel();
                setTimmer(60000);
                lifec = 2;
                break;
            case 2:
                life_img.setImageDrawable(getResources().getDrawable(R.drawable.life_1));
                countDownTimer.cancel();
                setTimmer(60000);
                lifec = 1;
                break;
        }
    }

    private void setQuestion() {
        ans_word = new ArrayList<>();
        correct_ans = new ArrayList<>();
        dash = getResources().getString(R.string.dash);
        thirukural = getKural();
        StringTokenizer token = new StringTokenizer(thirukural, " ");
        first.setText(token.nextToken());
        ans_word.add(token.nextToken());
        third.setText(token.nextToken());
        ans_word.add(token.nextToken());
        fifth.setText(token.nextToken());
        ans_word.add(token.nextToken());
        try {
            seventh.setText(token.nextToken());
        } catch (Exception e) {
            FirebaseCrash.report(e);
            e.printStackTrace();
        }
        second.setText(dash);
        fourth.setText(dash);
        sixth.setText(dash);
        Collections.shuffle(Defs.allwords);
        int i = 0;
        while (ans_word.size() < 9) {
            if (!ans_word.contains(Defs.allwords.get(i))) {
                ans_word.add(Defs.allwords.get(i));
                correct_ans.add(Defs.allwords.get(i));
            }
            i++;
        }
        Collections.shuffle(ans_word);
        a1.setText(ans_word.get(0));
        a2.setText(ans_word.get(1));
        a3.setText(ans_word.get(2));
        a4.setText(ans_word.get(3));
        a5.setText(ans_word.get(4));
        a6.setText(ans_word.get(5));
        a7.setText(ans_word.get(6));
        a8.setText(ans_word.get(7));
        a9.setText(ans_word.get(8));
        a1.setEnabled(true);
        a1.setTextColor(Color.BLACK);
        a2.setEnabled(true);
        a2.setTextColor(Color.BLACK);
        a3.setEnabled(true);
        a3.setTextColor(Color.BLACK);
        a4.setEnabled(true);
        a4.setTextColor(Color.BLACK);
        a5.setEnabled(true);
        a5.setTextColor(Color.BLACK);
        a6.setEnabled(true);
        a6.setTextColor(Color.BLACK);
        a7.setEnabled(true);
        a7.setTextColor(Color.BLACK);
        a8.setEnabled(true);
        a8.setTextColor(Color.BLACK);
        a9.setEnabled(true);
        a9.setTextColor(Color.BLACK);
    }

    private String getKural() {
        Set<String> stringset = sharedPrefs.getStringSet("kural_no_set", null);
        kuralnoarr = new ArrayList<>();
        if (stringset == null) {
//        if (stringset.equals(null) || stringset.size() == 0 || stringset.isEmpty()) {
            for (int i = 0; i < 1330; i++)
                kuralnoarr.add(String.valueOf(i));
        } else {
            for (String str : stringset)
                kuralnoarr.add(str);
        }
        Collections.shuffle(kuralnoarr);
        DBController controller = new DBController(getActivity());
        SQLiteDatabase db = controller.getReadableDatabase();
        String qus_kural = "";
        String kuralnostr = String.valueOf(kuralnoarr.get(0));
        String qry = "SELECT thirukural FROM kural where kuralno = '"+kuralnostr+"'";
        Cursor cursor = db.rawQuery(qry, null);
        if (cursor.moveToNext()) {
            qus_kural = cursor.getString(0);
        } else
            System.out.println("Syso empty db");
        cursor.close();
        db.close();
        kuralnoarr.remove(0);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        try {
            Set<String> set = new HashSet<>();
            set.addAll(kuralnoarr);
            editor.putStringSet("kural_no_set", set);
            editor.commit();
        } catch (Exception e) {
            FirebaseCrash.report(e);
            e.printStackTrace();
        }
        return qus_kural.replace("\n", "");
    }

    private void init(View rootView) {
        first = rootView.findViewById(R.id.first_tv);
        second = rootView.findViewById(R.id.second_tv);
        third = rootView.findViewById(R.id.third_tv);
        fourth = rootView.findViewById(R.id.fourth_tv);
        fifth = rootView.findViewById(R.id.fifth_tv);
        sixth = rootView.findViewById(R.id.sixth_tv);
        seventh = rootView.findViewById(R.id.seventh_tv);
        a1 = rootView.findViewById(R.id.word_tv_1);
        a2 = rootView.findViewById(R.id.word_tv_2);
        a3 = rootView.findViewById(R.id.word_tv_3);
        a4 = rootView.findViewById(R.id.word_tv_4);
        a5 = rootView.findViewById(R.id.word_tv_5);
        a6 = rootView.findViewById(R.id.word_tv_6);
        a7 = rootView.findViewById(R.id.word_tv_7);
        a8 = rootView.findViewById(R.id.word_tv_8);
        a9 = rootView.findViewById(R.id.word_tv_9);
        next = rootView.findViewById(R.id.next_btn);
        timmer = rootView.findViewById(R.id.timmer);
        score = rootView.findViewById(R.id.score_tv);
        life_img = rootView.findViewById(R.id.life_img);
        hintll = rootView.findViewById(R.id.hint_rl);
        hintCount = rootView.findViewById(R.id.noofhinttv);
        mAdView = rootView.findViewById(R.id.adView);
        second.setOnClickListener(this);
        fourth.setOnClickListener(this);
        sixth.setOnClickListener(this);
        a1.setOnClickListener(this);
        a2.setOnClickListener(this);
        a3.setOnClickListener(this);
        a4.setOnClickListener(this);
        a5.setOnClickListener(this);
        a6.setOnClickListener(this);
        a7.setOnClickListener(this);
        a8.setOnClickListener(this);
        a9.setOnClickListener(this);
        next.setOnClickListener(this);
        hintll.setOnClickListener(this);
    }

    public void checkAnswer() {
        countDownTimer.cancel();
        String ans = first.getText().toString().trim() + " " + second.getText().toString().trim() + " " + third.getText().toString().trim() + " " + fourth.getText().toString().trim() + "  " + fifth.getText().toString().trim() + " " + sixth.getText().toString().trim() + " " + seventh.getText().toString().trim();
        String ans2 = first.getText().toString().trim() + " " + second.getText().toString().trim() + " " + third.getText().toString().trim() + " " + fourth.getText().toString().trim() + " " + fifth.getText().toString().trim() + " " + sixth.getText().toString().trim() + " " + seventh.getText().toString().trim();
        if (thirukural.equalsIgnoreCase(ans) || thirukural.equalsIgnoreCase(ans2)) {
            playSound(3);
            AlertDialog.Builder correctDialog = new AlertDialog.Builder(getActivity());
            correctDialog.setCancelable(false);
            correctDialog.setIcon(R.drawable.life_1);
            correctDialog.setTitle("Result");
            correctDialog.setMessage("Correct Answer");
            correctDialog.setNeutralButton("Next", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    int scoreint = Integer.parseInt(score.getText().toString());
                    score.setText(String.valueOf(scoreint + 1));
                    if (scoreint % 5 == 0) {
                        int chint = Integer.parseInt(hintCount.getText().toString().trim());
                        hintCount.setText(String.valueOf(chint + 5));
                        SharedPreferences.Editor editor = sharedPrefs.edit();
                        try {
                            editor.putInt("hintCount", chint + 5);
                            editor.commit();
                        } catch (Exception e) {
                            FirebaseCrash.report(e);
                            e.printStackTrace();
                        }
                    }
                    setTimmer(60000);
                    setQuestion();
                }
            });
            correctDialog.create().show();
        } else {
            playSound(2);
            timmer.setText("0");
            if (isOnline()) {
                AlertDialog.Builder mt_builder = new AlertDialog.Builder(getActivity());
                mt_builder.setCancelable(false);
                mt_builder.setTitle("Wrong Answer");
                mt_builder.setIcon(R.drawable.life_heart_broken);
                mt_builder.setMessage("Do you want more time watch the video.");
                mt_builder.setPositiveButton("Watch Video", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if (mAd.isLoaded()) {
                            adLoadFor = 1;
                            mAd.show();
                        } else {
                            wrongAnswerDialog();
                        }
                    }
                })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                wrongAnswerDialog();
                            }
                        });

                // Create the AlertDialog object and return it
                mt_builder.create().show();
            } else
                wrongAnswerDialog();
        }
    }

    private void wrongAnswerDialog() {
        if (lifec != 1) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setCancelable(false);
            builder.setTitle("Wrong Answer / Timeout");
            builder.setIcon(R.drawable.life_heart_broken);
            builder.setMessage("Do you want try again or go to next Thirukural?");
            builder.setPositiveButton("Try again", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    wrongAns();
                }
            })
                    .setNegativeButton("Next", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            showCorrectAnswer();
                        }
                    });

            // Create the AlertDialog object and return it
            builder.create().show();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setCancelable(false);
            builder.setTitle(R.string.app_name);
            builder.setIcon(R.drawable.life_heart_broken);
            builder.setMessage("Game Over");
            builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    Defs.HighScore = Integer.parseInt(score.getText().toString());
                    FragmentManager manager = getActivity().getSupportFragmentManager();
                    FragmentTransaction transaction = manager.beginTransaction();
                    manager.popBackStackImmediate();
                    Defs.currentFragment = "home";
                    HomeFragment homeFragment = new HomeFragment();
                    transaction.replace(R.id.container, homeFragment, "Home");
                    transaction.addToBackStack(null);
                    transaction.commit();
                }
            });

            // Create the AlertDialog object and return it
            builder.create().show();
        }
    }

    private void showCorrectAnswer() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(false);
        builder.setTitle(R.string.app_name);
        builder.setIcon(R.drawable.life_heart_broken);
        builder.setMessage("Correct Answer is \n"+thirukural);
        builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                setQuestion();
                wrongAns();
            }
        });

        // Create the AlertDialog object and return it
        builder.create().show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.second_tv:
                if (!second.getText().toString().equalsIgnoreCase(dash)) {
                    int pos = ans_word.indexOf(second.getText().toString());
                    enableTv(pos);
                    second.setText(dash);
                }
                break;
            case R.id.fourth_tv:
                if (!fourth.getText().toString().equalsIgnoreCase(dash)) {
                    enableTv(ans_word.indexOf(fourth.getText().toString()));
                    fourth.setText(dash);
                }
                break;
            case R.id.sixth_tv:
                if (!sixth.getText().toString().equalsIgnoreCase(dash)) {
                    enableTv(ans_word.indexOf(sixth.getText().toString()));
                    sixth.setText(dash);
                }
                break;
            case R.id.word_tv_1:
                setAns(a1);
                break;
            case R.id.word_tv_2:
                setAns(a2);
                break;
            case R.id.word_tv_3:
                setAns(a3);
                break;
            case R.id.word_tv_4:
                setAns(a4);
                break;
            case R.id.word_tv_5:
                setAns(a5);
                break;
            case R.id.word_tv_6:
                setAns(a6);
                break;
            case R.id.word_tv_7:
                setAns(a7);
                break;
            case R.id.word_tv_8:
                setAns(a8);
                break;
            case R.id.word_tv_9:
                setAns(a9);
                break;
            case R.id.hint_rl:
                hintClick();
                break;
            case R.id.next_btn:
                checkAnswer();
                break;
        }
    }

    private void hintClick() {
        int hintCountInt = Integer.parseInt(hintCount.getText().toString().trim());
        if (hintCountInt < 1) {
            if (isOnline()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setCancelable(false);
                builder.setTitle("No Hint");
                builder.setIcon(android.R.drawable.stat_notify_error);
                builder.setMessage("You don't have enough hint. Do you want hint, watch the Video.");
                builder.setPositiveButton("Watch Video", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if (mAd.isLoaded()) {
                            adLoadFor = 2;
                            mAd.show();
                        } else {
                            dialog.dismiss();
                        }
                    }
                })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                // Create the AlertDialog object and return it
                builder.create().show();
            } else {
                Toast.makeText(getActivity().getApplicationContext(), "No Hint", Toast.LENGTH_LONG).show();
            }
        } else {
            showHint();
        }
    }

    private void showHint() {
        if (!correct_ans.isEmpty() || correct_ans.size() != 0) {
            hintCount.setText(String.valueOf(Integer.parseInt(hintCount.getText().toString().trim()) - 1));
            SharedPreferences.Editor editor = sharedPrefs.edit();
            try {
                editor.putInt("hintCount", Integer.parseInt(hintCount.getText().toString().trim()) - 1);
                editor.commit();
            } catch (Exception e) {
                FirebaseCrash.report(e);
                e.printStackTrace();
            }
            if (correct_ans.contains(a1.getText().toString().trim())) {
                a1.setTextColor(Color.RED);
                a1.setEnabled(false);
                correct_ans.remove(a1.getText().toString().trim());
            } else if (correct_ans.contains(a2.getText().toString().trim())) {
                a2.setTextColor(Color.RED);
                a2.setEnabled(false);
                correct_ans.remove(a2.getText().toString().trim());
            } else if (correct_ans.contains(a3.getText().toString().trim())) {
                a3.setTextColor(Color.RED);
                a3.setEnabled(false);
                correct_ans.remove(a3.getText().toString().trim());
            } else if (correct_ans.contains(a4.getText().toString().trim())) {
                a4.setTextColor(Color.RED);
                a4.setEnabled(false);
                correct_ans.remove(a4.getText().toString().trim());
            } else if (correct_ans.contains(a5.getText().toString().trim())) {
                a5.setTextColor(Color.RED);
                a5.setEnabled(false);
                correct_ans.remove(a5.getText().toString().trim());
            } else if (correct_ans.contains(a6.getText().toString().trim())) {
                a6.setTextColor(Color.RED);
                a6.setEnabled(false);
                correct_ans.remove(a6.getText().toString().trim());
            } else if (correct_ans.contains(a7.getText().toString().trim())) {
                a7.setTextColor(Color.RED);
                a7.setEnabled(false);
                correct_ans.remove(a7.getText().toString().trim());
            } else if (correct_ans.contains(a8.getText().toString().trim())) {
                a8.setTextColor(Color.RED);
                a8.setEnabled(false);
                correct_ans.remove(a8.getText().toString().trim());
            } else if (correct_ans.contains(a9.getText().toString().trim())) {
                a9.setTextColor(Color.RED);
                a9.setEnabled(false);
                correct_ans.remove(a9.getText().toString().trim());
            }
        } else {
            Toast.makeText(getActivity().getApplicationContext(), "No More Hint", Toast.LENGTH_LONG).show();
        }
    }

    private void setAns(TextView v) {
        int empty_tv = getEmptyTv();
        switch (empty_tv) {
            case 1:
                second.setText(v.getText().toString());
                break;
            case 2:
                fourth.setText(v.getText().toString());
                break;
            default:
                sixth.setText(v.getText().toString());
                break;
        }
        v.setEnabled(false);
        v.setTextColor(Color.GRAY);
    }

    private int getEmptyTv() {
        if (second.getText().toString().equalsIgnoreCase(dash))
            return 1;
        else if (fourth.getText().toString().equalsIgnoreCase(dash))
            return 2;
        else
            return 3;
    }

    private void enableTv(int pos) {
        switch (pos) {
            case 0:
                a1.setEnabled(true);
                a1.setTextColor(Color.BLACK);
                break;
            case 1:
                a2.setEnabled(true);
                a2.setTextColor(Color.BLACK);
                break;
            case 2:
                a3.setEnabled(true);
                a3.setTextColor(Color.BLACK);
                break;
            case 3:
                a4.setEnabled(true);
                a4.setTextColor(Color.BLACK);
                break;
            case 4:
                a5.setEnabled(true);
                a5.setTextColor(Color.BLACK);
                break;
            case 5:
                a6.setEnabled(true);
                a6.setTextColor(Color.BLACK);
                break;
            case 6:
                a7.setEnabled(true);
                a7.setTextColor(Color.BLACK);
                break;
            case 7:
                a8.setEnabled(true);
                a8.setTextColor(Color.BLACK);
                break;
        }
    }

    @Override
    public void onResume() {
        if (afterPause) {
            countDownTimer.cancel();
            setTimmer(tempTime);
            tempTime = 0;
            afterPause = false;
            if (mp != null) {
                mp.seekTo(mpseek);
                mp.start();
            }
        }
        mAd.resume(getActivity());
        super.onResume();
    }

    @Override
    public void onPause() {
        if (adLoadFor != 1) {
            tempTime = Integer.parseInt(timmer.getText().toString()) * 1000;
            afterPause = true;
        }
        if (mp != null) {
            mp.pause();
            mpseek = mp.getCurrentPosition();
        }
        mAd.pause(getActivity());
        super.onPause();
    }

    @Override
    public void onDestroy() {
        countDownTimer.cancel();
        stopPlaying();
        mAd.destroy(getActivity());
        super.onDestroy();
    }

    @Override
    public void onRewardedVideoAdLoaded() {
        loadRewardedVideoAd();
    }

    @Override
    public void onRewardedVideoAdOpened() {
        loadRewardedVideoAd();
    }

    @Override
    public void onRewardedVideoStarted() {
        loadRewardedVideoAd();
    }

    @Override
    public void onRewardedVideoAdClosed() {
        loadRewardedVideoAd();
    }

    @Override
    public void onRewarded(RewardItem rewardItem) {
        /*Toast.makeText(getActivity(), "onRewarded! currency: " + rewardItem.getType() + "  amount: " +
                rewardItem.getAmount(), Toast.LENGTH_SHORT).show();*/
        if (rewardItem.getAmount() == 1) {
            if (adLoadFor == 1) {
                countDownTimer.cancel();
                setTimmer(30000);
                adLoadFor = 0;
            } else {
                showHint();
            }
        }
        loadRewardedVideoAd();
        // Reward the user.
    }

    @Override
    public void onRewardedVideoAdLeftApplication() {
        loadRewardedVideoAd();
    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int i) {
        if (adLoadFor == 1) {
            loadRewardedVideoAd();
            wrongAnswerDialog();
            adLoadFor = 0;
        }
        loadRewardedVideoAd();
    }
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting())
            return true;
        else
            return false;
    }

    private void stopPlaying() {
        if (mp != null) {
            mp.stop();
            mp.release();
            mp = null;
        }
    }

    private void playSound(int sound) {
        stopPlaying();
        if (Defs.playSound) {
            if (sound == 1) {
                mp = MediaPlayer.create(getActivity(), R.raw.bg_sound);
            } else if (sound == 2) {
                mp = MediaPlayer.create(getActivity(), R.raw.wrong_answer);
            } else if (sound == 3) {
                mp = MediaPlayer.create(getActivity(), R.raw.correct_answer);
            }
            mp.start();
        }
    }
}
