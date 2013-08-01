package com.bluntllama.fivekind.fragments;

import android.app.ActionBar;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bluntllama.fivekind.JSONArrayAdapter;
import com.bluntllama.fivekind.R;
import com.bluntllama.fivekind.ScoreTable;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;

public class OnlineLeaderBoardFragment extends ListFragment {
    // For the json adapter, specify which columns go into which views
    String[] fromColumns = {ScoreTable.COLUMN_NAME_PLAYER_NAME, ScoreTable.COLUMN_NAME_TALLY_GAME};
    int[] toViews = {R.id.player_name, R.id.game_score};

    public static String FRAG_TAG = "onlineleaderboardtag";

    private ProgressBar progressBar;
    private TextView noConnectionView;
    private Button mRefreshButton;
    private JSONArrayAdapter mAdapter;
    private JSONObject info;
    private Button mButton0;
    private Button mButton1;
    private Button mButton2;
    LeaderBoardGetter mGetter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_leaderboard, container, false);

        progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        noConnectionView = (TextView) rootView.findViewById(R.id.no_connection);

        mButton0 = (Button)rootView.findViewById(R.id.timeframe_today);
        mButton1 = (Button)rootView.findViewById(R.id.timeframe_this_week);
        mButton2 = (Button)rootView.findViewById(R.id.timeframe_all_time);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar rightNow = Calendar.getInstance();
                mGetter = new LeaderBoardGetter();
                int dif;
                switch (view.getId()) {
                    case R.id.timeframe_today:
                        setButtonBg(0);
                        dif = (rightNow.get(Calendar.HOUR_OF_DAY) * 60 * 60 * 1000) + (rightNow.get(Calendar.MINUTE) * 60 * 1000) + (rightNow.get(Calendar.SECOND) * 1000) + rightNow.get(Calendar.MILLISECOND);
                        Log.d("5 Dice", "dif is " + dif);
                        Log.d("5 Dice", "url for today is: " + (rightNow.getTimeInMillis()-dif));
                        mGetter.execute("http://mshsprojects.net/matt/highscores.php?time=" + (rightNow.getTimeInMillis()-dif));
                        break;
                    case R.id.timeframe_this_week:
                        setButtonBg(1);
                        dif = ((rightNow.get(Calendar.DAY_OF_WEEK)-1) * 24 * 60 * 60 * 1000) + (rightNow.get(Calendar.HOUR_OF_DAY) * 60 * 60 * 1000) + (rightNow.get(Calendar.MINUTE) * 60 * 1000) + (rightNow.get(Calendar.SECOND) * 1000) + rightNow.get(Calendar.MILLISECOND);
                        Log.d("5 Dice", "url for this week is: " + rightNow.getTimeInMillis());
                        mGetter.execute("http://mshsprojects.net/matt/highscores.php?time=" + (rightNow.getTimeInMillis()-dif));
                        break;
                    case R.id.timeframe_all_time:
                        setButtonBg(2);
                        mGetter.execute("http://mshsprojects.net/matt/highscores.php?id=100");
                        break;
                }
            }
        };
        rootView.findViewById(R.id.timeframe_all_time).setOnClickListener(listener);
        rootView.findViewById(R.id.timeframe_this_week).setOnClickListener(listener);
        rootView.findViewById(R.id.timeframe_today).setOnClickListener(listener);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mGetter = new LeaderBoardGetter();
        Calendar rightNow = Calendar.getInstance();
        long dif = ((rightNow.get(Calendar.DAY_OF_WEEK)-1) * 24 * 60 * 60 * 1000) + (rightNow.get(Calendar.HOUR_OF_DAY) * 60 * 60 * 1000) + (rightNow.get(Calendar.MINUTE) * 60 * 1000) + (rightNow.get(Calendar.SECOND) * 1000) + rightNow.get(Calendar.MILLISECOND);
        Log.d("5 Dice", "url for this week is: " + rightNow.getTimeInMillis());
        mGetter.execute("http://mshsprojects.net/matt/highscores.php?time=" + (rightNow.getTimeInMillis()-dif));

        setButtonBg(1);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDetach() {
        if(!mGetter.isCancelled())
            mGetter.cancel(true);
        super.onDetach();
    }

    public class LeaderBoardGetter extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected void onPreExecute() {
            getListView().setVisibility(View.GONE);
            getActivity().findViewById(android.R.id.empty).setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            noConnectionView.setVisibility(View.GONE);
        }

        @Override
        protected JSONObject doInBackground(String... urls) {
            InputStream is = null;
            String json = "";
            JSONObject jObj = null;

            // Making HTTP request
            try {
                // defaultHttpClient
                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpGet httpPost = new HttpGet(urls[0]);

                HttpResponse httpResponse = httpClient.execute(httpPost);
                HttpEntity httpEntity = httpResponse.getEntity();
                is = httpEntity.getContent();

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        is, "iso-8859-1"), 8);
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                is.close();
                json = sb.toString();
            } catch (Exception e) {
                Log.e("Buffer Error", "Error converting result " + e.toString());
            }

            // try parse the string to a JSON object
            try {
                jObj = new JSONObject(json);
            } catch (JSONException e) {
                Log.e("JSON Parser", "Error parsing data " + e.toString());
            }

            // return JSON String
            return jObj;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            if (getFragmentManager().findFragmentByTag(FRAG_TAG) != null) {

                if (jsonObject != null) {
                    info = jsonObject;

                    try {
                        mAdapter = new JSONArrayAdapter(getActivity(), info.getJSONArray("scores"), R.layout.list_item_highscore, fromColumns, toViews);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    setListAdapter(mAdapter);

                    if (noConnectionView.getVisibility() == View.VISIBLE)
                        noConnectionView.setVisibility(View.GONE);

                    progressBar.setVisibility(View.GONE);
                    getListView().setVisibility(View.VISIBLE);
                } else {
                    noConnectionView.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                }
            }
        }
    }

    private void setButtonBg(int id) {
        switch (id) {
            case 0:
                mButton0.setBackgroundColor(getResources().getColor(R.color.holo_light_blue));
                mButton1.setBackgroundColor(Color.TRANSPARENT);
                mButton2.setBackgroundColor(Color.TRANSPARENT);
                break;
            case 1:
                mButton0.setBackgroundColor(Color.TRANSPARENT);
                mButton1.setBackgroundColor(getResources().getColor(R.color.holo_light_blue));
                mButton2.setBackgroundColor(Color.TRANSPARENT);
                break;
            case 2:
                mButton0.setBackgroundColor(Color.TRANSPARENT);
                mButton1.setBackgroundColor(Color.TRANSPARENT);
                mButton2.setBackgroundColor(getResources().getColor(R.color.holo_light_blue));
                break;
        }
    }
}