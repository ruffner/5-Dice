package com.bluntllama.fivekind.items;

import android.graphics.drawable.TransitionDrawable;
import android.view.View;
import android.widget.TextView;

import com.bluntllama.fivekind.R;

public class ScoreItem {
    public static final int SCORE_POSITION_ACES = 1;
    public static final int SCORE_POSITION_DEUCES = 2;
    public static final int SCORE_POSITION_TREYS = 3;
    public static final int SCORE_POSITION_FOURS = 4;
    public static final int SCORE_POSITION_FIVES = 5;
    public static final int SCORE_POSITION_SIXES = 6;
    public static final int SCORE_POSITION_TALLY_BASIC_PRIMARY = 7;
    public static final int SCORE_POSITION_TALLY_BASIC_BONUS = 8;
    public static final int SCORE_POSITION_TALLY_BASIC_TOTAL = 9;
    public static final int SCORE_POSITION_TWO_PAIR_SAME = 11;
    public static final int SCORE_POSITION_THREE_OF_A_KIND = 12;
    public static final int SCORE_POSITION_STRAIGHT = 13;
    public static final int SCORE_POSITION_FLUSH = 14;
    public static final int SCORE_POSITION_FULL_HOUSE = 15;
    public static final int SCORE_POSITION_FULL_HOUSE_SAME = 16;
    public static final int SCORE_POSITION_FOUR_OF_A_KIND = 17;
    public static final int SCORE_POSITION_YARBOROUGH = 18;
    public static final int SCORE_POSITION_ALL_THE_SAME = 19;
    public static final int SCORE_POSITION_TALLY_MAIN_SECTION = 20;
    public static final int SCORE_POSITION_TALLY_BASIC_SECTION = 21;
    public static final int SCORE_POSITION_TALLY_GAME = 22;

    public int score = 0;
    public boolean isTaken = false;
    public boolean isNA = false;

    public View view = null;

    public ScoreItem(View v) {
        view = v;
    }

    public ScoreItem(View v, int score) {
        view = v;
        this.score = score;

        ((TextView) view.findViewById(R.id.possible_points)).setText(Integer.toString(score));
    }

    public ScoreItem(View v, int score, boolean isTaken) {
        view = v;
        this.score = score;
        this.isTaken = isTaken;
        if(isTaken) {
            TransitionDrawable draw = (TransitionDrawable) view.getBackground();
            draw.startTransition(300);
            ((TextView) view.findViewById(R.id.possible_points)).setText(Integer.toString(score));
        }
    }

    public void setScore(int newScore) {
        if (!isTaken) {
            score = newScore;
            isNA = false;
            ((TextView) view.findViewById(R.id.possible_points)).setText(Integer.toString(score));
        }
    }

    public void setNA() {
        if (!isTaken) {
            ((TextView) view.findViewById(R.id.possible_points)).setText("N/A");
            isNA = true;
        }
    }

    public void setBlank(boolean override) {
        if(view != null) {
            if (override)
                ((TextView) view.findViewById(R.id.possible_points)).setText("");
            else if (!isTaken)
                ((TextView) view.findViewById(R.id.possible_points)).setText("");
        }
    }

    public void reset() {
        setBlank(true);
        isTaken = false;
        isNA = false;
        score = 0;
    }

    public void setTaken(boolean taken) {
        isTaken = taken;
        if(view != null) {
            TransitionDrawable draw = (TransitionDrawable) view.getBackground();
            if(isTaken)
                draw.startTransition(300);
        }
    }
}