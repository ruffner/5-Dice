package com.bluntllama.fivekind.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.bluntllama.fivekind.HighScoreDataSource;
import com.bluntllama.fivekind.R;
import com.bluntllama.fivekind.ScoreTable;
import com.bluntllama.fivekind.fragments.HighScoresFragment;
import com.bluntllama.fivekind.fragments.OnlineLeaderBoardFragment;
import com.bluntllama.fivekind.fragments.ScorePadFragment;
import com.bluntllama.fivekind.items.Die;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DiceActivity extends FragmentActivity implements ScorePadFragment.ScoreSelectedListener{
    static final String TAG = "5 Dice";
    public static final String PREFS_NAME = "prefs";
    static final String FRAGMENT_TAG = "frag";

    Die[] mDice = new Die[5];
    String[] rollsLeftLabels = new String[3];

    boolean isRolling = false;
    boolean scoreRecorded = false;
    boolean gameOver = false;
    boolean isSingleGame = true;

    private String[] mDrawertTitles;
    private CharSequence mTitle;
    private CharSequence mDrawerTitle = "5 Dice Menu";

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private SharedPreferences sharedPrefs;
    private SharedPreferences mPrefs;
    private ScorePadFragment scorePad;
    private HighScoreDataSource mDataSource;

    private Button mRollButton;
    int rollsLeft = 3;
    private int curPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.fragmented_preferences, false);
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(sharedPrefs.getString("pref_dice_layout", "screen_top").equals("screen_bottom"))
            setContentView(R.layout.activity_dice_lower);
        else
            setContentView(R.layout.activity_dice);

        mDrawertTitles = getResources().getStringArray(R.array.drawer_titles);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.list_item_drawer, mDrawertTitles));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        mDataSource = new HighScoreDataSource(this);
        mPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        rollsLeftLabels = getResources().getStringArray(R.array.rolls_left);

        mRollButton = (Button) findViewById(R.id.button1);

        setRollButtonOnClick();

        mDice[0] = new Die((ImageView) findViewById(R.id.imageView3));
        mDice[1] = new Die((ImageView) findViewById(R.id.imageView2));
        mDice[2] = new Die((ImageView) findViewById(R.id.imageView6));
        mDice[3] = new Die((ImageView) findViewById(R.id.imageView5));
        mDice[4] = new Die((ImageView) findViewById(R.id.imageView1));

        isSingleGame = mPrefs.getBoolean("isSingleGame", true);

        setTitle("5 Dice");

        // auto resume on startup
        if(mPrefs.getBoolean("isSingleGame", true)) {
            Bundle args = new Bundle();
            Fragment fragment = new ScorePadFragment();
            scorePad = (ScorePadFragment)fragment;
            scorePad.setId(0);
            scorePad.setDice(mDice);

            args.putBoolean(ScorePadFragment.ARG_RESTORE, true);
            fragment.setArguments(args);

            // Insert the fragment by replacing any existing fragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, fragment, FRAGMENT_TAG)
                    .commit();
        }

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        Log.d(TAG, "Window focused changed.");

        boolean large = ((getBaseContext().getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE);
        boolean xlarge = ((getBaseContext().getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == 4);
        boolean isTablet = large || xlarge;

        ViewGroup.LayoutParams params = findViewById(R.id.layout_dice).getLayoutParams();
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        if(!isTablet) {
            if(rotation == 0 | rotation == 2)
                params.height = mDice[0].imageView.getWidth();
            else
                params.width = mDice[0].imageView.getHeight();
        } else {
            if(rotation == 0 | rotation == 2)
                params.width = mDice[0].imageView.getHeight();
            else
                params.height = mDice[0].imageView.getWidth();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onResume() {
        super.onResume();

        ////////
        boolean large = ((getBaseContext().getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE);
        boolean xlarge = ((getBaseContext().getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == 4);
        boolean isTablet = large || xlarge;

        ViewGroup.LayoutParams params = findViewById(R.id.layout_dice).getLayoutParams();
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        if(!isTablet) {
            if(rotation == 0 | rotation == 2)
                params.height = mDice[0].imageView.getWidth();
            else
                params.width = mDice[0].imageView.getHeight();
        } else {
            if(rotation == 0 | rotation == 2)
                params.width = mDice[0].imageView.getHeight();
            else
                params.height = mDice[0].imageView.getWidth();
        }
        //////////

        if(gameOver && !scoreRecorded)
            findViewById(R.id.score_record_button).setVisibility(View.VISIBLE);

        if(isSingleGame)
            scorePad = (ScorePadFragment)getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);

        if(scorePad == null)
            mDrawerLayout.openDrawer(mDrawerList);

        Log.d(TAG, "onResume() called... rollsLeft is " + rollsLeft);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(isRolling)
            mRollButton.callOnClick();

        saveGameState();

        Log.d(TAG, "onPause() called, rollsLeft is " + rollsLeft);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item))
            return true;

        switch (item.getItemId()) {
            case R.id.action_help:
                startActivity(new Intent(this, HelpActivity.class));
                return true;
            case R.id.action_donate:
                // app icon in action bar clicked; go home
                Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=TPK9CY7ZLY6QN&lc=US&item_name=Matt%20Ruffner%20App%20Development&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHosted"));
                startActivity(viewIntent);
                return true;
            case R.id.action_send_feedback:
                Intent send = new Intent(Intent.ACTION_SENDTO);
                String uriText = "mailto:" + Uri.encode("bluntllama@gmail.com") +
                        "?subject=" + Uri.encode("5 Dice Feedback");
                Uri uri = Uri.parse(uriText);

                send.setData(uri);
                startActivity(Intent.createChooser(send, "Send Feedback..."));
                return true;
            case R.id.action_settings:
                startActivity(new Intent(this, PrefsActivity.class));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public Die[] getDice() {
        restoreGameState();

        return mDice;
    }

    @Override
    public void scoreSelected(int fragId) {
        newTurn();
    }

    @Override
    public void fragGameOver(int fragId) {
        Log.d(TAG, "fragGameOver called");

        disableDice();
        gameOver = true;
        mRollButton.setEnabled(false);
        mRollButton.setText(R.string.game_over);
        if(!scoreRecorded) {
            findViewById(R.id.score_record_button).setVisibility(View.VISIBLE);
            putInHighScores();
        }



    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    /** Swaps fragments in the main content view */
    private void selectItem(int position) {


        Fragment fragment;

        mDrawerLayout.closeDrawer(mDrawerList);
        Bundle args = new Bundle();
        FragmentManager fragmentManager = getSupportFragmentManager();

        // Create a new fragment and specify the planet to show based on position
        switch (position) {
            case 0:
                if(curPosition != 0) {
                    if(curPosition == 0 || curPosition == 1)
                        saveGameState();
                    restoreGameState();

                    findViewById(R.id.layout_dice).setVisibility(View.VISIBLE);
                    findViewById(R.id.button1).setVisibility(View.VISIBLE);

                    fragment = new ScorePadFragment();
                    scorePad = (ScorePadFragment)fragment;
                    scorePad.setId(0);
                    scorePad.setDice(mDice);

                    args.putBoolean(ScorePadFragment.ARG_RESTORE, true);
                    fragment.setArguments(args);

                    // Insert the fragment by replacing any existing fragment
                    fragmentManager.beginTransaction()
                            .replace(R.id.content_frame, fragment, FRAGMENT_TAG)
                            .commit();

                    curPosition = position;
                }
                break;
            case 1:
                findViewById(R.id.layout_dice).setVisibility(View.VISIBLE);
                findViewById(R.id.button1).setVisibility(View.VISIBLE);

                findViewById(R.id.score_record_button).setVisibility(View.GONE);

                gameOver = false;
                setRollButtonOnClick();

                resetDice();
                resetRollsLeft();
                newTurn();

                if(curPosition == 1 || curPosition == 0)
                    scorePad.reset();
                else {
                    fragment = new ScorePadFragment();
                    scorePad = (ScorePadFragment)fragment;
                    scorePad.setId(0);

                    args.putBoolean(ScorePadFragment.ARG_RESTORE, false);
                    fragment.setArguments(args);

                    // Insert the fragment by replacing any existing fragment
                    fragmentManager.beginTransaction()
                            .replace(R.id.content_frame, fragment, FRAGMENT_TAG)
                            .commit();
                }

                curPosition = position;
                break;
            case 2:
                if(curPosition == 0 || curPosition == 1)
                    saveGameState();

                findViewById(R.id.layout_dice).setVisibility(View.GONE);
                findViewById(R.id.button1).setVisibility(View.GONE);
                fragment = new HighScoresFragment();

                // Insert the fragment by replacing any existing fragment
                fragmentManager.beginTransaction()
                        .replace(R.id.content_frame, fragment, HighScoresFragment.FRAG_TAG)
                        .commit();

                curPosition = position;
                break;
            case 3:
                if(curPosition == 0 || curPosition == 1)
                    saveGameState();

                findViewById(R.id.layout_dice).setVisibility(View.GONE);
                findViewById(R.id.button1).setVisibility(View.GONE);
                fragment = new OnlineLeaderBoardFragment();

                // Insert the fragment by replacing any existing fragment
                fragmentManager.beginTransaction()
                        .replace(R.id.content_frame, fragment, OnlineLeaderBoardFragment.FRAG_TAG)
                        .commit();

                curPosition = position;
                break;
        }


        // Highlight the selected item, update the title, and close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(mDrawertTitles[position]);

    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    public void scoreViewOnClick(View view) {
        scorePad.scoreViewOnClick(view);
    }

    public void highlight(View v) {
        if (rollsLeft == 3) {
            Toast.makeText(this, "Please roll first.", Toast.LENGTH_SHORT).show();
            return;
        } else if (rollsLeft == 0) {
            Toast.makeText(this, "Please score your turn.", Toast.LENGTH_SHORT).show();
            return;
        }
        switch (v.getId()) {
            case R.id.imageView3:
                toggleBg(0);
                break;
            case R.id.imageView2:
                toggleBg(1);
                break;
            case R.id.imageView6:
                toggleBg(2);
                break;
            case R.id.imageView5:
                toggleBg(3);
                break;
            case R.id.imageView1:
                toggleBg(4);
                break;
        }
    }

    private void toggleBg(int a) {
        if (!mDice[a].isSelected) {
            mDice[a].imageView.setAlpha(0.3f);
            mDice[a].isSelected = true;
        } else {
            mDice[a].imageView.setAlpha(1.0f);
            mDice[a].isSelected = false;
        }
    }

    public void setRollButtonOnClick() {
        mRollButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isRolling) {
                    if(sharedPrefs.getBoolean("pref_one_click_roll", false))
                        mRollButton.setText(R.string.rolling);
                    else
                        mRollButton.setText(R.string.end_roll);
                } else {
                    mRollButton.setText(R.string.roll);
                    rollsLeft--;
                    mRollButton.setText(rollsLeftLabels[rollsLeft]);
                }
                rollDice();
                Log.v(TAG, "end of rollButton()");
            }
        });

    }

    public void rollDice() {
        if (!isRolling) {
            for (Die i : mDice)
                if (!i.isSelected)
                    i.anim.start();
            isRolling = true;
            if(sharedPrefs.getBoolean("pref_one_click_roll", false)) {
                Handler handler = new Handler();
                final Runnable r = new Runnable() {
                    public void run() {
                        if(isRolling)
                            mRollButton.callOnClick();
                    }
                };

                handler.postDelayed(r, Integer.parseInt(sharedPrefs.getString("pref_roll_time", "500")));
                mRollButton.setEnabled(false);
            }
        } else {
            if(sharedPrefs.getBoolean("pref_one_click_roll", false))
                mRollButton.setEnabled(true);
            Random r = new Random();
            for (Die i : mDice)
                if (!i.isSelected) {
                    i.anim.stop();
                    i.value = r.nextInt(6) + 1;
                    i.anim.selectDrawable(i.value - 1);
                }
            isRolling = false;
            scorePad.rollOver(mDice, rollsLeft);
        }



        if (rollsLeft == 0)
            turnOver();
    }

    private void resetDice() {
        for (Die i : mDice) {
            i.reset();
        }
    }

    private void disableDice() {
        for (Die i : mDice)
            i.setEnabled(false);
    }

    private void enableDice() {
        for (Die i : mDice)
            i.setEnabled(true);
    }

    private void turnOver() {
        mRollButton.setEnabled(false);
        disableDice();
    }

    private void newTurn() {
        enableDice();
        mRollButton.setEnabled(true);
        mRollButton.setText(R.string.roll);
        resetRollsLeft();
    }

    private void resetRollsLeft() {
        rollsLeft = 3;
        if(mRollButton != null)
            mRollButton.setText(rollsLeftLabels[rollsLeft]);
    }

    private void putInHighScores() {
        final ContentValues values = new ContentValues();

        final int gameScore = scorePad.getGameScore();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Your score: " + gameScore);

        View v = getLayoutInflater().inflate(R.layout.layout_highscore_dialog, null);
        builder.setView(v);

        final EditText edit = (EditText)v.findViewById(R.id.editText);
        final CheckBox cb = (CheckBox)v.findViewById(R.id.checkBox);

        // Add the buttons
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                scoreRecorded = true;
                findViewById(R.id.score_record_button).setVisibility(View.GONE);
                values.put(ScoreTable.COLUMN_NAME_PLAYER_NAME, (edit.getText().toString().equals("") ? "Player" : edit.getText().toString()));
                values.put(ScoreTable.COLUMN_NAME_TALLY_GAME, Integer.toString(gameScore));
                mDataSource.open();
                long insertId = mDataSource.insertScore(values);

                if(cb.isChecked()) {
                    new SendHighScore().execute(
                            Long.toString(mDataSource.getDateOfRecord(insertId)),
                            (edit.getText().toString().equals("") ? "Player" : edit.getText().toString()),
                            Integer.toString(gameScore)
                    );
                }

                mDataSource.close();
                mRollButton.setText(R.string.game_over);

            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Log.d(TAG, "you canceled");
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public class SendHighScore extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... data) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://mshsprojects.net/matt/register.php");

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
            nameValuePairs.add(new BasicNameValuePair(ScoreTable.COLUMN_NAME_GAME_DATE, data[0]));
            nameValuePairs.add(new BasicNameValuePair(ScoreTable.COLUMN_NAME_PLAYER_NAME, data[1]));
            nameValuePairs.add(new BasicNameValuePair(ScoreTable.COLUMN_NAME_TALLY_GAME, data[2]));

            try {
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            HttpResponse response = null;
            try {
                response = httpclient.execute(httppost);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(response == null)
                return "Failed to send high score";
            else {
                String ret = "An error occurred";
                try {
                    ret = EntityUtils.toString(response.getEntity());
                } catch (IOException e) {
                        e.printStackTrace();
                }
                return ret;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(DiceActivity.this, result, Toast.LENGTH_SHORT).show();
        }
    }

    public void buttonRecordScore(View v) {
        putInHighScores();
    }

    private void saveGameState() {
        if(getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG) != null) {
            SharedPreferences.Editor editor = mPrefs.edit();

            if(isRolling)
                mRollButton.callOnClick();

            for(int i = 0; i < 5; i++) {
                editor.putInt("dice" + Integer.toString(i), mDice[i].value);
                editor.putBoolean("dice" + Integer.toString(i) + "taken", mDice[i].isSelected);
            }

            editor.putInt("rolls_left", rollsLeft);
            editor.putBoolean("gameOver", gameOver);

            editor.commit();

            scorePad.save();
        }
    }

    private void restoreGameState() {
        mPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        rollsLeft = mPrefs.getInt("rolls_left", 3);

        for(int i = 0; i < 5; i++) {
            mDice[i].setValue(mPrefs.getInt("dice" + Integer.toString(i), 1));
            mDice[i].setEnabled(!mPrefs.getBoolean("dice" + Integer.toString(i) + "taken", false));
        }

        scoreRecorded = mPrefs.getBoolean("scoreRecorded", false);
        gameOver = mPrefs.getBoolean("gameOver", false);

        if(rollsLeft == 0)
            turnOver();

        mRollButton.setText(rollsLeftLabels[rollsLeft]);

        if(gameOver) {
            mRollButton.setEnabled(false);
            mRollButton.setText(R.string.game_over);
        }
    }
}