package com.bluntllama.fivekind.fragments;

import android.app.AlertDialog;import android.content.Context;import android.content.DialogInterface;import android.os.AsyncTask;import android.os.Bundle;import android.support.v4.app.ListFragment;import android.support.v4.widget.SimpleCursorAdapter;import android.view.LayoutInflater;import android.view.View;import android.view.ViewGroup;import android.widget.AdapterView;import android.widget.TextView;import android.widget.Toast;import com.bluntllama.fivekind.HighScoreDataSource;import com.bluntllama.fivekind.R;import com.bluntllama.fivekind.ScoreTable;import org.apache.http.HttpResponse;import org.apache.http.NameValuePair;import org.apache.http.client.HttpClient;import org.apache.http.client.entity.UrlEncodedFormEntity;import org.apache.http.client.methods.HttpPost;import org.apache.http.impl.client.DefaultHttpClient;import org.apache.http.message.BasicNameValuePair;import org.apache.http.util.EntityUtils;import java.io.IOException;import java.io.UnsupportedEncodingException;import java.util.ArrayList;import java.util.List;

public class HighScoresFragment extends ListFragment {
    public static String FRAG_TAG = "highscores_frag";

    static HighScoreDataSource mDataSource;

    static SendHighScore mHighScoreSender;

    static Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_high_scores, container, false);

        context = getActivity();

        mDataSource = new HighScoreDataSource(getActivity());

        return rootView;
    }

    private static void renewSender() {
        mHighScoreSender = new SendHighScore();
    }

    @Override
    public void onDetach() {
        mDataSource.close();
        super.onDetach();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final SimpleCursorAdapter mAdapter;

        // For the cursor adapter, specify which columns go into which views
        String[] fromColumns = {ScoreTable.COLUMN_NAME_PLAYER_NAME, ScoreTable.COLUMN_NAME_TALLY_GAME };
        int[] toViews = {R.id.player_name, R.id.game_score };

        // Create an empty adapter we will use to display the loaded data.
        // We pass null for the cursor, then update it in onLoadFinished()
        mAdapter = new SimpleCursorAdapter(
                getActivity(),
                R.layout.list_item_highscore,
                null,
                fromColumns,
                toViews,
                0
        );

        mDataSource.open();
        mAdapter.swapCursor(mDataSource.getHighScores());
        setListAdapter(mAdapter);
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView dv = (TextView)view.findViewById(R.id.date_view);
                if(dv.getVisibility() == View.VISIBLE) {
                    dv.setVisibility(View.GONE);
                } else {
                    view.findViewById(R.id.date_view).setVisibility(View.VISIBLE);
                    ((TextView)view.findViewById(R.id.date_view)).setText(mDataSource.getDateOfRecord(
                            ((TextView)view.findViewById(R.id.player_name)).getText().toString(),
                            Integer.parseInt(((TextView)view.findViewById(R.id.game_score)).getText().toString())
                    ));
                }
            }
        });

        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                final View v = view;
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.actions)
                        .setItems(R.array.actions_array, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        mHighScoreSender.execute(
                                                mDataSource.getLongDateOfRecord(
                                                        ((TextView) v.findViewById(R.id.player_name)).getText().toString(),
                                                        Integer.parseInt(((TextView) v.findViewById(R.id.game_score)).getText().toString())
                                                ),
                                                ((TextView) v.findViewById(R.id.player_name)).getText().toString(),
                                                ((TextView) v.findViewById(R.id.game_score)).getText().toString()
                                        );
                                        renewSender();
                                        break;
                                    case 1:
                                        mDataSource.deleteScore(
                                                ((TextView) v.findViewById(R.id.player_name)).getText().toString(),
                                                Integer.parseInt(((TextView) v.findViewById(R.id.game_score)).getText().toString())
                                        );
                                        mAdapter.swapCursor(mDataSource.getHighScores());
                                        break;
                                }
                            }
                        });

                builder.create().show();
                return true;
            }
        });

    }

    public static class SendHighScore extends AsyncTask<String, Void, String> {

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
            Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
        }
    }
}