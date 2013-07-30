package com.bluntllama.fivekind.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class OnlineLeaderBoardFragment extends ListFragment {
    // For the json adapter, specify which columns go into which views
    String[] fromColumns = {ScoreTable.COLUMN_NAME_PLAYER_NAME, ScoreTable.COLUMN_NAME_TALLY_GAME};
    int[] toViews = {R.id.player_name, R.id.game_score};

    public static String FRAG_TAG = "onlineleaderboardtag";

    private ProgressBar progressBar;
    private TextView noConnectionView;
    private JSONArrayAdapter mAdapter;
    private JSONObject info;
    LeaderBoardGetter mGetter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_leaderboard, container, false);

        progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        noConnectionView = (TextView) rootView.findViewById(R.id.no_connection);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mGetter = new LeaderBoardGetter();
        mGetter.execute("http://mshsprojects.net/matt/highscores.php?id=50");

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDetach() {
        if(!mGetter.isCancelled())
            mGetter.cancel(true);
        super.onDetach();
    }

    public void refresh() {
        mGetter = new LeaderBoardGetter();
        mGetter.execute("http://mshsprojects.net/matt/highscores.php?id=50");
    }

    public class LeaderBoardGetter extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected void onPreExecute() {
            getListView().setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
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
                }
            }
        }
    }
}