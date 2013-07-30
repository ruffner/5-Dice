package com.bluntllama.fivekind.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bluntllama.fivekind.R;
import com.bluntllama.fivekind.activities.DiceActivity;
import com.bluntllama.fivekind.items.Die;
import com.bluntllama.fivekind.items.ScoreItem;

public class ScorePadFragment extends Fragment {
    static final String TAG = "5 Dice";

    public static final String ARG_RESTORE = "booleanToRestore";

    private ScoreItem[] mScores = new ScoreItem[23];

    private int id;
    private int rollsLeft = 3;

    private boolean upper = false;
    private boolean lower = false;
    private boolean scoreChosen = false;

    private ViewGroup mContainerView;

    private Die[] mDice;

    private ScoreSelectedListener mListener;

    private SharedPreferences mPrefs;

    public interface ScoreSelectedListener {
        public void scoreSelected(int fragId);
        public void fragGameOver(int fragId);
        public Die[] getDice();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mListener = (ScoreSelectedListener)getActivity();

        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_score_pad, container, false);

        mContainerView = (LinearLayout)view.findViewById(R.id.container);

        if(getArguments().getBoolean(ARG_RESTORE)) {
            mDice = mListener.getDice();
            restore();
        } else
            for(int i = 1; i < 23; i++)
                if(i != 10)
                    mScores[i] = new ScoreItem(mContainerView.getChildAt(i));


        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onResume();
        //if(rollsLeft != 3)
        //    calcScores();
    }

    @Override
    public void onDetach() {
        Log.d(TAG, "onDetach() called");

        save();

        super.onDetach();
    }

    public void setId(int id) {
        this.id = id;
    }

    public void scoreViewOnClick(View view) {
        if(mScores[mContainerView.indexOfChild(view)].isTaken)
            return;
        else if(checkDiceBeforeRolls())
            return;
        else if (!scoreChosen && !mScores[mContainerView.indexOfChild(view)].isTaken) {
            TransitionDrawable draw = (TransitionDrawable) view.getBackground();

            Log.d(TAG, "index selected is " + mContainerView.indexOfChild(view));

            if (!mScores[mContainerView.indexOfChild(view)].isNA) {
                draw.startTransition(300);
                selectScore(mContainerView.indexOfChild(view));
            } else {
                askPutZero(mContainerView.indexOfChild(view), draw);
            }

        }
    }

    public boolean checkDiceBeforeRolls() {
        if(rollsLeft == 3 || scoreChosen) {
            Toast.makeText(getActivity(), "Please roll first.", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    private void selectScore(int index) {
        mScores[index].isTaken = true;

        scoreChosen = true;

        for (int i = ScoreItem.SCORE_POSITION_ACES; i <= ScoreItem.SCORE_POSITION_SIXES; i++) {
            if (!mScores[i].isTaken)
                break;
            if (i == ScoreItem.SCORE_POSITION_SIXES)
                completeBasic();
        }

        for (int i = ScoreItem.SCORE_POSITION_TWO_PAIR_SAME; i <= ScoreItem.SCORE_POSITION_ALL_THE_SAME; i++) {
            if (!mScores[i].isTaken)
                break;
            if (i == ScoreItem.SCORE_POSITION_ALL_THE_SAME)
                completeLower();
        }

        if (lower && upper)
            gameOver(true);
        else
            mListener.scoreSelected(id);
    }

    private void completeBasic() {
        Log.d(TAG, "ACES score is " + mScores[ScoreItem.SCORE_POSITION_ACES].score);
        Log.d(TAG, "DEUCES score is " + mScores[ScoreItem.SCORE_POSITION_DEUCES].score);
        Log.d(TAG, "TREYS score is " + mScores[ScoreItem.SCORE_POSITION_TREYS].score);
        Log.d(TAG, "FOURS score is " + mScores[ScoreItem.SCORE_POSITION_FOURS].score);
        Log.d(TAG, "FIVES score is " + mScores[ScoreItem.SCORE_POSITION_FIVES].score);
        Log.d(TAG, "SIXES score is " + mScores[ScoreItem.SCORE_POSITION_SIXES].score);

        int total = 0;
        for (int i = ScoreItem.SCORE_POSITION_ACES; i <= ScoreItem.SCORE_POSITION_SIXES; i++)
            total += mScores[i].score;
        mScores[ScoreItem.SCORE_POSITION_TALLY_BASIC_PRIMARY].view.setVisibility(View.VISIBLE);
        mScores[ScoreItem.SCORE_POSITION_TALLY_BASIC_PRIMARY].setScore(total);
        int bonus = 0;

        if (total >= 63 && total <= 70)
            bonus = 35;
        if (total >= 71 && total <= 77)
            bonus = 55;
        if (total >= 78)
            bonus = 55;

        mScores[ScoreItem.SCORE_POSITION_TALLY_BASIC_BONUS].view.setVisibility(View.VISIBLE);
        mScores[ScoreItem.SCORE_POSITION_TALLY_BASIC_BONUS].setScore(bonus);

        mScores[ScoreItem.SCORE_POSITION_TALLY_BASIC_TOTAL].view.setVisibility(View.VISIBLE);
        mScores[ScoreItem.SCORE_POSITION_TALLY_BASIC_TOTAL].setScore(total + bonus);

        upper = true;
    }

    private void completeLower() {
        int total = 0;
        int basic = 0;
        for (int i = ScoreItem.SCORE_POSITION_TWO_PAIR_SAME; i <= ScoreItem.SCORE_POSITION_ALL_THE_SAME; i++)
            total += mScores[i].score;

        basic = mScores[ScoreItem.SCORE_POSITION_TALLY_BASIC_TOTAL].score;

        mScores[ScoreItem.SCORE_POSITION_TALLY_MAIN_SECTION].setScore(total);
        mScores[ScoreItem.SCORE_POSITION_TALLY_BASIC_SECTION].setScore(basic);

        // finale
        mScores[ScoreItem.SCORE_POSITION_TALLY_GAME].setScore(total + basic);

        lower = true;
    }

    private void showLowerScores() {
        mScores[ScoreItem.SCORE_POSITION_TALLY_MAIN_SECTION].view.setVisibility(View.VISIBLE);
        mScores[ScoreItem.SCORE_POSITION_TALLY_BASIC_SECTION].view.setVisibility(View.VISIBLE);
        mScores[ScoreItem.SCORE_POSITION_TALLY_GAME].view.setVisibility(View.VISIBLE);
    }

    private void gameOver(boolean real) {
        completeLower();
        completeBasic();
        showLowerScores();
        if(real) mListener.fragGameOver(id);
    }

    private void calcScores() {
        int total = totalDice(mDice);
        boolean flush = false;
        int[] counts = new int[6];
        int[] colors = new int[]{numberOfRed(mDice), numberOfGreen(mDice), numberOfBlack(mDice)};
        for (int i = 0; i < 5; i++) {
            counts[mDice[i].value - 1]++;
        }

        // straights
        if (isStraight(counts))
            mScores[ScoreItem.SCORE_POSITION_STRAIGHT].setScore(30);
        else
            mScores[ScoreItem.SCORE_POSITION_STRAIGHT].setNA();

        // flush
        if (colors[0] == 5 || colors[1] == 5 || colors[2] == 5) {
            flush = true;
            mScores[ScoreItem.SCORE_POSITION_FLUSH].setScore(35);
        } else
            mScores[ScoreItem.SCORE_POSITION_FLUSH].setNA();

        // singles
        mScores[ScoreItem.SCORE_POSITION_ACES].setScore(counts[0]);
        mScores[ScoreItem.SCORE_POSITION_DEUCES].setScore(counts[1] * 2);
        mScores[ScoreItem.SCORE_POSITION_TREYS].setScore(counts[2] * 3);
        mScores[ScoreItem.SCORE_POSITION_FOURS].setScore(counts[3] * 4);
        mScores[ScoreItem.SCORE_POSITION_FIVES].setScore(counts[4] * 5);
        mScores[ScoreItem.SCORE_POSITION_SIXES].setScore(counts[5] * 6);

        // psh
        mScores[ScoreItem.SCORE_POSITION_YARBOROUGH].setScore(total);

        // 2 pair same
        int pairs = 0;
        for (int i : colors)
            if (i >= 4)
                for (int j : counts)
                    if (j >= 2)
                        pairs++;
        if (pairs == 2)
            mScores[ScoreItem.SCORE_POSITION_TWO_PAIR_SAME].setScore(total);
        else
            mScores[ScoreItem.SCORE_POSITION_TWO_PAIR_SAME].setNA();

        // three a kind, full houses, four a kind, kismet, 2 pair by default
        mScores[ScoreItem.SCORE_POSITION_THREE_OF_A_KIND].setNA();
        mScores[ScoreItem.SCORE_POSITION_FULL_HOUSE].setNA();
        mScores[ScoreItem.SCORE_POSITION_FULL_HOUSE_SAME].setNA();
        mScores[ScoreItem.SCORE_POSITION_FOUR_OF_A_KIND].setNA();
        mScores[ScoreItem.SCORE_POSITION_ALL_THE_SAME].setNA();

        for (int i = 0; i < 6; i++) {
            if (counts[i] == 3) {
                mScores[ScoreItem.SCORE_POSITION_THREE_OF_A_KIND].setScore(total);
                for (int j : counts) {
                    if (j == 2) {
                        mScores[ScoreItem.SCORE_POSITION_FULL_HOUSE].setScore(total + 15);
                        //mScores[ScoreItem.SCORE_POSITION_TWO_PAIR_SAME].setScore(total);
                        if (flush)
                            mScores[ScoreItem.SCORE_POSITION_FULL_HOUSE_SAME].setScore(total + 20);
                    }
                }
            }

            // at least 4 of a kind
            if (counts[i] == 4) {
                mScores[ScoreItem.SCORE_POSITION_THREE_OF_A_KIND].setScore(total);
                mScores[ScoreItem.SCORE_POSITION_TWO_PAIR_SAME].setScore(total);
                mScores[ScoreItem.SCORE_POSITION_FOUR_OF_A_KIND].setScore(total + 25);
            }

            // at least 5 of a kind
            if (counts[i] == 5) {
                mScores[ScoreItem.SCORE_POSITION_THREE_OF_A_KIND].setScore(total);
                mScores[ScoreItem.SCORE_POSITION_TWO_PAIR_SAME].setScore(total);
                mScores[ScoreItem.SCORE_POSITION_FULL_HOUSE].setScore(total + 15);
                mScores[ScoreItem.SCORE_POSITION_FULL_HOUSE_SAME].setScore(total + 20);
                mScores[ScoreItem.SCORE_POSITION_ALL_THE_SAME].setScore(total + 50);
                mScores[ScoreItem.SCORE_POSITION_FOUR_OF_A_KIND].setScore(total + 25);
            }
        }

    }

    private void hideScoreTotals() {
        mScores[ScoreItem.SCORE_POSITION_TALLY_BASIC_PRIMARY].view.setVisibility(View.GONE);
        mScores[ScoreItem.SCORE_POSITION_TALLY_BASIC_BONUS].view.setVisibility(View.GONE);
        mScores[ScoreItem.SCORE_POSITION_TALLY_BASIC_TOTAL].view.setVisibility(View.GONE);

        mScores[ScoreItem.SCORE_POSITION_TALLY_MAIN_SECTION].view.setVisibility(View.GONE);
        mScores[ScoreItem.SCORE_POSITION_TALLY_GAME].view.setVisibility(View.GONE);
        mScores[ScoreItem.SCORE_POSITION_TALLY_BASIC_SECTION].view.setVisibility(View.GONE);
    }

    private boolean isStraight(int[] counts) {
        for (int i : counts)
            if (i > 1)
                return false;
        if (counts[0] == 1 && counts[5] == 1)
            return false;
        return true;
    }

    private int totalDice(Die[] mDice) {
        int total = 0;
        for (Die i : mDice)
            total += i.value;
        return total;
    }

    private int numberOfRed(Die[] mDice) {
        int total = 0;
        for (Die i : mDice)
            if (i.value == 2 || i.value == 5)
                total++;
        return total;
    }

    private int numberOfGreen(Die[] mDice) {
        int total = 0;
        for (Die i : mDice)
            if (i.value == 3 || i.value == 4)
                total++;
        return total;
    }

    private int numberOfBlack(Die[] mDice) {
        int total = 0;
        for (Die i : mDice)
            if (i.value == 1 || i.value == 6)
                total++;
        return total;
    }

    private void askPutZero(final int index, final TransitionDrawable draw) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Add the buttons
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                mScores[index].setScore(0);
                draw.startTransition(300);
                selectScore(index);
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });
        builder.setMessage("Take a zero for this score?");
        builder.setIcon(android.R.drawable.alert_dark_frame);

        // Create the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void resetScores(boolean newGame) {
        for (int i = 1; i < 20; i++) {
            if (i != 7 && i != 8 && i != 9 && i != 10) {
                if (!newGame) {
                    mScores[i].setBlank(false);
                } else {
                    mScores[i].reset();
                    ((TransitionDrawable) mScores[i].view.getBackground()).resetTransition();
                }
            }
        }
    }

    public int getGameScore() {
        return mScores[ScoreItem.SCORE_POSITION_TALLY_GAME].score;
    }

    public void reset() {
        resetScores(true);
        hideScoreTotals();
        upper = false;
        lower = false;
        scoreChosen = false;
    }

    public void rollOver(Die[] dice, int rLeft) {
        rollsLeft = rLeft;
        mDice = dice;
        scoreChosen = false;
        calcScores();
    }

    public void setDice(Die[] dice) {
        mDice = dice;
    }

    public void save() {
        mPrefs = getActivity().getSharedPreferences(DiceActivity.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mPrefs.edit();

        for(int i = 1; i < 23; i++) {
            if(i != 10) {
                editor.putInt("frag" + id + "pos" + i + "score", mScores[i].score);
                if(i != 7 && i != 8 && i != 9 && i != 20 && i != 21 && i != 22)
                    editor.putBoolean("frag" + id + "pos" + i + "isTaken", mScores[i].isTaken);
            }
        }

        editor.putBoolean("frag" + id + "upper", upper);
        editor.putBoolean("frag" + id + "lower", lower);
        editor.putBoolean("frag" + id + "scoreChosen", scoreChosen);

        editor.commit();

        Log.d(TAG, "done saving, scoreChosen is " + scoreChosen);
    }

    public void restore() {
        mPrefs = getActivity().getSharedPreferences(DiceActivity.PREFS_NAME, Context.MODE_PRIVATE);

        for(int i = 1; i < 23; i++) {
            if(i != 10) {
                if(i != 7 && i != 8 && i != 9 && i != 20 && i != 21 && i != 22)
                    mScores[i] = new ScoreItem(
                            mContainerView.getChildAt(i),
                            mPrefs.getInt("frag" + id + "pos" + i + "score", 0),
                            mPrefs.getBoolean("frag" + id + "pos" + i + "isTaken", false)
                    );
                else
                    mScores[i] = new ScoreItem(
                            mContainerView.getChildAt(i),
                            mPrefs.getInt("frag" + id + "pos" + i + "score", 0)
                    );
            }
        }

        rollsLeft = mPrefs.getInt("rolls_left", 3);
        scoreChosen = mPrefs.getBoolean("frag" + id + "scoreChosen", false);

        if(mPrefs.getBoolean("frag" + id + "upper", false))
            completeBasic();
        if(mPrefs.getBoolean("frag" + id + "lower", false) && mPrefs.getBoolean("frag" + id + "upper", false)) {
            completeLower();
            gameOver(false);
        }

        if(rollsLeft != 3)
            calcScores();

        Log.d(TAG, "done loading, scoreChosen is " + scoreChosen);
    }
}